package com.hk47.realityoverlay.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.hk47.realityoverlay.R;
import com.hk47.realityoverlay.utils.SensorUtilities;

import static com.hk47.realityoverlay.data.Constants.LOCATION_UPDATE_INTERVAL;
import static com.hk47.realityoverlay.data.Constants.PUBLIX_LOCATION;
import static com.hk47.realityoverlay.data.Constants.RC_CAMERA_PERMISSION;
import static com.hk47.realityoverlay.data.Constants.RC_FINE_LOCATION_PERMISSION;

public class OverlayActivity extends AppCompatActivity implements
        SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = OverlayActivity.class.getSimpleName();

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mAccelSensor;
    private Sensor mCompassSensor;
    private Sensor mGyroSensor;
    private float[] mAcceleromterReading;
    private float[] mMagnetometerReading;

    // Location
    private Location mCurrentLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // Display
    FrameLayout mContainerView;
    CameraDisplayView mCameraDisplayView;
    OverlayDisplayView mOverlayDisplayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlay);

        // TODO Make this more user friendly by allowing interaction and then asking after click
        // TODO An initial splash screen, perhaps
        // Request applicable permissions
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA}, RC_CAMERA_PERMISSION);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, RC_FINE_LOCATION_PERMISSION);
        }

        mContainerView = (FrameLayout) findViewById(R.id.augmented_reality_container_view);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            mCameraDisplayView = new CameraDisplayView(this, this);
            mContainerView.addView(mCameraDisplayView);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mOverlayDisplayView = new OverlayDisplayView(this);
            mContainerView.addView(mOverlayDisplayView);
        }

        // TODO Make sure logic below does not occur until all permissions have been granted

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            registerSensors();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            unregisterSensors();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mCameraDisplayView = new CameraDisplayView(this, this);
                mContainerView.addView(mCameraDisplayView);
            }
        }
        if (requestCode == RC_FINE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mOverlayDisplayView = new OverlayDisplayView(this);
                mContainerView.addView(mOverlayDisplayView);
            }
        }
    }

    private void registerSensors() {
        mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mCompassSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mCompassSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSensors() {
        mSensorManager.unregisterListener(this, mAccelSensor);
        mSensorManager.unregisterListener(this, mCompassSensor);
        mSensorManager.unregisterListener(this, mGyroSensor);
    }

    // Updates the OverlayDisplayView when sensor data changes.
    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mAcceleromterReading =
                        SensorUtilities.filterSensors(event.values, mAcceleromterReading);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerReading =
                        SensorUtilities.filterSensors(event.values, mMagnetometerReading);
                break;
            case Sensor.TYPE_GYROSCOPE:
                break;
        }

        float[] orientation =
                SensorUtilities.computeDeviceOrientation(mAcceleromterReading, mMagnetometerReading);
        if (orientation != null) {

            // Convert azimuth relative to magnetic north from radians to degrees
            float azimuthValue = (float) Math.toDegrees(orientation[0]);
            if (azimuthValue < 0) {
                azimuthValue += 360f;
            }

            // Convert pitch and roll from radians to degrees
            float pitchValue = (float) Math.toDegrees(orientation[1]);
            float rollValue = (float) Math.toDegrees(orientation[2]);

            String azimuth = String.valueOf(azimuthValue);
            String pitch = String.valueOf(pitchValue);
            String roll = String.valueOf(rollValue);

            mOverlayDisplayView.setTextOne("Azimuth: " + azimuth);
            mOverlayDisplayView.setTextTwo("Pitch: " + pitch);
            mOverlayDisplayView.setTextThree("Roll: " + roll);

            if (mCurrentLocation != null) {
                mOverlayDisplayView.setHorizontalFOV(mCameraDisplayView.getHorizontalFOV());
                mOverlayDisplayView.setVerticalFOV(mCameraDisplayView.getVerticalFOV());
                mOverlayDisplayView.setAzimuth(azimuthValue);
                mOverlayDisplayView.setPitch(pitchValue);
                mOverlayDisplayView.setRoll(rollValue);
            }

            // Force the OverlayDisplayView to redraw when sensor data changes,
            // redrawing only when the camera is not pointed straight up or down
            if (pitchValue <= 75 && pitchValue >= -75) {
                mOverlayDisplayView.invalidate();
            }
        }
    }

    // Required Override to implement SensorEventListener, not used.
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Updates the OverlayDisplayView when the location com.hk47.realityoverlay.data changes.
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        float bearingToPublix = mCurrentLocation.bearingTo(PUBLIX_LOCATION);
        if (bearingToPublix < 0) {
            bearingToPublix += 360;
        }
        float distanceToPublix = mCurrentLocation.distanceTo(PUBLIX_LOCATION);

        // Update the OverlayDisplayView when location changes
        mOverlayDisplayView.setBearingToTarget(bearingToPublix);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
            mLocationRequest.setSmallestDisplacement(10);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }
}

package com.hk47.realityoverlay.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.hk47.realityoverlay.R;

public class OverlayDisplayView extends View {

    public static final String TAG = OverlayDisplayView.class.getSimpleName();

    private String mTextOne = "";
    private String mTextTwo = "";
    private String mTextThree = "";

    private float mViewportHeight;
    private float mViewportWidth;
    private boolean mGotViewports;

    private float mVerticalFOV;
    private float mHorizontalFOV;
    private float mBearingToTarget;
    private float mAzimuth;
    private float mPitch;
    private float mRoll;


    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public OverlayDisplayView(Context context) {
        super(context);
    }

    public void setTextOne (String textOne) {
        mTextOne = textOne;
    }

    public void setTextTwo (String textTwo) {
        mTextTwo = textTwo;
    }

    public void setTextThree (String textThree) {
        mTextThree = textThree;
    }

    public void setVerticalFOV(float verticalFOV) {
        mVerticalFOV = verticalFOV;
    }

    public void setHorizontalFOV(float horizontalFOV) {
        mHorizontalFOV = horizontalFOV;
    }

    public void setBearingToTarget(float bearingToTarget) {
        mBearingToTarget = bearingToTarget;
    }

    public void setAzimuth(float azimuth) {
        mAzimuth = azimuth;
    }

    public void setPitch(float pitch) {
        mPitch = pitch;
    }

    public void setRoll(float roll) {
        mRoll = roll;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Get the viewports only once
        if (!mGotViewports && mVerticalFOV > 0 && mHorizontalFOV > 0){
            mViewportHeight = canvas.getHeight() / mVerticalFOV;
            mViewportWidth = canvas.getWidth() / mHorizontalFOV;
            mGotViewports = true;
        }

        if (!mGotViewports) {
            return;
        }

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.canvas_text_size));
        paint.setColor(Color.GREEN);

        // TODO Remove azimuth, pitch, and roll display
        //canvas.drawText(mTextOne, canvas.getWidth() / 2, canvas.getHeight() / 4, paint);
        //canvas.drawText(mTextTwo, canvas.getWidth() / 2, canvas.getHeight() / 2, paint);
        //canvas.drawText(mTextThree, canvas.getWidth() / 2, (canvas.getHeight() * 3) / 4, paint);

        targetPaint.setColor(Color.RED);
        targetPaint.setStrokeWidth(10);

        // Center of view
        float x = canvas.getWidth() / 2;
        float y = canvas.getHeight() / 2;

//        float viewportWidth = canvas.getWidth() / mHorizontalFOV;
//        float viewportHeight = canvas.getHeight() / mVerticalFOV;




        // Rotate display around the center point based on the roll
        canvas.rotate((float) (0.0f - mRoll), x, y);

        // TODO If location points later incorporate some elevation offset
        // TODO Subtract from y to place them properly above the horizon (or below, Death Valley)
        // The horizon height is equal to y - dy
        float dy = mPitch * mViewportHeight;

        // TODO Remove elevation test dot
        //canvas.drawCircle(canvas.getWidth() / 2, y - dy - 40, 10f, targetPaint);

        // TODO *** If Landscape, (x,y) coordinates will need to be swapped and adjusted. ***
        // TODO Should pass device orientation to the constructor
//        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        int rotation = display.getRotation();
//
//        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
//            canvas.drawLine(y, 0 - canvas.getHeight(), y,  canvas.getWidth() + canvas.getHeight(), targetPaint);
//        }

        // TODO Remove horizon line
        //canvas.drawLine(0 - canvas.getHeight(), y - dy, canvas.getWidth() + canvas.getHeight(), y - dy, targetPaint);

        float xDegreesToTarget = mAzimuth - mBearingToTarget;

        float dx = mViewportWidth * xDegreesToTarget;

        // Draw the target.
        canvas.drawCircle(x - dx, y - dy, 40f, targetPaint);
    }
}

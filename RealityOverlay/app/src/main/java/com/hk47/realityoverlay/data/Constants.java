package com.hk47.realityoverlay.data;

import android.location.Location;

public class Constants {

    private Constants() {

    }

    public static final int RC_FINE_LOCATION_PERMISSION = 100;
    public static final int RC_CAMERA_PERMISSION = 200;

    // Location update interval in seconds
    public static final int LOCATION_UPDATE_INTERVAL = 1000;

    // Sensor filter constants
    public static final float LOW_PASS_FILTER_CONSTANT = 0.25f;

    // Lynn Haven
//    public final static Location PUBLIX_LOCATION = new Location("manual");
//    static {
//        PUBLIX_LOCATION.setLatitude(30.223109f);
//        PUBLIX_LOCATION.setLongitude(-85.651437f);
//        PUBLIX_LOCATION.setAltitude(0f);
//    }

    // 23rd Street
    public final static Location PUBLIX_LOCATION = new Location("manual");
    static {
        PUBLIX_LOCATION.setLatitude(30.193977f);
        PUBLIX_LOCATION.setLongitude(-85.669338f);
        PUBLIX_LOCATION.setAltitude(0f);
    }
}


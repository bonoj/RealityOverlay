# RealityOverlay
A simple skeleton for an augmented reality application.

#### Overlay Display
CameraDisplayView opens the back facing camera's preview

OverlayDisplayView draws the overlay over the camera preview.

OverlayActivity updates both in real time as device **location** and **orientation** change.

#### Device Location and Orientation
1. Location - Google Play Services API utilized to ACCESS_FINE_LOCATION.
2. Orientation - Accelerometer and magnetometer data fused and smoothed 
with a simple low pass filter to orient device in 3D space.

    * Heading is an azimuth to magnetic north, aligned to the back camera, changing as the device pans horizontally.
    * Pitch is aligned to portrait orientation, changing as the device is tilted vertically up or down
    * Roll is aligned such that the device may pivot around the camera axis without throwing off the heading.

#### Points of Interest
Points of interest are tracked and displayed along the horizon when the camera is facing them. The user's bearing to and distance from each point of interest is calculated in real time as the user's location changes.

#### TODO:
Currently, points of interest are hardcoded...
1. Implement Google Places API to retrieve points of interest within a given radius.
2. Store results in a local database to eliminate duplicate API calls over multiple sessions.

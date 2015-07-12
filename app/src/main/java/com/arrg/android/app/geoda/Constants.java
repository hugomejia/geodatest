package com.arrg.android.app.geoda;

import android.content.Context;
import android.os.Environment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public final class Constants {

    public static final String PACKAGE_NAME = "com.arrg.android.app.geoda";
    public static final String APP_DATA_SDCARD = Environment.getExternalStorageDirectory() + "/Geoda";
    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";
    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";
    public static String TYPE_OF_APP = "Demo";

    private Constants() {

    }
}

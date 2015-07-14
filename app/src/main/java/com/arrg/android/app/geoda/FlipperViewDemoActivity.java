package com.arrg.android.app.geoda;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FlipperViewDemoActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, LocationListener {

    protected GoogleMap mMap;

    protected static final String tag = "LifeCycleEventsMA";
    protected static final String TAG = "c-and-m-geofences";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    protected Boolean mGeofencesAdded;
    protected Boolean mRequestingLocationUpdates;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    protected PendingIntent mGeofencePendingIntent;
    protected SharedPreferences mSharedPreferences;
    protected String mLastUpdateTime;

    protected ArrayList<CircleOptions> mCircleOptions;
    protected ArrayList<Geofence> mGeofenceList;
    protected ArrayList<Integer> listOfVideos = new ArrayList<>();
    protected ArrayList<String> listOfId = new ArrayList<>();
    protected ArrayList<String> listOfPublicity = new ArrayList<>();

    protected int noOfVideo;
    protected int prepareGC = 0;

    protected SupportMapFragment mMapFragment;
    protected TextView textView;
    protected ViewFlipper flipper;

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String typeOfTransition = intent.getStringExtra("i");
            String idOfGeofence = intent.getStringExtra("t");

            if (typeOfTransition.equals(getString(R.string.geofence_transition_entered))) {
                Toast.makeText(FlipperViewDemoActivity.this, getResources().getString(R.string.geofence_transition_entered) + " " + idOfGeofence, Toast.LENGTH_SHORT).show();

                if (idOfGeofence.contains(",")) {
                    String idGeofences[] = idOfGeofence.split(",");
                    for (String id : idGeofences) {
                        if (!listOfId.contains(id.trim())) {
                            listOfId.add(id.trim());
                            addPublicityInFlipperView(id.trim());
                        }
                        Log.d(TAG, "Values of ID added: " + listOfId.toString());
                    }
                } else {
                    if (!listOfId.contains(idOfGeofence)) {
                        listOfId.add(idOfGeofence);
                        addPublicityInFlipperView(idOfGeofence);
                    }
                    Log.d(TAG, "Values of ID added: " + listOfId.toString());
                }
                String filesAdded = TextUtils.join("\n", listOfPublicity);
                textView.setText(filesAdded);
            } else if (typeOfTransition.equals(getString(R.string.geofence_transition_exited))) {
                Toast.makeText(FlipperViewDemoActivity.this, getResources().getString(R.string.geofence_transition_exited) + " " + idOfGeofence, Toast.LENGTH_SHORT).show();

                textView.setText("");
                listOfId.clear();
                listOfPublicity.clear();
                listOfVideos.clear();
                removeGeofences();
                flipper.removeAllViews();
                showPublicityInFlipperView();
                addGeofences();
                updateUI();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag, "In the onCreate() event");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flipper_view_demo);

        viewManager();
        showPublicityInFlipperView();
        settingsManager();
        updateValuesFromBundle(savedInstanceState);
        try {
            geofenceList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        buildGoogleApiClient();
        gpsManager();
    }

    @Override
    protected void onStart() {
        Log.d(tag, "In the onStart() event");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        Log.d(tag, "In the onResume() event");
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        Log.d(tag, "In the onPause() event");
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        Log.d(tag, "In the onStop() event");
        System.gc();
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        Log.d(tag, "In the onDestroy() event");
        removeGeofences();
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        startLocationUpdates();
        addGeofences();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        /*Toast.makeText(this, getResources().getString(R.string.location_updated_message), Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.tittle_exit)).setMessage(getString(R.string.body_exit)).setNegativeButton(android.R.string.no, null).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                FlipperViewDemoActivity.super.onBackPressed();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }).create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_flipper_view_demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }

        return super.onOptionsItemSelected(item);
    }

    public void viewManager() {
        mMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        mMap = mMapFragment.getMap();

        flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        textView = (TextView) findViewById(R.id.tvImagesAddedText);

        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(true);
    }

    public void settingsManager() {
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        mGeofenceList = new ArrayList<>();
        mCircleOptions = new ArrayList<>();

        mGeofencePendingIntent = null;
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
    }

    public void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            }

            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    public synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        createLocationRequest();
    }

    public void gpsManager() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabledGPS) {
            Toast.makeText(this, getString(R.string.gps_signal_error), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void updateUI() {
        if (mCurrentLocation != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("t"));

            prepareGC++;

            if (prepareGC == 300) {
                prepareGC = 0;
                System.gc();
            }

            double latitude = mCurrentLocation.getLatitude();
            double longitude = mCurrentLocation.getLongitude();

            LatLng latLng = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        }
    }

    public void addGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mGeofencesAdded = true;
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent()).setResultCallback(this);
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    public void removeGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mGeofencesAdded = false;
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent()).setResultCallback(this);
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    public void geofenceList() throws IOException {
        InputStream in = new FileInputStream(Constants.APP_DATA_SDCARD + "/GeofenceData.json");
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        reader.beginArray();

        while (reader.hasNext()) {
            mGeofenceList.add(geofencesBuilder(reader));
        }
        reader.endArray();

        for (int i = 0; i < mCircleOptions.size(); i++) {
            mMap.addCircle(mCircleOptions.get(i));
        }
    }

    public Geofence geofencesBuilder(JsonReader reader) throws IOException {
        String id = null;
        double lat = 0;
        double lon = 0;
        double radio = 0;
        long duration = 0;

        reader.beginObject();

        while (reader.hasNext()) {
            String name = reader.nextName();
            final boolean isNull = reader.peek() == JsonToken.NULL;

            if (name.equals("id") && !isNull) {
                id = reader.nextString();
            } else if (name.equals("lat") && !isNull) {
                lat = reader.nextDouble();
            } else if (name.equals("long") && !isNull) {
                lon = reader.nextDouble();
            } else if (name.equals("radio") && !isNull) {
                radio = reader.nextDouble();
            } else if (name.equals("duration")) {
                duration = reader.nextLong();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        mCircleOptions.add(new CircleOptions().center(new LatLng(lat, lon)).radius(radio).strokeColor(Color.TRANSPARENT).strokeWidth(2).fillColor(Color.parseColor("#4058ACFA")));
        return new Geofence.Builder().setRequestId((id)).setCircularRegion(lat, lon, Float.parseFloat(String.valueOf(radio))).setExpirationDuration(duration).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT).build();
    }

    public GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    public PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void onResult(Status status) {
        if (status.isSuccess()) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();
            Toast.makeText(this, getString(mGeofencesAdded ? R.string.geofences_added : R.string.geofences_removed), Toast.LENGTH_SHORT).show();
        } else {
            String errorMessage = GeofenceErrorMessages.getErrorString(this, status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void addPublicityInFlipperView(String idOfGeofence) {
        File imagesPath = new File(Constants.APP_DATA_SDCARD + "/" + idOfGeofence);

        if (imagesPath.exists()) {
            int imageCount = imagesPath.listFiles().length;

            for (int count = 0; count < imageCount; count++) {
                ImageView imageView = new ImageView(this);
                VideoView videoView = new VideoView(this);

                imageView.setLayoutParams(findViewById(R.id.llMain).getLayoutParams());
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                videoView.setLayoutParams(findViewById(R.id.llMain).getLayoutParams());

                if (imagesPath.listFiles()[count].getAbsolutePath().contains(".jpg")) {
                    Bitmap bmp = BitmapFactory.decodeFile(imagesPath.listFiles()[count].getAbsolutePath());
                    imageView.setImageBitmap(bmp);

                    Log.d(TAG, "Added File " + imagesPath.listFiles()[count].getName());
                    listOfPublicity.add(idOfGeofence + " - " + imagesPath.listFiles()[count].getName());

                    flipper.addView(imageView);
                } else if (imagesPath.listFiles()[count].getAbsolutePath().contains(".mp4")) {
                    videoView.setVideoPath(imagesPath.listFiles()[count].getAbsolutePath());
                    videoView.requestFocus();
                    videoView.setKeepScreenOn(true);

                    Log.d(TAG, "Added File " + imagesPath.listFiles()[count].getName());
                    listOfPublicity.add(idOfGeofence + " - " + imagesPath.listFiles()[count].getName());

                    flipper.addView(videoView);
                    listOfVideos.add(noOfVideo);
                }
                noOfVideo++;
            }
        }
    }

    public void showPublicityInFlipperView() {
        noOfVideo = 0;

        File imagesPath = new File(Constants.APP_DATA_SDCARD + "/Publicity");
        int imageCount = imagesPath.listFiles().length;

        for (int count = 0; count < imageCount; count++) {
            ImageView imageView = new ImageView(this);
            VideoView videoView = new VideoView(this);

            imageView.setLayoutParams(findViewById(R.id.llMain).getLayoutParams());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            videoView.setLayoutParams(findViewById(R.id.llMain).getLayoutParams());

            if (imagesPath.listFiles()[count].getAbsolutePath().contains(".jpg")) {
                Bitmap bmp = BitmapFactory.decodeFile(imagesPath.listFiles()[count].getAbsolutePath());
                imageView.setImageBitmap(bmp);

                Log.d(TAG, "Added File " + imagesPath.listFiles()[count].getName());
                listOfPublicity.add("Publicity - " + imagesPath.listFiles()[count].getName());

                flipper.addView(imageView);
            } else if (imagesPath.listFiles()[count].getAbsolutePath().contains(".mp4")) {
                videoView.setVideoPath(imagesPath.listFiles()[count].getAbsolutePath());
                videoView.requestFocus();
                videoView.setKeepScreenOn(true);

                Log.d(TAG, "Added File " + imagesPath.listFiles()[count].getName());
                listOfPublicity.add("Publicity - " + imagesPath.listFiles()[count].getName());

                flipper.addView(videoView);
                listOfVideos.add(noOfVideo);
            }
            noOfVideo++;
        }
        flipper.startFlipping();
        reproduceVideoWhileFlipping();

        String filesAdded = TextUtils.join("\n", listOfPublicity);
        textView.setText(filesAdded);
    }

    public void reproduceVideoWhileFlipping() {
        if (listOfVideos.contains(flipper.getDisplayedChild())) {
            ((VideoView) flipper.getCurrentView()).start();

            ((VideoView) flipper.getCurrentView()).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    ((VideoView) flipper.getCurrentView()).stopPlayback();
                }
            });
        }
        Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                if (listOfVideos.contains(flipper.getDisplayedChild())) {

                    ((VideoView) flipper.getCurrentView()).start();

                    ((VideoView) flipper.getCurrentView()).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            ((VideoView) flipper.getCurrentView()).stopPlayback();
                        }
                    });
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
            }
        };
        flipper.getInAnimation().setAnimationListener(mAnimationListener);
    }

    public void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " + "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }
}

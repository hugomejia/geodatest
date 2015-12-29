package com.arrg.android.app.geoda;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class SplashScreenActivity extends AppCompatActivity {

    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvLoading;
    private TextView tvWelcomeTo;

    private SharedPreferences preferences;
    private final String TAG = "LifeCycleEventsSCA";
    //URL JSON que contiene los otros dos archivos planos
    //private final String URLJson[] = {"https://www.dropbox.com/s/wjfj6wheq0fhg70/TypeOfApp.json?dl=1", Constants.APP_DATA_SDCARD, "TypeOfApp.json"};
    private final String URLJson[] = {"https://www.dropbox.com/s/16ic97v04q08lcf/TypeOfApp.json?dl=1", Constants.APP_DATA_SDCARD, "TypeOfApp.json"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        viewManager();
        loadManager();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        System.gc();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        System.gc();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.tittle_exit))
                .setMessage(getString(R.string.body_exit))
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        SplashScreenActivity.super.onBackPressed();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }).create().show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            tvWelcomeTo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
            tvAppName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
            ivLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
            ivLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotation));
            tvLoading.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        }
    }

    public void viewManager() {
        ivLogo = (ImageView) findViewById(R.id.ivLogo);
        tvAppName = (TextView) findViewById(R.id.tvAppName);
        tvLoading = (TextView) findViewById(R.id.tvLoading);
        tvWelcomeTo = (TextView) findViewById(R.id.tvWelcomeTo);
    }

    public void loadManager() {
        preferences = getSharedPreferences(Constants.PACKAGE_NAME + ".STARTUP_SETTINGS_PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();

        Time systemDate = new Time(Time.getCurrentTimezone());
        systemDate.setToNow();

        boolean firstInstall = preferences.getBoolean("first_start", false);
        boolean wasEdited = preferences.getBoolean("was_edited", false);
        String dateStored = preferences.getString("last_date", String.valueOf(systemDate.monthDay));

        if (!firstInstall || !String.valueOf(systemDate.monthDay).equals(dateStored)) {
            deletingOldData(new File(Constants.APP_DATA_SDCARD));
            creatingNewFolderData();

            edit.putBoolean("first_start", true);
            edit.putString("last_date", String.valueOf(systemDate.monthDay));

            edit.apply();
            startDownload();
        } else {
            if (wasEdited) {
                InputStream in;
                try {
                    in = new FileInputStream(Constants.APP_DATA_SDCARD + "/TypeOfApp.json");
                    readJSon(in);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                DownloadPublicity downloadPublicity = new DownloadPublicity(SplashScreenActivity.this, tvLoading);
                try {
                    downloadPublicity.downloadFiles();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean deletingOldData(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String aChildren : children) {
                boolean success = deletingOldData(new File(file, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    public void creatingNewFolderData() {
        File file = new File(Constants.APP_DATA_SDCARD);

        if (!file.exists()) {
            try {
                if (file.mkdir()) {
                    Log.d(TAG, "Folder " + file.getPath() + " fully created.");
                } else {
                    Log.d(TAG, "Folder " + file.getPath() + " already created.");
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void startDownload() {
        new DownloadJSonFile().execute(URLJson);
    }

    public void readJSon(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        String type = null;
        String publicityData = null;
        String geofenceData = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            final boolean isNull = reader.peek() == JsonToken.NULL;

            if (name.equals("type") && !isNull) {
                type = reader.nextString();
            } else if (name.equals("publicityData.json") && !isNull) {
                publicityData = reader.nextString();
            } else if (name.equals("geofenceData.json") && !isNull) {
                geofenceData = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("type_of_app", type);
        edit.apply();

        String url[] = {publicityData, "PublicityData.json", geofenceData, "GeofenceData.json"};

        new DownloadJSonFiles(SplashScreenActivity.this, tvLoading).execute(url);
    }

    class DownloadJSonFile extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            int count;

            try {
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int lengthOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                OutputStream output = new FileOutputStream(params[1] + "/" + params[2]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            super.onProgressUpdate();
        }

        @Override
        protected void onPostExecute(String params) {
            super.onPostExecute(params);

            InputStream in;
            try {
                in = new FileInputStream(Constants.APP_DATA_SDCARD + "/TypeOfApp.json");
                readJSon(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

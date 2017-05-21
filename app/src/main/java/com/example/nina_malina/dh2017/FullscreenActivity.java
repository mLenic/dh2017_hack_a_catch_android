package com.example.nina_malina.dh2017;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.hardware.SensorEventListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


public class FullscreenActivity extends AppCompatActivity{
    double ax,ay,az;
    private int mInterval = 100; // 0.1 seconds
    private Handler mHandler;
    float my_x;
    float my_y;
    String id;
    String uname;
    String target_name;
    String target_id;
    private UpdateCoordinatesTask mTask = null;
    private KillTask kTask = null;
    MyView board;
    SensorManager sensorManager;
    AccelerometerData ad;
    Button killButton;



    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.game_name);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        Intent intent = getIntent();
        uname = intent.getStringExtra("uname");
        id = intent.getStringExtra("id");
        my_x = intent.getFloatExtra("my_x", 0);
        my_y = intent.getFloatExtra("my_y", 0);
        target_name = intent.getStringExtra("target_name");
        target_id = intent.getStringExtra("target_id");
        mHandler = new Handler();
        board = (MyView) findViewById(R.id.myview);
        killButton = (Button) findViewById(R.id.kill_button);
        killButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                kTask = new KillTask();
                try {
                    kTask.execute(new URL("https://k1ller.herokuapp.com/kill"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        });

        ((TextView)findViewById(R.id.game_name)).setText("Hello, " + uname + "! \n Your target is " + target_name + ".\n Find and kill!");

        ad = new AccelerometerData();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(ad,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_FASTEST);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.


        startRepeatingTask();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(ad,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER
        ),SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private class AccelerometerData implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                ax=event.values[0];
                ay=event.values[1];
                az=event.values[2];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                mTask = new UpdateCoordinatesTask();
                try {
                    mTask.execute(new URL("https://k1ller.herokuapp.com/update"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };



    public class UpdateCoordinatesTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... params) {

            StringEntity stringEntity = null;
            try {
                String j = "{ \"_id\" : \"" + id + "\", \"ax\" : \"" + ax + "\", \"ay\" : \"" + ay + "\", \"az\" : \"" +az +"\" }";
//                System.out.println(j);
                stringEntity = new StringEntity(j);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            try {
                httppost = new HttpPost(new java.net.URI(params[0].toString()));
                httppost.setHeader("Content-type", "application/json");
                httppost.setEntity(stringEntity);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            InputStream inputStream = null;
            String result = "";
            try {
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                result = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try{if(inputStream != null)inputStream.close();}catch(Exception e){
                    e.printStackTrace();
                }
            }
            return result;



            // TODO: register the new account here.

        }



        @Override
        protected void onPostExecute(final String success) {


            if (success != "") {

                JSONObject jObject = null;
                double my_x = 0;
                float my_y = 0;


                try {

                    if(board.isDead){
                        Intent intent = new Intent(FullscreenActivity.this, EndScreenActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        stopRepeatingTask();
                        finish();

                    } else if(board.isWin) {
                        Intent intent = new Intent(FullscreenActivity.this, WinScreenActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        stopRepeatingTask();
                        finish();
                    }
                    else{
                        jObject = new JSONObject(success);
                        board.invalidate();
                        board.users = jObject.getJSONArray("users");
                        board.my_id = id;
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        protected void onCancelled() {
            //mAuthTask = null;
            //showProgress(false);
        }
    }


    public class KillTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... params) {

            StringEntity stringEntity = null;
            try {
                String j = "{ \"_id\" : \"" + id + "\" }";
                System.out.println(j);
                stringEntity = new StringEntity(j);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            try {
                httppost = new HttpPost(new java.net.URI(params[0].toString()));
                httppost.setHeader("Content-type", "application/json");
                httppost.setEntity(stringEntity);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            InputStream inputStream = null;
            String result = "";
            try {
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                result = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try{if(inputStream != null)inputStream.close();}catch(Exception e){
                    e.printStackTrace();
                }
            }
            return result;



            // TODO: register the new account here.

        }



        @Override
        protected void onPostExecute(final String success) {


            if(success != ""){

                JSONObject jObject = null;
                System.out.print("response " + success);
                JSONArray users;
                try {
                    jObject = new JSONObject(success);
                    users =  jObject.getJSONArray("users");

                    if(users != null){
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);
                            if (user.getString("_id").equals(id)) {

                                target_id = user.getString("targetUserId");
                                target_name = user.getString("targetUserName");
                                break;
                            }
                        }

                        ((TextView)findViewById(R.id.game_name)).setText("Hello, " + uname + "! \n Your target is " + target_name + ".\n Find and kill!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


//            if (success != "") {
//
//                JSONObject jObject = null;
//                double my_x = 0;
//                float my_y = 0;
//
//
//                try {
//                    jObject = new JSONObject(success);
//
//                    System.out.print(jObject.toString());
//                    board.invalidate();
//                    board.users = jObject.getJSONArray("users");
//                    board.my_id = id;
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
        }

        @Override
        protected void onCancelled() {
            //mAuthTask = null;
            //showProgress(false);
        }
    }
}


package com.pewds.oussa.subscribetopewdiepie;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.*;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.DecimalFormat;
import java.util.*;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final String API_KEY = "AIzaSyD2CpWyezXa5qZwHBHqu2pLecapLNfaOmQ";
    private static final float SHAKE_THRESHOLD = 15f; // m/S**2
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000;
    private long mLastShakeTime;
    private SensorManager mSensorMgr;
    TextView pewdsubcount;
    TextView Tseries;
    ProgressDialog progressDialog = null;
    Boolean online = true;
    Boolean timeris = false;
    Timer t = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pewdsubcount = findViewById(R.id.pewsubs);
        Tseries = findViewById(R.id.tsubs);
        FloatingActionButton fab = findViewById(R.id.share);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (online) {
                    Intent share = new Intent(android.content.Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    share.putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/user/PewDiePie");
                    startActivity(Intent.createChooser(share, "Share !"));
                }
                else {
                    Toast.makeText(MainActivity.this, "Please check your internet connection and restart the app", Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!timeris) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Setting up data");
            progressDialog.show();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    new LongOperation().execute();
                }
            }, 0, 1000);
        }
        // Get a sensor manager to listen for shakes
        mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Listen for shakes
        Sensor accelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
                Boolean restoredText = prefs.getBoolean("open",false);
                if(!restoredText) {
                    Snackbar.make(pewdsubcount, "Just shake your phone to subscribe to the best", Snackbar.LENGTH_LONG).setAction("got it", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences.Editor editor = getSharedPreferences("s", MODE_PRIVATE).edit();
                            editor.putBoolean("open",true);
                            editor.apply();
                        }
                    }).show();

                }
            }
        }, 4000);  //5 seconds
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(Math.pow(x, 2) +
                        Math.pow(y, 2) +
                        Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;


                if (acceleration > SHAKE_THRESHOLD) {
                    mLastShakeTime = curTime;
                    if(online) {
                        String url = "https://www.youtube.com/user/PewDiePie";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Please check your internet connection and restart the app", Toast.LENGTH_LONG).show();

                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorMgr.unregisterListener(this);
    }

    class LongOperation extends AsyncTask<String, Void, BigInteger[]> {

        @Override
        protected BigInteger[] doInBackground(String... params) {
            BigInteger[] subs = new BigInteger[2];
            if(isOnline()) {
                YouTube youtube = new YouTube.Builder(
                        new NetHttpTransport(),
                        new JacksonFactory(),
                        new HttpRequestInitializer() {
                            public void initialize(HttpRequest request) throws IOException {
                            }
                        })
                        .setApplicationName("youtube-cmdline-search-sample")
                        .setYouTubeRequestInitializer(new YouTubeRequestInitializer(API_KEY))
                        .build();

                for (int i = 0; i < 2; i++) {
                    String channelId;
                    if (i == 0) {
                        channelId = "UC-lHJZR3Gqxm24_Vd_AJ5Yw";
                    } else {
                        channelId = "UCq-Fj5jknLsUf-MWSy4_brA";
                    }

                    YouTube.Channels.List channels = null;
                    try {
                        channels = youtube.channels().list("snippet, statistics");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    channels.setId(channelId);

                    ChannelListResponse channelResponse = null;
                    try {
                        channelResponse = channels.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (channelResponse != null) {
                        for (Channel c : channelResponse.getItems()) {
                            //System.out.println("Name: " + c.getSnippet().getTitle());
                            subs[i] = c.getStatistics().getSubscriberCount();
                            //System.out.println();

                        }
                    } else {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "Sorry we have server issues, Just wait some time and restart the app", Toast.LENGTH_LONG).show();

                            }
                        });
                        try {
                            t.cancel();
                            timeris= true;
                        }
                        catch (Exception e){ throw e;}
                    }

                }
            }
            else {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Please check your internet connection and restart the app", Toast.LENGTH_LONG).show();
                    }
                });
                try {
                    t.cancel();
                    timeris= true;
                }
                catch (Exception e){throw e; }

            }
            return subs;
        }

        @Override
        protected void onPostExecute(BigInteger[] result) {

                for (int i = 0; i < result.length; i++) {
                    if(result[i] != null) {
                        String number = result[i].toString();
                        double amount = Double.parseDouble(number);
                        DecimalFormat formatter = new DecimalFormat("##,###");
                        if (i == 0) {
                            pewdsubcount.setText(formatter.format(amount));
                        } else {
                            Tseries.setText(formatter.format(amount));
                        }
                    }
            }
            if(progressDialog!= null) {
                progressDialog.hide();
            }

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
    public boolean isOnline() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();
            online = true;
            return true;
        } catch (IOException e) {
            online = false;
            return false;
        }
    }
}

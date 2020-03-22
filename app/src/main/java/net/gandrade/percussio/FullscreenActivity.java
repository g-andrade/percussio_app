package net.gandrade.percussio;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    private View mContentView;
    private Button mPlayButton;
    private Button mStopButton;
    private Vibrator mVibrator;

    /******************************************************************/
    private TcpClient mTcpClient;

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            String serverMessage = values[0];
            String[] serverMessageParts = serverMessage.split("\\s+");
            if (serverMessageParts.length == 2 && serverMessageParts[0].equals("BEAT")) {
                String strInterval = serverMessageParts[1];
                Log.d("blabla", "Server told us to play beat for " + strInterval + "ms");
                int interval = Integer.parseInt(serverMessageParts[1]);
                mVibrator.vibrate(interval);
                /*try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
            else {
                Log.w("blabla", "Server sent us an unmatched message: " + serverMessage);
            }
        }
    }

    /******************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentView = findViewById(android.R.id.content);

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // remove app title bar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setContentView(R.layout.activity_fullscreen);

        mPlayButton = (Button) findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTcpClient != null) {
                    mTcpClient.sendMessage("PLAY");
                }

                // Get instance of Vibrator from current Context
                /*Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                vibrator.vibrate(1000 / 8);
                vibrator.vibrate(1000 / 8);
                vibrator.vibrate(1000 / 8);*/
            }
        });

        mStopButton = (Button) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTcpClient != null) {
                    mTcpClient.sendMessage("STOP");
                }
            }
        });

        // Get instance of Vibrator from current Context
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        new ConnectTask().execute("");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}

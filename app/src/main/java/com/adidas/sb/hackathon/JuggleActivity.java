package com.adidas.sb.hackathon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.adidas.sensors.api.ConnectivityService;
import com.adidas.sensors.api.Sensor;
import com.adidas.sensors.api.SensorService;
import com.adidas.sensors.api.SensorServiceState;
import com.adidas.sensors.api.SensorServiceStateListener;
import com.adidas.sensors.api.smartball.BallCommandListener;
import com.adidas.sensors.api.smartball.DataDownloader;
import com.adidas.sensors.api.smartball.KickListener;
import com.adidas.sensors.api.smartball.SmartBallService;

public class JuggleActivity extends AppCompatActivity implements  KickListener {

    private static final String TAG = "JuggleActivity";

    private SmartBallService smartBallService;

    private CheckBox chbReadyToKick;
    private TextView txtJuggleCount;
    private DataDownloader downloader;
    private ConnectivityService connectivityService;
    private TextView txtConnectionStatus;
    public int juggles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent in = getIntent();
        Sensor sensor = in.getParcelableExtra(getString(R.string.INTENT_SELECTED_SENSOR));
        setContentView(R.layout.activity_juggle);

        chbReadyToKick = (CheckBox) findViewById(R.id.chbox_readyToKick);
        txtJuggleCount = (TextView) findViewById(R.id.txt_juggle_count);
        txtConnectionStatus = (TextView) findViewById(R.id.tv_connectivity_status);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        connectivityService = sensor.obtainService(this, ConnectivityService.class);
        connectivityService.start();

        smartBallService = sensor.obtainService(this, SmartBallService.class);
        smartBallService.setSensorServiceStateListener(new SensorServiceStateListener() {
            @Override
            public void sensorServiceStateChanged(SensorService service, @SensorServiceState int newState) {
                Log.i(TAG, "Connection state changed to " + newState);
                handleConnectionStateChanged(newState);
            }
        });
        smartBallService.setOnKickListener(this);
        resetActivityState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        smartBallService.subscribeToServices();
    }

    private void handleConnectionStateChanged(int newState) {
        switch (newState) {
            case SensorServiceState.STATE_UNKNOWN:
            case SensorServiceState.STATE_NOT_AVAILABLE:
            case SensorServiceState.STATE_DISCONNECTED:
                onDisconnect();
                break;
            case SensorServiceState.STATE_READY:
                onReconnect();
                break;
        }
    }

    private void onReconnect() {
        txtConnectionStatus.setText(R.string.connected);
        softResetBall(ResetReason.RECONNECTING);
    }

    private void onDisconnect() {
        txtConnectionStatus.setText(R.string.disconnected);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "in onDestroy");
        // Must send soft reset to cancel download if it is happening. Otherwise, ball may become unusable for at least 20 seconds
        softResetBall(ResetReason.CLOSING_ACTIVITY);
    }

    private void cancelDownload() {
        if (downloader != null) {
            // Unsubscribes data received listener. Has no effect if already unsubscribed
            Log.i(TAG, "Unsubscribing downloader");
            downloader.cancel();
            downloader = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "RELOAD_BTN").setIcon(R.drawable.ic_menu_refresh).
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        } else if (item.getTitle().equals("RELOAD_BTN")) {
            softResetBall(ResetReason.REFRESH_ACTIVITY);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void softResetBall(final ResetReason reason) {
        // Soft reset command will reset any download that is in progress
        smartBallService.sendSoftResetCommand(new BallCommandListener() {
            @Override
            public void commandSuccesful() {
                Log.i(TAG, "Soft reset succesful");
                switch (reason) {
                    case PREPARE_KICK:
                        initiateStartLogging();
                        break;
                    case REFRESH_ACTIVITY:
                        cancelDownload();
                        resetActivityState();
                        break;
                    case CLOSING_ACTIVITY:
                        cancelDownload();
                        smartBallService.destroy();
                        connectivityService.stop();
                        break;
                    case RECONNECTING:
                        cancelDownload();
                        downloader = null;
                }
            }

            @Override
            public void commandFailed() {
                onError();
            }
        });
    }

    private void resetActivityState() {
        chbReadyToKick.setChecked(false);
        chbReadyToKick.setEnabled(false);
        juggles = 0;
        txtJuggleCount.setText("0");
    }

    private void onError() {
        Toast.makeText(this, "Oops, the download failed. Make sure the ball is in range and try again", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: the last command failed");
    }

    public void clickStartJuggling(View view) {
        startJuggle();
    }

    public void clickStopJuggling(View view) {
        stopJuggle();
    }

    private void stopJuggle() {
        //send player_name + juggles to server3
        //go back to leadership activity

        chbReadyToKick.setChecked(false);
        chbReadyToKick.setEnabled(false);
        softResetBall(ResetReason.CLOSING_ACTIVITY);
    }

    //SmartBall operations
    private void startJuggle() {
        resetActivityState();
        softResetBall(ResetReason.PREPARE_KICK);
    }

    private void initiateStartLogging() {
        Log.i(TAG, "In initiateStartLogging, sending start logging");
        //puts the ball into logging mode, onReadyToKick and onKickDetected will be called afterwards
        smartBallService.startLogging(new BallCommandListener() {
            @Override
            public void commandSuccesful() {
                //nothing to do, let's wait to the kick
                Log.i(TAG, "Start logging succesful, waiting for kick");
            }
            
            @Override
            public void commandFailed() {
                onError();
            }
        });
    }

    @Override
    public void onReadyToKick(SmartBallService sbs) {
        chbReadyToKick.setChecked(true);
        chbReadyToKick.setEnabled(true);
    }

    @Override
    public void onKickDetected(SmartBallService sbs) {

        this.juggles += 1;

        String dribblingString = Integer.toString(juggles);

        txtJuggleCount.setText(dribblingString);

        /*chbKickDetected.setEnabled(true);

        if (chbKickDetected.isChecked()) {
            chbKickDetected.setChecked(false);
        } else {
            chbKickDetected.setChecked(true);
        }
        */
        softResetBall(ResetReason.PREPARE_KICK);
    }
}

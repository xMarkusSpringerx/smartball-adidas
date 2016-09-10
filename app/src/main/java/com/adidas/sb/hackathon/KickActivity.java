package com.adidas.sb.hackathon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.adidas.sensors.api.SensorService;
import com.adidas.sensors.api.SensorServiceState;
import com.adidas.sensors.api.SensorServiceStateListener;
import com.adidas.sensors.api.smartball.BallCommandListener;
import com.adidas.sensors.api.smartball.DataDownloader;
import com.adidas.sensors.api.smartball.DataDownloaderListener;
import com.adidas.sensors.api.smartball.KickData;
import com.adidas.sensors.api.smartball.KickListener;
import com.adidas.sensors.api.smartball.SmartBallService;
import com.adidas.sensors.api.ConnectivityService;
import com.adidas.sensors.api.Sensor;

public class KickActivity extends AppCompatActivity implements DataDownloaderListener, KickListener {

    private static final String TAG = "KickActivity";

    private SmartBallService smartBallService;

    private Button btnPositionTheBall;
    private Button btnDisplayData;
    private Button btnDownloadData;
    private CheckBox chbReadyToKick;
    private CheckBox chbKickDetected;
    private CheckBox chbDataDownloaded;
    private TextView txtDownloadProgress;
    private DataDownloader downloader;
    private ConnectivityService connectivityService;
    private KickData data;
    private TextView txtConnectionStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent in = getIntent();
        Sensor sensor = in.getParcelableExtra(getString(R.string.INTENT_SELECTED_SENSOR));
        setContentView(R.layout.activity_kick);

        btnPositionTheBall = (Button) findViewById(R.id.btn_position_the_ball);
        btnDisplayData = (Button) findViewById(R.id.btn_display_data);
        chbReadyToKick = (CheckBox) findViewById(R.id.chbox_readyToKick);
        chbKickDetected = (CheckBox) findViewById(R.id.chbox_kickDetected);
        btnDownloadData = (Button) findViewById(R.id.btn_data_downloading);
        chbDataDownloaded = (CheckBox) findViewById(R.id.chbox_dataDownloaded);
        txtDownloadProgress = (TextView) findViewById(R.id.txt_downloadPercentage);
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
        if (chbKickDetected.isChecked()) {
            softResetBall(ResetReason.RECONNECTING);
            btnDownloadData.setEnabled(true);
        }
    }

    private void onDisconnect() {
        txtConnectionStatus.setText(R.string.disconnected);
        btnDownloadData.setEnabled(false);
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
        chbDataDownloaded.setChecked(false);
        chbDataDownloaded.setEnabled(false);
        chbKickDetected.setChecked(false);
        chbKickDetected.setEnabled(false);
        chbReadyToKick.setChecked(false);
        chbReadyToKick.setEnabled(false);
        btnDownloadData.setEnabled(false);
        btnDisplayData.setEnabled(false);
        txtDownloadProgress.setText(getString(R.string.download_progress, 0));
    }

    private void onError() {
        Toast.makeText(this, "Oops, the download failed. Make sure the ball is in range and try again", Toast.LENGTH_SHORT).show();
        btnDownloadData.setEnabled(true);
        Log.e(TAG, "Error: the last command failed");
    }

    public void clickPositionTheBall(View view) {
        positionTheBall();
    }

    //SmartBall operations
    private void positionTheBall() {
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
        chbKickDetected.setChecked(true);
        btnDownloadData.setEnabled(true);
        chbKickDetected.setEnabled(true);
    }


    //Download data from the ball
    public void btnDownloadClick(View view) {
        //Initiate download and notify with updateProgress, downloadFailed or downloadFinished
        downloader = smartBallService.downloadKickData(this);
        btnDownloadData.setEnabled(false);
    }

    //called during download to update progress
    @Override
    public void updateProgress(int progress) {
        txtDownloadProgress.setText(getString(R.string.download_progress, progress));
    }

    //download failed, i.e. ball is out of range
    @Override
    public void downloadFailed() {
        //we could restart the download here
        onError();
    }

    @Override
    public void downloadFinished(KickData data) {
        chbDataDownloaded.setChecked(true);
        chbDataDownloaded.setEnabled(true);
        btnDownloadData.setEnabled(true);
        btnDisplayData.setEnabled(true);
        this.data = data;
        txtDownloadProgress.setText(getString(R.string.download_progress, 100));
    }



    //Pass the KickData to DisplayDataActivity
    public void btnDisplayDataClick(View view) {
        Intent intent = new Intent(this, DisplayDataActivity.class);
        intent.putExtra(getString(R.string.KICK_DATA), data);
        startActivity(intent);
    }
}
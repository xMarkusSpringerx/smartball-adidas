package com.adidas.sb.hackathon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.adidas.sensors.api.ServiceData;
import com.adidas.sensors.api.btle.BluetoothLESensorDataErrorCode;
import com.adidas.sensors.api.smartball.BallInfo;
import com.adidas.sensors.api.smartball.SmartBallService;
import com.adidas.sensors.api.BatteryMeasurementData;
import com.adidas.sensors.api.BatteryService;
import com.adidas.sensors.api.DeviceInformation;
import com.adidas.sensors.api.DeviceInformationService;
import com.adidas.sensors.api.Sensor;
import com.adidas.sensors.api.ServiceReadEvent;
import com.adidas.sensors.api.ServiceReadListener;
import com.adidas.sensors.api.ServiceReader;


public class BallInfoActivity extends AppCompatActivity {

    private ServiceReader deviceInfoReader;
    private ServiceReader batteryReader;
    private ServiceReader ballStatusReader;

    protected Sensor sensor;

    private TextView txtName;
    private TextView txtBatteryLevel;
    private TextView txtFirmware;
    private TextView txtIsBeingCharged;
    private TextView txtOnCharger;
    private TextView txtChargingStopped;
    private TextView txtSampleRate;
    private Button btnKick;
    private Button btnJuggle;
    private short samplePeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ball_info);

        txtName = (TextView) findViewById(R.id.main_name);
        txtBatteryLevel = (TextView) findViewById(R.id.main_battery);
        txtFirmware = (TextView) findViewById(R.id.main_firmware);
        txtIsBeingCharged = (TextView) findViewById(R.id.main_isBeingCharged);
        txtOnCharger = (TextView) findViewById(R.id.main_isOnCharger);
        txtChargingStopped = (TextView) findViewById(R.id.main_hasChargingStopped);
        txtSampleRate = (TextView) findViewById(R.id.main_sample_rate);
        btnKick = (Button) findViewById(R.id.btn_kick);
        btnJuggle = (Button) findViewById(R.id.btn_juggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sensor = getIntent().getParcelableExtra(getString(R.string.INTENT_SELECTED_SENSOR));
        txtName.setText(sensor.getName());

        //Get Ball's status
        SmartBallService smartBallService = sensor.obtainService(this, SmartBallService.class);
        ballStatusReader = smartBallService.readBuilder()
                .status()
                .samplePeriod()
                .build(new ServiceReadListener() {
                    @Override
                    public void onReadComplete(ServiceReadEvent event) {
                        if (event.hasFailed()) {
                            showError(event.getError(), txtIsBeingCharged);
                        } else {
                            showDeviceData(event.getData());
                        }
                    }
                });


        //Setup Battery Service
        BatteryService batteryService = sensor.obtainService(this, BatteryService.class);
        batteryReader = batteryService.readBuilder()
                .batteryLevel()
                .build(new ServiceReadListener() {
                    @Override
                    public void onReadComplete(ServiceReadEvent event) {
                        if (event.hasFailed()) {
                            showError(event.getError(), txtBatteryLevel);
                        } else {
                            showDeviceData(event.getData());
                        }
                    }
                });


        //Get the ball's firmware
        DeviceInformationService devInfoService = sensor.obtainService(this, DeviceInformationService.class);
        deviceInfoReader = devInfoService.readBuilder()
                .firmwareRevision()
                .build(new ServiceReadListener() {
                    @Override
                    public void onReadComplete(ServiceReadEvent event) {
                        if (event.hasFailed()) {
                            showError(event.getError(), txtFirmware);
                        } else {
                            showDeviceData(event.getData());
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //read the battery, firmware and ball status when this activity comes into foreground
        batteryReader.read();
        deviceInfoReader.read();
        ballStatusReader.read();
    }

    @Override
    protected void onPause() {
        super.onPause();

        batteryReader.cancel();
        deviceInfoReader.cancel();
        ballStatusReader.cancel();
    }

    public void btnKickClick(View view) {
        Intent iActivityKick = new Intent(this, KickActivity.class);
        iActivityKick.putExtra(getString(R.string.INTENT_SELECTED_SENSOR), sensor);
        startActivity(iActivityKick);
    }

    public void btnJuggleClick(View view) {
        Intent iActivityJuggleLeaderboard = new Intent(this, JuggleLeaderboardActivity.class);
        iActivityJuggleLeaderboard.putExtra(getString(R.string.INTENT_SELECTED_SENSOR), sensor);
        startActivity(iActivityJuggleLeaderboard);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showDeviceData(ServiceData data) {
        if (data instanceof BatteryMeasurementData) {
            BatteryMeasurementData batteryData = (BatteryMeasurementData) data;
            txtBatteryLevel.setText(String.valueOf(batteryData.getBatteryLevel()));
        } else if (data instanceof BallInfo) {
            BallInfo info = (BallInfo) data;
            btnKick.setEnabled(true);
            btnJuggle.setEnabled(true);
            txtIsBeingCharged.setText(String.valueOf(info.isBeingCharged()));
            txtChargingStopped.setText(String.valueOf(info.hasChargingStopped()));
            txtOnCharger.setText(String.valueOf(info.isOnCharger()));
            samplePeriod = info.getSamplePeriod();
            txtSampleRate.setText(String.valueOf(1000 / samplePeriod));
        } else if (data instanceof DeviceInformation) {
            DeviceInformation deviceInformation = (DeviceInformation) data;
            txtFirmware.setText(deviceInformation.getFirmwareRevision());
        }
    }

    private void showError(@BluetoothLESensorDataErrorCode int errorCode, TextView textDisplay) {

        if (errorCode != BluetoothLESensorDataErrorCode.SUCCESS) {
            String error;
            switch (errorCode) {
                case BluetoothLESensorDataErrorCode.GATT_SERVICE_NOT_AVAILABLE:
                    error = getString(R.string.error_gatt_service_not_available);
                    break;
                case BluetoothLESensorDataErrorCode.SENSOR_DISCONNECTED:
                    error = getString(R.string.error_device_disconnected);
                    break;
                case BluetoothLESensorDataErrorCode.BLUETOOTH_OFF:
                    error = getString(R.string.error_bluetooth_off);
                    break;
                case BluetoothLESensorDataErrorCode.GATT_CONNECT_FAILED:
                    error = getString(R.string.error_gatt_connect_failed);
                    break;
                default:
                    error = String.valueOf(errorCode);
                    break;
            }
            if (textDisplay != null)
                textDisplay.setText(getString(R.string.error_with_type, error));
        }
    }
}
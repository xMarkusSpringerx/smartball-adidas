package com.adidas.sb.hackathon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.adidas.sensors.api.btle.BluetoothLESensorUtils;
import com.adidas.sensors.api.smartball.SmartBallService;
import com.adidas.sensors.api.Sensor;
import com.adidas.sensors.api.btle.BluetoothLEAdapterFactory;
import com.adidas.sensors.api.btle.ScanEvent;
import com.adidas.sensors.api.btle.SensorScanListener;
import com.adidas.sensors.api.btle.SensorScanner;

import java.util.ArrayList;

public class SelectBallActivity extends AppCompatActivity {

    private SensorScanner sensorScanner;
    private ListView theListView;
    //private CardView cardView;
    private Sensor sensor;
    private ArrayList<SensorInfo> allSensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ball);

        // check location permission
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }

        allSensors = new ArrayList<SensorInfo>();
        theListView = (ListView) findViewById(R.id.selectball_listview);
        //cardView = (CardView) findViewById(R.id.select_ball_card_view);
        final SensorInfoAdapter itemsAdapter = new SensorInfoAdapter(this, allSensors);
        theListView.setAdapter(itemsAdapter);

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), BallInfoActivity.class);
                intent.putExtra(getString(R.string.INTENT_SELECTED_SENSOR), allSensors.get(i).getSensor());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //Prepare for a BT LE scan, the scan itself will start in onResume
        // only scan for sensors containing SmartBall service
        SensorScanner.ScanConfiguration config = new SensorScanner.ScanConfiguration()
                .addService(SmartBallService.DIGITAL_BALL_SERVICE);

        BluetoothAdapter adapter = BluetoothLEAdapterFactory.getBluetoothAdapter(this);

        sensorScanner = new SensorScanner(this, adapter, config);
        sensorScanner.setOnSensorScanListener(new SensorScanListener() {
            @Override
            public void onSensorFound(ScanEvent scanEvent) {
                sensor = scanEvent.getSensor();
                SensorInfo info = new SensorInfo(sensor, scanEvent.getRssi());
                displaySensorInfo(info, itemsAdapter);
            }
        });
    }

    private void displaySensorInfo(SensorInfo info, SensorInfoAdapter itemsAdapter) {
        if (info.getName() != null) {
            if (allSensors.contains(info)) {
                allSensors.remove(info);
            }
            int position = 0;
            while (position != -1) {
                if (position == allSensors.size() || info.getRssi() > allSensors.get(position).getRssi()) {
                    itemsAdapter.insert(info, position);
                    position = -1;
                } else {
                    position++;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //start scanning when the activity is in foreground
        sensorScanner.startScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop scanning when this activity is in background
        sensorScanner.stopScanning();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "App will not work without location permission. Please restart the app and grant the permission.", Toast.LENGTH_LONG).show();
        }
    }
}

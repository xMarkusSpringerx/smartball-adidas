package com.adidas.sb.hackathon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.adidas.sensors.api.smartball.KickData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.renderer.XAxisRenderer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DisplayDataActivity extends AppCompatActivity {

    private LineChart lineChart;
    private KickData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);
        Intent intent = getIntent();
        data = intent.getParcelableExtra(getString(R.string.KICK_DATA));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        short samplePeriod = data.getSamplePeriod();

        lineChart = (LineChart) findViewById(R.id.chart);

        List<Entry> entriesX = new ArrayList<Entry>();
        List<Entry> entriesY = new ArrayList<Entry>();
        List<Entry> entriesZ = new ArrayList<Entry>();
        for (int i = 0; i < data.getX().length; ++i) {
            entriesX.add(new Entry((float) i * samplePeriod, (float) data.getX(i)));
            entriesY.add(new Entry((float) i * samplePeriod, (float) data.getY(i)));
            entriesZ.add(new Entry((float) i * samplePeriod, (float) data.getZ(i)));
        }
        LineDataSet dataSetX = new LineDataSet(entriesX, "X"); // add entries to dataset
        LineDataSet dataSetY = new LineDataSet(entriesY, "Y");
        LineDataSet dataSetZ = new LineDataSet(entriesZ, "Z");

        dataSetX.setColor(Color.RED);
        dataSetY.setColor(Color.GREEN);
        dataSetZ.setColor(Color.BLUE);
        dataSetX.setMode(LineDataSet.Mode.LINEAR);
        dataSetX.setDrawCircles(false);
        dataSetY.setDrawCircles(false);
        dataSetZ.setDrawCircles(false);

        LineData lineData = new LineData(dataSetX, dataSetY, dataSetZ);

        lineChart.setDescription("");
        lineChart.getLegend().setPosition(Legend.LegendPosition.ABOVE_CHART_CENTER);//.setEnabled(true);
        lineChart.getAxisLeft().setDrawLabels(true);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getXAxis().setDrawLabels(true);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.setData(lineData);
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
}

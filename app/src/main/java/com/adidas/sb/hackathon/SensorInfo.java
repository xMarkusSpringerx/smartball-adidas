package com.adidas.sb.hackathon;

import com.adidas.sensors.api.Sensor;

/**
 * Created by domenm on 17.8.2016.
 */
public class SensorInfo {

    private String name;
    private int rssi;
    private Sensor sensor;

    public SensorInfo(Sensor sensor, int rssi) {
        this.sensor = sensor;
        this.name = sensor.getName();
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    public int getRssi() {
        return rssi;
    }

    public Sensor getSensor() {
        return sensor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorInfo that = (SensorInfo) o;

        return sensor.getAddress().equals(that.sensor.getAddress()) && name.equals(that.getName());

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + sensor.getAddress().hashCode();
        return result;
    }
}

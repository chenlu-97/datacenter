package com.sensorweb.datacenterweatherservice.entity;

import lombok.Data;

import java.time.Instant;

@Data
public class HBWeatherStation {
    private int id;
    private String stationId;
    private float lat;
    private float lon;
    private float prs;
    private float tem;
    private float rhu;
    private float pre_1h;
    private float gst;
    private float gst_10cm;
    private float vis;
    private float win_d;
    private float win_s;
    private Instant queryTime;
}

package com.sensorweb.datacenterweatherservice.entity;


import lombok.Data;

import java.time.Instant;


@Data
public class ChinaWeather2 {
    private int id;
    private String stationId;
    private Instant time;
    private String lng;
    private String lat;


    private String win_s_avg;
    private String tem;
    private String rhu;
    private String prs;
    private String vis;
    private String prs_sea;
    private String prs_max;

    private String prs_min;
    private String tem_max;
    private String tem_min;
    private String rhu_min;
    private String vap;
    private String pre_1h;
    private String win_d_inst_max;

    private String win_s_max;
    private String win_d_s_max;
    private String win_s_avg_2mi;
    private String win_d_avg_2mi;
    private String win_s_inst_max;
    private String windPower;

}

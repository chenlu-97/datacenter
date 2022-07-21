package com.sensorweb.datacenterairservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

@Data
public class AirQualityHour {
    private String stationName;
    private String uniqueCode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Instant queryTime;
    private String pm25OneHour;
    private String pm10OneHour;
    private String so2OneHour;
    private String no2OneHour;
    private String coOneHour;
    private String o3OneHour;
    private String aqi;
    private String primaryEP;
    private String aqDegree;
    private String aqType;

    //2022.7.1新加字段
    private String noOneHour;
    private String noxOneHour;
    private String temperature;
    private String humidity;
    private String windDirection;
    private String windSpeed;
    private String visibility;
}

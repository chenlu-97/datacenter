package com.sensorweb.datacentergeeservice.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

@Data
public class Landsat {

    private String imageID;
    private String sensorID;
    private String spacecraftID;
    private String Coordinates;
    private String geom;
    private Instant Date;
    private String Time;
    private String imageSize;
    private String Ellipsoid;
    private Float Cloudcover;
    private String Thumburl;
    private String imageType;
    private String filePath;
    private String waveBand = "430nm~12510nm";
    private String bandInfo = "可见光&近红外&远红外";
}

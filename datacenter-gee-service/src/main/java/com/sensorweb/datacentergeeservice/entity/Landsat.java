package com.sensorweb.datacentergeeservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
@Data
public class Landsat {

    private String imageID;
    private String sensorID;
    private String spacecraftID;
    private String Coordinates;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String Date;
    private String Time;
    private String imageSize;
    private String Ellipsoid;
    private String Cloudcover;
    private String Thumburl;
    private String imageType;
    private String filePath;
    private String waveBand = "430nm~12510nm";
    private String bandInfo = "可见光&近红外&远红外";
}

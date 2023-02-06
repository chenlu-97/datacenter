package com.sensorweb.datacenterairservice.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

/**
 * @TableName water_quality_waterstation_hourly
 */
@Data
public class WaterQualityWaterstationHourly implements Serializable {


    private String id;

    /**
     *
     */
    private String areacode;

    /**
     *
     */
    private Instant datatime;

    /**
     *
     */
    private String riverid;
    /**
     *
     */
    private String siteid;

    /**
     *
     */
    private String wpcode;

    /**
     *
     */
    private String mnname;


    private String e01001;
    private String e01002;
    private String w01001;
    private String w01003;
    private String w01009;
    private String w01010;
    private String w01014;
    private String w01016;
    private String w01022;
    private String w19011;
    private String w21001;
    private String w21003;
    private String w21011;
    private String w22008;

    private String w01019;
    private String w01023;
    private String w02007;
    private String w02005;
    private String w01024;


    private static final long serialVersionUID = 1L;


}
package com.sensorweb.datacenterairservice.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName raw_data_super_station_hourly
 */
@Data
public class RawDataSuperStationHourly implements Serializable {



    private String id;
    /**
     * 
     */
    private String st;

    /**
     * 
     */
    private String svalue;

    /**
     * 
     */
    private Date times;

    /**
     * 
     */
    private String categpry;

    /**
     * 
     */
    private String stNa;

    /**
     * 
     */
    private String epname;

    private static final long serialVersionUID = 1L;
}
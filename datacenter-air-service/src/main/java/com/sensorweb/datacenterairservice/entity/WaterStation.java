package com.sensorweb.datacenterairservice.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName water_station
 */
@Data
public class WaterStation implements Serializable {
    /**
     * 
     */
    private String mn;

    /**
     * 
     */
    private String areaid;

    /**
     * 
     */
    private String areaname;

    /**
     * 
     */
    private String riverid;

    /**
     * 
     */
    private String rivername;

    /**
     * 
     */
    private String mnname;

    /**
     * 
     */
    private float lon;

    /**
     * 
     */
    private float lat;

    /**
     * 
     */
    private String geom;

    /**
     * 
     */
    private Integer geoType;

    private static final long serialVersionUID = 1L;

}
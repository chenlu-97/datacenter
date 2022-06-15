package com.sensorweb.datacenterlaadsservice.entity;

import lombok.Data;

import java.time.Instant;




@Data
public class GPM_3IMERGDE {
    private String gpmId;
    private String title;
    private String updated;
    private String link;
    private String filePath;
    private Instant time;
    private String bbox;
    private String wkt;
    private String productType;
    private Instant getDate;
}

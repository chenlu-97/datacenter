package com.sensorweb.datacenterlaadsservice.entity;


import lombok.Data;

import java.time.Instant;

@Data
public class GLDAS {

    private int id;
    private String gldasId;
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

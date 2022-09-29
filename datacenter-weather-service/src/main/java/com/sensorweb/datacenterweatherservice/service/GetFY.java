package com.sensorweb.datacenterweatherservice.service;


import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import com.sensorweb.datacenterweatherservice.util.WeatherConstant;
import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Service
@EnableScheduling
public class GetFY {



    @PostConstruct
    public void getApiDocument() throws IOException {
        String param =  "userId=BCWH_QXT_WHUZHY&pwd=whuzhy_210323&interfaceId=getSateFileByTimeRange&elements=Datetime,FILE_SIZE,File_URL,SATE_Sensor_Chanl&dataCode=SATE_GEO_RAW_DMZ_FY4A&timeRange=[20220914000000,20220914200000]&limitCnt=30&dataFormat=json";
        String document = DataCenterUtils.doGet("http://61.183.207.181:8008/cimiss-web/api", param);
        System.out.println("接入的FY = " + document);
//        return document;
    }


    @PostConstruct
    public void getApi() throws IOException {
        String param =  "userId=BCWH_QXT_WHUZHY&pwd=whuzhy_210323&interfaceId=getSurfSixEle&times=20220908000000&dataCode=SURF_CHN_MUL_HOR&dataFormat=json&limitCnt=30";
        String document = DataCenterUtils.doGet("http://61.183.207.181:8008/cimiss-web/api", param);
        System.out.println("接入的气象 = " + document);
//        return document;
    }




}

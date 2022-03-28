package com.sensorweb.datacenterhimawariservice;

import com.sensorweb.datacenterhimawariservice.controller.GetHimawariController;
import com.sensorweb.datacenterhimawariservice.dao.HimawariMapper;
import com.sensorweb.datacenterhimawariservice.service.InsertHimawariService;
import com.sensorweb.datacenterhimawariservice.service.NewInsertHimawariService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@SpringBootTest
class DatacenterInsertHimawariServiceApplicationTests {

    @Autowired
    private NewInsertHimawariService newinsertHimawariService;

    @Autowired
    private HimawariMapper himawariMapper;

    @Test
    void contextLoads() throws Exception {

//        LocalDateTime dateTime = LocalDateTime.now();
//        LocalDateTime time = dateTime.minusHours(7*24*60*60);
//        System.out.println("time = " + time);
//        boolean flag  = !insertHimawariService.insertData(time);
//        System.out.println("flag = " + flag);

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");
        String strDate3 = dtf3.format(dateTime);
        dateTime=LocalDateTime.parse(strDate3,dtf3);

        Instant timeNew = himawariMapper.selectMaxTimeData().get(0).getTime();
        Instant timeNow = dateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
        while(timeNew.isBefore(timeNow)){
            LocalDateTime timeNew1 = LocalDateTime.ofInstant(timeNew.plusSeconds(60*60),ZoneId.systemDefault());
            System.out.println("timeNew1 = " + timeNew1);

            timeNew = timeNew.plusSeconds(60*60);
        }

//        newinsertHimawariService.insertDataByHour();

    }

}

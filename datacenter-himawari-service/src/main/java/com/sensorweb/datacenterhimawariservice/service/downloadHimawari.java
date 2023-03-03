package com.sensorweb.datacenterhimawariservice.service;


import com.sensorweb.datacenterhimawariservice.dao.HimawariMapper;
import com.sensorweb.datacenterhimawariservice.entity.Himawari;
import com.sensorweb.datacenterhimawariservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

@Slf4j

public class downloadHimawari implements Runnable{
    @Override
    public void run() {
        System.out.println("-----2022-12-13 00:00:00---------2022-12-25 00:00:00--------Himawari----");
        HimawariMapper himawariMapper = (HimawariMapper) ApplicationContextUtil.getBean("himawariMapper");
        NewInsertHimawariService newInsertHimawariService = (NewInsertHimawariService) ApplicationContextUtil.getBean("newInsertHimawariService");
        boolean flag ;
        String startTime = "2022-12-13 14:00:00";
        String endTime = "2022-12-25 00:00:00";
        Instant timeNew = DataCenterUtils.string2Instant(startTime);
        Instant timeNow = DataCenterUtils.string2Instant(endTime);
        while(timeNew.isBefore(timeNow)){
            LocalDateTime time = LocalDateTime.ofInstant(timeNew,ZoneId.systemDefault());
//            System.out.println("time = " + time);
            int year = time.getYear();
            Month month = time.getMonth();
            String monthValue = month.getValue()<10?"0"+month.getValue():month.getValue()+"";
            String day = time.getDayOfMonth()<10?"0"+time.getDayOfMonth():time.getDayOfMonth()+"";
            String hour = time.getHour()<10?"0"+time.getHour():time.getHour()+"";
            String minute = time.getMinute()<10?"0"+time.getMinute():time.getMinute()+"";
            String fileName = newInsertHimawariService.getName(year+"", monthValue, day, hour, minute);
            List<Himawari> himawaris= himawariMapper.selectByTime(timeNew);
            if(himawaris.size()>0){
//                log.info(timeNew.plusSeconds(8*60*60)+"已存在该数据，无需下载！！！");
            }else {
                try {
                    flag = !newInsertHimawariService.insertData(time);
                    if (!flag) {
                        log.info("Himawari接入时间: " + time + "Status: Success");
                        DataCenterUtils.sendMessage("Himawari-8" + timeNew.toString(), "卫星-葵花8号", "这是一条获取的葵花8号卫星的数据",1);
                    } else {
                        log.info("Himawari接入时间: ----" + time + "下载失败！！");
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            timeNew = timeNew.plusSeconds(60*60);
        }
    }
}

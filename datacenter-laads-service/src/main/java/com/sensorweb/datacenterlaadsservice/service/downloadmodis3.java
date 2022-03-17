package com.sensorweb.datacenterlaadsservice.service;


import com.sensorweb.datacenterlaadsservice.dao.EntryMapper;
import com.sensorweb.datacenterlaadsservice.entity.Entry;
import com.sensorweb.datacenterlaadsservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

@Slf4j
public class downloadmodis3 implements Runnable{

    @Override
    public void run() {
        System.out.println("-----20200101---------20220315--------MOD13A2----");
        InsertLAADSService insertLAADSService = (InsertLAADSService) ApplicationContextUtil.getBean("insertLAADSService");
        EntryMapper entryMapper = (EntryMapper) ApplicationContextUtil.getBean("entryMapper");
        try {
            String startTime = "2020-01-01 00:00:00";
            String endTime = "2022-03-15 00:00:00";
            String product = "MOD13A2";
            String bbox = "95,24,123,35";
            SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            Calendar startloop = Calendar.getInstance();
            Calendar endloop = Calendar.getInstance();
            try {
                startloop.setTime(format.parse(startTime));
                endloop.setTime(format.parse(endTime));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            while(startloop.before(endloop))
            {
                Instant start = DataCenterUtils.string2Instant(format.format(startloop.getTime()));
                Instant end = DataCenterUtils.string2Instant(format.format(startloop.getTime())).plusSeconds(24*60*60);
                List<Entry> entrys = entryMapper.getFilePath(product,start,end);
                if(entrys.size()>0){
                        System.out.println(startTime+"已存在该数据，无需下载！！！");
                        log.info(startTime+"已存在该数据，无需下载！！！");
                }else{
                    String flag = insertLAADSService.insertData2(start.toString().replace("T"," ").replace("Z",""), end.toString().replace("T"," ").replace("Z",""), bbox, product);
                        System.out.println( startTime+ flag);
                        log.info(startTime+flag);
                }
                startloop.add(Calendar.DAY_OF_MONTH,1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

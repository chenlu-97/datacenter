package com.sensorweb.datacenterlaadsservice.download;


import com.sensorweb.datacenterlaadsservice.dao.EntryMapper;
import com.sensorweb.datacenterlaadsservice.dao.GPM_3IMERGDEMapper;
import com.sensorweb.datacenterlaadsservice.entity.Entry;
import com.sensorweb.datacenterlaadsservice.entity.GPM_3IMERGDE;
import com.sensorweb.datacenterlaadsservice.service.InsertGPM_3IMERGDE;
import com.sensorweb.datacenterlaadsservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

@Slf4j
public class downloadmodis7 implements Runnable{

    @Override
    public void run() {
        System.out.println("-----20200101---------20220608--------GPM_3IMERGDE----");
        InsertGPM_3IMERGDE insertGPM_3IMERGDE = (InsertGPM_3IMERGDE) ApplicationContextUtil.getBean("insertGPM_3IMERGDE");
//        GPM_3IMERGDEMapper gpm_3IMERGDEMapper = (GPM_3IMERGDEMapper) ApplicationContextUtil.getBean("gpm_3IMERGDEMapper");
        try {
            String startTime = "2020-01-01 00:00:00";
            String endTime = "2022-07-25 00:00:00";
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
                try {
                Instant start = DataCenterUtils.string2Instant(format.format(startloop.getTime()));
                Instant end = DataCenterUtils.string2Instant(format.format(startloop.getTime())).plusSeconds(24*60*60);
                List<GPM_3IMERGDE> gpm_3IMERGDE = insertGPM_3IMERGDE.getFilePath(start,end);
                if(gpm_3IMERGDE.size()>0){
//                        System.out.println(product+start.toString()+"已存在该数据，无需下载！！！");
//                        log.info(product+start.toString()+"已存在该数据，无需下载！！！");
                }else{
                    Boolean flag = insertGPM_3IMERGDE.insertData(start.plusSeconds(8*60*60).toString());
                    System.out.println( "GPM_3IMERGDE"+"接入时间："+start.plusSeconds(8*60*60).toString()+ "状态："+flag);
                    log.info("GPM_3IMERGDE："+"接入时间："+start.plusSeconds(8*60*60).toString()+ "状态："+flag);
                    Thread.sleep(5 * 1000); //下载太快会断开连接，这里休息5秒
                }
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                startloop.add(Calendar.DAY_OF_MONTH,1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

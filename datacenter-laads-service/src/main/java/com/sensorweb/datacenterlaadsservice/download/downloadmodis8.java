package com.sensorweb.datacenterlaadsservice.download;


import com.sensorweb.datacenterlaadsservice.entity.GLDAS;
import com.sensorweb.datacenterlaadsservice.entity.GPM_3IMERGDE;
import com.sensorweb.datacenterlaadsservice.service.InsertGLDASService;
import com.sensorweb.datacenterlaadsservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

@Slf4j
public class downloadmodis8 implements Runnable{

    @Override
    public void run() {
        System.out.println("-----20200101---------20220301--------GLDAS----");
        InsertGLDASService insertGLDASService = (InsertGLDASService) ApplicationContextUtil.getBean("insertGLDASService");
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
                List<GLDAS> gldas = insertGLDASService.getFilePath(start,end);
                if(gldas.size()>0){
//                        System.out.println(product+start.toString()+"已存在该数据，无需下载！！！");
//                        log.info(product+start.toString()+"已存在该数据，无需下载！！！");
                }else{
                    Boolean flag = insertGLDASService.insertData(start.plusSeconds(8*60*60).toString());
                    System.out.println( "GLDAS"+"接入时间："+start.plusSeconds(8*60*60).toString()+ "状态："+flag);
                    log.info("GLDAS："+"接入时间："+start.plusSeconds(8*60*60).toString()+ "状态："+flag);
                    Thread.sleep(3 * 1000); //下载太快会断开连接，这里休息3秒
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

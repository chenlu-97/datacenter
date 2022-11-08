package com.sensorweb.datacenterlaadsservice.download;

import com.sensorweb.datacenterlaadsservice.dao.MERRA2Mapper;
import com.sensorweb.datacenterlaadsservice.entity.MERRA2;
import com.sensorweb.datacenterlaadsservice.service.InsertMERRA2;
import com.sensorweb.datacenterlaadsservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

@Slf4j
public class downloadmodis10 implements Runnable{

    @Override
    public void run() {
        System.out.println("-----20200101---------20221026--------MERRA2----");
        InsertMERRA2 insertMERRA2 = (InsertMERRA2) ApplicationContextUtil.getBean("insertMERRA2");
//        MERRA2Mapper merra2 = (MERRA2Mapper) ApplicationContextUtil.getBean("merra2");
        try {
            String startTime = "2020-01-01 00:00:00";
            String endTime = "2022-10-26 00:00:00";
            String product = "MERRA2";

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
//                List<MERRA2> merra2s = merra2.getFilePath(product,start,end);
//                if(merra2s.size()>0){
//                        System.out.println(product+start.toString()+"已存在该数据，无需下载！！！");
//                        log.info(product+start.toString()+"已存在该数据，无需下载！！！");
//                }else{
                    boolean flag = insertMERRA2.insertData(start.plusSeconds(8*60*60).toString());
                    System.out.println( "获取MERRA2产品："+product+"接入时间："+start.toString()+ "状态："+flag);
                    log.info("获取MERRA2产品："+product+"接入时间："+start.toString()+ "状态："+flag);
                    Thread.sleep(60 * 1000); //下载太快会断开连接，这里休息1分钟
//                }
                startloop.add(Calendar.DAY_OF_MONTH,1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

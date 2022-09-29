package com.sensorweb.datacenterairservice.thread;


import com.alibaba.fastjson.JSONArray;
import com.sensorweb.datacenterairservice.service.InsertAirService;
import com.sensorweb.datacenterairservice.service.WaterQualityWaterstationHourlyService;
import com.sensorweb.datacenterairservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;

@Slf4j
public class downloadAir implements Runnable{
    @Override
    public void run() {
        try {
            //起始时间
            String str="2020-12-01 00:00:00";
            //结束时间
            String str1="2021-01-01 00:00:00";
            System.out.println("-----------"+str+"-----------"+str1+"------------湖北省空气站点---------");
            InsertAirService insertAirService = (InsertAirService) ApplicationContextUtil.getBean("insertAirService");
            SimpleDateFormat format= new  SimpleDateFormat("yyyy-MM-dd HH:ss:00");
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            try {
                start.setTime(format.parse(str));
                end.setTime(format.parse(str1));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            while(start.before(end))
            {
                Instant startTime = DataCenterUtils.string2Instant(format.format(start.getTime())).plusSeconds(8*60*60);
                Instant endTime = DataCenterUtils.string2Instant(format.format(start.getTime())).plusSeconds(32*60*60).minusSeconds(1);
                log.info("------ 开始下载" + format.format(start.getTime()) + "的空气站-----");
                String begin = startTime.toString().replace("T", " ").replace("Z", "");
                String stop = endTime.toString().replace("T", " ").replace("Z", "");

                JSONArray access_res = insertAirService.getInfo(begin,stop,insertAirService.getStationId());
                boolean flag = insertAirService.accessinforming(access_res);
//                boolean flag  = insertAirService.accessinforming(begin,stop);
                if(flag){
                    System.out.println("湖北省空气站点接入时间: " + begin + "Status: Success");
                }else{
                    System.out.println("湖北省空气站点接入时间: " + begin + "Status: Fail");
                }
                start.add(Calendar.DAY_OF_MONTH,1);
                Thread.sleep(2 * 1000); //下载太快会断开连接，这里休息2秒
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.info("湖北省空气站点接入时间: " + "Status: Fail");
        }
    }



}

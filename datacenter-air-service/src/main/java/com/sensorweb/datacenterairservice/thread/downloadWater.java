package com.sensorweb.datacenterairservice.thread;


import com.alibaba.fastjson.JSONArray;
import com.sensorweb.datacenterairservice.entity.WaterStation;
import com.sensorweb.datacenterairservice.service.WaterQualityWaterstationHourlyService;
import com.sensorweb.datacenterairservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

@Slf4j
public class downloadWater implements Runnable{
    @Override
    public void run() {
        try {
            //起始时间
            String str="2022-09-13 15:00:00";
            //结束时间
            String str1="2022-09-14 12:00:00";
            System.out.println("-----------"+str+"-----------"+str1+"------------湖北省水质站点---------");
            //2022-07-07 10:00:00 之前的没有接入
            WaterQualityWaterstationHourlyService waterQualityWaterstationHourlyService = (WaterQualityWaterstationHourlyService) ApplicationContextUtil.getBean("waterQualityWaterstationHourlyService");
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
                Instant endTime = DataCenterUtils.string2Instant(format.format(start.getTime())).plusSeconds(9*60*60).minusSeconds(1);
                log.info("------ 开始下载" + format.format(start.getTime()) + "的水质站-----");
                String begin = startTime.toString().replace("T", " ").replace("Z", "");
                String stop = endTime.toString().replace("T", " ").replace("Z", "");
                JSONArray access_res = waterQualityWaterstationHourlyService.getInfo(begin,stop,waterQualityWaterstationHourlyService.getStationId());
                boolean flag = waterQualityWaterstationHourlyService.accessinforming(access_res);
                if (flag) {
                    System.out.println("湖北省水质站点接入时间: " + begin + "Status: Success");
                } else {
                    System.out.println("湖北省水质站点接入时间: " + begin + "Status: Fail");
                }
                start.add(Calendar.HOUR_OF_DAY,1);
                Thread.sleep(2 * 1000); //下载太快会断开连接，这里休息2秒
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.info("湖北省水质站点接入时间: " + "Status: Fail");
        }
    }

}

package com.sensorweb.datacenterairservice.thread;


import com.sensorweb.datacenterairservice.service.RawDataSuperStationHourlyService;
import com.sensorweb.datacenterairservice.service.WaterQualityWaterstationHourlyService;
import com.sensorweb.datacenterairservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;

@Slf4j
public class downloadSuperAirStation implements Runnable{
    @Override
    public void run() {
        try {
            //起始时间
            String str="2022-04-01 00:00:00";
            //结束时间
            String str1="2022-05-01 00:00:00";
            //2022-07-07 10:00:00 之前的没有接入
            RawDataSuperStationHourlyService rawDataSuperStationHourlyService = (RawDataSuperStationHourlyService) ApplicationContextUtil.getBean("rawDataSuperStationHourlyService");
            log.info("------ 开始下载" + str + "到" + str1 + "的空气超级站-----");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:ss:00");
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            try {
                start.setTime(format.parse(str));
                end.setTime(format.parse(str1));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            while (start.before(end)) {
                Instant startTime = DataCenterUtils.string2Instant(format.format(start.getTime())).plusSeconds(8 * 60 * 60);
                Instant endTime = DataCenterUtils.string2Instant(format.format(start.getTime())).plusSeconds(32 * 60 * 60).minusSeconds(1);
                String begin = startTime.toString().replace("T", " ").replace("Z", "");
                String stop = endTime.toString().replace("T", " ").replace("Z", "");
                boolean flag = rawDataSuperStationHourlyService.accessinforming(begin, stop);
                if (flag) {
                    System.out.println("空气超级站接入时间: " + begin + "Status: Success");
                } else {
                    System.out.println("空气超级站接入时间: " + begin + "Status: Fail");
                }
                start.add(Calendar.DAY_OF_MONTH,1);
//                start.add(Calendar.DAY_OF_MONTH, 1);
                Thread.sleep(2 * 1000); //下载太快会断开连接，这里休息2秒
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.info("空气超级站接入: " + "Status: Fail");
        }
    }


}

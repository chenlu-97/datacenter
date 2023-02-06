package com.sensorweb.datacenterairservice.thread;


import com.sensorweb.datacenterairservice.dao.RawDataSuperStationHourlyMapper;
import com.sensorweb.datacenterairservice.entity.RawDataSuperStationHourly;
import com.sensorweb.datacenterairservice.feign.moveClient;
import com.sensorweb.datacenterairservice.service.RawDataSuperStationHourlyService;
import com.sensorweb.datacenterairservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Slf4j
public class downloadSuperAirStation1 implements Runnable{
    @Override
    public void run() {
        try {
            //起始时间
            String str = "2022-12-07 00:00:00";
            //结束时间
            String str1 = "2022-12-28 00:00:00";
            //2022-07-07 10:00:00 之前的没有接入
            RawDataSuperStationHourlyMapper rawDataSuperStationHourlyMapper = (RawDataSuperStationHourlyMapper) ApplicationContextUtil.getBean("rawDataSuperStationHourlyMapper");
            moveClient moveclient = (moveClient) ApplicationContextUtil.getBean("moveclient");
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
                int flag = 0;
                try {
                    System.out.println("开始同步！！");
                    List<Map> superstations = moveclient.getSuperStationByTime(format.format(start.getTime()), format.format(end.getTime()));
                    System.out.println("start = " + start);
                    System.out.println("end = " + end);
                    List<RawDataSuperStationHourly> rawDataSuperStations = new ArrayList<>();
                    System.out.println("superstations.size() = " + superstations.size());
                    if (superstations != null && superstations.size() > 0) {
                        for (int j = 0; j < superstations.size(); j++) {
                            try {
                                Map access = superstations.get(j);
                                RawDataSuperStationHourly rawDataSuperStationHourly = new RawDataSuperStationHourly();
                                rawDataSuperStationHourly.setSt((String) access.get("ST"));
                                rawDataSuperStationHourly.setSvalue((String) access.get("SVALUE"));
                                rawDataSuperStationHourly.setTimes(DataCenterUtils.string2Instant(access.get("TIMES").toString()));
                                rawDataSuperStationHourly.setStNa((String) access.get("ST_NA"));
                                rawDataSuperStationHourly.setEpname((String) access.get("EPNAME"));
                                rawDataSuperStationHourly.setCategpry((String) access.get("CATEGPRY"));
                                rawDataSuperStationHourly.setId((String) access.get("id"));
                                flag = rawDataSuperStationHourlyMapper.store(rawDataSuperStationHourly);
                                rawDataSuperStations.add(rawDataSuperStationHourly);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
//
                        if (flag > 0) {
                            System.out.println("空气超级站同步时间: " + start + "Status: Success");
                            log.info("空气超级站同步时间: " + start + "Status: Success");
                            DataCenterUtils.sendMessage("WhCC_SuperAir" + start, "站网-武汉城市群超级空气站", "这是一条获取的武汉城市群超级空气站数据",superstations.size());
                        } else {
                            System.out.println("空气超级站同步失败时间: " + start + "Status: Fail");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.info("空气超级站接入: " + "Status: Fail");
        }
    }


}

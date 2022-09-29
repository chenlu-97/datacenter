package com.sensorweb.datacenterairservice.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.sensorweb.datacenterairservice.config.OkHttpUtil;
import com.sensorweb.datacenterairservice.dao.RawDataSuperStationHourlyMapper;
import com.sensorweb.datacenterairservice.entity.RawDataSuperStationHourly;
import com.sensorweb.datacenterairservice.util.OkHttpUtils.OkHttpRequest;
import com.sensorweb.datacenterairservice.util.OkHttpUtils.Url;
import com.sensorweb.datacenterairservice.util.timeTransfer.SecondAndDate;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;


/**
 * @author Amour
 * @description 针对表【raw_data_super_station_hourly】的数据库操作Service
 * @createDate 2022-06-22 11:23:38
 */
@Slf4j
@Service
@EnableScheduling
public class RawDataSuperStationHourlyService {

    @Autowired
    private RawDataSuperStationHourlyMapper rawDataSuperStationHourlyMapper;


    @Autowired
    OkHttpUtil okHttpUtil;


    /**
     * 每小时接入一次数据
     */
    @Scheduled(cron = "0 10 0/1 * * ?") //每个小时的35分开始接入
//    @PostConstruct
    public void insertDataByHour() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00").withZone(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
//        LocalDateTime endDate = dateTime.plusMinutes(14);
        String start = formatter.format(dateTime.minusHours(1));
        String end = formatter1.format(dateTime.minusMinutes(10).minusSeconds(1));
//        String start = "2021-01-01 00:00:00";
//        String end = "2022-07-1 00:00:00";
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean flag = true;
                    try {
                        flag = accessinforming(start,end);
                        if (flag) {
                            log.info("空气超级站接入时间: " + start + "Status: Success");
                            DataCenterUtils.sendMessage("SuperAirStation"+ start, "站网-空气超级站点","这是一条省站推送的空气超级站点数据");
                            System.out.println("空气超级站接入时间: " + start + "Status: Success");
                        } else {
                            System.out.println("空气超级站接入时间: " + start + "Status: Fail");
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        log.info("空气超级站接入时间: " + start + "Status: Fail");
                        String mes = "空气超级站接入失败！！----失败时间 ："+ start;
                        // 发送邮件
                        SendException("SuperAirStation",start,mes);
                    }

            }
        }).start();
    }



//    @PostConstruct
    public boolean accessinforming(String start, String end) {
        int flag = 0;
        long startSecond = SecondAndDate.datatosecond(start);
        long endSecond = SecondAndDate.datatosecond(end);

        HashMap content2 = new HashMap<String, String>();
        content2.put("size", "10000000");
        content2.put("page", "0");
//        for (Object s : Url.SuperStationCode) {
            JSONArray access_res = new JSONArray();
            try {
                JSONObject body_content = new JSONObject();
                HashMap content1 = new HashMap<String, String>();
                content1.put("st","SW4200001,SW4201001,SW4202001,SW4205001,SW4206001,SW4207001,SW4208001,SW4209001,SW4209002,SW4211001,SW4212001" );
                content1.put("startTime", startSecond + "");
                content1.put("endTime", endSecond + "");

                body_content.put("params", content1);
                body_content.put("page", content2);
                access_res = OkHttpRequest.accessSuperStationData(body_content);
            }catch(Exception e){
            e.printStackTrace();
        }
//            log.info("接入成功");
            if (access_res != null && access_res.size() > 0) {
                for (int j = 0; j < access_res.size(); j++) {
                    try {
                        JSONObject access = (JSONObject) access_res.get(j);
                        RawDataSuperStationHourly rawDataSuperStationHourly = new RawDataSuperStationHourly();
                        rawDataSuperStationHourly.setSt(access.getString("ST"));
                        rawDataSuperStationHourly.setSvalue(access.getString("SVALUE"));
                        rawDataSuperStationHourly.setTimes(access.getDate("TIMES"));
                        rawDataSuperStationHourly.setStNa(access.getString("ST_NA"));
                        rawDataSuperStationHourly.setEpname(access.getString("EPNAME"));
                        rawDataSuperStationHourly.setCategpry(access.getString("CATEGPRY"));
                        rawDataSuperStationHourly.setId(rawDataSuperStationHourly.getTimes().getTime()+rawDataSuperStationHourly.getSt()+rawDataSuperStationHourly.getEpname());
                        flag = rawDataSuperStationHourlyMapper.store(rawDataSuperStationHourly);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

//        }
        return flag>0 ;
    }


    public void SendException(String type, String time, String details) throws IOException {

        String url = "http://ai-ecloud.whu.edu.cn/gateway/ai-sensing-open-service/exception/data";
        JSONObject param = new JSONObject();
        param.put("type", type);
        param.put("time", time);
        param.put("details",details);
        String res = null;
        try {
            res  =  okHttpUtil.doPostJson(url,param.toString());
            System.out.println( "发送成功！！！"+res);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("发送失败！！！" +res);
        }
    }





}

package com.sensorweb.datacenterairservice.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterairservice.config.OkHttpUtil;
import com.sensorweb.datacenterairservice.dao.WaterQualityWaterstationHourlyMapper;
import com.sensorweb.datacenterairservice.dao.WaterStationMapper;
import com.sensorweb.datacenterairservice.entity.RawDataSuperStationHourly;
import com.sensorweb.datacenterairservice.entity.WaterQualityWaterstationHourly;
import com.sensorweb.datacenterairservice.entity.WaterStation;
import com.sensorweb.datacenterairservice.feign.moveClient;
import com.sensorweb.datacenterairservice.util.OkHttpUtils.OkHttpRequest;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amour
 * @description 针对表【water_quality_waterstation_hourly 】的数据库操作Service
 * @createDate 2022-06-21 16:26:58
 */
@Slf4j
@Service
@EnableScheduling
public class WaterQualityWaterstationHourlyService {

    @Autowired
    private WaterStationMapper waterStationMapper;

    @Autowired
    private WaterQualityWaterstationHourlyMapper waterQualityWaterstationHourlyMapper;

    @Autowired
    OkHttpUtil okHttpUtil;

    @Autowired
    moveClient moveclient;


    /**
     * 每小时接入一次数据
     */
//    @Scheduled(cron = "0 10 0/1 * * ?") //每个小时的50分开始接入
//    @PostConstruct
    public void insertDataByHour() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00").withZone(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        String start = formatter.format(dateTime.minusHours(1));
        String end = formatter1.format(dateTime.minusMinutes(11));
//        LocalDateTime endDate = dateTime.plusMinutes(14);
//        String start = formatter.format(dateTime);
//        String end = formatter1.format(endDate);
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean flag = true;
                    try {
                        JSONArray access_res = getInfo(start,end,getStationId());
                        flag = accessinforming(access_res);
                        if (flag) {
                            log.info("湖北省水质站点接入时间: " + start + "Status: Success");
                            DataCenterUtils.sendMessage("HB_water"+ start, "站网-湖北省水质站点","这是一条省站推送的湖北省水质站点数据",access_res.size());
                            System.out.println("湖北省水质站点接入时间: " + start + "Status: Success");
                        }else{
                            log.info("湖北省水质站点接入时间: " + start + "Status: Fail");
                            System.out.println("湖北省水质站点接入时间: " + start + "Status: Fail");
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        log.info("湖北省水质站点接入时间: " + start + "Status: Fail");
                        String mes = "湖北省水质站点接入失败！！----失败时间 ："+ start;
                        // 发送邮件
                        SendException("HB_Water",start,mes);
                }
            }
        }).start();
    }



    /**
     * 每小时更新一次数据
     */
    @Scheduled(cron = "0 12 0/1 * * ?") //每个小时的35分开始接入
    public void updateDataByHour() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00").withZone(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        String start = formatter.format(dateTime.minusHours(1));
        String end = formatter1.format(dateTime.minusMinutes(13));
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                int flag = 0;
                try {
                    System.out.println("开始同步");
                    List<WaterQualityWaterstationHourly> water_exit = waterQualityWaterstationHourlyMapper.selectMaxTimeData();
                    Instant begin = water_exit.get(0).getDatatime();
                    String begin_time = begin.plusSeconds(8*60*60).toString().replace("T"," ").replace("Z","");
//                    System.out.println("begin_time = " + begin_time);
                    List<Map> waterstations = moveclient.getWaterStationByTime(begin_time,end);
                    System.out.println("更新的水站从"+begin_time+"到"+end +",共有"+waterstations.size()+"个数据更新了");
                    if (waterstations != null && waterstations.size() > 0) {
                        for (int j = 0; j < waterstations.size(); j++) {
                            try {
                                Map access = waterstations.get(j);
                                WaterQualityWaterstationHourly waterQualityWaterstationHourly = new WaterQualityWaterstationHourly();
                                Instant times =  DataCenterUtils.string2Instant(access.get("dataTime").toString().replace("T", " ").replace("Z", ""));
                                waterQualityWaterstationHourly.setDatatime(times);
                                waterQualityWaterstationHourly.setAreacode((String) access.get("areaCode"));
                                waterQualityWaterstationHourly.setRiverid((String) access.get("riverId"));
                                waterQualityWaterstationHourly.setSiteid((String) access.get("siteId"));
                                waterQualityWaterstationHourly.setWpcode((String) access.get("WPCode"));
                                waterQualityWaterstationHourly.setMnname((String) access.get("mnName"));
                                waterQualityWaterstationHourly.setE01001((String) access.get("e01001"));
                                waterQualityWaterstationHourly.setE01002((String) access.get("e01002"));
                                waterQualityWaterstationHourly.setW01001((String) access.get("w01001"));
                                waterQualityWaterstationHourly.setW01003((String) access.get("w01003"));
                                waterQualityWaterstationHourly.setW01009((String) access.get("w01009"));
                                waterQualityWaterstationHourly.setW01010((String) access.get("w01010"));
                                waterQualityWaterstationHourly.setW01014((String) access.get("w01014"));
                                waterQualityWaterstationHourly.setW01016((String) access.get("w01016"));
                                waterQualityWaterstationHourly.setW01022((String) access.get("w01022"));
                                waterQualityWaterstationHourly.setW19011((String) access.get("w19011"));
                                waterQualityWaterstationHourly.setW21001((String) access.get("w21001"));
                                waterQualityWaterstationHourly.setW21003((String) access.get("w21003"));
                                waterQualityWaterstationHourly.setW21011((String) access.get("w21011"));
                                waterQualityWaterstationHourly.setW22008((String) access.get("w22008"));
                                waterQualityWaterstationHourly.setW01019((String) access.get("w01019"));
                                waterQualityWaterstationHourly.setW01023((String) access.get("w01023"));
                                waterQualityWaterstationHourly.setW02007((String) access.get("w02007"));
                                waterQualityWaterstationHourly.setW02005((String) access.get("w02005"));
                                waterQualityWaterstationHourly.setW01024((String) access.get("w01024"));
                                waterQualityWaterstationHourly.setId((String) access.get("id"));
                                flag = waterQualityWaterstationHourlyMapper.store(waterQualityWaterstationHourly);
                                if (flag >0){
                                    log.info("湖北省水质更新时间: " + start.toString() + "Status: Success");
                                    DataCenterUtils.sendMessage("HB_Water"+ start, "站网-湖北水质站","这是一条获取的湖北水质站数据",waterstations.size());
                                }else{
                                    log.info("湖北省水质更新失败时间: " + start.toString() + "Status: fail");
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }

            }
        }).start();
    }


    public boolean accessinforming(JSONArray access_res) throws Exception {
        int flag = 0;
        List<WaterStation> res = waterStationMapper.quaryMnId();
            if (access_res != null && access_res.size() > 0) {
                for (int i = 0; i < res.size(); i++) {
                        WaterQualityWaterstationHourly waterQualityWaterstationHourly = new WaterQualityWaterstationHourly();
                        String station_id = res.get(i).getMn();
                        for (int j = 0; j < access_res.size(); j++) {
                            JSONObject access = (JSONObject) access_res.get(j);
                            String id = access.getString("siteId");
                            if (id.equals(station_id)) {
                                waterQualityWaterstationHourly.setSiteid(access.getString("siteId"));
                                waterQualityWaterstationHourly.setAreacode((String) access.get("areaCode"));
                                long data = Long.parseLong(access.getString("dataTime"));
                                waterQualityWaterstationHourly.setDatatime(SecondAndDate.sceondtodata(data).toInstant());
                                waterQualityWaterstationHourly.setRiverid(access.getString("riverId"));
                                waterQualityWaterstationHourly.setMnname(access.getString("mnName"));
                                waterQualityWaterstationHourly.setId(SecondAndDate.sceondtodata(data).toInstant().plusSeconds(8*60*60).toString() + access.getString("siteId"));
                                String fieldName = access.getString("lHCodeID");
                                try {
                                    Field field = waterQualityWaterstationHourly.getClass().getDeclaredField(fieldName);
                                    field.setAccessible(true);
                                    String name = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                                    Method code = waterQualityWaterstationHourly.getClass().getMethod("set" + name, String.class);
                                    code.invoke(waterQualityWaterstationHourly, access.getString("dataValue"));
                                } catch (NoSuchFieldException | NoSuchMethodException e) {
                                    e.printStackTrace();
                                    continue;
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                    try {
                        if (waterQualityWaterstationHourly.getId() == null) {
                        } else {
                           flag = waterQualityWaterstationHourlyMapper.store2(waterQualityWaterstationHourly);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        return flag > 0;
        }


    /**
     * 从okhttp获取站点信息
     * */
    public JSONArray getInfo(String start, String end,String Ids){
        long startSecond = SecondAndDate.datatosecond(start);
        long endSecond = SecondAndDate.datatosecond(end);

        JSONObject body_content = new JSONObject();
        JSONArray access_res = new JSONArray();
        try {
            HashMap content1 = new HashMap<String, String>();
            content1.put("siteCode", Ids);
            content1.put("startTime", startSecond + "");
            content1.put("endTime", endSecond + "");

            HashMap content2 = new HashMap<String, String>();
            content2.put("size", "1000000");
            content2.put("page", "0");
            body_content.put("params", content1);
            body_content.put("page", content2);
            access_res = OkHttpRequest.accessWaterData(body_content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return access_res;
    }





    public String getStationId(){
        List<WaterStation> res = waterStationMapper.quaryMnId();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < res.size(); j++) {
            sb.append(res.get(j).getMn()).append(",");
        }
        String Ids = sb.substring(0, sb.length() - 1);
        return Ids;
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

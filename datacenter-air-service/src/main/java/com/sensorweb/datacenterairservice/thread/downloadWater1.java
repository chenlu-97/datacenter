package com.sensorweb.datacenterairservice.thread;


import com.alibaba.fastjson.JSONArray;
import com.sensorweb.datacenterairservice.dao.WaterQualityWaterstationHourlyMapper;
import com.sensorweb.datacenterairservice.entity.WaterQualityWaterstationHourly;
import com.sensorweb.datacenterairservice.feign.moveClient;
import com.sensorweb.datacenterairservice.service.WaterQualityWaterstationHourlyService;
import com.sensorweb.datacenterairservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.Map;



@Slf4j
public class downloadWater1 implements Runnable{
//    @Autowired
//    moveClient moveclient;
    @Override
    public void run() {
        try {
            //起始时间
            String str = "2022-12-07 00:00:00";
            //结束时间
            String str1 = "2022-12-28 00:00:00";
            System.out.println("-----------"+str+"-----------"+str1+"------------湖北省水质站点---------");
            moveClient moveclient = (moveClient) ApplicationContextUtil.getBean("moveclient");
            WaterQualityWaterstationHourlyMapper waterQualityWaterstationHourlyMapper = (WaterQualityWaterstationHourlyMapper) ApplicationContextUtil.getBean("waterQualityWaterstationHourlyMapper");
            SimpleDateFormat format= new  SimpleDateFormat("yyyy-MM-dd HH:ss:mm");
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
                int flag = 0;
                try {
                    List<Map> waterstations = moveclient.getWaterStationByTime(format.format(start.getTime()),format.format(end.getTime()));
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

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.info("湖北省水质站点接入时间: " + "Status: Fail");
        }
    }

    @Autowired
    moveClient moveClient;
}

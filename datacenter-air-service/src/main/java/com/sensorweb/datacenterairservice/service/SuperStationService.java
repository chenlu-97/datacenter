package com.sensorweb.datacenterairservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.sensorweb.datacenterairservice.dao.AirStationMapper;
import com.sensorweb.datacenterairservice.entity.AirStationModel;
import com.sensorweb.datacenterairservice.util.AirConstant;
import com.sensorweb.datacenterairservice.util.OkHttpUtils.OkHttpRequest;
import com.sensorweb.datacenterairservice.util.OkHttpUtils.Url;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;


@Slf4j
@Service
@EnableScheduling
public class SuperStationService implements Url {
    @Autowired
    private AirStationMapper airStationMapper;



    /**
     * 接入空气超级站的站点信息
     * @throws Exception
     */
//    @PostConstruct
    public void accessinforming() {

        HashMap content2 = new HashMap<String, String>();
        content2.put("size", "100000");
        content2.put("page", "0");
//        for (Object s : SuperStationCode) {

            JSONObject body_content = new JSONObject();
            HashMap content1 = new HashMap<String, String>();
            content1.put("st",Url.SuperStationid);

            body_content.put("params", content1);
            body_content.put("page", content2);

            JSONArray access_res = OkHttpRequest.accessSuperStation(body_content);
            if (access_res != null && access_res.size() > 0) {
                for (int j = 0; j < access_res.size(); j++) {

                    AirStationModel station = new AirStationModel();
                    JSONObject access = (JSONObject) access_res.get(j);
                    System.out.println(access);

                    station.setCity(access.getString("CITY"));
                    station.setStationName(access.getString("ST_NA"));
                    station.setStationType("SuperAirStation");

                    station.setStationId(access.getString("ST"));
                    station.setLat(access.getFloat("LAT"));
                    station.setLon(access.getFloat("LNG"));

                    Float lon = access.getFloat("LNG");
                    Float lat = access.getFloat("LAT");
                    String geo = "POINT" + "(" + lon.floatValue() + " " + lat.floatValue() + ")";
                    station.setGeom(geo);

                    airStationMapper.insertData(station);

                }
//            }
        }
    }


    /**
     * 接入空气的站点信息
     * @throws Exception
     */
//    @PostConstruct
    public void accessinforming12313() {


        JSONObject body_content = new JSONObject();


        HashMap content1 = new HashMap<String, String>();

        HashMap content2 = new HashMap<String, String>();
        content2.put("size", "100000");
        content2.put("page", "0");
        body_content.put("params", content1);
        body_content.put("page", content2);

        JSONArray access_res = OkHttpRequest.accessAirStation(body_content);
        if (access_res != null && access_res.size() > 0) {
            for (int j = 0; j < access_res.size(); j++) {
                try {
                    AirStationModel station1 = new AirStationModel();
                    JSONObject access = (JSONObject) access_res.get(j);
                    station1.setRegion(access.getString("AREA"));
                    station1.setCity(access.getString("CITY"));
                    station1.setStationName(access.getString("ST_NA"));
                    station1.setStationType("NEW_HB_AIR");
                    station1.setStationId(access.getString("UNC"));
                    Float lon = 0f;
                    Float lat = 0f;
                    if(access.get("LAT")!=null && access.get("LNG")!=null){
                        lon = access.getFloat("LNG");
                        lat = access.getFloat("LAT");
                    }else{
                        JSONObject  jsonObject = getGeoAddress(station1.getCity()+station1.getStationName());
                        if (jsonObject!=null) {
                            Object result = jsonObject.get("result");
                            if (result!=null) {
                                Object location = ((JSONObject) result).get("location");
                                lon = ((JSONObject) location).getFloatValue("lng");
                                lat = ((JSONObject) location).getFloatValue("lat");
                            }
                        }
                    }
                    station1.setLat(lat);
                    station1.setLon(lon);
                    String geo = "POINT" + "(" + lon.floatValue() + " " + lat.floatValue() + ")";
                    station1.setGeom(geo);
                    airStationMapper.insertData(station1);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 根据地址获取经纬度信息,转换成JSON对象返回
     * @param address
     */
    public JSONObject getGeoAddress(String address) throws IOException {
        String param = "address=" + URLEncoder.encode(address, "utf-8") + "&output=json" + "&ak=" + AirConstant.BAIDU_AK;
        String document = DataCenterUtils.doGet(AirConstant.BAIDU_ADDRESS_API, param);
        if (document!=null) {
            return JSON.parseObject(document);
        }
        return null;
    }
}

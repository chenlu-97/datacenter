package com.sensorweb.datacenterairservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterairservice.dao.WaterStationMapper;
import com.sensorweb.datacenterairservice.entity.WaterStation;
import com.sensorweb.datacenterairservice.util.AirConstant;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;

/**
* @author Amour
* @description 针对表【water_station】的数据库操作Service
* @createDate 2022-06-20 17:19:30
*/

@Slf4j
@Service
@EnableScheduling
public class WaterStationService {


    @Autowired
    private WaterStationMapper waterStationMapper;


//    public static WaterStationService waterStationService;
//
//    @PostConstruct  //用于在依赖关系注入完成之后需要执行的方法上，以执行任何初始化
//    public void init() {
//        waterStationService = this;
//    }

//    public  void test() {
//
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .build();
//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, "{\r\n    \"clientId\":\"6a84a017969b10ad\",\r\n    \"clientSecret\":\"3ab91a689212479aa58e90d611003e63\"\r\n}");
//        Request request = new Request.Builder()
//                .url("http://219.140.164.18:21000/sky-api/rate-auth/auth/clientToken")
//                .method("POST", body)
//                .addHeader("Content-Type", "application/json")
//                .build();
//        Response response;
//
//        {
//            try {
//                response = client.newCall(request).execute();
//                ResponseBody responseBody = response.body();
//                String res = responseBody.string();
//                System.out.println(res);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    @PostConstruct
    public void testToken() {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n" +
                "\"params\":{\n" +
                "},\n" +
                "\"page\":{\n" +
                "\"size\": \"1000\",\n" +
                "\"page\": \"0\"\n" +
                "}\n" +
                "}");
        Request request = new Request.Builder()
                .url("http://219.140.164.18:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=c3598504-2dc8-4663-b392-03abd528cf15")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer d67d2043-24fe-4c00-b951-a9c43232a224")
                .build();
        Response response;
        {
            try {
                response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                String res = responseBody.string();
                JSONObject resJson = (JSONObject) JSONObject.parse(res);
                JSONObject dataJson = (JSONObject) resJson.get("data");
                JSONArray station = (JSONArray) dataJson.get("list");
//                ArrayList<WaterStation> waterStations = new ArrayList<WaterStation>();
                for (int i = 0; i < station.size(); i++) {
                    WaterStation waterStation = new WaterStation();
                    JSONObject jsonObject = (JSONObject) station.get(i);
                    waterStation.setMn((String) jsonObject.get("mn"));
                    waterStation.setAreaid((String) jsonObject.get("areaId"));
                    waterStation.setAreaname((String) jsonObject.get("areaName"));
                    waterStation.setRiverid((String) jsonObject.get("riverId"));
                    waterStation.setRivername((String) jsonObject.get("riverName"));
                    waterStation.setMnname((String) jsonObject.get("mnName"));

//                    BigDecimal lat =  new BigDecimal(jsonObject.get("latitude").toString()) ;
//                    BigDecimal lon =  new BigDecimal(jsonObject.get("longitude").toString()) ;

                    Float lon = 0f;
                    Float lat = 0f;
                    if(jsonObject.get("latitude")!=null && jsonObject.get("longitude")!=null){
                        lon = jsonObject.getFloat("longitude");
                        lat = jsonObject.getFloat("latitude");
                    }else{
                        JSONObject  jsonObject1 = getGeoAddress((String) jsonObject.get("areaName")+ jsonObject.get("mnName"));
                        if (jsonObject1!=null) {
                            Object result = jsonObject1.get("result");
                            if (result!=null) {
                                Object location = ((JSONObject) result).get("location");
                                lon = ((JSONObject) location).getFloatValue("lng");
                                lat = ((JSONObject) location).getFloatValue("lat");
                            }
                        }
                    }
                    waterStation.setLat(lat);
                    waterStation.setLon(lon);
                    String geo = "POINT" + "(" + lon.floatValue() + " " + lat.floatValue() + ")";
                    waterStation.setGeom(geo);

                    waterStationMapper.store(waterStation);
                }

                System.out.println(station);
                System.out.println(resJson);

            } catch (IOException e) {
                e.printStackTrace();
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

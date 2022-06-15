package com.sensorweb.datacentermobileservice;

import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacentermobileservice.service.SurveyingVesselService;
import com.sensorweb.datacentermobileservice.util.OkHttpUtil;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;

@SpringBootTest
class DatacenterMobileServiceApplicationTests {
    @Autowired
    private SurveyingVesselService surveyingVesselService;

    @Autowired
    private OkHttpUtil okHttpUtil;

    @Test
    void contextLoads() throws IOException, ParseException {
//        List<ChinaWeather> chinaWeathers = insertWeatherInfo.getWeatherInfo();
//        System.out.println("!!!");

//        boolean flag = insertWeatherInfo.insertWeatherInfoBatch(insertWeatherInfo.getWeatherInfo());
//        System.out.println("flag = " + flag);
//        boolean test =  tianXingZhouService.getIOTInfo();
//        System.out.println("test = " + test);
//        String data1 = "##0349QN=20211021095827821;ST=22;CN=2011;PW=123456;MN=YD4210001;Flag=4;CP=&&DataTime=20211021095827;a01001-Rtd=13.9,a01001-Flag=N;a01002-Rtd=65.8,a01002-Flag=N;a010061-Rtd=1.03,a010061-Flag=N;a01007-Rtd=0.3,a01007-Flag=N;a01008-Rtd=92.6,a01008-Flag=N;a80001-Rtd=39.36,a80001-Flag=N;a81001-Rtd=30.4614278,a81001-Flag=N;a81002-Rtd=114.392925,a81002-Flag=N&&8801";
//        Matcher m = Pattern.compile("(?<==).+?(?=,)").matcher(data1);
//        while (m.find()) {
//            String result = m.group().trim();
//            System.out.println("result = " + result);
//        }


//       boolean i =  measuringVehicleService.insertData("##0349QN=20211021095827821;ST=22;CN=2011;PW=123456;MN=YD4210001;Flag=4;CP=&&DataTime=20211021095827;a01001-Rtd=13.9,a01001-Flag=N;a01002-Rtd=65.8,a01002-Flag=N;a010061-Rtd=1.03,a010061-Flag=N;a01007-Rtd=0.3,a01007-Flag=N;a01008-Rtd=92.6,a01008-Flag=N;a80001-Rtd=39.36,a80001-Flag=N;a81001-Rtd=30.4614278,a81001-Flag=N;a81002-Rtd=114.392925,a81002-Flag=N&&8801");
//        System.out.println("i = " + i);
//
//        String data = "##0121QN=20211103145342000;ST=21;CN=3003;PW=lhsoft;MN=lhsoft;Flag=1;CP=&&DataTime=20211103143500;Lng=114.035912,Lat=30.082891&&7A80";
//        surveyingVesselService.updateGPS(data);
//        String url = "http://ai-ecloud.whu.edu.cn/gateway/ai-sensing-back-service/api/ws/move";
//        JSONObject param = new JSONObject();
//        param.put("type", "ship1");
//        param.put("lng", "123");
//        param.put("lat", "123");
//        param.put("time", URLEncoder.encode("2022-01-01 00:00:00","utf-8"));
//        param.put("details", URLEncoder.encode("这是一条走航船GPS的位置推送","utf-8"));
//
////                            String param = "type=ship1"+"lng="+surveyingVessel.getLon()+"lat="+surveyingVessel.getLat()+
////                                        "time="+ URLEncoder.encode((surveyingVessel.getDataTime().toString().replace("T"," ").replace("Z","")),"utf-8")
////                                    +"details="+URLEncoder.encode("这是一条走航船GPS的位置推送","utf-8");
//        System.out.println("param = " + param);
//        okHttpUtil.doPostJson(url,param.toString());  //推送经纬度到武大前端
//        System.out.println("走航船GPS推送成功");

       String token =  surveyingVesselService.gettoken();
        System.out.println("token = " + token);

    }

    }
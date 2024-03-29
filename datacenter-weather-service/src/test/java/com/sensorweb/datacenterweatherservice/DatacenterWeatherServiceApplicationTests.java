package com.sensorweb.datacenterweatherservice;

//import com.sensorweb.datacenterweatherservice.service.MeasuringVehicleService;
import com.sensorweb.datacenterweatherservice.service.HBWeatherStationService;
import com.sensorweb.datacenterweatherservice.service.InsertCHWeather;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@SpringBootTest
class DatacenterWeatherServiceApplicationTests {
    @Autowired
    private HBWeatherStationService hbWeatherStationService;
    @Autowired
    private InsertCHWeather insertCHWeather;
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
//
//
//       boolean i =  measuringVehicleService.insertData("##0349QN=20211021095827821;ST=22;CN=2011;PW=123456;MN=YD4210001;Flag=4;CP=&&DataTime=20211021095827;a01001-Rtd=13.9,a01001-Flag=N;a01002-Rtd=65.8,a01002-Flag=N;a010061-Rtd=1.03,a010061-Flag=N;a01007-Rtd=0.3,a01007-Flag=N;a01008-Rtd=92.6,a01008-Flag=N;a80001-Rtd=39.36,a80001-Flag=N;a81001-Rtd=30.4614278,a81001-Flag=N;a81002-Rtd=114.392925,a81002-Flag=N&&8801");
//        System.out.println("i = " + i);
//
//        String document = hbWeatherStationService.getApiDocument("20220621000000");
//        boolean res = hbWeatherStationService.getIOTInfo(document);
//        System.out.println("res = " + res);
        insertCHWeather.getWeatherInfo();
    }

    }
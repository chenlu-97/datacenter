package com.sensorweb.sosobsservice.service;


import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import com.sensorweb.sosobsservice.dao.ObservationMapper;
import com.sensorweb.sosobsservice.dao.dataStatisticsMapper;
import com.sensorweb.sosobsservice.entity.dataStatistics;
import com.sensorweb.sosobsservice.feign.*;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForYear;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class DataStatisticsService {
    @Autowired
    private GetObservationExpandService getObservationExpandService;

    @Autowired
    private ObservationMapper observationMapper;
    @Autowired
    private AirFeignClient airFeignClient;
    @Autowired
    private WeatherFeignClient weatherFeignClient;
    @Autowired
    private HimawariFeignClient himawariFeignClient;
    @Autowired
    private GeeFeignClient geeFeignClient;
    @Autowired
    private LaadsFeignClient laadsFeignClient;
    @Autowired
    private OfflineFeignClient offlineFeignClient;
    @Autowired
    private LittleSensorFeignClient littleSensorFeignClient;

    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private dataStatisticsMapper dataStatisticsMapper;



/**
* 2022 .9.22 修改
*
**/

    @XxlJob("DC_Overview_DataNumber")
    public boolean DataNumber() {
        int AirStation=1;
        int WeatherStation=1;
        int offline = 1;
        int laads  = 1;
        int gee = 1;
        int himawari = 1;
        int mobile = 1;
        int littleSensor = 1;
        int uav = 1;
        try{
            AirStation = dataStatisticsMapper.getHBAirNum() + dataStatisticsMapper.getSuperAirNum()+dataStatisticsMapper.getCHAirNum()+dataStatisticsMapper.getTWAirNum()+ dataStatisticsMapper.getWaterNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            offline = dataStatisticsMapper.getWaterQualityNum()+ dataStatisticsMapper.getWaterPollutionNum()+dataStatisticsMapper.getGFNum()+dataStatisticsMapper.getyaEBNum()+dataStatisticsMapper.getyaMRNum()+dataStatisticsMapper.getwaterAutoNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            WeatherStation = dataStatisticsMapper.getWeatherNum() + dataStatisticsMapper.getCHWeatherNum() + dataStatisticsMapper.getHBWeatherNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            laads = dataStatisticsMapper.getModisNum()+dataStatisticsMapper.getGPMNum()+ dataStatisticsMapper.getGLDASNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            gee =dataStatisticsMapper.getLandsatNum() +dataStatisticsMapper.getFYNum()+dataStatisticsMapper.getSentinelNum()+dataStatisticsMapper.getCopernicusNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            himawari = dataStatisticsMapper.getHimawariNum() ;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            mobile =  getVehicleNumber()+getVesselNumber();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            littleSensor = dataStatisticsMapper.getLittleSensorNumber()+dataStatisticsMapper.getWeatherSensorNumber();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            uav = dataStatisticsMapper.getUAVNum() +dataStatisticsMapper.getMailDataNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        int num = AirStation+WeatherStation+offline+himawari+laads+gee+mobile +littleSensor+ uav;

        dataStatistics statistics = new dataStatistics();
        int res = 0;

        res = ((int)Math.floor(num/10000));

        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_Overview_DataNumber");

        JSONObject number = new JSONObject();
        number.put("Overview_DataNumber", res);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_Overview_DataNumber数据条数统计成功！！");
        }else{
            System.out.println("DC_Overview_DataNumber数据条数统计失败！！");
        }
        return i>0;
    }

    @XxlJob("DC_Overview_DataNumberToday")
    public boolean DataNumberToday() {
        int Station=1;
        int satellite =1;
        int mobile  = 1;
        int  uva= 1;
        int other =1;

        int num = 1;

        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant nowTime= DataCenterUtils.string2Instant(formatter.format(dateTime));
        String tmp = nowTime.plusSeconds(8*60*60).toString().substring(0,nowTime.toString().indexOf('T'))+" 00:00:00";
        Instant preTime = DataCenterUtils.string2Instant(tmp);

//        try{
//            Station = dataStatisticsMapper.getCHAirNumByTime(preTime,nowTime) + dataStatisticsMapper.getWeatherNumByTime(preTime,nowTime)
//            +dataStatisticsMapper.getCHWeatherNumByTime(preTime,nowTime)+dataStatisticsMapper.getHBAirNumByTime(preTime,nowTime)
//            +dataStatisticsMapper.getHBWeatherNumByTime(preTime,nowTime)+dataStatisticsMapper.getSuperAirNumByTime(preTime,nowTime)+
//            dataStatisticsMapper.getWaterNumByTime(preTime,nowTime)+dataStatisticsMapper.getTWAirNumByTime(preTime,nowTime);
//
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        try{
//            satellite = dataStatisticsMapper.getHimawariNumByTime(preTime,nowTime) + dataStatisticsMapper.getModisNumByTime(preTime,nowTime)+
//                    dataStatisticsMapper.getLandsatNumByTime(preTime,nowTime)+dataStatisticsMapper.getGLDASNumByTime(preTime,nowTime)+
//                    dataStatisticsMapper.getGPMNumByTime(preTime,nowTime)
//                    +dataStatisticsMapper.getFYNumByTime(preTime,nowTime)
//                    +dataStatisticsMapper.getSentineByTime(preTime,nowTime)+dataStatisticsMapper.getCopernicus(preTime,nowTime);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        try{
//            uva = dataStatisticsMapper.getUAVNumByTime(preTime,nowTime);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        try{
//            mobile =  getVehicleNumberByTime(preTime,nowTime)+getVesselNumberByTime(preTime,nowTime);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//        try{
//            other = dataStatisticsMapper.getNumberByTime(preTime,nowTime) +dataStatisticsMapper.getWeatherSensorByTime(preTime,nowTime);
//        }catch(Exception e){
//            e.printStackTrace();
//        }

        try{
            num = dataStatisticsMapper.getTodayNumber(preTime);
        }catch(Exception e){
            e.printStackTrace();
        }

        int res = 0;

        res = ((int)Math.floor(num/10000));

        dataStatistics statistics = new dataStatistics();
        statistics.setCreateTime(nowTime);

        statistics.setJobId("DC_Overview_DataNumberToday");
        JSONObject number = new JSONObject();
        number.put("Overview_DataNumberToday",res);
        statistics.setStatistics(number.toString());
//        System.out.println("statistics = " + statistics);
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_Overview_DataNumberToday数据条数统计成功！！");
        }else{
            System.out.println("DC_Overview_DataNumberToday数据条数统计失败！！");
        }
        return i>0;
    }


    @XxlJob("DC_Overview_DataSize")
    public boolean DataSize() {


        long size = 0;

        try{
            List<String> databases = dataStatisticsMapper.getFilepath2("database");
            if(databases.size()>0) {
                for (String database : databases) {
                    if (database == null || database.equals(" ")) {

                    } else {
                        size = size + dataStatisticsMapper.getDatabaseSize('"'+database+'"');
                    }

                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            List<String> filePaths = dataStatisticsMapper.getFilepath2("file");
            if(filePaths.size()>0) {
                for (String filePath : filePaths) {
                    if (filePath == null || filePath.equals(" ")) {

                    } else {
                        size = size + getObservationExpandService.getFileSize(new File(filePath));
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        DecimalFormat df=new DecimalFormat("0.0");//设置保留位数

        double res = Double.valueOf(df.format((float)size/1024/1024/1024/1024));

        dataStatistics statistics = new dataStatistics();
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_Overview_DataSize");

        JSONObject number = new JSONObject();
        number.put("Overview_DataSize",res);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_Overview_DataSize数据条数统计成功！！");
        }else{
            System.out.println("DC_Overview_DataSize数据条数统计失败！！");
        }
        return i>0;
    }

    @XxlJob("DC_Overview_DataInterface")
    public boolean DataInterface() {
        int query=0;
        int delete=0;
        int add=0;
        int update=0;
        int other = 0;
        try{
            query = dataStatisticsMapper.getInterfaceNum("Data_Query");
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            delete = dataStatisticsMapper.getInterfaceNum("User_Base");;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            add =dataStatisticsMapper.getInterfaceNum("Data_ADD");;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            update = dataStatisticsMapper.getInterfaceNum("Handle_Air")+dataStatisticsMapper.getInterfaceNum("Handle_Water")
            +dataStatisticsMapper.getInterfaceNum("Handle_Ecology")+dataStatisticsMapper.getInterfaceNum("Handle_Soil");
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            other = 16;
        }catch(Exception e){
            e.printStackTrace();
        }

        Map station = new LinkedHashMap();


        Map querys = new LinkedHashMap();
        querys.put("name","查询");
        querys.put("number",query);

        Map deletes = new LinkedHashMap();
        deletes.put("name","删除");
        deletes.put("number",delete);

        Map adds = new LinkedHashMap();
        adds.put("name","添加");
        adds.put("number",add);

        Map updates = new LinkedHashMap();
        updates.put("name","更新");
        updates.put("number",update);

        Map others = new LinkedHashMap();
        others.put("name","其他");
        others.put("number",other);

        Map totals = new LinkedHashMap();
        totals.put("name","总计");
        totals.put("number",query+delete+add+update+other);


        station.put("query",querys);
        station.put("delete",deletes);
        station.put("add",adds);
        station.put("update",updates);
        station.put("other",others);
        station.put("total",totals);


        dataStatistics statistics = new dataStatistics();
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_Overview_DataInterface");

        JSONObject number = new JSONObject();
        number.put("Overview_DataInterface", station);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_Overview_DataInterface数据条数统计成功！！");
        }else{
            System.out.println("DC_Overview_DataInterface数据条数统计失败！！");
        }
        return i>0;

    }

//    @PostConstruct
//    public void test() {
//        long station=0;
//
//        try{
//            station = station + dataStatisticsMapper.getDatabaseSize('"' +"gf"+ '"');
//            System.out.println("station = " + station);
//            station = 1000000000;
//            DecimalFormat df=new DecimalFormat("0.0");//设置保留位数
//            double station_size = Double.valueOf(df.format((float)station/1024/1024/1024));
//            System.out.println("station_size = " + station_size);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//    }
    @XxlJob("DC_Overview_StorageContent")
    public boolean StorageContent() {
        long station=0;
        long satellite=0;
        long mobile=0;
        long uav=0;
        long product = 0;

        double station_size = 0;
        double satellite_size = 0;
        double mobile_size = 0;
        double uav_size = 0;
        double product_size = 0;
        double total_size = 0;


        try{
            List<String> databases = dataStatisticsMapper.getFilepath("地面站网","database");
            if(databases.size()>0) {
                for (String database : databases) {
                    if (database == null || database.equals(" ")) {

                    } else {
                        station = station + dataStatisticsMapper.getDatabaseSize('"'+database+'"');
                    }

                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            List<String> filePaths = dataStatisticsMapper.getFilepath("地面站网","file");
            if(filePaths.size()>0) {
                for (String filePath : filePaths) {
                    if (filePath == null || filePath.equals(" ")) {

                    } else {
                        long tmp = getObservationExpandService.getFileSize(new File(filePath));
                        station = station + tmp;
                    }

                }
            }
            DecimalFormat df1=new DecimalFormat("0.00000");//设置保留位数
//            System.out.println("station = " + station);
            station_size = Double.valueOf(df1.format((float)station/1024/1024/1024/1024));
//            System.out.println("station_size = " + station_size);
        }catch(Exception e){
            e.printStackTrace();
        }



        try {
            List<String> databases = dataStatisticsMapper.getFilepath("卫星遥感", "database");
            if (databases.size() > 0) {
                for (String database : databases) {
                    if (database == null || database.equals(" ")) {

                    } else {
                        satellite = satellite + dataStatisticsMapper.getDatabaseSize('"'+database+'"');
                    }

                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        try {
            List<String> filePaths = dataStatisticsMapper.getFilepath("卫星遥感", "file");
            if (filePaths.size() > 0){
                for (String filePath : filePaths) {
                    if (filePath == null || filePath.equals(" ")) {

                    } else {
                        long tmp = getObservationExpandService.getFileSize(new File(filePath));
                        satellite = satellite + tmp;
                    }

                }
            }
//            System.out.println("satellite = " + satellite);
            DecimalFormat df2 =new DecimalFormat("0.0");//设置保留位数
            satellite_size = Double.valueOf(df2.format((float)satellite/1024/1024/1024/1024));
        }catch(Exception e){
            e.printStackTrace();
        }



        try{
            List<String> databases = dataStatisticsMapper.getFilepath("移动车船","database");

            if(databases.size()>0){
                for(String database:databases){
                    if(database == null || database.equals(" ")){

                    }else{
                        mobile = mobile + dataStatisticsMapper.getDatabaseSize('"'+database+'"');
                    }

                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        try{

            List<String> filePaths = dataStatisticsMapper.getFilepath("移动车船","file");
            if(filePaths.size()>0){
                for(String filePath:filePaths){
                    if(filePath == null || filePath.equals(" ")){

                    }else{
                        long tmp = getObservationExpandService.getFileSize(new File(filePath));
                        mobile = mobile + tmp;
                    }

                }
            }

//            System.out.println("mobile = " + mobile);
            DecimalFormat df3=new DecimalFormat("0.00000");//设置保留位数
            mobile_size = Double.valueOf(df3.format((float)mobile/1024/1024/1024/1024));
//            System.out.println("mobile_size = " + mobile_size);
        }catch(Exception e){
            e.printStackTrace();
        }



        try{
            List<String> databases = dataStatisticsMapper.getFilepath("低空遥感","database");
            if(databases.size()>0){
                for(String database:databases){
                    if(database == null || database.equals(" ")){

                    }else{
                        uav = uav + dataStatisticsMapper.getDatabaseSize('"'+database+'"');
                    }

                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            List<String> filePaths = dataStatisticsMapper.getFilepath("低空遥感","file");
            if(filePaths.size()>0){
                for(String filePath:filePaths){
                    if(filePath == null || filePath.equals(" ")){

                    }else{
                        long tmp = getObservationExpandService.getFileSize(new File(filePath));
                        uav = uav + tmp;
                    }

                }
            }
//            System.out.println("uav = " + uav);
            DecimalFormat df4=new DecimalFormat("0.00000");//设置保留位数
            uav_size = Double.valueOf(df4.format((float)uav/1024/1024/1024/1024));
//            System.out.println("uav_size = " + uav_size);
        }catch(Exception e){
            e.printStackTrace();
        }



        try{
            List<String> databases = dataStatisticsMapper.getFilepath("产品","database");

            if(databases.size()>0){
                for(String database:databases){
                    if(database == null || database.equals(" ")){

                    }else{
                        product = product + dataStatisticsMapper.getDatabaseSize('"'+database+'"');
                    }

                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            List<String> filePaths = dataStatisticsMapper.getFilepath("产品","file");
            if(filePaths.size()>0){
                for(String filePath:filePaths){
                    if(filePath == null || filePath.equals(" ")){

                    }else{
                        long tmp = getObservationExpandService.getFileSize(new File(filePath));
                        product = product + tmp;
                    }
                }
            }

//            System.out.println("product = " + product);
            DecimalFormat df5=new DecimalFormat("0.00000");//设置保留位数
            product_size = Double.valueOf(df5.format((float)product/1024/1024/1024/1024));
//            System.out.println("product_size = " + product_size);
        }catch(Exception e){
            e.printStackTrace();
        }



        Map result = new LinkedHashMap();
        Map stations = new LinkedHashMap();
        stations.put("name","地面站网");
        stations.put("number",station_size);

        Map satellites = new LinkedHashMap();
        satellites.put("name","卫星遥感");
        satellites.put("number",satellite_size);

        Map mobiles = new LinkedHashMap();
        mobiles.put("name","移动车船");
        mobiles.put("number",mobile_size);

        Map uavs = new LinkedHashMap();
        uavs.put("name","低空遥感");
        uavs.put("number",uav_size);

        Map products = new LinkedHashMap();
        products.put("name","产品");
        products.put("number",product_size);

        DecimalFormat df6=new DecimalFormat("0.00");//设置保留位数
        Long totalnum = station+satellite+mobile+uav+product;
        total_size = Double.valueOf(df6.format((float)totalnum/1024/1024/1024/1024));

        Map totals = new LinkedHashMap();
        totals.put("name","总计");
        totals.put("number",total_size);


        result.put("station_size",station_size);
        result.put("satellite_size",satellite_size);
        result.put("mobile_size",mobile_size);
        result.put("uav_size",uav_size);
        result.put("product_size",product_size);
        result.put("total",totals);


        dataStatistics statistics = new dataStatistics();
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_Overview_StorageContent");

        JSONObject number = new JSONObject();
        number.put("Overview_StorageContent", result);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_Overview_StorageContent数据条数统计成功！！");
        }else{
            System.out.println("DC_Overview_StorageContent数据条数统计失败！！");
        }
        return i>0;
    }

    @XxlJob("DC_Overview_SensorPlatform")
    public boolean SensorPlatform() {
        int HBWeatherStation=1;
        int HBWater =1;
        int HBHJStation=1;
        int CHWeatherStation=1;
        int CHAirStation=1;
        int nasa = 1;
        int usgs  = 1;
        int himawari = 1 ;
        int sentinel = 1;
        int littleSensor = 1;
        int GF = 1;
        int tw = 1;
        int fy = 1;
        int copernicus = 1;


        try{
            HBHJStation = dataStatisticsMapper.getHBAirNum() + dataStatisticsMapper.getSuperAirNum()+dataStatisticsMapper.getWaterNum()+getVehicleNumber();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            HBWater = dataStatisticsMapper.getWaterQualityNum()+ dataStatisticsMapper.getWaterPollutionNum()+getVesselNumber() + dataStatisticsMapper.getwaterAutoNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            tw = dataStatisticsMapper.getTWAirNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            CHAirStation =dataStatisticsMapper.getCHAirNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            HBWeatherStation =dataStatisticsMapper.getHBWeatherNum()+dataStatisticsMapper.getFYNum()+dataStatisticsMapper.getyaEBNum()+dataStatisticsMapper.getyaMRNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            CHWeatherStation =dataStatisticsMapper.getCHWeatherNum()+dataStatisticsMapper.getWeatherNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            GF = dataStatisticsMapper.getGFNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            nasa =dataStatisticsMapper.getModisNum()+dataStatisticsMapper.getGPMNum()+ dataStatisticsMapper.getGLDASNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            usgs =dataStatisticsMapper.getLandsatNum() ;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            himawari = dataStatisticsMapper.getHimawariNum() ;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            sentinel =  dataStatisticsMapper.getSentinelNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            littleSensor = dataStatisticsMapper.getLittleSensorNumber()+dataStatisticsMapper.getWeatherSensorNumber()+dataStatisticsMapper.getTXZNum()+dataStatisticsMapper.getUAVNum() ;
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            fy = dataStatisticsMapper.getFYNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            copernicus = dataStatisticsMapper.getCopernicusNum();
        }catch(Exception e){
            e.printStackTrace();
        }


        Map sensor = new LinkedHashMap();


        Map HBHJStations = new LinkedHashMap();
        HBHJStations.put("name","湖北省环境监测站数据中心");
        HBHJStations.put("number",((double)(Math.round(HBHJStation/10000))));

        // 长江流域水环境监测中心数据中心
        Map water = new LinkedHashMap();
        water.put("name","长江流域水环境监测中心数据中心");
        water.put("number",((double)(Math.round(HBWater/10000))));

        Map CHAirStations = new LinkedHashMap();
        CHAirStations.put("name","中国空气质量发布平台");
        CHAirStations.put("number",((double)(Math.round(CHAirStation/10000))));

        Map TWEPStations = new LinkedHashMap();
        TWEPStations.put("name","台湾EPA空气质量网");
        TWEPStations.put("number",((double)(Math.round(tw/10000))));

        Map HBWeatherStations = new LinkedHashMap();
        HBWeatherStations.put("name","湖北气象局");
        HBWeatherStations.put("number",((double)(Math.round(HBWeatherStation/10000))));

        Map CHWeatherStations = new LinkedHashMap();
        CHWeatherStations.put("name","中国天气网");
        CHWeatherStations.put("number",((double)(Math.round(CHWeatherStation/10000))));

        Map WuhanSensors = new LinkedHashMap();
        WuhanSensors.put("name","武汉大学传感网");
        WuhanSensors.put("number",((double)(Math.round(littleSensor/10000))));

        Map NASAs = new LinkedHashMap();
        NASAs.put("name","NASA LAADS（美国航空航天宇航局)");
        NASAs.put("number",((double)(Math.round(nasa/10000))));

        Map USGSs = new LinkedHashMap();
        USGSs.put("name","USGS（美国地质勘探局）");
        USGSs.put("number",((double)(Math.round(usgs/10000))));

        Map Himawaris = new LinkedHashMap();
        Himawaris.put("name","JAXA（日本航天局）");
        Himawaris.put("number",((double)(Math.round(himawari/10000))));

        Map fengyuns = new LinkedHashMap();
        fengyuns.put("name","风云卫星遥感数据服务网");
        fengyuns.put("number",((double)(Math.round(fy/10000))));

        Map gaofengs = new LinkedHashMap();
        gaofengs.put("name","湖北高分中心");
        gaofengs.put("number",((double)(Math.round(GF/10000))));


        Map mobiles = new LinkedHashMap();
        mobiles.put("name","ESA（欧洲航天局）");
        mobiles.put("number",((double)(Math.round(sentinel/10000))));   //这个改成 ESA（欧洲航天局）


        Map uavs = new LinkedHashMap();
        uavs.put("name","Copernicus官网");
        uavs.put("number",((double)(Math.round(copernicus/10000))));   //这个改成 Copernicus官网


        Map totals = new LinkedHashMap();
        totals.put("name","总计");
        totals.put("number",(int)Math.floor(HBHJStation/10000)+
                (int)Math.floor(CHAirStation/10000)+ (int)Math.floor(HBWeatherStation/10000)
                +(int)Math.floor(CHWeatherStation/10000)+(int)Math.floor(nasa/10000)+(int)Math.floor(usgs/10000)
                +(int)Math.floor(himawari/10000)+(int)Math.floor(fy/10000)+(int)Math.floor(GF/10000)
                + (int)Math.floor(littleSensor/10000)+(int)Math.floor(copernicus/10000)+(int)Math.floor(HBWater/1000)+
                (int)Math.floor(sentinel/1000)+(int)Math.floor(copernicus/1000));


        sensor.put("HBHJStation",HBHJStations);
        sensor.put("CHAirStation",CHAirStations);
        sensor.put("TWEPStation",TWEPStations);
        sensor.put("tianxingzhouStation",water);

        sensor.put("HBWeatherStation",HBWeatherStations);
        sensor.put("CHWeatherStation",CHWeatherStations);

        sensor.put("NASA",NASAs);
        sensor.put("USGS",USGSs);
        sensor.put("Himawari",Himawaris);
        sensor.put("fengyun",fengyuns);
        sensor.put("gaofeng",gaofengs);

        sensor.put("WuhanSensor",WuhanSensors);

        sensor.put("mobile",mobiles);

        sensor.put("uav",uavs);

        sensor.put("total", totals);

        dataStatistics statistics = new dataStatistics();

        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_Overview_SensorPlatform");

        JSONObject number = new JSONObject();
        number.put("Overview_SensorPlatform", sensor);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_Overview_SensorPlatform数据条数统计成功！！");
        }else{
            System.out.println("DC_Overview_SensorPlatform数据条数统计失败！！");
        }
        return i>0;
    }

    @XxlJob("DC_BasicData_GroundStation")
    public boolean GroundStation() {
        int AirStation=1;
        int WeatherStation=1;
        int WaterStation=1;
        int SuperStation=1;
        try{
            AirStation = dataStatisticsMapper.getCHAirNum()+ dataStatisticsMapper.getHBAirNum()+ dataStatisticsMapper.getTWAirNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            WeatherStation = dataStatisticsMapper.getWeatherNum() + dataStatisticsMapper.getCHWeatherNum() + dataStatisticsMapper.getHBWeatherNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            WaterStation = dataStatisticsMapper.getWaterQualityNum()+ dataStatisticsMapper.getWaterPollutionNum()+dataStatisticsMapper.getWaterNum()+dataStatisticsMapper.getwaterAutoNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            SuperStation = dataStatisticsMapper.getSuperAirNum();
        }catch(Exception e){
            e.printStackTrace();
        }


        int num = AirStation+WeatherStation+WaterStation+SuperStation;

        Map station = new LinkedHashMap();


        Map Airs = new LinkedHashMap();
        Airs.put("name","空气");
        Airs.put("number",((double)(Math.round(AirStation/10000))));

        Map Weathers = new LinkedHashMap();
        Weathers.put("name","气象");
        Weathers.put("number",((double)(Math.round(WeatherStation/10000))));

        Map Waters = new LinkedHashMap();
        Waters.put("name","水质");
        Waters.put("number",((double)(Math.round(WaterStation/10000))));

        Map Supers = new LinkedHashMap();
        Supers.put("name","超级");
        Supers.put("number",((double)(Math.round(SuperStation/10000))));


        Map totals = new LinkedHashMap();
        totals.put("name","总计");
        totals.put("number",((int)Math.floor(num/10000)));


        station.put("total",totals);
        station.put("Air_num",Airs);
        station.put("Weather_num",Weathers);
        station.put("Water_num",Waters);
        station.put("Super_num",Supers);



        Map AirStations = new LinkedHashMap();
        AirStations.put("name","空气");
        AirStations.put("number",2351);

        Map WeatherStations = new LinkedHashMap();
        WeatherStations.put("name","气象");
        WeatherStations.put("number",5468);

        Map WaterStations = new LinkedHashMap();
        WaterStations.put("name","水质");
        WaterStations.put("number",811);

        Map SuperStations = new LinkedHashMap();
        SuperStations.put("name","超级");
        SuperStations.put("number",11);



        station.put("AirStation",AirStations);
        station.put("WeatherStation",WeatherStations);
        station.put("WaterStation",WaterStations);
        station.put("SuperStation",SuperStations);

        dataStatistics statistics = new dataStatistics();

        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_BasicData_GroundStation");

        JSONObject number = new JSONObject();
        number.put("BasicData_GroundStation", station);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_BasicData_GroundStation数据条数统计成功！！");
        }else{
            System.out.println("DC_BasicData_GroundStation数据条数统计失败！！");
        }
        return i>0;
    }


    @XxlJob("DC_BasicData_Satellite")
    public boolean Satellite() {

        int modis = 1;
        int landsat  = 1;
        int himawari = 1 ;
        int GF = 1;
        int fy = 1;
        int gldas = 1;
        int sentinel =1 ;


        try{
            GF = dataStatisticsMapper.getGFNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            modis = dataStatisticsMapper.getModisNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            landsat =dataStatisticsMapper.getLandsatNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            himawari = dataStatisticsMapper.getHimawariNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            gldas =dataStatisticsMapper.getGLDASNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            sentinel = dataStatisticsMapper.getSentinelNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            fy = dataStatisticsMapper.getFYNum();
        }catch(Exception e){
            e.printStackTrace();
        }



        //风云需要确定接口！！！

        Map satellite = new LinkedHashMap();

        int total = modis+landsat+himawari+fy+GF+gldas+sentinel;

        Map Modiss = new LinkedHashMap();
        Modiss.put("name","Modis");
        Modiss.put("number",((double)(Math.round(modis/10000))));

        Map Landsats = new LinkedHashMap();
        Landsats.put("name","Landsat");
        Landsats.put("number",((double)(Math.round(landsat/10000))));

        Map himawaris = new LinkedHashMap();
        himawaris.put("name","Himawari");
        himawaris.put("number",((double)(Math.round(himawari/10000))));

        Map fengyuns = new LinkedHashMap();
        fengyuns.put("name","风云");
        fengyuns.put("number",((double)(Math.round(fy/10000))));

        Map gaofens = new LinkedHashMap();
        gaofens.put("name","高分");
        gaofens.put("number",((double)(Math.round(GF/10000))));

        Map GLDASs = new LinkedHashMap();
        GLDASs.put("name","GLDAS");
        GLDASs.put("number",((double)(Math.round(gldas/10000))));

        Map GPMs = new LinkedHashMap();
        GPMs.put("name","Sentinel");
        GPMs.put("number",((double)(Math.round(sentinel/10000))));


        Map totals = new LinkedHashMap();
        totals.put("name","总计");
        totals.put("number",((int)(Math.floor(total/10000))));


        satellite.put("total",totals);
        satellite.put("Modis",Modiss);
        satellite.put("Landsat",Landsats);
        satellite.put("himawari",himawaris);
        satellite.put("fengyun",fengyuns);
        satellite.put("gaofen",gaofens);
        satellite.put("GLDAS",GLDASs);
        satellite.put("GPM",GPMs);

        dataStatistics statistics = new dataStatistics();

        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_BasicData_Satellite");

        JSONObject number = new JSONObject();
        number.put("BasicData_Satellite", satellite);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_BasicData_Satellite数据条数统计成功！！");
        }else{
            System.out.println("DC_BasicData_Satellite数据条数统计失败！！");
        }
        return i>0;

    }


    @XxlJob("DC_BasicData_VehicleVessel")
    public boolean VehicleVessel() {

        int Vehicle = 1;
        int Vessel = 1;
        int sampling = 1;

        try{
            Vehicle =  getVehicleNumber();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            Vessel =  getVesselNumber();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            sampling =  dataStatisticsMapper.getMailDataNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        //需要确定人工采样的数据接口！！！！
        int total = Vehicle+Vessel+sampling;

        Map sensor = new LinkedHashMap();

        Map Vehicles = new LinkedHashMap();
        Vehicles.put("name","走航车");
        Vehicles.put("number",((double)(Math.round(Vehicle/10000))));

        Map Vessels = new LinkedHashMap();
        Vessels.put("name","走航船");
        Vessels.put("number",((double)(Math.round(Vessel/10000))));

        Map samplings = new LinkedHashMap();
        samplings.put("name","人工采样");
        samplings.put("number",((double)(Math.round(sampling/10000))));

        Map totals = new LinkedHashMap();
        totals.put("name","总计");
        totals.put("number",((int)(Math.floor(total/10000))));


        sensor.put("Vehicle",Vehicles);
        sensor.put("Vessel",Vessels);
        sensor.put("sampling",samplings);
        sensor.put("total",totals);


        dataStatistics statistics = new dataStatistics();
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_BasicData_VehicleVessel");

        JSONObject number = new JSONObject();
        number.put("BasicData_VehicleVessel", sensor);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_BasicData_VehicleVessel数据条数统计成功！！");
        }else{
            System.out.println("DC_BasicData_VehicleVessel数据条数统计失败！！");
        }
        return i>0;
    }


    @XxlJob("DC_BasicData_LowARS")
    public boolean LowAltitude() {
        int uhd185 = 1;
        int Thermal_infrared_camera = 1;
        int detecting_instrument = 1;

//        try{
//            uhd185 =  getVehicleNumber();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
        try{
            Thermal_infrared_camera =  dataStatisticsMapper.getUAVNum();
        }catch(Exception e){
            e.printStackTrace();
        }
//        try{
//            detecting_instrument =  getVesselNumber();
//        }catch(Exception e){
//            e.printStackTrace();
//        }

        //需要确定无人机的数据接口！！！！

        Map sensor = new LinkedHashMap();

        Map UHD185s = new LinkedHashMap();
        UHD185s.put("name","UHD185");
        UHD185s.put("number",uhd185);

        Map Thermal_infrared_cameras = new LinkedHashMap();
        Thermal_infrared_cameras.put("name","热红外相机");
        Thermal_infrared_cameras.put("number",Thermal_infrared_camera);

        Map detecting_instruments = new LinkedHashMap();
        detecting_instruments.put("name","探测仪");
        detecting_instruments.put("number",detecting_instrument);

        Map totals = new LinkedHashMap();
        totals.put("name","总计");
        totals.put("number",uhd185+Thermal_infrared_camera+detecting_instrument);

        sensor.put("UHD185",UHD185s);
        sensor.put("Thermal_infrared_camera",Thermal_infrared_cameras);
        sensor.put("detecting_instrument",detecting_instruments);
        sensor.put("total",totals);


        dataStatistics statistics = new dataStatistics();
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_BasicData_LowARS");

        JSONObject number = new JSONObject();
        number.put("BasicData_LowARS", sensor);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_BasicData_LowARS数据条数统计成功！！");
        }else{
            System.out.println("DC_BasicData_LowARS数据条数统计失败！！");
        }
        return i>0;
    }


    @XxlJob("DC_Product_ProductType")
    public boolean ProductType() {
        int air=1;
        int ecology=1;
        int soli=1;
        int water=1;
        int other = 1;
        int other2 = 1;
        int other3 = 1;

        try{
            air = dataStatisticsMapper.getProductTypeNum("大气");
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            ecology = dataStatisticsMapper.getProductTypeNum("生态");
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            soli =dataStatisticsMapper.getProductTypeNum("土壤");
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            water = dataStatisticsMapper.getProductTypeNum("水质");
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            other = dataStatisticsMapper.getProductTypeNum("模拟同化");
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            other2 = dataStatisticsMapper.getProductTypeNum("遥感基础产品");
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            other3 = dataStatisticsMapper.getProductTypeNum("智能认知");
        }catch(Exception e){
            e.printStackTrace();
        }


        Map station = new LinkedHashMap();


        Map airs = new LinkedHashMap();
        airs.put("name","大气");
        airs.put("number",air);

        Map waters = new LinkedHashMap();
        waters.put("name","水质");
        waters.put("number",water);

        Map solis = new LinkedHashMap();
        solis.put("name","土壤");
        solis.put("number",soli);

        Map ecologys = new LinkedHashMap();
        ecologys.put("name","生态");
        ecologys.put("number",ecology);

        Map others = new LinkedHashMap();
        others.put("name","模拟同化");
        others.put("number",other);

        Map others2 = new LinkedHashMap();
        others2.put("name","遥感基础");
        others2.put("number",other2);

        Map others3 = new LinkedHashMap();
        others3.put("name","智能认知");
        others3.put("number",other3);



        Map totals = new LinkedHashMap();
        totals.put("name","总计");
        totals.put("number",air+water+soli+ecology+other+other2+other3);


        station.put("air",airs);
        station.put("water",waters);
        station.put("soli",solis);
        station.put("ecology",ecologys);
        station.put("moni",others);
        station.put("yaogan",others2);
        station.put("zhineng",others3);

        station.put("total",totals);

        dataStatistics statistics = new dataStatistics();
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_Product_ProductType");

        JSONObject number = new JSONObject();
        number.put("Product_ProductType", station);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_Product_ProductType数据条数统计成功！！");
        }else{
            System.out.println("DC_Product_ProductType数据条数统计失败！！");
        }
        return i>0;

    }

    @XxlJob("DC_Product_ProductNumber")
    public boolean ProductNumber() {
        List<Map<String,Object>> res = new ArrayList<>();

        //年的
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
        calendar.add(Calendar.MONTH, -12);
        for (int i=0; i<=12; i++) {
            String begin = DataCenterUtils.getFirstDay(calendar);
            String end = DataCenterUtils.getLastDay(calendar);
            Instant start_mounth = DataCenterUtils.string2Instant(begin);
            Instant stop_mounth = DataCenterUtils.string2Instant(end);

            int air=0;
            int ecology=0;
            int soli=0;
            int water=0;
            int other = 0;
            int other2 = 0;
            int other3 = 0;

            try{
                air = dataStatisticsMapper.getProductNumByTime2("大气模块",stop_mounth);

            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                ecology =dataStatisticsMapper.getProductNumByTime2("生态模块",stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                soli =dataStatisticsMapper.getProductNumByTime2("土壤模块",stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                water =dataStatisticsMapper.getProductNumByTime2("水质模块",stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other = dataStatisticsMapper.getProductNumByTime2("模拟同化模块",stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }


            try{
                other2 = dataStatisticsMapper.getProductNumByTime2("遥感基础模块",stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }


            try{
                other3 = dataStatisticsMapper.getProductNumByTime2("智能认知模块",stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }


            Map airs = new LinkedHashMap();
            airs.put("name","大气");
            airs.put("number",air);

            Map waters = new LinkedHashMap();
            waters.put("name","水质");
            waters.put("number",water);

            Map solis = new LinkedHashMap();
            solis.put("name","土壤");
            solis.put("number",soli);

            Map ecologys = new LinkedHashMap();
            ecologys.put("name","生态");
            ecologys.put("number",ecology);

            Map others = new LinkedHashMap();
            others.put("name","模拟同化");
            others.put("number",other);


            Map others2 = new LinkedHashMap();
            others2.put("name","遥感基础");
            others2.put("number",other2);


            Map others3 = new LinkedHashMap();
            others3.put("name","智能认知");
            others3.put("number",other3);


            Map<String,Object> tmp = new LinkedHashMap();
            String time = stop_mounth.plusSeconds(8*60*60).toString();
            tmp.put("air",airs);
            tmp.put("water",waters);
            tmp.put("soli",solis);
            tmp.put("ecology",ecologys);
            tmp.put("moni",others);
            tmp.put("yaogan",others2);
            tmp.put("zhineng",others3);
            tmp.put("time",time.substring(0,4)+"年"+time.substring(5,7)+"月");
            res.add(tmp);
            calendar.add(Calendar.MONTH, 1);
        }
//        while(start.isBefore(stop)){
//            int air=0;
//            int ecology=0;
//            int soli=0;
//            int water=0;
//            int other = 0;
//            Instant end = start.plusSeconds(24*60*60);
//            try{
//                air = dataStatisticsMapper.getProductNumByTime2("Air",end);
//
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            try{
//                ecology =dataStatisticsMapper.getProductNumByTime2("Ecology",end);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            try{
//                soli =dataStatisticsMapper.getProductNumByTime2("Soli",end);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            try{
//                water =dataStatisticsMapper.getProductNumByTime2("Water",end);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//
//            try{
//                other = dataStatisticsMapper.getProductNumByTime2(" ",end);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//
//            Map airs = new LinkedHashMap();
//            airs.put("name","大气");
//            airs.put("number",air);
//
//            Map waters = new LinkedHashMap();
//            waters.put("name","水质");
//            waters.put("number",water);
//
//            Map solis = new LinkedHashMap();
//            solis.put("name","土壤");
//            solis.put("number",soli);
//
//            Map ecologys = new LinkedHashMap();
//            ecologys.put("name","生态");
//            ecologys.put("number",ecology);
//
//            Map others = new LinkedHashMap();
//            others.put("name","其他");
//            others.put("number",other);
//
//            Map<String,Object> tmp = new LinkedHashMap();
//            String time = start.plusSeconds(9*60*60).toString();
//            tmp.put("air",airs);
//            tmp.put("water",waters);
//            tmp.put("soli",solis);
//            tmp.put("ecology",ecologys);
//            tmp.put("other",others);
//            tmp.put("time",time.substring(time.indexOf("T")+1,time.indexOf("Z")).substring(0,2)+"时");
//            res.add(tmp);
//            start = start.plusSeconds(60*60);
//        }
        dataStatistics statistics = new dataStatistics();
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);

        statistics.setJobId("DC_Product_ProductNumber");


        JSONObject number = new JSONObject();
        number.put("Product_ProductNumber", res);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_Product_ProductNumber数据条数统计成功！！");
        }else{
            System.out.println("DC_Product_ProductNumber数据条数统计失败！！");
        }
        return i>0;
    }


    @XxlJob("DC_RealTime_Data")
    public boolean Data() {

        List<Map<String,Object>> year = new ArrayList<>();

        List<Map<String,Object>> month = new ArrayList<>();

        List<Map<String,Object>> day = new ArrayList<>();

        Map<String,Object> res = new LinkedHashMap<>();


        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//        System.out.println(df.format(new Date()));
        String timeNow = df.format(new Date());// new Date()为获取当前系统时间

        Instant start = DataCenterUtils.string2Instant(timeNow).minusSeconds(24*60*60);//减24小时
        Instant stop = DataCenterUtils.string2Instant(timeNow);
//        System.out.println("start = " + start);



        //年的
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
        calendar.add(Calendar.MONTH, -12);
        for (int i=0; i<=12; i++) {
            String begin = DataCenterUtils.getFirstDay(calendar);
            String end = DataCenterUtils.getLastDay(calendar);
            Instant start_mounth = DataCenterUtils.string2Instant(begin);
            Instant stop_mounth = DataCenterUtils.string2Instant(end);

            int Station=0;
            int satellite =0;
            int mobile  = 0;
            int  uva= 0;
            int other =0;

            try{
                Station = dataStatisticsMapper.getCHAirNumByTime(start_mounth,stop_mounth) + dataStatisticsMapper.getWeatherNumByTime(start_mounth,stop_mounth)
                        +dataStatisticsMapper.getCHWeatherNumByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getHBAirNumByTime(start_mounth,stop_mounth)
                        +dataStatisticsMapper.getHBWeatherNumByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getSuperAirNumByTime(start_mounth,stop_mounth)+
                        dataStatisticsMapper.getWaterNumByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getTWAirNumByTime(start_mounth,stop_mounth);

            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                satellite = dataStatisticsMapper.getHimawariNumByTime(start_mounth,stop_mounth) + dataStatisticsMapper.getModisNumByTime(start_mounth,stop_mounth)+
                        dataStatisticsMapper.getLandsatNumByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getGLDASNumByTime(start_mounth,stop_mounth)+
                        dataStatisticsMapper.getGPMNumByTime(start_mounth,stop_mounth)
                        +dataStatisticsMapper.getFYNumByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getSentineByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getCopernicus(start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                uva = dataStatisticsMapper.getUAVNumByTime(start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                mobile =  getVehicleNumberByTime(start_mounth,stop_mounth)+getVesselNumberByTime(start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other = dataStatisticsMapper.getNumberByTime(start_mounth,stop_mounth) +dataStatisticsMapper.getWeatherSensorByTime(start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }

            Map<String,Object> tmp = new LinkedHashMap();

            String time = stop_mounth.plusSeconds(8*60*60).toString();


            Map Stations = new LinkedHashMap();
            Stations.put("name","地面站网");
            Stations.put("number",Station);

            Map satellites = new LinkedHashMap();
            satellites.put("name","卫星遥感");
            satellites.put("number",satellite);

            Map mobiles = new LinkedHashMap();
            mobiles.put("name","移动车船");
            mobiles.put("number",mobile);

            Map uvas = new LinkedHashMap();
            uvas.put("name","低空遥感");
            uvas.put("number",uva);

            Map others = new LinkedHashMap();
            others.put("name","其他");
            others.put("number",other);


            tmp.put("Station",Stations);
            tmp.put("satellite",satellites);
            tmp.put("mobile",mobiles);
            tmp.put("uva",uvas);
            tmp.put("other",others);
            tmp.put("time",time.substring(0,4)+"年"+time.substring(5,7)+"月");
            year.add(tmp);
            calendar.add(Calendar.MONTH, 1);
        }

        //月的
        Calendar calendar1 = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
        calendar1.add(Calendar.DATE, -14);
        for (int i=0; i<=14; i++) {
            String begin = DataCenterUtils.getFirstTime(calendar1);
            String end = DataCenterUtils.getLastTime(calendar1);
            Instant start_mounth = DataCenterUtils.string2Instant(begin);
            Instant stop_mounth = DataCenterUtils.string2Instant(end);

            int Station=0;
            int satellite =0;
            int mobile  = 0;
            int  uva= 0;
            int other =0;

            try{
                Station = dataStatisticsMapper.getCHAirNumByTime(start_mounth,stop_mounth) + dataStatisticsMapper.getWeatherNumByTime(start_mounth,stop_mounth)
                        +dataStatisticsMapper.getCHWeatherNumByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getHBAirNumByTime(start_mounth,stop_mounth)
                        +dataStatisticsMapper.getHBWeatherNumByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getSuperAirNumByTime(start_mounth,stop_mounth)+
                        dataStatisticsMapper.getWaterNumByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getTWAirNumByTime(start_mounth,stop_mounth);

            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                satellite = dataStatisticsMapper.getHimawariNumByTime(start_mounth,stop_mounth) + dataStatisticsMapper.getModisNumByTime(start_mounth,stop_mounth)+
                        dataStatisticsMapper.getLandsatNumByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getGLDASNumByTime(start_mounth,stop_mounth)+
                        dataStatisticsMapper.getGPMNumByTime(start_mounth,stop_mounth)
                        +dataStatisticsMapper.getFYNumByTime(start_mounth,stop_mounth)+dataStatisticsMapper.getSentineByTime(start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                uva = dataStatisticsMapper.getUAVNumByTime(start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                mobile =  getVehicleNumberByTime(start_mounth,stop_mounth)+getVesselNumberByTime(start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other = dataStatisticsMapper.getNumberByTime(start_mounth,stop_mounth)+ dataStatisticsMapper.getWeatherSensorByTime(start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }

            Map<String,Object> tmp = new LinkedHashMap();

            String time = stop_mounth.plusSeconds(8*60*60).toString();


            Map Stations = new LinkedHashMap();
            Stations.put("name","地面站网");
            Stations.put("number",Station);

            Map satellites = new LinkedHashMap();
            satellites.put("name","卫星遥感");
            satellites.put("number",satellite);

            Map mobiles = new LinkedHashMap();
            mobiles.put("name","移动车船");
            mobiles.put("number",mobile);

            Map uvas = new LinkedHashMap();
            uvas.put("name","低空遥感");
            uvas.put("number",uva);

            Map others = new LinkedHashMap();
            others.put("name","其他");
            others.put("number",other);


            tmp.put("Station",Stations);
            tmp.put("satellite",satellites);
            tmp.put("mobile",mobiles);
            tmp.put("uva",uvas);
            tmp.put("other",others);


            tmp.put("time",time.substring(5,7)+"月"+time.substring(8,10)+"日");
            month.add(tmp);
            calendar1.add(Calendar.DATE, 1);
        }

        //天的
        while(start.isBefore(stop)){
            int Station=0;
            int satellite =0;
            int mobile  = 0;
            int  uva= 0;
            int other =0;
            Instant end = start.plusSeconds(60*60);
            try{
                Station = dataStatisticsMapper.getCHAirNumByTime(start,end) + dataStatisticsMapper.getWeatherNumByTime(start,end)
                        +dataStatisticsMapper.getCHWeatherNumByTime(start,end)+dataStatisticsMapper.getHBAirNumByTime(start,end)
                        +dataStatisticsMapper.getHBWeatherNumByTime(start,end)+dataStatisticsMapper.getSuperAirNumByTime(start,end)+
                        dataStatisticsMapper.getWaterNumByTime(start,end)+dataStatisticsMapper.getTWAirNumByTime(start,end);

            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                satellite = dataStatisticsMapper.getHimawariNumByTime(start,end) + dataStatisticsMapper.getModisNumByTime(start,end)+
                        dataStatisticsMapper.getLandsatNumByTime(start,end)+dataStatisticsMapper.getGLDASNumByTime(start,end)+
                        dataStatisticsMapper.getGPMNumByTime(start,end)
                        +dataStatisticsMapper.getFYNumByTime(start,end)+dataStatisticsMapper.getSentineByTime(start,end)+dataStatisticsMapper.getCopernicus(start,end);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                uva = dataStatisticsMapper.getUAVNumByTime(start,end);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                mobile =  getVehicleNumberByTime(start,end)+getVesselNumberByTime(start,end);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other = dataStatisticsMapper.getNumberByTime(start,end)+dataStatisticsMapper.getWeatherSensorByTime(start,end);
            }catch(Exception e){
                e.printStackTrace();
            }

            Map<String,Object> tmp = new LinkedHashMap();
            String time = start.plusSeconds(8*60*60).toString();


            Map Stations = new LinkedHashMap();
            Stations.put("name","地面站网");
            Stations.put("number",Station);

            Map satellites = new LinkedHashMap();
            satellites.put("name","卫星遥感");
            satellites.put("number",satellite);

            Map mobiles = new LinkedHashMap();
            mobiles.put("name","移动车船");
            mobiles.put("number",mobile);

            Map uvas = new LinkedHashMap();
            uvas.put("name","低空遥感");
            uvas.put("number",uva);

            Map others = new LinkedHashMap();
            others.put("name","其他");
            others.put("number",other);


            tmp.put("Station",Stations);
            tmp.put("satellite",satellites);
            tmp.put("mobile",mobiles);
            tmp.put("uva",uvas);
            tmp.put("other",others);
            tmp.put("time",time.substring(time.indexOf("T")+1,time.indexOf("Z")).substring(0,2)+"时");
            day.add(tmp);
            start = start.plusSeconds(60*60);
        }


        res.put("year_table",year);
        res.put("month_table",month);
        res.put("day_table",day);

        dataStatistics statistics = new dataStatistics();

        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_RealTime_Data");

        JSONObject number = new JSONObject();
        number.put("RealTime_Data", res);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_RealTime_Data数据条数统计成功！！");
        }else{
            System.out.println("DC_RealTime_Data数据条数统计失败！！");
        }
        return i>0;
    }


    @XxlJob("DC_RealTime_Product")
    public boolean Product() {

        List<Map<String,Object>> year = new ArrayList<>();

        List<Map<String,Object>> month = new ArrayList<>();

        List<Map<String,Object>> day = new ArrayList<>();

        Map<String,Object> res = new LinkedHashMap<>();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String time_now = df.format(new Date());// new Date()为获取当前系统时间
        Instant start = DataCenterUtils.string2Instant(time_now).minusSeconds(24*60*60);//减24小时
        Instant stop = DataCenterUtils.string2Instant(time_now);


        //年的
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
        calendar.add(Calendar.MONTH, -12);
        for (int i=0; i<=12; i++) {
            String begin = DataCenterUtils.getFirstDay(calendar);
            String end = DataCenterUtils.getLastDay(calendar);
            Instant start_mounth = DataCenterUtils.string2Instant(begin);
            Instant stop_mounth = DataCenterUtils.string2Instant(end);

            int air=0;
            int ecology=0;
            int soli=0;
            int water=0;
            int other = 0;
            int other2 = 0;
            int other3 = 0;

            try{
                air = dataStatisticsMapper.getProductNumByTime("空气模块",start_mounth,stop_mounth);

            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                ecology =dataStatisticsMapper.getProductNumByTime("生态模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                soli =dataStatisticsMapper.getProductNumByTime("土壤模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                water =dataStatisticsMapper.getProductNumByTime("水质模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other = dataStatisticsMapper.getProductNumByTime("模拟同化模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other2 = dataStatisticsMapper.getProductNumByTime("遥感基础模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other3 = dataStatisticsMapper.getProductNumByTime("智能认知模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }


            Map airs = new LinkedHashMap();
            airs.put("name","大气");
            airs.put("number",air);

            Map waters = new LinkedHashMap();
            waters.put("name","水质");
            waters.put("number",water);

            Map solis = new LinkedHashMap();
            solis.put("name","土壤");
            solis.put("number",soli);

            Map ecologys = new LinkedHashMap();
            ecologys.put("name","生态");
            ecologys.put("number",ecology);

            Map others = new LinkedHashMap();
            others.put("name","模拟同化");
            others.put("number",other);

            Map others2 = new LinkedHashMap();
            others2.put("name","遥感基础");
            others2.put("number",other2);

            Map others3 = new LinkedHashMap();
            others3.put("name","智能认知");
            others3.put("number",other3);

            Map<String,Object> tmp = new LinkedHashMap();
            String time = stop_mounth.plusSeconds(8*60*60).toString();
            tmp.put("air",airs);
            tmp.put("water",waters);
            tmp.put("soli",solis);
            tmp.put("ecology",ecologys);
            tmp.put("moni",others);
            tmp.put("yaogan",others2);
            tmp.put("zhineng",others3);
            tmp.put("time",time.substring(0,4)+"年"+time.substring(5,7)+"月");
            year.add(tmp);

            calendar.add(Calendar.MONTH, 1);
        }


        //月的
        Calendar calendar1 = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
        calendar1.add(Calendar.DATE, -14);
        for (int i=0; i<=14; i++) {
            String begin = DataCenterUtils.getFirstTime(calendar1);
            String end = DataCenterUtils.getLastTime(calendar1);
            Instant start_mounth = DataCenterUtils.string2Instant(begin);
            Instant stop_mounth = DataCenterUtils.string2Instant(end);

            int air=0;
            int ecology=0;
            int soli=0;
            int water=0;
            int other = 0;
            int other2 = 0;
            int other3 = 0;

            try{
                air = dataStatisticsMapper.getProductNumByTime("空气模块",start_mounth,stop_mounth);

            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                ecology =dataStatisticsMapper.getProductNumByTime("生态模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                soli =dataStatisticsMapper.getProductNumByTime("土壤模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                water =dataStatisticsMapper.getProductNumByTime("水质模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other = dataStatisticsMapper.getProductNumByTime("模拟同化模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                other2 = dataStatisticsMapper.getProductNumByTime("遥感基础模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other3 = dataStatisticsMapper.getProductNumByTime("智能认知模块",start_mounth,stop_mounth);
            }catch(Exception e){
                e.printStackTrace();
            }


            Map airs = new LinkedHashMap();
            airs.put("name","大气");
            airs.put("number",air);

            Map waters = new LinkedHashMap();
            waters.put("name","水质");
            waters.put("number",water);

            Map solis = new LinkedHashMap();
            solis.put("name","土壤");
            solis.put("number",soli);

            Map ecologys = new LinkedHashMap();
            ecologys.put("name","生态");
            ecologys.put("number",ecology);

            Map others = new LinkedHashMap();
            others.put("name","模拟同化");
            others.put("number",other);


            Map others2 = new LinkedHashMap();
            others2.put("name","遥感基础");
            others2.put("number",other2);


            Map others3 = new LinkedHashMap();
            others3.put("name","智能认知");
            others3.put("number",other3);

            Map<String,Object> tmp = new LinkedHashMap();
            String time = stop_mounth.plusSeconds(8*60*60).toString();
            tmp.put("air",airs);
            tmp.put("water",waters);
            tmp.put("soli",solis);
            tmp.put("ecology",ecologys);
            tmp.put("moni",others);
            tmp.put("yaogan",others2);
            tmp.put("zhineng",others3);
            tmp.put("time",time.substring(5,7)+"月"+time.substring(8,10)+"日");
            month.add(tmp);

            calendar1.add(Calendar.DATE, 1);
        }


        //天的
        while(start.isBefore(stop)){
            int air=0;
            int ecology=0;
            int soli=0;
            int water=0;
            int other = 0;
            int other2 = 0;
            int other3 = 0;
            Instant end = start.plusSeconds(60*60);
            try{
                air = dataStatisticsMapper.getProductNumByTime("大气模块",start,end);

            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                ecology =dataStatisticsMapper.getProductNumByTime("生态模块",start,end);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                soli =dataStatisticsMapper.getProductNumByTime("土壤模块",start,end);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                water =dataStatisticsMapper.getProductNumByTime("水质模块",start,end);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other = dataStatisticsMapper.getProductNumByTime("模拟同化模块",start,end);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other2 = dataStatisticsMapper.getProductNumByTime("遥感基础模块",start,end);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other3 = dataStatisticsMapper.getProductNumByTime("智能认知模块",start,end);
            }catch(Exception e){
                e.printStackTrace();
            }

            Map airs = new LinkedHashMap();
            airs.put("name","大气");
            airs.put("number",air);

            Map waters = new LinkedHashMap();
            waters.put("name","水质");
            waters.put("number",water);

            Map solis = new LinkedHashMap();
            solis.put("name","土壤");
            solis.put("number",soli);

            Map ecologys = new LinkedHashMap();
            ecologys.put("name","生态");
            ecologys.put("number",ecology);

            Map others = new LinkedHashMap();
            others.put("name","模拟同化");
            others.put("number",other);

            Map others2 = new LinkedHashMap();
            others2.put("name","遥感基础");
            others2.put("number",other2);

            Map others3 = new LinkedHashMap();
            others3.put("name","智能认知");
            others3.put("number",other3);

            Map<String,Object> tmp = new LinkedHashMap();
            String time = start.plusSeconds(9*60*60).toString();
            tmp.put("air",airs);
            tmp.put("water",waters);
            tmp.put("soli",solis);
            tmp.put("ecology",ecologys);
            tmp.put("moni",others);
            tmp.put("yaogan",others2);
            tmp.put("zhineng",others3);
            tmp.put("time",time.substring(time.indexOf("T")+1,time.indexOf("Z")).substring(0,2)+"时");
            day.add(tmp);
            start = start.plusSeconds(60*60);
        }


        res.put("year_table",year);
        res.put("month_table",month);
        res.put("day_table",day);

        dataStatistics statistics = new dataStatistics();

        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);


        statistics.setJobId("DC_RealTime_Product");

        JSONObject number = new JSONObject();
        number.put("RealTime_Product", res);
        statistics.setStatistics(number.toString());
        int i = dataStatisticsMapper.insert(statistics);
        if(i>0){
            System.out.println("DC_RealTime_Product数据条数统计成功！！");
        }else{
            System.out.println("DC_RealTime_Product数据条数统计失败！！");
        }
        return i>0;
    }



    public int getVesselNumber() throws IOException {
        String document = DataCenterUtils.doGet( "http://114.55.99.71:443/getVesselNumber", null);
        int  result = Integer.valueOf(document);
        return result;
    }

    public int getVehicleNumber() throws IOException {
        String document = DataCenterUtils.doGet( "http://114.55.99.71:443/getVehicleNumber", null);
        int  result = Integer.valueOf(document);
        return result;
    }


    private int getVesselNumberByTime(Instant nowTime, Instant preTime) throws IOException {
//        String start = preTime.plusSeconds(8*60*60).toString().replace("T", " ").replace("Z", "");
//
//        String end = nowTime.plusSeconds(8*60*60).toString().replace("T", " ").replace("Z", "");

        String start = preTime.plusSeconds(8*60*60).toString();
        String end = nowTime.plusSeconds(8*60*60).toString();
        String param = "startTime="+start +"&endTime="+end;
        String document = DataCenterUtils.doGet( "http://114.55.99.71:443/getVesselNumberByTime", param);
        int  result = Integer.valueOf(document);
        return result;
    }


    private int getVehicleNumberByTime(Instant nowTime, Instant preTime) throws IOException {
//        String start = preTime.plusSeconds(8*60*60).toString().replace("T", " ").replace("Z", "");
//        String end = nowTime.plusSeconds(8*60*60).toString().replace("T", " ").replace("Z", "");
        String start = preTime.plusSeconds(8*60*60).toString();
        String end = nowTime.plusSeconds(8*60*60).toString();
        String param = "startTime="+start +"&endTime="+end;
        String document = DataCenterUtils.doGet( "http://114.55.99.71:443/getVehicleNumberByTime", param);
        int  result = Integer.valueOf(document);
        return result;
    }


}

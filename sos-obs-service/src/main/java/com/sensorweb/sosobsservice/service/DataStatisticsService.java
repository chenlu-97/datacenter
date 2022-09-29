package com.sensorweb.sosobsservice.service;


import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import com.sensorweb.sosobsservice.dao.ObservationMapper;
import com.sensorweb.sosobsservice.dao.dataStatisticsMapper;
import com.sensorweb.sosobsservice.entity.dataStatistics;
import com.sensorweb.sosobsservice.feign.*;
import com.sensorweb.sosobsservice.util.ObsConstant;
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

//    @PostConstruct
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
            offline = dataStatisticsMapper.getWaterQualityNum()+ dataStatisticsMapper.getWaterPollutionNum()+dataStatisticsMapper.getGFNum();
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
            gee =dataStatisticsMapper.getLandsatNum() +dataStatisticsMapper.getFYNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            himawari = dataStatisticsMapper.getHimawariNum() ;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            mobile =  getVehicleNumber()+getVesselNumber()+ dataStatisticsMapper.getMailDataNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            littleSensor = littleSensorFeignClient.getLittleSensorNumber() ;
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
        String res = null;
        if(num!=0){
            res = String.valueOf((int)Math.floor(num/10000))+"W";
        }else{
            res = "error";
        }
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


        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant nowTime= DataCenterUtils.string2Instant(formatter.format(dateTime));
        Instant preTime = nowTime.minusSeconds(24*60*60);

        try{
            Station = dataStatisticsMapper.getCHAirNumByTime(preTime,nowTime) + dataStatisticsMapper.getWeatherNumByTime(preTime,nowTime)
            +dataStatisticsMapper.getCHWeatherNumByTime(preTime,nowTime)+dataStatisticsMapper.getHBAirNumByTime(preTime,nowTime)
            +dataStatisticsMapper.getHBWeatherNumByTime(preTime,nowTime)+dataStatisticsMapper.getSuperAirNumByTime(preTime,nowTime)+
            dataStatisticsMapper.getWaterNumByTime(preTime,nowTime)+dataStatisticsMapper.getTWAirNumByTime(preTime,nowTime);

        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            satellite = dataStatisticsMapper.getHimawariNumByTime(preTime,nowTime) + dataStatisticsMapper.getModisNumByTime(preTime,nowTime)+
                    dataStatisticsMapper.getLandsatNumByTime(preTime,nowTime)+dataStatisticsMapper.getGLDASNumByTime(preTime,nowTime)+
                    dataStatisticsMapper.getGPMNumByTime(preTime,nowTime)
                    +dataStatisticsMapper.getFYNumByTime(preTime,nowTime);
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            uva = dataStatisticsMapper.getUAVNumByTime(preTime,nowTime);
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            mobile =  getVehicleNumberByTime(preTime,nowTime)+getVesselNumberByTime(preTime,nowTime);
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            other = littleSensorFeignClient.getNumberByTime(preTime,nowTime) ;
        }catch(Exception e){
            e.printStackTrace();
        }
        int num = Station+satellite+uva+mobile+other;
        dataStatistics statistics = new dataStatistics();
        statistics.setCreateTime(nowTime);

        statistics.setJobId("DC_Overview_DataNumberToday");
        JSONObject number = new JSONObject();
        number.put("Overview_DataNumberToday",num);
        statistics.setStatistics(number.toString());
        System.out.println("statistics = " + statistics);
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
        long tmp = getObservationExpandService.getFileSize(new File(ObsConstant.File_Path));
        DecimalFormat df=new DecimalFormat("0.0");//设置保留位数
        String res = df.format((float)tmp/1024/1024/1024/1024)+"T";

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
            query = dataStatisticsMapper.getInterfaceNum("GET");
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            delete = dataStatisticsMapper.getInterfaceNum("DELETE");;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            add =dataStatisticsMapper.getInterfaceNum("GET/POST");;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            update = dataStatisticsMapper.getInterfaceNum("POST");;
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            other = dataStatisticsMapper.getInterfaceNum(" ");;
        }catch(Exception e){
            e.printStackTrace();
        }

        Map station = new LinkedHashMap();


        station.put("查询",query);
        station.put("删除",delete);
        station.put("增加",add);
        station.put("修改",update);
        station.put("其他",other);


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

    @XxlJob("DC_Overview_StorageContent")
    public boolean StorageContent() {
        int station=0;
        int satellite=0;
        int mobile=0;
        int uav=0;
        int product = 0;
        String station_size = null;
        String satellite_size = null;
        String mobile_size = null;
        String uav_size = null;
        String product_size = null;
        try{
            List<String> databases = dataStatisticsMapper.getFilepath("地面站网","file");
            List<String> filePaths = dataStatisticsMapper.getFilepath("地面站网","database");

            if(databases.size()>0) {
                for (String database : databases) {
                    if (database.isEmpty()) {

                    } else {
                        station = station + dataStatisticsMapper.getDatabaseSize(database);
                    }

                }
            }

            if(filePaths.size()>0) {
                for (String filePath : filePaths) {
                    if (filePath.isEmpty()) {

                    } else {
                        long tmp = getObservationExpandService.getFileSize(new File(filePath));
                        station = station + Integer.valueOf((int) tmp);
                    }

                }
            }
            DecimalFormat df=new DecimalFormat("0.0");//设置保留位数
            station_size = df.format((float)station/1024/1024/1024/1024)+"T";
        }catch(Exception e){
            e.printStackTrace();
        }

        try {
            List<String> databases = dataStatisticsMapper.getFilepath("卫星遥感", "file");
            List<String> filePaths = dataStatisticsMapper.getFilepath("卫星遥感", "database");

            if (databases.size() > 0) {
                for (String database : databases) {
                    if (database.isEmpty()) {

                    } else {
                        satellite = satellite + dataStatisticsMapper.getDatabaseSize(database);
                    }

                }
            }

            if (filePaths.size() > 0){
                for (String filePath : filePaths) {
                    if (filePath.isEmpty()) {

                    } else {
                        long tmp = getObservationExpandService.getFileSize(new File(filePath));
                        satellite = satellite + Integer.valueOf((int) tmp);
                    }

                }
            }
            DecimalFormat df=new DecimalFormat("0.0");//设置保留位数
            satellite_size = df.format((float)satellite/1024/1024/1024/1024)+"T";
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            List<String> databases = dataStatisticsMapper.getFilepath("移动车船","file");
            List<String> filePaths = dataStatisticsMapper.getFilepath("移动车船","database");

            if(databases.size()>0){
                for(String database:databases){
                    if(database.isEmpty()){

                    }else{
                        mobile = mobile + dataStatisticsMapper.getDatabaseSize(database);
                    }

                }
            }

            if(filePaths.size()>0){
                for(String filePath:filePaths){
                    if(filePath.isEmpty()){

                    }else{
                        long tmp = getObservationExpandService.getFileSize(new File(filePath));
                        mobile = mobile + Integer.valueOf((int) tmp);
                    }

                }
            }

            DecimalFormat df=new DecimalFormat("0.0");//设置保留位数
            mobile_size = df.format((float)satellite/1024/1024/1024/1024)+"T";
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            List<String> databases = dataStatisticsMapper.getFilepath("低空遥感","file");
            List<String> filePaths = dataStatisticsMapper.getFilepath("低空遥感","database");

            if(databases.size()>0){
                for(String database:databases){
                    if(database.isEmpty()){

                    }else{
                        uav = uav + dataStatisticsMapper.getDatabaseSize(database);
                    }

                }
            }

            if(filePaths.size()>0){
                for(String filePath:filePaths){
                    if(filePath.isEmpty()){

                    }else{
                        long tmp = getObservationExpandService.getFileSize(new File(filePath));
                        uav = uav + Integer.valueOf((int) tmp);
                    }

                }
            }

            DecimalFormat df=new DecimalFormat("0.0");//设置保留位数
            uav_size = df.format((float)satellite/1024/1024/1024/1024)+"T";
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            List<String> databases = dataStatisticsMapper.getFilepath("产品","file");
            List<String> filePaths = dataStatisticsMapper.getFilepath("产品","database");

            if(databases.size()>0){
                for(String database:databases){
                    if(database.isEmpty()){

                    }else{
                        product = product + dataStatisticsMapper.getDatabaseSize(database);
                    }

                }
            }

            if(filePaths.size()>0){
                for(String filePath:filePaths){
                    if(filePath.isEmpty()){

                    }else{
                        long tmp = getObservationExpandService.getFileSize(new File(filePath));
                        product = product + Integer.valueOf((int) tmp);
                    }

                }
            }

            DecimalFormat df=new DecimalFormat("0.0");//设置保留位数
            product_size = df.format((float)satellite/1024/1024/1024/1024)+"T";
        }catch(Exception e){
            e.printStackTrace();
        }

        Map result = new LinkedHashMap();


        result.put("地面站网",station_size);
        result.put("卫星遥感",satellite_size);
        result.put("移动车船",mobile_size);
        result.put("低空遥感",uav_size);
        result.put("产品",product_size);


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
        int HBHJStation=1;
        int CHWeatherStation=1;
        int CHAirStation=1;
        int nasa = 1;
        int usgs  = 1;
        int himawari = 1 ;
        int mobile = 1;
        int littleSensor = 1;
        int GF = 1;
        int tw = 1;
        int fy = 1;
        int tianxingzhou = 1;
        int uav = 1;

        try{
            HBHJStation = dataStatisticsMapper.getHBAirNum() + dataStatisticsMapper.getSuperAirNum()+dataStatisticsMapper.getWaterNum()+
                    dataStatisticsMapper.getWaterQualityNum()+ dataStatisticsMapper.getWaterPollutionNum()+dataStatisticsMapper.getWeatherNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            tw = dataStatisticsMapper.getTWAirNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            CHAirStation =dataStatisticsMapper.getCHWeatherNum()+dataStatisticsMapper.getWeatherNum();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            HBWeatherStation =dataStatisticsMapper.getHBWeatherNum();
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
            CHWeatherStation = dataStatisticsMapper.getWeatherNum() + dataStatisticsMapper.getCHWeatherNum() + dataStatisticsMapper.getHBWeatherNum();
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
            mobile =  getVehicleNumber()+getVesselNumber();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            littleSensor = littleSensorFeignClient.getLittleSensorNumber() ;
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            tianxingzhou = dataStatisticsMapper.getTXZNum() ;
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            fy = dataStatisticsMapper.getFYNum();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            uav = dataStatisticsMapper.getUAVNum();
        }catch(Exception e){
            e.printStackTrace();
        }


        Map sensor = new LinkedHashMap();

        sensor.put("湖北省环境监测站",((int)Math.floor(HBHJStation/10000))+"w");
        sensor.put("中国空气质量网",((int)Math.floor(CHAirStation/10000))+"w");
        sensor.put("台湾ERA空气质量网",((int)Math.floor(tw/10000))+"w");
        sensor.put("湖北气象局",((int)Math.floor(HBWeatherStation/10000))+"w");
        sensor.put("中国气象网",((int)Math.floor(CHWeatherStation/10000))+"w");

        sensor.put("NASA",((int)Math.floor(nasa/10000))+"w");
        sensor.put("USGS",((int)Math.floor(usgs/10000))+"w");
        sensor.put("Himawari卫星官网",((int)Math.floor(himawari/10000))+"w");
        sensor.put("风云卫星遥感数据服务网",((int)Math.floor(fy/10000))+"w");
        sensor.put("高分中心",((float)Math.floor(GF/10000))+"w");

        sensor.put("武大空气传感器",((int)Math.floor(littleSensor/10000))+"w");
        sensor.put("移动车船",((int)Math.floor(mobile/10000))+"w");
        sensor.put("天兴洲综合站",((float)Math.floor(tianxingzhou/10000))+"w");

        sensor.put("无人机",((float)Math.floor(uav/10000))+"w");


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
            WaterStation = dataStatisticsMapper.getWaterQualityNum()+ dataStatisticsMapper.getWaterPollutionNum()+dataStatisticsMapper.getWaterNum();
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

        station.put("总计",((int)Math.floor(num/10000)));
        station.put("空气数据量",((int)Math.floor(AirStation/10000)));
        station.put("气象数据量",((int)Math.floor(WeatherStation/10000)));
        station.put("水质数据量",((int)Math.floor(WaterStation/10000)));
        station.put("超级数据量",((int)Math.floor(SuperStation/10000)));

        station.put("空气站点数量",2351);
        station.put("气象站点数量",5468);
        station.put("水质站点数量",811);
        station.put("超级站点数量",11);

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
        int gpm =1 ;


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
            gpm = dataStatisticsMapper.getGPMNum();
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


        satellite.put("Modis",modis);
        satellite.put("Landsat",landsat);
        satellite.put("葵花",himawari);
        satellite.put("风云",fy);
        satellite.put("高分",GF);
        satellite.put("GLDAS",gldas);
        satellite.put("GPM",gpm);



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

        Map sensor = new LinkedHashMap();

        sensor.put("走航车",Vehicle);
        sensor.put("走航船",Vessel);
        sensor.put("人工采样",sampling);


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

        sensor.put("UHD185",uhd185);
        sensor.put("热红外相机",Thermal_infrared_camera);
        sensor.put("探测仪",detecting_instrument);


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
        try{
            air = dataStatisticsMapper.getProductTypeNum("Air");
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            ecology = dataStatisticsMapper.getProductTypeNum("Ecology");;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            soli =dataStatisticsMapper.getProductTypeNum("Soli");;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            water = dataStatisticsMapper.getProductTypeNum("Water");;
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            other = dataStatisticsMapper.getProductTypeNum(" ");;
        }catch(Exception e){
            e.printStackTrace();
        }

        Map station = new LinkedHashMap();


        station.put("大气",air);
        station.put("水质",water);
        station.put("土壤",soli);
        station.put("生态",ecology);
        station.put("其他",other);


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
        int air=1;
        int ecology=1;
        int soli=1;
        int water=1;
        int other = 1;
        try{
            air = dataStatisticsMapper.getProductNum("Air");
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            ecology = dataStatisticsMapper.getProductNum("Ecology");;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            soli =dataStatisticsMapper.getProductNum("Soli");;
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            water = dataStatisticsMapper.getProductNum("Water");;
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            other = dataStatisticsMapper.getProductNum(" ");;
        }catch(Exception e){
            e.printStackTrace();
        }

        Map station = new LinkedHashMap();


        station.put("大气",air);
        station.put("水质",water);
        station.put("土壤",soli);
        station.put("生态",ecology);
        station.put("其他",other);


        dataStatistics statistics = new dataStatistics();
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        Instant time= DataCenterUtils.string2Instant(formatter.format(dateTime));
        statistics.setCreateTime(time);
        statistics.setJobId("DC_Product_ProductNumber");

        JSONObject number = new JSONObject();
        number.put("Product_ProductNumber", station);
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

        List<Map<String,Object>> res = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
//        System.out.println(df.format(new Date()));
        String timeNow = df.format(new Date());// new Date()为获取当前系统时间

        Instant start = DataCenterUtils.string2Instant(timeNow).minusSeconds(24*60*60);//减24小时
        Instant stop = DataCenterUtils.string2Instant(timeNow);
        System.out.println("start = " + start);


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
                        +dataStatisticsMapper.getFYNumByTime(start,end);
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
                other = littleSensorFeignClient.getNumberByTime(start,end) ;
            }catch(Exception e){
                e.printStackTrace();
            }

            Map<String,Object> tmp = new LinkedHashMap();
            String time = start.plusSeconds(9*60*60).toString();
            tmp.put("地面站网",Station);
            tmp.put("卫星遥感",satellite);
            tmp.put("移动车船",mobile);
            tmp.put("低空遥感",uva);
            tmp.put("其他",other);
            tmp.put("time",time.substring(time.indexOf("T")+1,time.indexOf("Z")).substring(0,2)+"时");
            res.add(tmp);
            start = start.plusSeconds(60*60);
        }

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
        List<Map<String,Object>> res = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String time_now = df.format(new Date());// new Date()为获取当前系统时间
        Instant start = DataCenterUtils.string2Instant(time_now).minusSeconds(24*60*60);//减24小时
        Instant stop = DataCenterUtils.string2Instant(time_now);

        while(start.isBefore(stop)){
            int air=0;
            int ecology=0;
            int soli=0;
            int water=0;
            int other = 0;
            Instant end = start.plusSeconds(60*60);
            try{
                air = dataStatisticsMapper.getProductNumByTime("Air",start,end);

            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                ecology =dataStatisticsMapper.getProductNumByTime("Ecology",start,end);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                soli =dataStatisticsMapper.getProductNumByTime("Soli",start,end);
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                water =dataStatisticsMapper.getProductNumByTime("Water",start,end);
            }catch(Exception e){
                e.printStackTrace();
            }

            try{
                other = dataStatisticsMapper.getProductNumByTime(" ",start,end);
            }catch(Exception e){
                e.printStackTrace();
            }

            Map<String,Object> tmp = new LinkedHashMap();
            String time = start.plusSeconds(9*60*60).toString();
            tmp.put("水质",water);
            tmp.put("土壤",soli);
            tmp.put("大气",air);
            tmp.put("生态",ecology);
            tmp.put("其他",other);
            tmp.put("time",time.substring(time.indexOf("T")+1,time.indexOf("Z")).substring(0,2)+"时");
            res.add(tmp);
            start = start.plusSeconds(60*60);
        }

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
        System.out.println("start = " + start);
        System.out.println("end = " + end);
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

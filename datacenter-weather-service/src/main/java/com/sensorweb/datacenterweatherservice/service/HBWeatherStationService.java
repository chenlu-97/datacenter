package com.sensorweb.datacenterweatherservice.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import com.sensorweb.datacenterutil.utils.SendMail;
import com.sensorweb.datacenterweatherservice.dao.HBWeatherStationMapper;
import com.sensorweb.datacenterweatherservice.dao.WeatherStationMapper;
import com.sensorweb.datacenterweatherservice.entity.HBWeatherStation;
import com.sensorweb.datacenterweatherservice.entity.WeatherStationModel;
import com.sensorweb.datacenterweatherservice.util.OkHttpUtil;
import com.sensorweb.datacenterweatherservice.util.WeatherConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
@EnableScheduling
public class HBWeatherStationService {
    @Autowired
    HBWeatherStationMapper hbWeatherStationMapper;
    @Autowired
    WeatherStationMapper weatherStationMapper;
    @Autowired
    OkHttpUtil okHttpUtil;



    /**
     * 每小时接入一次数据
     */
    @Scheduled(cron = "0 25 0/1 * * ?") //每个小时的25分开始接入
    public void insertDataByHour() {
//        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH0000");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -8);//由于接入时间是utc时间和正常的系统时间差8小时，这里需要减去8小时
        String dateTime = format.format(calendar.getTime());
        LocalDateTime dateTime1 = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        String date = dateTime1.toString().substring(0,dateTime1.toString().indexOf(".")).replace("T"," ");
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean flag = false;
                try {
                    String document = getApiDocument(dateTime);
                    flag= getIOTInfo(document);
//                    flag= getIOTInfo2(document);
                    if (flag) {
                        int num = hbWeatherStationMapper.selectMaxTimeData().size();
                        log.info("湖北气象接入时间: " + date + "         Status: Success");
                        System.out.println("湖北气象接入时间: " + date+ "         Status: Success");
                        DataCenterUtils.sendMessage("HB_Weather_"+date, "站网-湖北省气象","这是一条湖北省气象数据的",num);
                        if(num<2509){
                            int gap = 2509-num;
                            String mes = "接入时间 ："+ date+"-----湖北气象站接入部分缺失（站点数据应为2509个），现在接入为：" + num +"差值为"+ gap;
                            // 发送邮件
//                            SendMail.sendemail(mes);
                            SendException("HB_Weather",date,mes);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                    log.info("湖北气象站接入时间: " + date + "Status: Fail");
                    String mes = "湖北气象接入失败！！----失败时间 ："+ date;
                    // 发送邮件
//                    SendMail.sendemail(mes);
                    SendException("HB_Weather",date,mes);
                }
            }
        }).start();
    }




    public String getApiDocument(String time) throws IOException {
        String param = "userId=" + WeatherConstant.HBWEATHER_STATION_ID +"&pwd=" + WeatherConstant.HBWEATHER_STATION_PASSWORD + "&interfaceId=" + WeatherConstant.HBWEATHER_STATION_INTERFACEID + "&times=" + time + "&dataCode=" + WeatherConstant.HBWEATHER_STATION_DATACODE + "&dataFormat=" + WeatherConstant.HBWEATHER_STATION_DATAFORMAT;
        String document = DataCenterUtils.doGet(WeatherConstant.GET_HBWEATHER_STATION_URL, param);
        return document;
    }

    /**
     * 字符串转Instant  yyyy-MM-dd HH:mm:ss
     * @return
     * @throws IOException
     */
    public Instant str2Instant(String time) {
        if (StringUtils.isBlank(time)) {
            return null;
        }
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        dateTimeFormatter.withZone(ZoneId.of("Asia/Shanghai"));
        LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
        return localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
    }

    /**
     * 通过api请求数据入库
     */

    public boolean getIOTInfo(String document) throws IOException {
        int statue = 0;
        HBWeatherStation hbWeatherStation = new HBWeatherStation();
        JSONObject jsonObject = JSON.parseObject(document);
        JSONArray message = jsonObject.getJSONArray("DS");
        Instant queryTime = str2Instant(jsonObject.getString("responseTime")).plusSeconds(8*60*60).minusSeconds(20*60);
        for (int i = 0; i < message.size(); i++) {
            JSONObject object = message.getJSONObject(i);
            hbWeatherStation.setStationId(object.getString("Station_Name"));
            hbWeatherStation.setLat(Float.parseFloat(object.getString("Lat")));
            hbWeatherStation.setLon(Float.parseFloat(object.getString("Lon")));
            hbWeatherStation.setPrs(Float.parseFloat(object.getString("PRS")));
            hbWeatherStation.setTem(Float.parseFloat(object.getString("TEM")));
            hbWeatherStation.setRhu(Float.parseFloat(object.getString("RHU")));
            hbWeatherStation.setPre_1h(Float.parseFloat(object.getString("PRE_1h")));
            hbWeatherStation.setGst(Float.parseFloat(object.getString("GST")));
            hbWeatherStation.setGst_10cm(Float.parseFloat(object.getString("GST_10cm")));
            hbWeatherStation.setVis(Float.parseFloat(object.getString("VIS")));
            hbWeatherStation.setQueryTime(queryTime);
            statue = hbWeatherStationMapper.insertData(hbWeatherStation);
        }
        return statue>0;
    }

    public List<HBWeatherStation> getDataByPage(int pageNum, int pageSize) {
       return hbWeatherStationMapper.selectByPage(pageNum, pageSize);
    }

    /**
     * 通过api请求站点信息的入库
     */

    public boolean getIOTInfo2(String document) throws IOException {
        int statue = 0;
        WeatherStationModel weatherStationModel = new WeatherStationModel();
        JSONObject jsonObject = JSON.parseObject(document);
        JSONArray message = jsonObject.getJSONArray("DS");
        for (int i = 0; i < message.size(); i++) {
            JSONObject object = message.getJSONObject(i);
            weatherStationModel.setStationId(object.getString("Station_Name"));
            weatherStationModel.setLat(Float.parseFloat(object.getString("Lat")));
            weatherStationModel.setLon(Float.parseFloat(object.getString("Lon")));
            weatherStationModel.setStationName(object.getString("Station_Name"));
            weatherStationModel.setProvince("湖北省");
            weatherStationModel.setStationType("HB_WEATHER");
            weatherStationModel.setGeom("POINT("+Float.parseFloat(object.getString("Lon"))+" "+Float.parseFloat(object.getString("Lat"))+")");
            statue = weatherStationMapper.insertData(weatherStationModel);
        }
        return statue>0;
    }

//    @PostConstruct
    public void insertStation() throws IOException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH0000");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -8);//由于接入时间是utc时间和正常的系统时间差8小时，这里需要减去8小时
        String dateTime = format.format(calendar.getTime());
        String document = getApiDocument(dateTime);
        boolean flag= getIOTInfo2(document);
        log.info("flag------:"+flag);
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

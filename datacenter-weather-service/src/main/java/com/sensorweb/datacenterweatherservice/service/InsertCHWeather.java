package com.sensorweb.datacenterweatherservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import com.sensorweb.datacenterweatherservice.dao.ChinaWeatherMapper;
import com.sensorweb.datacenterweatherservice.dao.WeatherMapper;
import com.sensorweb.datacenterweatherservice.dao.WeatherStationMapper;
import com.sensorweb.datacenterweatherservice.entity.ChinaWeather;
import com.sensorweb.datacenterweatherservice.entity.ChinaWeather2;
import com.sensorweb.datacenterweatherservice.entity.WeatherStationModel;
import com.sensorweb.datacenterweatherservice.util.OkHttpUtil;
import com.sensorweb.datacenterweatherservice.util.WeatherConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
@Service
@EnableScheduling
public class InsertCHWeather {


    @Autowired
    private ChinaWeatherMapper chinaWeatherMapper;

    @Autowired
    private WeatherStationMapper weatherStationMapper;

    @Autowired
    OkHttpUtil okHttpUtil;

    /**
     * 每小时接入一次数据
     */
    @Scheduled(cron = "0 25 0/1 * * ?") //每个小时的20分开始接入
    public void insertDataByHour() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        String date = dateTime.toString().substring(0,dateTime.toString().indexOf(".")).replace("T"," ");
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean flag = false;
                try {
                    flag = getWeatherInfo();
                    if (flag) {
                        log.info("接入中国地面气象站逐小时数据时间: " + date + "Status: Success");
                        System.out.println("接入中国地面气象站逐小时数据时间: " + date + "Status: Success");
                        DataCenterUtils.sendMessage("CH_WEATHER_"+date, "站网-中国气象","这是一条接入中国地面气象站逐小时数据");
                        int num = chinaWeatherMapper.selectMaxTimeData().size();
                        if(num<2169){
                            int gap = 2169-num;
                            String mes = "接入时间 ："+ date+"-----接入中国地面气象站逐小时数据部分缺失（站点数据应为2169），现在接入为：" + num +"差值为"+ gap;
                            // 发送邮件
//                            SendMail.sendemail(mes);
                            SendException("CH_WEATHER",date,mes);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                    log.info("接入中国地面气象站逐小时数据时间: " + date + "Status: Fail");
                    String mes = "接入中国地面气象站逐小时数据接入失败！！----失败时间 ："+ date;
                    // 发送邮件
//                    SendMail.sendemail(mes);
                    SendException("CH_WEATHER",date,mes);
                }
            }
        }).start();
    }

    /**
     * 通过调用中国气象局接口获取对应的气象数据，最后以json对象的形式返回
     */
    public JSONObject getWeatherResultByArea(String areaId,String time) throws IOException {
        String param = "userId=" + WeatherConstant.user_id + "&pwd=" +WeatherConstant.pwd +"&dataFormat=json" +"&interfaceId=getSurfEleByTimeRangeAndStaID"+ "&dataCode=SURF_CHN_MUL_HOR"+
                "&timeRange="+time+ "&staIDs="+areaId+"&elements="+WeatherConstant.element;
        String document = DataCenterUtils.doGet(WeatherConstant.CHINA_WEATHER_URL_2, param);
        if (document!=null) {
            return JSON.parseObject(document);
        }
        return null;
    }


    /**
     * 首先获取气象站点，然后调用API获取气象数据，因每次请求的站点数量不能超过20个，为了提高效率，每次请求20个站点
     */
    public boolean getWeatherInfo() throws IOException, ParseException {
        int res = 0;
        List<WeatherStationModel> weatherStationModels = weatherStationMapper.selectByStationType("CH_WEATHER");
        for (int i=0; i< weatherStationModels.size();) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j<30 && i< weatherStationModels.size(); j++) {
                sb.append(weatherStationModels.get(i).getStationId()).append(",");
                i++;
            }
            String areaIds = sb.substring(0, sb.length()-1);
            String timeRange = getNowTime();
            JSONObject jsonObject = getWeatherResultByArea(URLEncoder.encode(areaIds, "utf-8"),timeRange);
            if (jsonObject!=null) {
                JSONArray observes = jsonObject.getJSONArray("DS");
                if(observes.size()>0){
                    for (int j = 0; j < observes.size(); j++) {
                        JSONObject object = observes.getJSONObject(j);
                        ChinaWeather2 chinaWeather2 = new ChinaWeather2();
                        chinaWeather2.setStationId(object.getString("Station_Id_C"));
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
                        Calendar calendar = Calendar.getInstance();
                        String dateTime = format.format(calendar.getTime());
                        chinaWeather2.setTime(str2Instant(dateTime));
                        chinaWeather2.setWin_s_avg(object.getString("WIN_S_Avg_2mi"));
                        chinaWeather2.setPrs(object.getString("PRS"));
                        chinaWeather2.setPrs_sea(object.getString("PRS_Sea"));
                        chinaWeather2.setPrs_max(object.getString("PRS_Max"));
                        chinaWeather2.setPrs_min(object.getString("PRS_Min"));
                        chinaWeather2.setTem(object.getString("TEM"));
                        chinaWeather2.setTem_max(object.getString("TEM_Max"));
                        chinaWeather2.setTem_min(object.getString("TEM_Min"));
                        chinaWeather2.setRhu(object.getString("RHU"));
                        chinaWeather2.setRhu_min(object.getString("RHU_Min"));
                        chinaWeather2.setVap(object.getString("VAP"));
                        chinaWeather2.setPre_1h(object.getString("PRE_1h"));
                        chinaWeather2.setWin_d_inst_max(object.getString("WIN_D_INST_Max"));
                        chinaWeather2.setWin_s_max(object.getString("WIN_S_Max"));
                        chinaWeather2.setWin_d_s_max(object.getString("WIN_D_S_Max"));
                        chinaWeather2.setWin_s_avg_2mi(object.getString("WIN_S_Avg_2mi"));
                        chinaWeather2.setWin_d_avg_2mi(object.getString("WIN_D_Avg_2mi"));
                        chinaWeather2.setWin_s_inst_max(object.getString("WIN_S_Inst_Max"));
                        chinaWeather2.setWindPower(object.getString("windpower"));
                        res = chinaWeatherMapper.insertData(chinaWeather2);
                    }

                }else{
                    return res>0;
                }
            }
        }
        return res>0;
    }


    /**
     * 根据时间生成日期时间
     * @param
     * @return
     */
    public String getNowTime() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH0000");
        Calendar calendar_start = Calendar.getInstance();
        Calendar calendar_end = Calendar.getInstance();
        calendar_start.add(Calendar.HOUR_OF_DAY, -8);//由于接入时间是utc时间和正常的系统时间差8小时，这里需要减去8小时
        calendar_end.add(Calendar.HOUR_OF_DAY, -7);//由于接入时间是utc时间和正常的系统时间差8小时，这里需要减去8小时
        String start = format.format(calendar_start.getTime());
        String end = format.format(calendar_end.getTime());
        String time = "["+start+","+end+"]";
        return time;
    }

    public Instant str2Instant(String time) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        dateTimeFormatter.withZone(ZoneId.of("Asia/Shanghai"));
        LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
        return localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
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

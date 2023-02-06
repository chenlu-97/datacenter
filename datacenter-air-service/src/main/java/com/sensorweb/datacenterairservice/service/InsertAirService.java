package com.sensorweb.datacenterairservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterairservice.config.OkHttpUtil;
import com.sensorweb.datacenterairservice.dao.AirQualityHourMapper;
import com.sensorweb.datacenterairservice.dao.AirStationMapper;
import com.sensorweb.datacenterairservice.entity.AirQualityDay;
import com.sensorweb.datacenterairservice.entity.AirQualityHour;
import com.sensorweb.datacenterairservice.entity.AirStationModel;
import com.sensorweb.datacenterairservice.feign.ObsFeignClient;
import com.sensorweb.datacenterairservice.feign.SensorFeignClient;
import com.sensorweb.datacenterairservice.util.AirConstant;
import com.sensorweb.datacenterairservice.util.OkHttpUtils.OkHttpRequest;
import com.sensorweb.datacenterairservice.util.timeTransfer.SecondAndDate;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@EnableScheduling
@Component
@RequiredArgsConstructor
public class InsertAirService extends Thread implements AirConstant {
    @Autowired
    private AirQualityHourMapper airQualityHourMapper;

    @Autowired
    private AirStationMapper airStationMapper;

    @Autowired
    private SensorFeignClient sensorFeignClient;

    @Autowired
    private ObsFeignClient obsFeignClient;

    @Autowired
    OkHttpUtil okHttpUtil;



    /**
     * 每小时接入一次数据
     */
    @Scheduled(cron = "0 25 0/1 * * ?") //每个小时的25分开始接入
    public void insertDataByHour() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00").withZone(ZoneId.of("Asia/Shanghai"));
        String time = formatter.format(dateTime);
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean flag = true;
                while (flag) {
                    try {
                        flag = !insertHourDataByHour(time);
                        if (!flag) {
                            int num = airQualityHourMapper.selectMaxTimeData().size();
                            log.info("湖北省监测站接入时间: " + time + "Status: Success");
                            DataCenterUtils.sendMessage("HB_AIR"+ time, "站网-湖北省空气质量","这是一条省站推送的湖北省空气质量数据",num);
                            System.out.println("湖北省监测站接入时间: " + time + "Status: Success");
                            if(num!=273){
                                int gap = 273-num;
                                String mes = "接入时间 ："+ time+"------湖北省监测站接入部分缺失（站点数据应为273），现在接入为：" + num +"差值为"+ gap;
                                // 发送邮件
//                                SendMail.sendemail(mes);
                                SendException("HB_AIR",time,mes);
                            }
                        } else {
                            System.out.println("湖北省监测站，等待中...");
                        }
                        Thread.sleep(2 * 60 * 1000);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        log.info("湖北省监测站接入时间: " + time + "Status: Fail");
                        String mes = "湖北省监测站接入失败！！----失败时间 ："+ time;
                        // 发送邮件
//                        SendMail.sendemail(mes);
                        SendException("HB_AIR",time,mes);
                        break;
                    }
                }
            }
        }).start();
    }


    /**
     * 每小时接入一次数据
     */
//    @Scheduled(cron = "0 40 0/1 * * ?") //每个小时的35分开始接入
//    @PostConstruct
    public void insertDataByHour2() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00").withZone(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00").withZone(ZoneId.of("Asia/Shanghai"));
        LocalDateTime endDate = dateTime.plusMinutes(24);
        String start = formatter.format(dateTime);
        String end = formatter1.format(endDate);
//        String start = "2020-01-01 00:00:00";
//        String end = "2020-01-01 23:59:59";
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean flag ;
//                while (flag) {
                    try {
                        JSONArray access_res = getInfo(start,end,getStationId());
                        flag = !accessinforming(access_res);
                        if (!flag) {
                            log.info("湖北省监测站更新空气数据时间: " + start + "Status: Success");
//                            DataCenterUtils.sendMessage("HB_water"+ start, "站网-湖北省水质站点","这是一条省站推送的湖北省水质站点数据");
                            System.out.println("湖北省监测站更新空气数据时间: " + start + "Status: Success");
                        }
                        else {
                            System.out.println("等待中...");
                        }
//                        Thread.sleep(2 * 60 * 1000);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        log.info("湖北省监测站更新空气数据时间: " + start.toString() + "Status: Fail");
                        String mes = "湖北省监测站更新空气数据失败！！----失败时间 ："+ start;
                        // 发送邮件
//                        SendMail.sendemail(mes);
//                        SendException("HB_Water",start.toString(),mes);
//                        break;
//                    }
                }
            }
        }).start();
    }

    /**
     * 从okhttp获取站点信息
     * */
    public JSONArray getInfo(String start, String end,String Ids){
        long startSecond = SecondAndDate.datatosecond(start);
        long endSecond = SecondAndDate.datatosecond(end);

        JSONObject body_content = new JSONObject();

        HashMap content1 = new HashMap<String, String>();
        HashMap content2 = new HashMap<String, String>();
        content1.put("siteCode", Ids);
        content1.put("startTime", startSecond + "");
        content1.put("endTime", endSecond + "");

        content2.put("size", "1000000");
        content2.put("page", "0");
        body_content.put("params", content1);
        body_content.put("page", content2);
        JSONArray access_res = OkHttpRequest.accessAirStationData(body_content);
        return access_res;
    }

    public String getStationId(){
        int flag = 0;
        List<String> res = airStationMapper.quaryStationId();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < res.size(); j++) {
            sb.append(res.get(j)).append(",");
        }
        String Ids = sb.substring(0, sb.length() - 1);
        return Ids;
    }

    public boolean accessinforming(JSONArray access_res) {
        List<String> res = airStationMapper.quaryStationId();
            int flag = 0 ;
            if (access_res != null && access_res.size() > 0) {
                for (int i = 0; i < res.size(); i++) {
                AirQualityHour airQualityHour = new AirQualityHour();
                String station_id = res.get(i);
                for (int j = 0; j < access_res.size(); j++) {
                    JSONObject access = (JSONObject) access_res.get(j);
                    if (access.getString("uniqueCode").equals(station_id)) {
                        String fieldName = access.getString("epName");
                        if (fieldName.equals("NO")) {
                            Float no = Float.valueOf(access.getString("sValue")) * 1000;
                            airQualityHour.setNoOneHour(no.toString().substring(0, no.toString().indexOf(".")));
                        } else if (fieldName.equals("NOX")) {
                            Float nox = Float.valueOf(access.getString("sValue")) * 1000;
                            airQualityHour.setNoxOneHour(nox.toString().substring(0, nox.toString().indexOf(".")));
                        } else if (fieldName.equals("温度")) {
                            airQualityHour.setTemperature(access.getString("sValue"));
                        } else if (fieldName.equals("湿度")) {
                            airQualityHour.setHumidity(access.getString("sValue"));
                        } else if (fieldName.equals("风向")) {
                            airQualityHour.setWindDirection(access.getString("sValue"));
                        } else if (fieldName.equals("风速")) {
                            airQualityHour.setWindSpeed(access.getString("sValue"));
                        } else if (fieldName.equals("能见度")) {
                            airQualityHour.setVisibility(access.getString("sValue"));
                        } else if (fieldName.equals("SO2")) {
                            Float so2 = Float.valueOf(access.getString("sValue")) * 1000;
                            airQualityHour.setSo2OneHour(so2.toString().substring(0, so2.toString().indexOf(".")));
                        } else if (fieldName.equals("NO2")) {
                            Float no2 = Float.valueOf(access.getString("sValue")) * 1000;
                            airQualityHour.setNo2OneHour(no2.toString().substring(0, no2.toString().indexOf(".")));
                        } else if (fieldName.equals("CO")) {
                            airQualityHour.setCoOneHour(access.getString("sValue"));
                        } else if (fieldName.equals("O3")) {
                            Float o3 = Float.valueOf(access.getString("sValue")) * 1000;
                            airQualityHour.setO3OneHour(o3.toString().substring(0, o3.toString().indexOf(".")));
                        } else if (fieldName.equals("PM25")) {
                            Float pm25 = Float.valueOf(access.getString("sValue")) * 1000;
                            airQualityHour.setPm25OneHour(pm25.toString().substring(0, pm25.toString().indexOf(".")));
                        } else if (fieldName.equals("PM10")) {
                            Float pm10 = Float.valueOf(access.getString("sValue")) * 1000;
                            airQualityHour.setPm10OneHour(pm10.toString().substring(0, pm10.toString().indexOf(".")));
                        }
                        airQualityHour.setUniqueCode(access.getString("uniqueCode"));
                        airQualityHour.setStationName(access.getString("sStationName"));
                        long data = Long.parseLong(access.getString("sDatetime"));
                        airQualityHour.setQueryTime(SecondAndDate.sceondtodata(data).toInstant());
                    }
                }

                try {
                    if (airQualityHour.getUniqueCode() == null) {
                    } else {
                        flag =  airQualityHourMapper.insertData(airQualityHour);
                    }
                } catch (Exception e) {
                        e.printStackTrace();
                }
            }
        }
        return flag>0;
    }


    /**
     * 根据时间接入指定时间的小时数据（当数据库中缺少某个时间段的数据时，可作为数据补充）
     * @throws Exception
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertHourDataByHour(String time) throws Exception {
        int status = 0 ;
        String param = "UsrName=" + AirConstant.USER_NAME + "&passWord=" + AirConstant.PASSWORD +
                "&date=" + URLEncoder.encode(time, "utf-8");
        String document = DataCenterUtils.doGet(AirConstant.GET_LAST_HOURS_DATA, param);
        List<Object> objects = parseXmlDoc(document);
        if (objects.size()==0) {
            return false;
        }
        //将环境监测站数据
        for (Object o : objects) {
            if (o instanceof AirQualityHour) {
                AirQualityHour airQualityHour = (AirQualityHour) o;
                status = airQualityHourMapper.insertData(airQualityHour);
//                if (status>0) {
//                    Observation observation = new Observation();
//                    observation.setProcedureId(airQualityHour.getUniqueCode());
//                    observation.setObsTime(airQualityHour.getQueryTime());
//                    observation.setMapping("air_quality_hourly");
//                    observation.setObsProperty("AirQuality");
//                    observation.setType("HB_AIR");
//                    AirStationModel airStationModel = airStationMapper.selectByStationId(airQualityHour.getUniqueCode()).get(0);
//                    if (airStationModel!=null) {
//                        String wkt = "POINT(" + airStationModel.getLon() + " " + airStationModel.getLat() + ")";
//                        observation.setWkt(wkt);
//                        String bbox = airStationModel.getLon() + " " + airStationModel.getLat() + "," + airStationModel.getLon() + " " + airStationModel.getLat();
//                        observation.setBbox(bbox);
//
//                        boolean isWH = obsFeignClient.insertBeforeSelectWHSpa(wkt);
//                        boolean isCJ = obsFeignClient.insertBeforeSelectCJSpa(wkt);
//                        if(isWH==true&&isCJ==true){
//                            observation.setGeoType(1);
//                        }
//                        else if(isWH==false&&isCJ==true){
//                            observation.setGeoType(2);
//                        }else{
//                            observation.setGeoType(3);
//                        }
//
//                    }
//                    observation.setBeginTime(getResultTime(airQualityHour));
//                    observation.setEndTime(getResultTime(airQualityHour).plusSeconds(60*60));
////                    observation.setOutId(airQualityHour.getId());
//                    boolean flag = true;
////                    try {
////                        flag = sensorFeignClient.isExist(observation.getProcedureId());
////                        System.out.println(flag);
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                    }
//
//                    if (flag) {
//                        try{
//                            if(observation != null)
//                            obsFeignClient.insertData(observation);
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    } else {
//                        log.info("procedure:" + observation.getProcedureId() + "不存在");
//                    }
//                }
            } else {
//                return false;
            }
        }
        return status>0;
    }

    /**
     * 根据不同的数据模型，获取对应的时间数据
     * @param object
     * @return
     */
    public Instant getResultTime(Object object) {
        Instant res = null;
        if (object instanceof AirQualityDay) {
            res = ((AirQualityDay) object).getQueryTime();
        } else {
            res = ((AirQualityHour) object).getQueryTime();
        }
        return res;
    }

    /**
     * 解析湖北省监测站响应的xml文档
     * @param document
     * @return
     * @throws Exception
     */
    public List<Object> parseXmlDoc(String document) throws Exception {
        //读取xml文档，返回Document对象
        Document xmlContent = DocumentHelper.parseText(document);
        Element root = xmlContent.getRootElement();
        String type = root.getName().substring(root.getName().lastIndexOf("_")+1);
        List<Object> objects = new ArrayList<>();
        //判断返回数据类型，选择对应的数据模型
        switch (type) {
            case "HourAqiModel":
            case "IHourAqiModel": {
                if (!root.elementIterator().hasNext()) {
                    return objects;
                }
                for (Iterator i = root.elementIterator(); i.hasNext();) {
                    Element element = (Element) i.next();
                    objects.add(getHourAqiInfo(element));
                }
                break;
            }
            case "DayAqiModel":
            case "IDayAqiModel":
            case "CDayModel": {
                if (!root.elementIterator().hasNext()) {
                    return objects;
                }
                for (Iterator i = root.elementIterator(); i.hasNext();) {
                    Element element = (Element) i.next();
                    objects.add(getDayAqiInfo(element));
                }
                break;
            }
        }
        return objects;
    }

    /**
     * 解析Element要素节点，获取相关信息，包装成AirQualityHour模型返回
     * @param hourAqiElement
     * @return
     */
    public AirQualityHour getHourAqiInfo(Element hourAqiElement) throws ParseException {
        AirQualityHour airQualityHour = new AirQualityHour();
        for (Iterator j = hourAqiElement.elementIterator(); j.hasNext();) {
            Element attribute = (Element) j.next();
            if (attribute.getName().equals("UniqueCode")) {
                airQualityHour.setUniqueCode(attribute.getText());
                continue;
            }
            if (attribute.getName().equals("QueryTime")) {
                airQualityHour.setQueryTime(DataCenterUtils.string2LocalDateTime2(attribute.getText()).toInstant(ZoneOffset.ofHours(+8)));
                continue;
            }
            if (attribute.getName().equals("StationName")) {
                airQualityHour.setStationName(attribute.getText());
                continue;
            }
            if (attribute.getName().equals("PM25OneHour")) {
                airQualityHour.setPm25OneHour(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("PM10OneHour")) {
                airQualityHour.setPm10OneHour(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("SO2OneHour")) {
                airQualityHour.setSo2OneHour(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("NO2OneHour")) {
                airQualityHour.setNo2OneHour(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("COOneHour")) {
                airQualityHour.setCoOneHour(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("O3OneHour")) {
                airQualityHour.setO3OneHour(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("AQI")) {
                airQualityHour.setAqi(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("PrimaryEP")) {
                airQualityHour.setPrimaryEP(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("AQDegree")) {
                airQualityHour.setAqDegree(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("AQType")) {
                airQualityHour.setAqType(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
            }
        }
        return airQualityHour;
    }

    /**
     * 解析Element要素节点，获取相关信息，包装成AirQualityDay模型返回
     * @param hourAqiElement
     * @return
     */
    public AirQualityDay getDayAqiInfo(Element hourAqiElement) throws ParseException {
        AirQualityDay airQualityDay = new AirQualityDay();
        for (Iterator j = hourAqiElement.elementIterator(); j.hasNext();) {
            Element attribute = (Element) j.next();
            if (attribute.getName().equals("UniqueCode")) {
                airQualityDay.setUniqueCode(attribute.getText());
                continue;
            }
            if (attribute.getName().equals("QueryTime") || attribute.getName().equals("SDateTime")) {
                airQualityDay.setQueryTime(DataCenterUtils.string2LocalDateTime2(attribute.getText()).toInstant(ZoneOffset.ofHours(+8)));
                continue;
            }
            if (attribute.getName().equals("StationName")) {
                airQualityDay.setStationName(attribute.getText());
                continue;
            }
            if (attribute.getName().equals("PM25")) {
                airQualityDay.setPm25(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("PM10")) {
                airQualityDay.setPm10(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("SO2")) {
                airQualityDay.setSo2(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("NO2")) {
                airQualityDay.setNo2(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("CO")) {
                airQualityDay.setCo(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("O3EightHour")) {
                airQualityDay.setO3EightHour(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
                continue;
            }
            if (attribute.getName().equals("AQI")) {
                airQualityDay.setO3EightHour(attribute.getText().equals("NA")||StringUtils.isBlank(attribute.getText()) ? -1+"":attribute.getText());
            }
        }
        return airQualityDay;
    }

    /**
     * 将湖北省环境监测站站点注册到数据库
     * @param objects
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertWHAirStationInfo(List<Object> objects) throws IOException {
        List<AirStationModel> res = new ArrayList<>();
        if (objects!=null && objects.size()>0) {
            if (objects.get(0) instanceof AirQualityHour) {
                for (Object object : objects) {
                    AirStationModel airStationModel = new AirStationModel();
                    AirQualityHour airQualityHour = (AirQualityHour) object;
                    airStationModel.setStationId(airQualityHour.getUniqueCode());
                    airStationModel.setStationName(airQualityHour.getStationName());
                    JSONObject jsonObject = null;
                    if (airStationModel.getStationName()!=null) {
                        jsonObject = getGeoAddress(airStationModel.getStationName());
                    }
                    if (jsonObject!=null) {
                        Object result = jsonObject.get("result");
                        if (result!=null) {
                            Object location = ((JSONObject) result).get("location");
                            airStationModel.setLon(((JSONObject) location).getFloatValue("lng"));
                            airStationModel.setLat(((JSONObject) location).getFloatValue("lat"));
                        }
                    }
                    airStationModel.setStationType("HB_AIR");
                    res.add(airStationModel);
                }
            } else if (objects.get(0) instanceof AirQualityDay) {
                for (Object object : objects) {

                }
            }
        }
        int status = airStationMapper.insertDataBatch(res);
        return status > 0;
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

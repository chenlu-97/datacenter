package com.sensorweb.datacenterairservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterairservice.dao.AirStationMapper;
import com.sensorweb.datacenterairservice.dao.ChinaAirQualityHourMapper;
import com.sensorweb.datacenterairservice.entity.AirStationModel;
import com.sensorweb.datacenterairservice.entity.ChinaAirQualityHour;
import com.sensorweb.datacenterairservice.util.AirConstant;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@EnableScheduling
public class NewInsertChinaAirService extends Thread implements AirConstant {

    @Autowired
    private AirStationMapper airStationMapper;

    @Autowired
    private ChinaAirQualityHourMapper chinaAirQualityHourMapper;

    @Value("${datacenter.excel.path}")
    private String excelPath;



    /**
     * 每小时接入一次数据
     */
//    @Scheduled(cron = "0 35 0/1 * * ?") //每个小时的35分开始接入
    public void insertChinaDataByHour() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00").withZone(ZoneId.of("Asia/Shanghai"));
        String time = formatter.format(dateTime);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flag;
                try {
                    flag =  getFromCSV2();
                    if (flag) {
                        log.info("全国空气质量监测站接入时间: " + dateTime.toString() + "Status: Success");
                        DataCenterUtils.sendMessage("CH_AIR"+ time, "站网-全国空气质量","这是一条获取的全国空气质量数据");
                        System.out.println("全国空气质量监测站接入时间: " + dateTime.toString() + "Status: Success");
                    }
                } catch (Exception e) {
                    log.info("全国空气质量监测站接入时间: " + dateTime.toString() + "Status: Fail");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获取城市csv中站点id和城市经纬度，并根据id去访问获取每个城市的站点监测内容（根据行政区划）
     * @return
     * @throws
     */
//    public boolean getFromCSV(){
//        List<ChinaAirQualityHour> listChinaAirQualityHour = new ArrayList<>();
//        boolean flag = false;
//        int statue = 0 ;
//        String tmpDir = "/Users/chenlu/Desktop/test/China-City-List-latest.xlsx";
//        File excel = new File(tmpDir);
//        try {
//            if (excel.isFile() && excel.exists()) {   //判断文件是否存在
//                String[] split = excel.getName().split("\\.");  //.是特殊字符，需要转义！！！！！
//                Workbook wb;
//                //根据文件后缀（xls/xlsx）进行判断
//                if ( "xls".equals(split[1])){
//                    FileInputStream fis = new FileInputStream(excel);   //文件流对象
//                    wb = new HSSFWorkbook(fis);
//                }else if ("xlsx".equals(split[1])){
//                    wb = new XSSFWorkbook(excel);
//                }else {
//                    System.out.println("文件类型错误!");
//                    return flag;
//                }
//                //开始解析
//                Sheet sheet = wb.getSheetAt(0);     //读取sheet 0
//
//                int firstRowIndex = sheet.getFirstRowNum()+1;   //第一行是列名，所以不读
//                int lastRowIndex = sheet.getLastRowNum();
//                for(int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
//                    Row row = sheet.getRow(rIndex);
//                    String StationID = null;
//                    String StationName = null;
//                    String Area = null;
//                    Double lon = null;
//                    Double lat = null;
//                    if (row != null) {
//                        Cell StationIDCell = row.getCell(0);
//                        if (StationIDCell!=null) {
//                            DecimalFormat df = new DecimalFormat("0");
//                            StationID = df.format(StationIDCell.getNumericCellValue());
//                        }
//                        Cell StationNameCell = row.getCell(2);
//                        if (StationNameCell!=null) {
//                            StationName = StationNameCell.getStringCellValue();
//                        }
//                        Cell AreaCell = row.getCell(7);
//                        if (AreaCell!=null) {
//                            Area = AreaCell.getStringCellValue();
//                        }
//                        Cell latCell = row.getCell(11);
//                        if (latCell!=null) {
//                            lat = latCell.getNumericCellValue();
//                        }
//                        Cell lonCell = row.getCell(12);
//                        if (lonCell!=null) {
//                            lon = lonCell.getNumericCellValue();
//                        }
//                        System.out.println("StationID = " + StationID);
//                        String str = getApiDocument(StationID);
//                        ChinaAirQualityHour chinaAirQualityHour = getChinaAQHInfo(str);
////                        System.out.println("chinaAirQualityHour = " + chinaAirQualityHour);
//                        chinaAirQualityHour.setStationCode(String.valueOf(StationID));
//                        chinaAirQualityHour.setPositionName(StationName);
//                        chinaAirQualityHour.setArea(Area);
//                        chinaAirQualityHour.setLat(String.valueOf(lat));
//                        chinaAirQualityHour.setLng(String.valueOf(lon));
//                        chinaAirQualityHour.setTime(Long.valueOf(getTime()));
//                        statue = chinaAirQualityHourMapper.insert(chinaAirQualityHour);
////                        listChinaAirQualityHour.add(chinaAirQualityHour);
//                    }
//                }
////                return flag = insertDataByHour(listChinaAirQualityHour);
//            } else {
//                System.out.println("找不到指定的文件");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return statue>0;
//    }

    /**
     * 获取城市csv中站点id和城市经纬度，并根据id去访问获取每个城市的站点监测内容(根据站点获取)
     * @return
     * @throws
     */
    public boolean getFromCSV2(){
        boolean flag = false;
        int status = 0 ;
        File excel = new File(excelPath);
        try {
            if (excel.isFile() && excel.exists()) {   //判断文件是否存在
                String[] split = excel.getName().split("\\.");  //.是特殊字符，需要转义！！！！！
                Workbook wb;
                //根据文件后缀（xls/xlsx）进行判断
                if ( "xls".equals(split[1])){
                    FileInputStream fis = new FileInputStream(excel);   //文件流对象
                    wb = new HSSFWorkbook(fis);
                }else if ("xlsx".equals(split[1])){
                    wb = new XSSFWorkbook(excel);
                }else {
                    System.out.println("文件类型错误!");
                    return flag;
                }
                //开始解析
                Sheet sheet = wb.getSheetAt(0);     //读取sheet 0
                int firstRowIndex = sheet.getFirstRowNum()+1;   //第一行是列名，所以不读
                int lastRowIndex = sheet.getLastRowNum();
                for(int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
                    Row row = sheet.getRow(rIndex);
                    String StationID = null;
                    String CityName = null;
                    String Area = null;
                    Double lon = null;
                    Double lat = null;
                    if (row != null) {
                        Cell StationIDCell = row.getCell(0);
                        if (StationIDCell!=null) {
                            DecimalFormat df = new DecimalFormat("0");
                            StationID = df.format(StationIDCell.getNumericCellValue());
                        }
                        Cell CityNameCell = row.getCell(9);
                        if (CityNameCell!=null) {
                            CityName = CityNameCell.getStringCellValue();
                        }
                        Cell AreaCell = row.getCell(7);
                        if (AreaCell!=null) {
                            Area = AreaCell.getStringCellValue();
                        }
                        Cell latCell = row.getCell(11);
                        if (latCell!=null) {
                            lat = latCell.getNumericCellValue();
                        }
                        Cell lonCell = row.getCell(12);
                        if (lonCell!=null) {
                            lon = lonCell.getNumericCellValue();
                        }

                        String str = null;
                        //调用接口获取站点信息
                        try {
                            if(StationID != null) {
                                str = getApiDocument(StationID);
                            }
                        }catch (Exception e){
                            System.out.println("获取站点信息出错！！！！");
                            e.printStackTrace();
                        }
                        //解析站点信息并进行插入操作
                        try {
                            if (!StringUtils.isBlank(str)) {
                                JSONObject jsonObjects = JSON.parseObject(str);
                                JSONArray station = jsonObjects.getJSONArray("station");
                                if(station == null) {
                                    ChinaAirQualityHour chinaAirQualityHour = getChinaAQHInfo(str);
                                    chinaAirQualityHour.setStationCode(StationID);
                                    chinaAirQualityHour.setPositionName(CityName);
                                    chinaAirQualityHour.setArea(Area);
                                    chinaAirQualityHour.setLat(String.valueOf(lat));
                                    chinaAirQualityHour.setLng(String.valueOf(lon));
                                    chinaAirQualityHour.setTime(Long.valueOf(getTime()));
//                                    System.out.println("获取的站点为----------------：" + CityName);
                                    status = chinaAirQualityHourMapper.insert(chinaAirQualityHour);
                                }else{
                                    List<ChinaAirQualityHour> chinaAirQualityHours = getChinaAQHInfo2(str);
                                    for (int i = 0; i < chinaAirQualityHours.size(); i++) {
                                        JSONObject jsonObject = null;
                                        if (chinaAirQualityHours.get(i) != null) {
                                            jsonObject = getGeoAddress(CityName + chinaAirQualityHours.get(i).getPositionName());
//                                            System.out.println("获取的站点为：" + CityName + chinaAirQualityHours.get(i).getPositionName());
                                        }
                                        if (jsonObject != null) {
                                            Object result = jsonObject.get("result");
                                            if (result != null) {
                                                Object location = ((JSONObject) result).get("location");
                                                chinaAirQualityHours.get(i).setLng(String.valueOf(((JSONObject) location).getFloatValue("lng")));
                                                chinaAirQualityHours.get(i).setLat(String.valueOf(((JSONObject) location).getFloatValue("lat")));
                                            }
                                        }
                                        chinaAirQualityHours.get(i).setArea(Area);
                                        chinaAirQualityHours.get(i).setPositionName(CityName + chinaAirQualityHours.get(i).getPositionName());
                                        chinaAirQualityHours.get(i).setTime(Long.valueOf(getTime()));
                                        status = chinaAirQualityHourMapper.insert(chinaAirQualityHours.get(i));
                                    }
                                }
                            }
                        }catch (Exception e) {
                            System.out.println("解析站点信息或者插入信息出错！！！！");
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status>0;
    }


    public String getApiDocument(String stationID) throws IOException, InterruptedException {
        String param = "location=" + stationID + "&key=" + AirConstant.token1;
        try{
            return DataCenterUtils.doGet(AirConstant.Get_China_air_data_hourly, param);
        }catch (Exception e){
            Thread.sleep( 1000 );
            return DataCenterUtils.doGet(AirConstant.Get_China_air_data_hourly, param);
        }

    }

    /**
     * 直接从城市空气返回的json中获取信息
     * @param
     */
    public ChinaAirQualityHour getChinaAQHInfo(String document) {
        ChinaAirQualityHour chinaAirQualityHour = new ChinaAirQualityHour();
        if (!StringUtils.isBlank(document)) {
//            JSONArray jsonObjects = JSON.parseArray(document);
            JSONObject jsonObjects = JSON.parseObject(document);
            JSONObject information = jsonObjects.getJSONObject("now");
            if(information!=null){
                chinaAirQualityHour.setAqi(Integer.valueOf(information.getString("aqi")));
                chinaAirQualityHour.setQuality(information.getString("category"));
                chinaAirQualityHour.setPrimaryPollutant(information.getString("primary"));
                chinaAirQualityHour.setPm10(Integer.valueOf(information.getString("pm10")));
                chinaAirQualityHour.setPm25(Integer.valueOf(information.getString("pm2p5")));
                chinaAirQualityHour.setNo2(Integer.valueOf(information.getString("no2")));
                chinaAirQualityHour.setSo2(Integer.valueOf(information.getString("so2")));
                chinaAirQualityHour.setCo(Float.valueOf(information.getString("co")));
                chinaAirQualityHour.setO3(Integer.valueOf(information.getString("o3")));
                chinaAirQualityHour.setTimePoint(str2Instant(information.getString("pubTime")));
            }
        }
        return chinaAirQualityHour;
    }

    /**
     * 根据获取的城市空气json信息来获取里面的station参数
     * @param
     */
    public List<ChinaAirQualityHour> getChinaAQHInfo2(String document) {
        List<ChinaAirQualityHour> res = new ArrayList<>();
        if (!StringUtils.isBlank(document)) {
            JSONObject jsonObjects = JSON.parseObject(document);
            JSONArray station = jsonObjects.getJSONArray("station");
            if(station !=null) {
                for (int i = 0; i < station.size(); i++) {
                        ChinaAirQualityHour chinaAirQualityHour = new ChinaAirQualityHour();
                        JSONObject feed = station.getJSONObject(i);
                    if(!feed.getString("name").isEmpty()) { //过滤掉站点名为空的
                        chinaAirQualityHour.setStationCode(feed.getString("id"));
                        chinaAirQualityHour.setPositionName(feed.getString("name"));
                        if (!feed.getString("aqi").isEmpty()) { //valueOf容易为空报错，需要先判断是不是为空
                            chinaAirQualityHour.setAqi(Integer.valueOf(feed.getString("aqi")));
                        }
                        if (!feed.getString("category").isEmpty()) {
                            chinaAirQualityHour.setQuality(feed.getString("category"));
                        }
                        chinaAirQualityHour.setPrimaryPollutant(feed.getString("primary"));
                        if (!feed.getString("pm10").isEmpty()) {
                            chinaAirQualityHour.setPm10(Integer.valueOf(feed.getString("pm10")));
                        }
                        if (!feed.getString("pm2p5").isEmpty()) {
                            chinaAirQualityHour.setPm25(Integer.valueOf(feed.getString("pm2p5")));
                        }
                        if (!feed.getString("no2").isEmpty()) {
                            chinaAirQualityHour.setNo2(Integer.valueOf(feed.getString("no2")));
                        }
                        if (!feed.getString("so2").isEmpty()) {
                            chinaAirQualityHour.setSo2(Integer.valueOf(feed.getString("so2")));
                        }
                        if (!feed.getString("co").isEmpty()) {
                            chinaAirQualityHour.setCo(Float.valueOf(feed.getString("co")));
                        }
                        if (!feed.getString("o3").isEmpty()) {
                            chinaAirQualityHour.setO3(Integer.valueOf(feed.getString("o3")));
                        }
                        chinaAirQualityHour.setTimePoint(str2Instant(feed.getString("pubTime")));
                        res.add(chinaAirQualityHour);
                    }
                }
            }
        }
        return res;
    }



    /**
     * 注册站点的方法
     * @return
     * @throws
     */
    public boolean getStation(){
        boolean flag = false;
        List<ChinaAirQualityHour> chinaAirQualityHours = new ArrayList<>();
        try {
            File excel = new File(excelPath);
            try {
                if (excel.isFile() && excel.exists()) {   //判断文件是否存在
                    String[] split = excel.getName().split("\\.");  //.是特殊字符，需要转义！！！！！
                    Workbook wb = null;
                    //根据文件后缀（xls/xlsx）进行判断
                    if ("xls".equals(split[1])) {
                        FileInputStream fis = new FileInputStream(excel);   //文件流对象
                        wb = new HSSFWorkbook(fis);
                    } else if ("xlsx".equals(split[1])) {
                        wb = new XSSFWorkbook(excel);
                    } else {
                        System.out.println("文件类型错误!");
                    }
                    //开始解析
                    Sheet sheet = wb.getSheetAt(0);     //读取sheet 0

                    int firstRowIndex = sheet.getFirstRowNum() + 1;   //第一行是列名，所以不读
                    int lastRowIndex = sheet.getLastRowNum();
                    for (int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
                        Row row = sheet.getRow(rIndex);
                        String StationID = null;
                        String CityName = null;
                        String Area = null;
                        Double lon = null;
                        Double lat = null;
                        if (row != null) {
                            Cell StationIDCell = row.getCell(0);
                            if (StationIDCell != null) {
                                DecimalFormat df = new DecimalFormat("0");
                                StationID = df.format(StationIDCell.getNumericCellValue());
                            }
                            Cell CityNameCell = row.getCell(9);
                            if (CityNameCell != null) {
                                CityName = CityNameCell.getStringCellValue();
                            }
                            Cell AreaCell = row.getCell(7);
                            if (AreaCell != null) {
                                Area = AreaCell.getStringCellValue();
                            }
                            Cell latCell = row.getCell(11);
                            if (latCell != null) {
                                lat = latCell.getNumericCellValue();
                            }
                            Cell lonCell = row.getCell(12);
                            if (lonCell != null) {
                                lon = lonCell.getNumericCellValue();
                            }
                        }
                        String document = getApiDocument(StationID);
                        if (!StringUtils.isBlank(document)) {
                            JSONObject jsonObjects = JSON.parseObject(document);
                            JSONArray station = jsonObjects.getJSONArray("station");
                            if (station == null) {
                                ChinaAirQualityHour chinaAirQualityHour = getChinaAQHInfo(document);
                                chinaAirQualityHour.setStationCode(StationID);
                                chinaAirQualityHour.setPositionName(CityName);
                                chinaAirQualityHour.setArea(Area);
                                chinaAirQualityHour.setLat(String.valueOf(lat));
                                chinaAirQualityHour.setLng(String.valueOf(lon));
                                chinaAirQualityHours.add(chinaAirQualityHour);

                            }else {
                                chinaAirQualityHours = getChinaAQHInfo2(document);
                                for (int i = 0; i < chinaAirQualityHours.size(); i++) {
                                    JSONObject jsonObject = null;
                                    if (chinaAirQualityHours.get(i) != null) {
                                        jsonObject = getGeoAddress(CityName + chinaAirQualityHours.get(i).getPositionName());
                                    }
                                    if (jsonObject != null) {
                                        Object result = jsonObject.get("result");
                                        if (result != null) {
                                            Object location = ((JSONObject) result).get("location");
                                            chinaAirQualityHours.get(i).setLng(String.valueOf(((JSONObject) location).getFloatValue("lng")));
                                            chinaAirQualityHours.get(i).setLat(String.valueOf(((JSONObject) location).getFloatValue("lat")));
                                        }
                                    }
                                    chinaAirQualityHours.get(i).setArea(Area);
                                    chinaAirQualityHours.get(i).setPositionName(CityName + chinaAirQualityHours.get(i).getPositionName());
                                }
                            }

                            for (ChinaAirQualityHour chinaAirQualityHour : chinaAirQualityHours) {
                                AirStationModel airStationModel = new AirStationModel();
                                airStationModel.setStationId(chinaAirQualityHour.getStationCode());
                                airStationModel.setStationName(chinaAirQualityHour.getPositionName());
                                airStationModel.setProvince(chinaAirQualityHour.getArea());
                                airStationModel.setCity(CityName);
                                airStationModel.setRegion(chinaAirQualityHour.getPositionName());
                                airStationModel.setLon(Float.valueOf(chinaAirQualityHour.getLng()));
                                airStationModel.setLat(Float.valueOf(chinaAirQualityHour.getLat()));
                                airStationModel.setStationType("CH_AIR");
                                int status = airStationMapper.insertData(airStationModel);
                                flag = status > 0;
                            }
                        }
                    }
                }else{
                    System.out.println("找不到指定的文件");
                }
            }catch(Exception e){
                e.printStackTrace();
                return flag;
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
            return flag;
        }
        return flag;
    }

    /**
     * 将全国空气站点注册到数据库
     * @param
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertCHAirStationInfo(List<ChinaAirQualityHour> chinaAirQualityHours) throws IOException {
        List<AirStationModel> res = new ArrayList<>();
        if (chinaAirQualityHours!=null && chinaAirQualityHours.size()>0) {
            for (ChinaAirQualityHour chinaAirQualityHour : chinaAirQualityHours) {
                AirStationModel airStationModel = new AirStationModel();
                airStationModel.setStationId(chinaAirQualityHour.getStationCode());
                airStationModel.setStationName(chinaAirQualityHour.getPositionName());
                airStationModel.setRegion(chinaAirQualityHour.getArea());
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
                airStationModel.setStationType("CH_AIR");
                res.add(airStationModel);
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

    /**
     * 字符串转Instant  yyyy-MM-dd'T'HH:mm:ss'Z'
     * @return
     * @throws IOException
     */
    public Instant str2Instant(String time) {
        if (StringUtils.isBlank(time)) {
            return null;
        }
        time = time.substring(0, time.indexOf("+")).replace("T"," ")+":00";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        dateTimeFormatter.withZone(ZoneId.of("Asia/Shanghai"));
        LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
        return localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
    }




    public String getQTInfo(String stationId) {
        SimpleDateFormat sd = new SimpleDateFormat();// 格式化时间
        sd.applyPattern("yyyyMMddHH:mm:ss");
        Date date = new Date();// 获取当前时间
//        System.out.println("现在时间：" + sd.format(date));
        String time = sd.format(date);
        String time1 = time.substring(0,time.indexOf(":"));
        String time2 = time1+"0000";
        return stationId + time2;
    }

    public String getTime() {
        SimpleDateFormat sd = new SimpleDateFormat();// 格式化时间
        sd.applyPattern("yyyyMMddHH:mm:ss");
        Date date = new Date();// 获取当前时间
//        System.out.println("现在时间：" + sd.format(date));
        String time = sd.format(date);
        String time1 = time.substring(0,time.indexOf(":"));
        String time2 = time1+"0000";
        return time2;
    }
}



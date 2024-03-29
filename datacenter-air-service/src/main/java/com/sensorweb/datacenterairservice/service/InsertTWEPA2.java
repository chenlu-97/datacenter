package com.sensorweb.datacenterairservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterairservice.config.OkHttpUtil;
import com.sensorweb.datacenterairservice.dao.AirStationMapper;
import com.sensorweb.datacenterairservice.dao.TWEPAMapper;
import com.sensorweb.datacenterairservice.entity.AirStationModel;
import com.sensorweb.datacenterairservice.entity.TWEPA;
import com.sensorweb.datacenterairservice.feign.ObsFeignClient;
import com.sensorweb.datacenterairservice.feign.SensorFeignClient;
import com.sensorweb.datacenterairservice.util.AirConstant;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import com.sensorweb.datacenterutil.utils.DownloadFile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
@EnableScheduling
public class InsertTWEPA2 implements AirConstant {

    @Autowired
    private AirStationMapper airStationMapper;

    @Value("${datacenter.tmpDir}")
    private String tmpDir;

    @Autowired
    private TWEPAMapper twepaMapper;

    @Autowired
    private SensorFeignClient sensorFeignClient;

    @Autowired
    private ObsFeignClient obsFeignClient;

    @Autowired
    OkHttpUtil okHttpUtil;


    /**
     * 每小时接入一次数据
     */
//    @Scheduled(cron = "0 30 0/1 * * ?") //每个小时的30分开始接入
//    @PostConstruct
    public void insertDataByHour() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        String date = dateTime.toString().substring(0,dateTime.toString().indexOf(".")).replace("T"," ");
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean flag = false;
//                try {
                    String path = downloadFile();
                    String document = getDocumentByGZip(path);
                    flag = insertTwEPAInfoBatch(getEPAInfo(document));
                    System.out.println("flag = " + flag);
//                    if (flag) {
//                        int num = twepaMapper.selectMaxTimeData().size();
//                        log.info("台湾EPA接入时间: " + date + "Status: Success");
//
//                        System.out.println("台湾EPA接入时间: " + date + "Status: Success");
//                    }
//                } catch (Exception e) {
//                    log.error(e.getMessage());
//                    log.info("台湾EPA接入时间: " +date + "Status: Fail");
//                }
            }
        }).start();
    }


    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertTwEPAInfoBatch(List<TWEPA> twepas) {

        for (TWEPA twepa:twepas) {
            int status = twepaMapper.insertData(twepa);
        }

        return true;
    }

    public Instant str2Instant(String time) {
        String pattern = "yyyy-MM-dd HH:mm";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        dateTimeFormatter.withZone(ZoneId.of("Asia/Shanghai"));
        LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
        return localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
    }

    /**
     * 解析JSON字符串信息
     */
    public List<TWEPA> getEPAInfo(String document) {
        List<TWEPA> res = new ArrayList<>();
        if (!StringUtils.isBlank(document)) {
            JSONObject jsonObject = JSON.parseObject(document);
            JSONArray feeds = jsonObject.getJSONArray("feeds");
            if (feeds!=null) {
                for (int i=0; i<feeds.size(); i++) {
                    TWEPA twepa = new TWEPA();
                    JSONObject feed = feeds.getJSONObject(i);
                    twepa.setTime(str2Instant(feed.getString("datacreationdate")));
                    twepa.setAqi(feed.getString("AQI"));
                    twepa.setCo(feed.getString("CO"));
                    twepa.setCo8hr(feed.getString("CO_8hr"));
                    twepa.setCountry(feed.getString("Country"));
                    twepa.setImportDate(str2Instant(feed.getString("datacreationdate")));
                    twepa.setLon(Double.parseDouble(feed.getString("longitude")));
                    twepa.setLat(Double.parseDouble(feed.getString("latitude")));
                    twepa.setNo(feed.getString("NO"));
                    twepa.setNo2(feed.getString("NO2"));
                    twepa.setNox(feed.getString("NOx"));
                    twepa.setO3(feed.getString("O3"));
                    twepa.setO38hr(feed.getString("O3_8hr"));
                    twepa.setPm10(feed.getString("PM10"));
                    twepa.setPm10Avg(feed.getString("PM10_AVG"));
                    twepa.setPm25(feed.getString("PM2_5"));
                    twepa.setPm25Avg(feed.getString("PM2_5_AVG"));
                    twepa.setPollutant(feed.getString("Pollutant"));
                    twepa.setPublishTime(str2Instant(feed.getString("datacreationdate")));
                    twepa.setSo2(feed.getString("SO2"));
                    twepa.setSo2Avg(feed.getString("SO2_AVG"));
                    twepa.setSiteEngName(feed.getString("SiteEngName"));
                    twepa.setSiteId(feed.getString("SiteId"));
                    twepa.setSiteName(feed.getString("SiteName"));
                    twepa.setSiteType(feed.getString("SiteType"));
                    twepa.setStatus(feed.getString("Status"));
                    twepa.setWindDirec(feed.getString("WindDirec"));
                    twepa.setWindSpeed(feed.getString("WindSpeed"));
                    twepa.setApp(feed.getString("app"));
                    twepa.setDate(feed.getString("date"));
                    twepa.setFmtOpt(feed.getString("fmt_opt"));
                    twepa.setVerFormat(feed.getString("ver_format"));
                    twepa.setDeviceId(feed.getString("device_id"));
                    res.add(twepa);
                }
            }
        }
        return res;
    }

    /**
     * 不进行解压，直接获取压缩包里的文件内容
     */
    public String getDocumentByGZip(String filePath) {
        String res = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath)), StandardCharsets.ISO_8859_1));
            String line = null;
            while( (line = reader.readLine()) != null){
                res = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 通过网络下载链接下载数据文件到本地目录,并返回下载成功的文件路径
     */
    public String downloadFile() {
        String filePath  = tmpDir + getNowTime() + ".json.gz";
        try {
            DownloadFile.downloadUsingStream(AirConstant.TW_EPA_URL, filePath);
        } catch (IOException e) {
            log.debug(e.getMessage());
            return null;
        }
        return filePath;
    }

    /**
     * 根据时间生成日期时间字符串
     * @return
     */
    public String getNowTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
    }

    /**
     * 将台湾EPA站点注册到数据库
     * @param
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertTWAirStationInfo(List<TWEPA> twepas) throws IOException {
        List<AirStationModel> res = new ArrayList<>();
        if (twepas!=null && twepas.size()>0) {
            for (TWEPA twepa : twepas) {
                AirStationModel airStationModel = new AirStationModel();
                airStationModel.setStationId(twepa.getSiteId());
                airStationModel.setStationName(twepa.getSiteName());
                airStationModel.setTownship(twepa.getCountry());
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
                airStationModel.setStationType("TW_EPA_AIR");
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

}

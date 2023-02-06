package com.sensorweb.datacenterhimawariservice.service;

import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterhimawariservice.config.OkHttpUtil;
import com.sensorweb.datacenterhimawariservice.dao.HimawariMapper;
import com.sensorweb.datacenterhimawariservice.entity.Himawari;
import com.sensorweb.datacenterhimawariservice.feign.ObsFeignClient;
import com.sensorweb.datacenterhimawariservice.feign.SensorFeignClient;
import com.sensorweb.datacenterhimawariservice.util.HimawariConstant;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import com.sensorweb.datacenterutil.utils.FTPUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Configuration
@EnableScheduling
public class NewInsertHimawariService implements HimawariConstant {

    @Value("${datacenter.path.himawari}")
    private String upload;

    @Autowired
    private HimawariMapper himawariMapper;

    @Autowired
    private SensorFeignClient sensorFeignClient;

    @Autowired
    private ObsFeignClient obsFeignClient;

    @Autowired
    OkHttpUtil okHttpUtil;


    /**
     * 每隔一个小时执行一次，为了以小时为单位接入数据
     */
    @Scheduled(cron = "0 35 0/1 * * ?")
    public void insertDataByHour() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LocalDateTime dateTime = LocalDateTime.now();
                DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");
                String strDate3 = dtf3.format(dateTime);
                dateTime=LocalDateTime.parse(strDate3,dtf3);

                Instant timeNew = himawariMapper.selectMaxTimeData().get(0).getTime();
                Instant timeNow = dateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
                while(timeNew.isBefore(timeNow)){
                    LocalDateTime time = LocalDateTime.ofInstant(timeNew.plusSeconds(60*60),ZoneId.systemDefault());
                    System.out.println("time = " + time);
                    int year = time.getYear();
                    Month month = time.getMonth();
                    String monthValue = month.getValue()<10?"0"+month.getValue():month.getValue()+"";
                    String day = time.getDayOfMonth()<10?"0"+time.getDayOfMonth():time.getDayOfMonth()+"";
                    String hour = time.getHour()<10?"0"+time.getHour():time.getHour()+"";
                    String minute = time.getMinute()<10?"0"+time.getMinute():time.getMinute()+"";
                    String fileName = getName(year+"", monthValue, day, hour, minute);
                    if (himawariMapper.selectByName(fileName)!=null) {
                        log.info("数据已存在");
                        return ;
                    }else {
                        boolean flag = true;
                        int i = 0;
                        while (flag) {
                            try {
                                flag = !insertData(time);
                                if (!flag) {
                                    log.info("Himawari接入时间: " + time + "Status: Success");
                                    DataCenterUtils.sendMessage("Himawari-8" + dateTime.toString(), "卫星-葵花8号", "这是一条获取的葵花8号卫星的数据");
                                } else {
                                    log.info("Himawari接入时间: ----" + time + "---暂无最新的数据！！等下个小时再试");
                                    SendException("Himawari", timeNew.toString(), timeNow + "当前接入Himawari的数据失败");
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            }
                            if (i > 2) {
                                break;
                            }
                            i++;
                        }
                    }
                    timeNew = timeNew.plusSeconds(60*60);
                }
            }
        }).start();
    }


    /**
     * Registry Himawari Data, from dateTime to now
     * @param dateTime YYYY-DD-MMThh:mm:ss  is start time
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertData(LocalDateTime dateTime) throws Exception {
        dateTime = dateTime.minusHours(8);//本地时间需要减去8才能化为UTC
        int year = dateTime.getYear();
        Month month = dateTime.getMonth();
        String monthValue = month.getValue()<10?"0"+month.getValue():month.getValue()+"";
        String day = dateTime.getDayOfMonth()<10?"0"+dateTime.getDayOfMonth():dateTime.getDayOfMonth()+"";
        String hour = dateTime.getHour()<10?"0"+dateTime.getHour():dateTime.getHour()+"";
        String minute = dateTime.getMinute()<10?"0"+dateTime.getMinute():dateTime.getMinute()+"";
        Himawari himawari = getData(year+"", monthValue, day, hour,minute);
        if (himawari==null) {
            return false;
        }
        int status = himawariMapper.insertData(himawari);
        return status>0;
    }
    
  /**
     * get the data of APR
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     */
    public Himawari getData(String year, String month, String day, String hour, String minute) throws ParseException {
        FTPClient ftpClient = FTPUtils.getFTPClient(HimawariConstant.HAMAWARI_HOST, HimawariConstant.HAMAWARI_USERNAME, HimawariConstant.HAMAWARI_PASSWORD);
        String fileName = getName(year, month, day, hour, minute);
        String filePath = HimawariConstant.AREOSOL_PROPERTY_LEVEL3 + year + month + "/" + day + "/";
        String uploadFilePath = upload + fileName;
        boolean flag = FTPUtils.downloadFTP(ftpClient, filePath, fileName, uploadFilePath);
        Himawari himawari = new Himawari();
        if (flag) {
            himawari.setName(fileName);
            String date = year + "-" + month + "-" + day + "T" + hour + ":00:00";
            Instant temp = DataCenterUtils.string2LocalDateTime(date).atZone(ZoneId.of("UTC")).toInstant();
            himawari.setTime(temp);
            himawari.setUrl(filePath + fileName);
            himawari.setLocalPath(uploadFilePath);
            return himawari;
        }
        return null;
    }

    /**
     * get the name of APR by naming convention
     * @param year YYYY
     * @param month MM
     * @param day DD
     * @param hour hh
     * @param minute mm
     * example: H08_20150727_0800_1HARP001_FLDK.02401_02401.nc
     */
    public String getName(String year, String month, String day, String hour, String minute) {
        if (StringUtils.isBlank(year) || StringUtils.isBlank(month) || StringUtils.isBlank(day) || StringUtils.isBlank(hour)) {
            log.error("parameter is not right");
        }
        StringBuilder sb = new StringBuilder();
//        sb.append("H08_").append(year).append(month).append(day).append("_").append(hour).append("00_1HARP031_FLDK.02401_02401.nc");  2022.12.13 14:00:00 开始更换为了卫星9号
        sb.append("H09_").append(year).append(month).append(day).append("_").append(hour).append("00_1HARP031_FLDK.02401_02401.nc");
        return sb.toString();

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

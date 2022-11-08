package com.sensorweb.datacenterlaadsservice.service;


import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterlaadsservice.config.OkHttpUtil;
import com.sensorweb.datacenterlaadsservice.dao.EntryMapper;
import com.sensorweb.datacenterlaadsservice.dao.GPM_3IMERGDEMapper;
import com.sensorweb.datacenterlaadsservice.dao.MERRA2Mapper;
import com.sensorweb.datacenterlaadsservice.entity.Entry;
import com.sensorweb.datacenterlaadsservice.entity.GPM_3IMERGDE;
import com.sensorweb.datacenterlaadsservice.entity.HttpsUrlValidator;
import com.sensorweb.datacenterlaadsservice.entity.MERRA2;
import com.sensorweb.datacenterlaadsservice.util.DownloadUtil;
import com.sensorweb.datacenterlaadsservice.util.LAADSConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@EnableScheduling
public class InsertMERRA2 {
    @Autowired
    private GPM_3IMERGDEMapper gpm_3IMERGDEMapper;


    @Autowired
    private MERRA2Mapper merra2Mapper;


    @Value("${datacenter.path.MERRA2}")
    private String savePath;

    @Autowired
    OkHttpUtil okHttpUtil;

    @Autowired
    DownloadUtil downloadUtil;

    @Scheduled(cron = "00 30 22 25 * ?")//每月25号的12：30分执行一次
//    @PostConstruct
    public void insert_MERRA2_Data() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        Calendar calendar = Calendar.getInstance();
        String timeNow = format.format(calendar.getTime());
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                String time = null;
                if (merra2Mapper.selectNew() != null) {
                    time = merra2Mapper.selectNew().get(0).getStart().plusSeconds(32*60*60).toString();
                    System.out.println("time = " + time);
                }
                try {
                    boolean flag = insertData(time);
                    if (flag) {
                        log.info("MERRA2接入时间: " + time + "Status: Success");
                        System.out.println("MERRA2接入时间: " + time + "Status: Success");
                    }else{
                        log.error("MERRA2还未更新数据  "+time);
                        System.out.println("MERRA2还未更新数据  "+time );
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
//                    log.error(e.getMessage());
                    log.error("MERRA2接入时间: " + calendar.getTime().toString() + "Status: Fail");
                    System.out.println("MERRA2接入时间: " + calendar.getTime().toString() + "Status: Fail");
                    SendException("MERRA2E",time,timeNow+"MERRA2接入数据失败");
                }
            }
        }).start();
    }

//    @PostConstruct
//    public void insert_MERRA2_Data2() throws Exception {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
//        Calendar calendar = Calendar.getInstance();
//        String timeNow = format.format(calendar.getTime());
//        String time = null;
//        boolean flag = insertData("2020-01-01 00:00:00");
//        System.out.println("flag = " + flag);
//    }


    /**
     * 数据接入，将数据存储到本地数据库，并将数据文件存储到本地，该方法是MERRA2产品下载，由于和modis下载源略微不同，但是可以相对的复用
     */
    public boolean insertData(String time) throws Exception {

        String pathType = new String();
        String fileName = new String();
        int res = 0 ;
        String[] types = {"slv","flx","aer"};
        String time1 = time.replace("T", " ").replace("Z", "");
        time = time.substring(0, time.indexOf("T")).replace("-", "");
        String year = time.substring(0, 4);
        String month = time.substring(4, 6);
        try{
            for(String type:types) {

                String[] headers = new String[36];
                MERRA2  merra2 = new MERRA2();
                if (type.equals("slv")) {
                    pathType = "https://goldsmr4.gesdisc.eosdis.nasa.gov/data/MERRA2/M2T1NXSLV.5.12.4/";
                    fileName = "MERRA2_400.tavg1_2d_slv_Nx." + time + ".nc4";
                    headers[1] = "goldsmr4.gesdisc.eosdis.nasa.gov";
                }
                if (type.equals("flx")) {
                    pathType = "https://goldsmr4.gesdisc.eosdis.nasa.gov/data/MERRA2/M2T1NXFLX.5.12.4/";
                    fileName = "MERRA2_400.tavg1_2d_flx_Nx." + time + ".nc4";
                    headers[1] = "goldsmr4.gesdisc.eosdis.nasa.gov";
                }
                if (type.equals("aer")) {
                    pathType = "https://goldsmr5.gesdisc.eosdis.nasa.gov/data/MERRA2/M2I3NVAER.5.12.4/";
                    fileName = "MERRA2_400.inst3_3d_aer_Nv." + time + ".nc4";
                    headers[1] = "goldsmr5.gesdisc.eosdis.nasa.gov";
                }

                String downloadPath = pathType + year + File.separator + month + File.separator + fileName;
//            System.out.println("downloadPath = " + downloadPath);

                //https://goldsmr4.gesdisc.eosdis.nasa.gov/data/MERRA2/M2T1NXSLV.5.12.4/2022/09/  tavg1_2d_slv_Nx
                //https://goldsmr4.gesdisc.eosdis.nasa.gov/data/MERRA2/M2T1NXFLX.5.12.4/2022/09/  tavg1_2d_flx_Nx
                //https://goldsmr5.gesdisc.eosdis.nasa.gov/data/MERRA2/M2I3NVAER.5.12.4/2022/09/  inst3_3d_aer_Nv
                String filePath =  savePath + type + File.separator + year + File.separator + month;

                File file = new File(filePath);
                if (!file.exists()) {
                    boolean flag = file.mkdirs();
                }

                headers[0] = "Host";
                headers[2] = "Cookie";headers[3] = LAADSConstant.Cookie;
                headers[4] = "Connection";headers[5] = "keep-alive";
                headers[6] = "sec-ch-ua";headers[7] = "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Google Chrome\";v=\"101\"";
                headers[8] = "sec-ch-ua-mobile";headers[9] = "?0";
                headers[10] = "sec-ch-ua-platform";headers[11] = "\"Windows\"";
                headers[12] = "DNT";headers[13] = "1";
                headers[14] = "Upgrade-Insecure-Requests";headers[15] = "1";
                headers[16] = "User-Agent";headers[17] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.0.0 Safari/537.36";
                headers[18] = "Accept";headers[19] = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9";
                headers[20] = "Sec-Fetch-Site";headers[21] = "none";
                headers[22] = "Sec-Fetch-Mode";headers[23] = "navigate";
                headers[24] = "Sec-Fetch-User";headers[25] = "?1";
                headers[26] = "Sec-Fetch-Dest";headers[27] = "document";
                headers[28] = "Accept-Encoding";headers[29] = "gzip, deflate, br";
                headers[30] = "Accept-Language";headers[31] = "zh-CN,zh;q=0.9";
                headers[32] = "Range";headers[33] = "bytes=6995968-407382484";
                headers[34] = "If-Range";headers[35] = "\"184829d5-5eb4208df1fdf";

                String localPath = downloadUtil.downloadBySysc(downloadPath, headers, filePath, fileName);

//            String localPath = downloadFromUrl(downloadPath, fileName, filePath);
                int i = 0;

                while (localPath.equals("fail")) {
                    i++;
                    if (i > 1) {
                        break;
                    }
//                localPath = downloadFromUrl(downloadPath, fileName, filePath);
                    localPath = downloadUtil.downloadBySysc(downloadPath, headers, filePath, fileName);
                }
                if (localPath != "fail" && localPath != "none" && localPath != null) {
                    merra2.setFilePath(localPath);
                    merra2.setSatellite("MERRA2");
                    merra2.setProductType(type);
                    merra2.setEntryId(fileName);
                    merra2.setTitle(fileName);
                    merra2.setBbox("-180.0,-90.0,180.0,90.0");
                    merra2.setLink(downloadPath);
                    merra2.setStart(str2Instant(time1));
                    res = merra2Mapper.insertData(merra2);
                } else if (localPath == "none") {
                    log.info("MERRA2暂无数据！！！");
                    System.out.println("MERRA2暂无数据！！！");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return res>0;
    }


    public Instant str2Instant(String time) {
//        String time = "2021-09-21 00:00:00";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        dateTimeFormatter.withZone(ZoneId.of("Asia/Shanghai"));
        LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
        return localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
    }


    /**
     * 通过url下载LAADS文件，需要cookie授权的情况下
     *
     * @param url
     * @param fileName
     * @param savePath
     */
    public String downloadFromUrl(String url, String fileName, String savePath) throws Exception {
        String res = null;
        try {
            HttpsUrlValidator.retrieveResponseFromServer(url);
            URL httpUrl = new URL(url);
            //其他方式可以见Proxy.Type属性
            HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            connection.setRequestProperty("Accept-Encoding","gzip, deflate, br");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Host", "gpm1.gesdisc.eosdis.nasa.gov");
            connection.setRequestProperty("Cookie", LAADSConstant.Cookie);
//            connection.setConnectTimeout(30000);
            //设置https协议访问
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
//            System.setProperty("jdk.tls.useExtendedMasterSecret", "false");//设置环境变量
            InputStream inputStream = connection.getInputStream();
            byte[] getData = readInputStream(inputStream);
            if(getData.length<1000){ //如果网页返回值小于1mb ,则返回空
                return "none";
            }else{
                // 文件保存位置
                File saveDir = new File(savePath);
                if (!saveDir.exists()) {
                    boolean flag = saveDir.mkdir();
                }
                File file = new File(saveDir +File.separator +fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(getData);
                fos.close();
                inputStream.close();
                res = savePath  + File.separator + fileName;
            }
        }
        catch (Exception e) {
            Matcher error = Pattern.compile("java.io.FileNotFoundException").matcher(e.toString());
            while (error.find()) {
                return "none";
            }
            e.printStackTrace();
            return "fail";
        }
        return res;
    }


    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }


    public List<GPM_3IMERGDE> getFilePath(Instant start, Instant end) {
       return gpm_3IMERGDEMapper.getFilePath(start,end);
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

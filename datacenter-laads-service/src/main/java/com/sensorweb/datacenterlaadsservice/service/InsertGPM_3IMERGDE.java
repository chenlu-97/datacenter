package com.sensorweb.datacenterlaadsservice.service;


import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterlaadsservice.config.OkHttpUtil;
import com.sensorweb.datacenterlaadsservice.dao.GPM_3IMERGDEMapper;
import com.sensorweb.datacenterlaadsservice.entity.GLDAS;
import com.sensorweb.datacenterlaadsservice.entity.GPM_3IMERGDE;
import com.sensorweb.datacenterlaadsservice.entity.HttpsUrlValidator;
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@EnableScheduling
public class InsertGPM_3IMERGDE {
    @Autowired
    private GPM_3IMERGDEMapper gpm_3IMERGDEMapper;

    @Value("${datacenter.path.GPM}")
    private String savePath;

    @Value("${datacenter.path.other}")
    private String filePath;

    @Autowired
    OkHttpUtil okHttpUtil;

    @Autowired
    DownloadUtil downloadUtil;

    @Scheduled(cron = "00 30 12 20 * ?")//每月20号的12：30分执行一次
    public void insert_GPM_3IMERGDE_Data() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        Calendar calendar = Calendar.getInstance();
        String timeNow = format.format(calendar.getTime());
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                String time = null;
                if (gpm_3IMERGDEMapper.selectNew() != null) {
                    time = gpm_3IMERGDEMapper.selectNew().getTime().toString();
                    System.out.println("time = " + time);
                }
                try {
                    boolean flag = insertData(time);

                    if (flag) {
                        log.info("GPM_3IMERGDE接入时间: " + time + "Status: Success");
                        System.out.println("GPM_3IMERGDE接入时间: " + time + "Status: Success");
                    }else{
                        log.error("GPM_3IMERGDE还未更新数据  "+time);
                        System.out.println("GPM_3IMERGDE还未更新数据  "+time );
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    log.error("GPM_3IMERGDE接入时间: " + calendar.getTime().toString() + "Status: Fail");
                    System.out.println("GPM_3IMERGDE接入时间: " + calendar.getTime().toString() + "Status: Fail");
                    SendException("GPM_3IMERGDE",time,timeNow+"GPM_3IMERGDE接入数据失败");
                }
            }
        }).start();
    }



    /**
     * 数据接入，将数据存储到本地数据库，并将数据文件存储到本地
     * //    https://gpm1.gesdisc.eosdis.nasa.gov/data/GPM_L3/GPM_3IMERGDE.06/2022/06/3B-DAY-E.MS.MRG.3IMERG.20220601-S000000-E235959.V06.nc4
     * @param time 2022-03-01T00:00:00z
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertData(String time) throws Exception {
        int j = 0;
        try{
                String date = time.replace("T", " ").replace("Z", "").substring(0, time.indexOf("T")).replace("-", "");
                String year = time.substring(0,4);
                String mounth = time.substring(5,7);
                String downloadurl;
                String fileName;
                fileName = "3B-DAY-E.MS.MRG.3IMERG." + date +"-S000000-E235959.V06.nc4";
                downloadurl = "https://gpm1.gesdisc.eosdis.nasa.gov/data/GPM_L3/GPM_3IMERGDE.06/" + year  + "/" +mounth+ "/" + fileName;

                String[] headers = new String[4];
                headers[0] = "Host";
                headers[1] = "gpm1.gesdisc.eosdis.nasa.gov";
                headers[2] = "Cookie";
                headers[3] = LAADSConstant.Cookie;
//                headers[3] = "urs_guid_ops=7a27b89f-9bb6-4dc4-aacc-df93fad34f3a; _ga_XXXXXXXXXX=GS1.1.1646118719614.fstj2aol.1.1.1646118719.0; _4c_=fVJNj5swEP0rK58DsbENJreqlVZ7aG89R8YegrV8yTiwaZT%2F3nEWslIjlYtnHm%2FefF7J0kBPDiyXVHJGqcgE25F3uEzkcCXe2fjM5EBqWWpbWZpIq7NEcKCJhjJLjASdK2NKQQuyIx93LVkwWvKcCn7bETOuGldy9i1KNSGM02G%2FX5Yl7fWk09Mw77tzG1wH1um96%2FQJTrptwV%2F2rrfwkTaha1HdDBZQgJWpSmO28Ac9oSiaox%2Fs2YRjuIyRskD1Mtl3%2FGFhdgaOi7OhibFFpr7QBtypCRFmLAra0aOTobVg4mH5N2xFH2Ely%2B5irZux2OPkDYL90AOiFQSN3q9o%2BmGZIEp%2Fb%2FzQwQtOGuEBh0x%2BaoOmhxq8v1PQm1yITWzDWRHcygomn%2BA4r8W2g9FtjMBdIhdMcANulXTaReD12%2FH3249YripolnPBUpbzggqpuCK3bWs0Z4WgLKcFTjbgplQuaPyQMbvtEKxldW5smVRAeSIknoQuK5PUQnFdsaoGzR%2BHgEeVqSgpVkmmNsWxXRXZF1lIXkjOy40sHvnHeWVnT9Xmz9V%2Bzjt2%2F5%2B4py5vt78%3D; _gid=GA1.2.1319028625.1658732149; 111548195126179120137161431810=s%3AayflQMNmrYsvP0_Pq0wmYxg8ddfISD3a.AoilyQ8RkYwnUbNkGIF%2BkmmCNJKnygakRMMmIYLTgf0; _gat_UA-56929103-1=1; _ga=GA1.2.987026341.1637045838; nasa_gesdisc_data_archive=E48FuFIWQGSVwmgaHy/RjP9x1CTU16UMSX2EwPM3QHQg2paqUZdqggSR9bF7U7trSG4+XgOcHeZVAhRLiX9+yBHnrnP+QJ8m+MSMXFD4up3tOcILJXC0VBYi6C4qejso";
                String localPath = downloadUtil.downloadBySysc(downloadurl, headers, savePath +  year + File.separator + mounth, fileName);
//                  String localPath = downloadFromUrl(downloadurl, fileName, savePath +  year + File.separator + mounth);
                int i = 0; //防止失败，重试2次
                while (localPath.equals("fail")) {
                    i++;
                    if (i > 1) {
                        break;
                    }
                    localPath = downloadUtil.downloadBySysc(downloadurl, headers, savePath +  year + File.separator + mounth, fileName);
//                    localPath = downloadFromUrl(downloadurl, fileName, savePath +  year + File.separator + mounth);
                }
                GPM_3IMERGDE gpm_3IMERGDE = new GPM_3IMERGDE();
                if (localPath != "fail" && localPath != "none") {
                    gpm_3IMERGDE.setFilePath(localPath);
                    gpm_3IMERGDE.setTitle("GPM_3IMERGDE");
                    gpm_3IMERGDE.setProductType("GPM_3IMERGDE");
                    gpm_3IMERGDE.setGpmId(fileName);
                    gpm_3IMERGDE.setBbox("-180.0,-90.0,180.0,90.0");
                    gpm_3IMERGDE.setLink(downloadurl);
                    gpm_3IMERGDE.setTime(str2Instant(date+"000000"));
                    j = gpm_3IMERGDEMapper.insertData(gpm_3IMERGDE);
                }else if(localPath == "none"){
                    log.info("GPM_3IMERGDE暂无数据！！！");
                    System.out.println("GPM_3IMERGDE暂无数据！！！");
                }
            return j > 0;
        }catch(Exception e){
            e.printStackTrace();

        }
        return j > 0;
    }



    public Instant str2Instant(String time) {
//        String time = "2021-09-21 00:00:00";
        String pattern = "yyyyMMddHHmmss";
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
                File file = new File(saveDir + File.separator +fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(getData);
                fos.close();
                inputStream.close();
                res = savePath + File.separator + fileName;
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

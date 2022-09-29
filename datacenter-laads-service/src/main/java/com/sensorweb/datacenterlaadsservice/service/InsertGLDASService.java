package com.sensorweb.datacenterlaadsservice.service;


import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterlaadsservice.config.OkHttpUtil;
import com.sensorweb.datacenterlaadsservice.dao.GLDASMapper;
import com.sensorweb.datacenterlaadsservice.entity.GLDAS;
import com.sensorweb.datacenterlaadsservice.entity.HttpsUrlValidator;
import com.sensorweb.datacenterlaadsservice.feign.ObsFeignClient;
import com.sensorweb.datacenterlaadsservice.feign.SensorFeignClient;
import com.sensorweb.datacenterlaadsservice.util.DownloadUtil;
import com.sensorweb.datacenterlaadsservice.util.LAADSConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
@EnableScheduling
public class InsertGLDASService {


    @Autowired
    private GLDASMapper gldasMapper;

    @Value("${datacenter.path.gldas}")
    private String savePath;

    @Value("${datacenter.path.other}")
    private String filePath;

    @Autowired
    OkHttpUtil okHttpUtil;

    @Autowired
    DownloadUtil downloadUtil;

    @Scheduled(cron = "00 30 12 20 * ?")//每月20号的12：30分执行一次
    public void insertGLDASData() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        Calendar calendar = Calendar.getInstance();
        String timeNow = format.format(calendar.getTime());
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                String time = null;
                if (gldasMapper.selectNew() != null) {
                    time = gldasMapper.selectNew().getTime().plusSeconds(12*60*60).toString();
                    System.out.println("time = " + time);
                }
                try {
                        boolean flag = insertData(time);

                    if (flag) {
                        log.info("GLDAS接入时间: " + time + "Status: Success");
                        System.out.println("GLDAS接入时间: " + time + "Status: Success");
                    }else{
                        log.error("GLDAS还未更新数据  "+time);
                        System.out.println("GLDAS还未更新数据  "+time );
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    log.error("GLDAS接入时间: " + calendar.getTime().toString() + "Status: Fail");
                    SendException("GLDAS",time,timeNow+"GLDAS接入数据失败");
                    System.out.println("GLDAS接入时间: " + calendar.getTime().toString() + "Status: Fail");
                }
            }
        }).start();
    }




    /**
     * 数据接入，将数据存储到本地数据库，并将数据文件存储到本地
     * //            https://hydro1.gesdisc.eosdis.nasa.gov/data/GLDAS/GLDAS_NOAH025_3H_EP.2.1/2022/039/GLDAS_NOAH025_3H_EP.A20220208.2100.021.nc4
     * //            https://hydro1.gesdisc.eosdis.nasa.gov/data/GLDAS/GLDAS_NOAH025_3H.2.1/2022/039/GLDAS_NOAH025_3H.A20220208.2100.021.nc4
     * @param time 2022-03-01T00:00:00z
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertData(String time) throws Exception {
        int j = 0;
        try{
            String date = time.replace("T", " ").replace("Z", "").substring(0, time.indexOf("T")).replace("-", "");
            String[] moments = {"0000","0300","0600","0900","1200","1500","1800","2100"};
            for(String moment:moments){
                String year = time.substring(0,4);
                String mounth = time.substring(5,7);
                String day = YearMouthToday(date);
                String downloadurl;
                String fileName;
                fileName = "GLDAS_NOAH025_3H.A" + date +"."+ moment + ".021.nc4";
                downloadurl = "https://hydro1.gesdisc.eosdis.nasa.gov/data/GLDAS/GLDAS_NOAH025_3H.2.1/" + year  + "/" +day+ "/" + fileName;
                String[] headers = new  String[4];
                headers[0] = "Host";
                headers[1] = "hydro1.gesdisc.eosdis.nasa.gov";
                headers[2] = "Cookie";
//                headers[3] = "nasa_gesdisc_data_archive=3rxxwfeDT1ektQrt07auFY7W5TrVIaSKkp5IMs+fiRmj0RSdKC8qvIwAPwNb9QiKb4WxiVubgbFonzxejxiA/zHpTkVrXLRpYa9DpxjHvwYsqohRXdSWcL6Btkw9zXeslavwZ3Ssvut/hWQ3IbUkHA==; 191111097613312241618150851714=s%3AxHQf7mE5NbCl8ogP5AsutLuqu7BGJMbI.wAk7uNv84kYiilFLLJsM%2F6MiyNufQgXRT1DOe54EaYw; _gid=GA1.2.40201582.1655280106; _ga=GA1.1.936238580.1654653312; _ga_XXXXXXXXXX=GS1.1.1655280109.1.0.1655280153.0; _dd_s=logs=1&id=a3b84765-f14c-40e9-b915-75c2086f2f50&created=1655280153396&expire=1655281258506";
                headers[3] = LAADSConstant.Cookie;
                System.out.println(headers);
                System.out.println(downloadurl);
                String localPath = downloadUtil.downloadBySysc(downloadurl, headers, savePath + year + File.separator + mounth, fileName);
//                String localPath = downloadFromUrl(downloadurl, fileName, savePath +  year + File.separator + mounth);
                int i = 0; //防止失败，重试2次
                while (localPath.equals("fail")) {
                    i++;
                    if (i > 1) {
                        break;
                    }
//                    localPath = downloadFromUrl(downloadurl, fileName, savePath +  year + File.separator + mounth);
                    localPath  = downloadUtil.downloadBySysc(downloadurl, headers, savePath + File.separator + year + File.separator + mounth, fileName);
                }
                GLDAS gldas = new GLDAS();
                if (localPath != "fail" && localPath != "none") {
                    gldas.setFilePath(localPath);
                    gldas.setTitle("GLDAS");
                    gldas.setProductType("GLDAS_Product");
                    gldas.setGldasId(fileName);
                    gldas.setBbox("-180.0,-90.0,180.0,90.0");
                    gldas.setLink(downloadurl);
                    gldas.setTime(str2Instant(date+moment+"00"));
                    j = gldasMapper.insertData(gldas);
                }else if(localPath == "none"){
                    log.info("GLDAS_Product暂无数据！！！");
                    System.out.println("GLDAS_Product暂无数据！！！");
                }
            }
            return j > 0;
        }catch(Exception e){
            e.printStackTrace();

        }
        return j > 0;
    }


    public String YearMouthToday(String time){
        Integer num = 0;
        int [] dayOfmounth;
        Integer year = Integer.valueOf(time.substring(0,4));
        if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
            dayOfmounth = new int[]{31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        } else {
            dayOfmounth = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        }
        Integer mounth = Integer.valueOf(time.substring(4, 6));
        Integer day = Integer.valueOf(time.substring(6, 8));
        for(int i = 0 ; i< mounth-1;i++) {
            num = num + dayOfmounth[i];
        }
        Integer res = num+day;
        //实例化format，格式为“000”
        Format f1=new DecimalFormat("000");
        String  count =f1.format(res);
        return count;
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
     * 通过url下载LAADS文件，需要Token授权的情况下
     *
     * @param url
     * @param fileName
     * @param savePath
     */
    public String downloadFromUrl(String url, String fileName, String savePath) {
        String res = null;
        try {
            HttpsUrlValidator.retrieveResponseFromServer(url);
            URL httpUrl = new URL(url);
            //其他方式可以见Proxy.Type属性
            HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Host", "hydro1.gesdisc.eosdis.nasa.gov");
            connection.setRequestProperty("Cookie", LAADSConstant.Cookie);
            //设置https协议访问
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
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
                File file = new File(saveDir + File.separator+fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(getData);
                fos.close();
                inputStream.close();
                res = savePath + File.separator + fileName;
            }
        } catch (Exception e) {
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



//    public void update(){
//        List<GLDAS> all = gldasMapper.getall();
//        for(GLDAS one :all){
//            String id =  one.getGldasId();
////            System.out.println("id = " + id);
//            String year = id.substring(id.indexOf(".A")+2).substring(0,4);
////            System.out.println("year = " + year);
//            String newPath = one.getFilePath();
//            newPath = newPath.substring(0,newPath.lastIndexOf("gldas"))+"GLADS_data"+"/"+year+"/"+id;
//            System.out.println("newPath = " + newPath);
//            gldasMapper.updateFileName(id,newPath);
//        }
//
//    }

    //    /**
//     * 数据接入，将数据存储到本地数据库，并将数据文件存储到本地
//     */
//    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
//    public boolean insertDataByCookie(String loadfile) throws Exception {
//        boolean res = false;
//        int j = 0;
//        File file = new File(filePath+loadfile);
//        try{
//            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
//            String downloadurl = null;
//            while((downloadurl = br.readLine())!=null){//使用readLine方法，一次读一行
//                int tmp = downloadurl.lastIndexOf("/");
//                String fileName = downloadurl.substring(tmp+1);
//                String localPath = downloadFromUrl(downloadurl, fileName, savePath);
//                System.out.println("下载状态： " + localPath);
//                String date = downloadurl.substring(downloadurl.indexOf(".A")+2,downloadurl.indexOf(".021")).replace(".","");
//                if (localPath != "fail" && localPath != "none") {
//                  j =  insertDB(fileName,downloadurl,date);
//
//                }else if(localPath == "none"){
//                    log.info("GLDAS暂无数据！！！");
//                    System.out.println("GLDAS暂无数据！！！");
//                }
//            }br.close();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return j > 0;
//    }

//    public String getCookie(String downloadurl) {
//        System.setProperty("webdriver.firefox.bin", filePath+"geckodriver");
//        WebDriver driver = new FirefoxDriver();
//        driver.get("https://urs.earthdata.nasa.gov/");
//        WebElement username = driver.findElement(By.id("username"));
//        WebElement password = driver.findElement(By.id("password"));
//        WebElement login_button = driver.findElement(By.name("commit"));
//        username.sendKeys("CUG_chenlu");
//        password.sendKeys("Chenlu1997");
//        login_button.click();
//        String pageSource = driver.getPageSource();
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        driver.get(downloadurl);
//        Set<Cookie> cookies = driver.manage().getCookies();
//        String cookieStr = "";
//        for (Cookie cookie : cookies) {
//            cookieStr += cookie.getName() + "=" + cookie.getValue() + "; ";
//        }
//        if (cookieStr.lastIndexOf(";") != -1) {
//            cookieStr = cookieStr.substring(0, cookieStr.lastIndexOf(";"));
////            System.out.println("cookieStr = " + cookieStr);
//        }
//        return cookieStr;
//    }
//
//
//    public int insertDB(String fileName, String downloadurl, String date){
//        int j ;
//        GLDAS gldas = new GLDAS();
//        gldas.setFilePath("/data/Ai-Sensing/DataCenter/gldas/"+fileName);
//        gldas.setTitle("GLDAS");
//        gldas.setProductType("GLDAS");
//        gldas.setGldasId(fileName);
//        gldas.setBbox("-180.0,-90.0,180.0,90.0");
//        gldas.setLink(downloadurl);
//        gldas.setTime(str2Instant(date+"00"));
//        j = gldasMapper.insertData(gldas);
//        return j;
//    }


//    public String getGLDASByBasic(String time) throws Exception {
//
//        String date = time.replace("T", " ").replace("Z", "").substring(0, time.indexOf("T")).replace("-", "");
//        String[] moments = {"0000", "0300", "0600", "0900", "1200", "1500", "1800", "2100"};
//        for (String moment : moments) {
//            String year = time.substring(0, 4);
//            String day = YearMouthToday(date);
//            String downloadurl;
//            String fileName;
//            if (Integer.valueOf(date) < 20201101) {
//                fileName = "GLDAS_NOAH025_3H.A" + date + "." + moment + ".021.nc4";
//                downloadurl = "https://hydro1.gesdisc.eosdis.nasa.gov/data/GLDAS/GLDAS_NOAH025_3H/" + year + "/" + day + "/" + fileName;
//            } else {
//                fileName = "GLDAS_NOAH025_3H_EP.A" + date + "." + moment + ".021.nc4";
//                downloadurl = "https://hydro1.gesdisc.eosdis.nasa.gov/data/GLDAS/GLDAS_NOAH025_3H_EP.2.1/" + year + "/" + day + "/" + fileName;
//            }
//            downloadUtil.downloadBySysc(downloadurl, null, savePath, fileName);
//
////
////
////        InputStream inputStream = response.body().byteStream();
////        byte[] getData = readInputStream(inputStream);
////        if(getData.length<1000){ //如果网页返回值小于1mb ,则返回空
////            return "no_data";
////        }else{
////            // 文件保存位置
////            File saveDir = new File(savePath);
////            if (!saveDir.exists()) {
////                boolean flag = saveDir.mkdir();
////            }
////            File file = new File(saveDir + File.separator + fileName);
////            FileOutputStream fos = new FileOutputStream(file);
////            fos.write(getData);
////            fos.close();
////            inputStream.close();
////            return savePath+fileName;
////        }
//        }
//        return null;
//    }


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


    public List<GLDAS> getFilePath(Instant start, Instant end) {
        return gldasMapper.getFilePath(start,end);
    }
}

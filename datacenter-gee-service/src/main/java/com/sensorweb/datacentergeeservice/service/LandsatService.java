package com.sensorweb.datacentergeeservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacentergeeservice.config.OkHttpUtil;
import com.sensorweb.datacentergeeservice.dao.LandsatMapper;
import com.sensorweb.datacentergeeservice.entity.Landsat;
import com.sensorweb.datacentergeeservice.util.GoogleServiceConstant;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

@Slf4j
@Service
public class LandsatService implements GoogleServiceConstant{
    @Autowired
    LandsatMapper landsatMapper;

    @Autowired
    OkHttpUtil okHttpUtil;

    @Value("${datacenter.path.save}")
    private String savePath;


//    public GoogleCredentials getCredentials() throws IOException {
//        HttpTransportFactory httpTransportFactory = getHttpTransportFactory("127.0.0.1", 10808, "", "");
//        return GoogleCredentials.getApplicationDefault(httpTransportFactory);
//    }
//    public HttpTransportFactory getHttpTransportFactory(String proxyHost, int proxyPort, String proxyName, String proxyPassword) {
//        HttpHost proxyHostDetails = new HttpHost(proxyHost, proxyPort);
//        HttpRoutePlanner httpRoutePlanner = new DefaultProxyRoutePlanner(proxyHostDetails);
//        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(
//                new AuthScope(proxyHostDetails.getHostName(), proxyHostDetails.getPort()),
//                new UsernamePasswordCredentials(proxyName, proxyPassword)
//        );
//        HttpClient httpClient = ApacheHttpTransport.newDefaultHttpClientBuilder()
//                .setRoutePlanner(httpRoutePlanner)
//                .setProxyAuthenticationStrategy(ProxyAuthenticationStrategy.INSTANCE)
//                .setDefaultCredentialsProvider(credentialsProvider)
//                .build();
//        final HttpTransport httpTransport = new ApacheHttpTransport(httpClient);
//        return new HttpTransportFactory() {
//            @Override
//            public HttpTransport create() {
//                return httpTransport;
//            }
//        };
//    }

    public List<Landsat> getAll() {
        return landsatMapper.selectAll();
    }

    public List<Landsat> getByattribute(String spacecraftID, String Date, String Cloudcover, String imageType) {
        return landsatMapper.selectByattribute(spacecraftID,Date,Cloudcover,imageType);
    }

    public List<Landsat> getLandsatByPage(int pageNum, int pageSize) {
        return landsatMapper.selectByPage(pageNum,pageSize);
    }

    public int getLandsatNum() {
        return landsatMapper.selectNum();
    }

//    @Scheduled(cron = "0 00 11 * * ?") //每天上午11点接入
//    public void getLandsat() {
//        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
//        String time = formatter.format(dateTime);
//        LocalDateTime begin_date = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
//        LocalDateTime end_date = begin_date.plusSeconds(24*60*60);
//        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Shanghai"));
//        String begin = formatter1.format(begin_date);
//        String end = formatter1.format(end_date);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                    try {
////                        MODIS/006/MCD12Q1
////                        MODIS/006/MOD13A2
////                        MODIS/006/MOD11A2
////                        MODIS/006/MOD11A1
////                        COPERNICUS/S2_SR
//                        String Landsat8 = "LANDSAT/LC08/C01/T1";
//                        String url = "http://172.16.100.2:8009/getLandsat/";
//                        String param = "image="+Landsat8+"&startDate="+begin+"&endDate="+end;
//                        String statue = DataCenterUtils.doGet(url,param);
//                        if (statue=="0") {
//                            log.info("------ 目前暂无影像！！！！！-----");
//                            System.out.println("------ 目前暂无影像！！！！！-----");
//                        } else if(statue=="fail"){
//                            log.info("------ 连接GEE失败-----");
//                            System.out.println("------ 连接GEE失败-----");
//                        }else if(statue=="success"){
//                            log.info("------获取影像成功！！！！！！！-----");
//                            System.out.println("------ 获取影像成功！！！！！！！-----");
//                            DataCenterUtils.sendMessage("Landsat-8"+ time, "Landsat-8","GEE获取的Landsat-8影像成功");
//                        }
//                    } catch (Exception e) {
//                        log.error(e.getMessage());
//                        log.info("GEE获取Landsat-8影像 " + time + "Status: Fail");
//                        System.out.println(e.getMessage());
//                    }
//            }
//        }).start();
//    }

//    @Scheduled(cron = "0 00 11 * * ?") //每天上午11点接入
    public void getLandsat() {
                System.out.println("-----------------------------");
                try {
                    //起始时间
                    String str="20200101";
                    //结束时间
                    String str1="20220311";
                    SimpleDateFormat format= new  SimpleDateFormat("yyyyMMdd");
                    Calendar start = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    try {
                        start.setTime(format.parse(str));
                        end.setTime(format.parse(str1));
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    while(start.before(end))
                    {
                        log.info("------ 开始下载" + format.format(start.getTime()) +"的影像-----");
                        String statue =  downloadLandsat(format.format(start.getTime()));
                        if (statue.equals("cookie error")){
                            log.info("------ cookie error 过期了，要更换cookie-----");
                        } else if(statue.equals("fail")){
                            log.info("------ landsat8下载失败-----");
                        }else if(statue.equals("success")){
                            log.info(format.format(start.getTime())+"-----获取影像成功！！！！！！！----");
                            DataCenterUtils.sendMessage("Landsat-8"+ format.format(start.getTime()), "Landsat-8","USGS获取的Landsat-8影像成功");
                        }
                        start.add(Calendar.DAY_OF_MONTH,1);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage());
                    log.info("GEE获取Landsat-8影像 " + "Status: Fail");
                    System.out.println(e.getMessage());
                }
    }




    /**
     * 通过url下载landsat文件，需要cookie的情况下
     *
     * @param url
     * @param fileName
     * @param savePath
     */
    public String downloadFromUrl(String url, String fileName, String savePath,int num) {
        String res = null;
        try {
            URL httpUrl = new URL(url);
            //其他方式可以见Proxy.Type属性
            HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Host", "hydro1.gesdisc.eosdis.nasa.gov");
            //用num作为状态码，先用num=2 使用预先存的cookie进行访问，如果 返回的是cookie过期，则用num=1进行访问，调用getcookie的方法进行登录
            if (num == 2){
                connection.setRequestProperty("Cookie", GoogleServiceConstant.landsat_cookie2);
            }else if(num == 1){
                connection.setRequestProperty("Cookie", getCookie());
            }
            //设置https协议访问
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");

            int code = connection.getResponseCode(); //访问状态码
            int length = connection.getContentLength(); //返回内容的大小
            if(code == 200){
                if(length == -1){
                    return "cookie过期";
                }else if(length < 1000){
                    return "no_data";
                }else{
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    //使用bufferedInputStream 缓存流的方式来获取下载文件，不然大文件会出现内存溢出的情况
                    inputStream = new BufferedInputStream(connection.getInputStream());
                    File saveDir = new File(savePath);
                    if (!saveDir.exists()) {
                        saveDir.mkdirs();
                    }
                    File file = new File(saveDir + File.separator + fileName);
                    outputStream = new FileOutputStream(file);

                    //这里也很关键每次读取的大小为5M 不一次性读取完
                    byte[] buffer = new byte[1024 * 1024 * 5];// 5MB

                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    inputStream.close();
                    res = savePath + fileName;
                }
            }else{
                return "error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return res;
    }



    /**
    * 5e83d14fec7cae84  Landsat 8-9 OLI/TIRS C2 L2的下载编号   5e81f14f92acf9ef Landsat 8-9 OLI/TIRS C2 L1的下载编号
    *   下载连接示例：
    *       https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/LC91270372022065LGN00
    *        https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/LC91270382022065LGN00
    *        https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/LC91270382022065LGN00
    *        https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/LC91300452022054LGN00
    *  长江经济带的行号和列号范围：
    *         117-135;
    *         035-046;
     * landsat命名规则：LC91300452022054LGN00
     * LC9 ：卫星名 Landsat9
     * 130 ：行号
    *  045 ：列号
     * 2022 ：年份
     * 054 ：该日期在该年份中的第几天 2月23日
     * LGN ：接站代码
     * 00 ：产品级别
    */
    public String downloadLandsat(String time) {
        String res = null;
        boolean finalres = false;
        for(int row = 35;row<=46;row++){
            for(int path = 117;path<=135;path++){
//                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
//                Calendar calendarNow = Calendar.getInstance();
//                String time = format.format(calendarNow.getTime());
                String day_num = YearMouthToday(time);
                String year = time.substring(0,4);
                Format f1=new DecimalFormat("000");
                String filename = "LC8"+path+f1.format(row)+year+day_num+"LGN00";
                String url = "https://earthexplorer.usgs.gov/download/5e81f14f92acf9ef/"+filename;
                res = downloadFromUrl(url, filename+".tar", savePath,2);
                 if (res == "cookie过期"){
                     res = downloadFromUrl(url, filename+".tar", savePath,1);
                    if(res == "error"){
                        return "cookie error";
                    }else if(res.equals("no_data")){
                        System.out.println(url+ " "+res);
                    }else{
                        finalres =  saveToDB(res);
                    }
                }else if(res.equals("no_data") || res.equals("error")){
                     System.out.println(url+ " "+res);
                 }else{
                     System.out.println("下载成功 : " +url+ " "+res);
                     finalres = saveToDB(res);
                 }
            }
        }
        if(finalres){
            return "success";
        }else{
            return "fail";
        }
    }

    public boolean saveToDB(String filepath){
        String unzippath = filepath.substring(0,filepath.indexOf(".tar"));
        Landsat landsat = new Landsat();
        int flag = 0 ;
        //解压
        try {
            TarArchiveInputStream tais = new TarArchiveInputStream(new FileInputStream(new File(filepath)));
            deTarFile(unzippath, tais);
            tais.close();
            String newfilepath =  renamefile(unzippath);
            String newfilename = newfilepath.substring(newfilepath.lastIndexOf("LC"));
            String doc = readJsonFile(newfilepath+File.separator+newfilename.replace("T1","RT")+"_stac.json");
            landsat =  getLandsatInfo(doc,newfilepath);
            flag =  landsatMapper.insertLandsat(landsat);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if(flag>0){
            return  true;
        }else{
            return  false;
        }
    }


    public String getCookie() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\chenlu\\Desktop\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.get("https://ers.cr.usgs.gov/login");
        WebElement username = driver.findElement(By.name("username"));
        WebElement password = driver.findElement(By.name("password"));
        WebElement login_button = driver.findElement(By.id("loginButton"));
        username.sendKeys("chenlu");
        password.sendKeys("Chenlu199704");
        login_button.click();
        String pageSource = driver.getPageSource();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.get("https://earthexplorer.usgs.gov/");
        Set<Cookie> cookies = driver.manage().getCookies();
        String cookieStr = "";
        for (Cookie cookie : cookies) {
            cookieStr += cookie.getName() + "=" + cookie.getValue() + "; ";
        }
        if (cookieStr.lastIndexOf(";") != -1) {
            cookieStr = cookieStr.substring(0, cookieStr.lastIndexOf(";"));
        }
        return cookieStr;
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

    public static void deTarFile(String destPath, TarArchiveInputStream tais) throws Exception {
        TarArchiveEntry tae = null;
        File saveFile = new File(destPath);
        if (!saveFile.exists()) {
            saveFile.mkdirs();
        }
        while ((tae = tais.getNextTarEntry()) != null) {
            String dir = destPath + File.separator +tae.getName();//tar档中文件
            File dirFile = new File(dir);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dirFile));
            int count;
            byte data[] = new byte[1024];
            while ((count = tais.read(data, 0, 1024)) != -1) {
                bos.write(data, 0, count);
            }
            bos.close();
        }
    }


    public static String renamefile(String destPath) {

        File f = new File(destPath);
        File list[] = f.listFiles();
        String filename = null;
        for (int i = 0; i < list.length; i++) {
            if (list[i].isFile()) {
                String temp = list[i].getName();
                if(temp.substring(temp.lastIndexOf("_")).equals("_stac.json")){
                  filename = temp.replace("RT","T1");
                    //想命名的原文件夹的路径
                    File file1 = new File(destPath);
                    //将原文件夹更改为A，其中路径是必要的
                   String newFilePath = destPath.substring(0,destPath.lastIndexOf("LC"))+filename;
                    file1.renameTo(new File(newFilePath));
                    return newFilePath;
                }
            }
        }
        return null;
    }


    public String readJsonFile(String jsonPath) {
        File jsonFile = new File(jsonPath);
        String jsonStr = "";
        try {
            //File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析JSON字符串信息
     */
    public Landsat getLandsatInfo(String document,String filepath) {
        Landsat res = new Landsat();
        if (!StringUtils.isBlank(document)) {
            JSONObject jsonObject = JSON.parseObject(document);
            JSONObject properties = jsonObject.getJSONObject("properties");

            res.setImageID(jsonObject.getString("id").replace("RT","T1"));
            res.setSensorID("OLI_TIRS");
            res.setSpacecraftID(properties.getString("platform"));
            res.setDate(str2Instant(properties.getString("datetime")).plusSeconds(8*60*60));
            res.setCloudcover(properties.getString("eo:cloud_cover"));
            res.setImageSize("30");
            res.setEllipsoid("WGS84");
            res.setImageType("ALL");
            JSONArray geoms = jsonObject.getJSONArray("bbox");
            String min_lon = geoms.getBigDecimal(0).toString();
            String min_lat = geoms.getBigDecimal(1).toString();
            String max_lon = geoms.getBigDecimal(2).toString();
            String max_lat = geoms.getBigDecimal(3).toString();
            String bbox_tmp = max_lon + ' ' + max_lat + ',' + min_lon + ' ' + max_lat + ','
                    + min_lon + ' ' + min_lat + ',' + max_lon+ ' ' + min_lat + ',' + max_lon + ' ' + max_lat;
            String wkt = "POLYGON((" + bbox_tmp  + "))";
            res.setCoordinates(wkt);
            res.setFilePath(filepath);
        }
        return res;
    }

    public Instant str2Instant(String time) {
        time = time.substring(0, time.indexOf(".")).replace("T"," ");
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        dateTimeFormatter.withZone(ZoneId.of("Asia/Shanghai"));
        LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
        return localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
    }

//
//    public boolean getLandsatByUSGS() throws IOException {
//        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(new BasicAuthInterceptor("chenlu", "Chenlu199704"))
//                .build();
//        final Request request = new Request.Builder()
//                .url("https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/LC91270372022065LGN00")
//                .build();
//        Response response = client.newCall(request).execute();
////        String responseFileName = getHeaderFileName(response);
//
//
//        InputStream inputStream = response.body().byteStream();
//        byte[] getData = readInputStream(inputStream);
//        if(getData.length<1000){ //如果网页返回值小于1mb ,则返回空
//            return false;
//        }else{
//            // 文件保存位置
//            File saveDir = new File("E:/ladds/");
//            if (!saveDir.exists()) {
//                boolean flag = saveDir.mkdir();
//            }
//            File file = new File(saveDir + File.separator + "LC91270372022065LGN00");
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(getData);
//            fos.close();
//            inputStream.close();
//            return true;
//        }
//    }


//    /**
//     * 解析文件头
//     * Content-Disposition:attachment;filename=FileName.txt
//     * Content-Disposition: attachment; filename*="UTF-8''%E6%9B%BF%E6%8D%A2%E5%AE%9E%E9%AA%8C%E6%8A%A5%E5%91%8A.pdf"
//     */
//    private static String getHeaderFileName(Response response) {
//        String dispositionHeader = response.header("Content-Disposition");
//        if (!TextUtils.isEmpty(dispositionHeader)) {
//            dispositionHeader.replace("attachment;filename=", "");
//            dispositionHeader.replace("filename*=utf-8", "");
//            String[] strings = dispositionHeader.split("; ");
//            if (strings.length > 1) {
//                dispositionHeader = strings[1].replace("filename=", "");
//                dispositionHeader = dispositionHeader.replace("\"", "");
//                return dispositionHeader;
//            }
//            return "";
//        }
//        return "";
//    }


}

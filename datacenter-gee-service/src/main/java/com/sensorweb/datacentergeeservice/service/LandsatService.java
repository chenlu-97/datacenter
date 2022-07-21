package com.sensorweb.datacentergeeservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacentergeeservice.config.OkHttpUtil;
import com.sensorweb.datacentergeeservice.dao.LandsatMapper;
import com.sensorweb.datacentergeeservice.entity.Landsat;
import com.sensorweb.datacentergeeservice.util.GoogleServiceConstant;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.Format;
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
public class LandsatService implements GoogleServiceConstant{
    @Autowired
    LandsatMapper landsatMapper;

    @Autowired
    OkHttpUtil okHttpUtil;

    @Value("${datacenter.path.save}")
    private String savePath;

    @Value("${datacenter.path.cookie}")
    private String cookiePath;


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



    @Scheduled(cron = "0 00 12 * * ?") //每天12点接入
    public void insertData() {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                System.out.println("-----------------------------");
                //获取数据库中最新的数据时间
                List<Landsat> landsats = landsatMapper.selectNew();
                String timeNew = landsats.get(0).getDate().toString();
                timeNew = timeNew.replace("T", " ").replace("Z", "").substring(0, timeNew.indexOf("T")).replace("-", "");
                //获取当天的时间
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                Calendar calendarNow = Calendar.getInstance();
                String timeNow = format.format(calendarNow.getTime());
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                try {
                    try {
                        start.setTime(format.parse(timeNew));
                        end.setTime(format.parse(timeNow));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    while (start.before(end)) {
                        log.info("------ 开始下载" + format.format(start.getTime()) + "的影像-----");
                        String statue1 = downloadLandsat(format.format(start.getTime()),"https://earthexplorer.usgs.gov/download/5e81f14f92acf9ef/","L1TP"); //L1TP
                        String statue2 = downloadLandsat(format.format(start.getTime()),"https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/","L2SP"); //L2SP
                        if (statue1.equals("cookie error")||statue2.equals("cookie error")) {
                            log.info("------ cookie error 过期了，要更换cookie-----");
                        } else if (statue1.equals("fail")||statue2.equals("fail")) {
                            log.info("------ landsat8下载失败-----");
                        } else if (statue1.equals("success")||statue2.equals("success")) {
                            log.info(format.format(start.getTime()) + "-----获取影像成功！！！！！！！----");
                            DataCenterUtils.sendMessage("Landsat-8" + format.format(start.getTime()), "Landsat-8", "USGS获取的Landsat-8影像成功");
                        }
                        start.add(Calendar.DAY_OF_MONTH, 1);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage());
                    log.info("GEE获取Landsat-8影像 " + "Status: Fail");
                    System.out.println(e.getMessage());
                    SendException("landsat",timeNew,timeNow+"当前接入LandSat的数据失败");

                }
            }
        }).start();
    }


    /**
     * 通过url下载landsat文件
     *
     * @param url
     * @param fileName
     * @param savePath
     */
    public String downloadFromUrl(String url, String fileName, String savePath) {
        String res = null;
        try {
            URL httpUrl = new URL(url);
            //其他方式可以见Proxy.Type属性
            HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Host", "hydro1.gesdisc.eosdis.nasa.gov");
//            connection.setRequestProperty("Cookie", DataCenterUtils.readTxt(cookiePath));
            connection.setRequestProperty("Cookie", landsat_cookie);
            //设置https协议访问
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");

//            int code = connection.getResponseCode(); //访问状态码
            int length = connection.getContentLength(); //返回内容的大小
//            System.out.println("访问状态码code = " + code);
            if(length == -1){
                return "cookie过期";
            }else if(length < 1000) {
                return "no_data";
            }
            else if(length < 100000 && length>1000){
                    return "cookie过期";
            }
            else{
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
                res = savePath + File.separator +fileName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return res;
    }


//    public String getlandsatbyid(String time) throws IOException {
//        for (int row = 35; row <= 46; row++) {
//            for (int path = 117; path <= 135; path++) {
//                String day_num = YearMouthToday(time);
//                String year = time.substring(0, 4);
//                String mounth = time.substring(5, 7);
//                Format f1 = new DecimalFormat("000");
//                String filename = "LC8" + path + f1.format(row) + year + day_num + "LGN00";
//            }
//        }
//    }



    /**
    * 5e83d14fec7cae84  Landsat 8-9 OLI/TIRS C2 L2 SP的下载编号   5e81f14f92acf9ef Landsat 8-9 OLI/TIRS C2 L1  TP的下载编号
    *   下载连接示例：
    *       https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/LC91270372022065LGN00
    *        https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/LC91270382022065LGN00
    *        https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/LC91270382022065LGN00
    *        https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/LC91300452022054LGN00
    *  长江经济带的行号和列号范围：
    *         117-135;
    *         035-046;
     * landsat命名规则：LC81300452022054LGN00
     * LC9 ：卫星名 Landsat8
     * 130 ：行号
    *  045 ：列号
     * 2022 ：年份
     * 054 ：该日期在该年份中的第几天 2月23日
     * LGN ：接站代码
     * 00 ：产品级别
     *
     * https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/
    */
    public String downloadLandsat(String time,String baseUrl,String type) throws IOException {
        String res = null;
        boolean finalres = false;
        for(int row = 35;row<=46;row++){
            for(int path = 117;path<=135;path++){

                Instant startTime = string2Instant(time);
                Instant endTime = string2Instant(time).plusSeconds(24*60*60);
                List<Landsat> landsat_exit = landsatMapper.getLandsat2(type,row,path,startTime,endTime);
                if(landsat_exit.size()>0){
//                    System.out.println("已经存在该影像！！！");
                }else{
//                    System.out.println("没有该影像，需要下载，下载中！！！");
                    String day_num = YearMouthToday(time);
                    String year = time.substring(0,4);
                    String mounth = time.substring(4,6);
                    Format f1=new DecimalFormat("000");
                    String filename = "LC8"+path+f1.format(row)+year+day_num+"LGN00";
                    String url = baseUrl+filename;
                    res = downloadFromUrl(url, filename+".tar", savePath+  year +File.separator + mounth);
                    if (res == "cookie过期"){
//                   getCookie();
                        res = downloadFromUrl(url, filename+".tar", savePath+ year +File.separator + mounth);
                        if(res == "cookie过期"){
                            return "cookie error";
                        }else if(res.equals("no_data")){

                        }else if(res.equals("error")){
                            return "fail";
                        }
                        else{
                            System.out.println("下载成功 : " +url+ " "+res);
                            finalres =  saveToDB(res);
                        }
                    }else if(res.equals("no_data")){
//                     System.out.println(url+ " "+res);
                    }else if(res.equals("error")){
                        return "fail";
                    }else{
                        System.out.println("下载成功 : " +url+ " "+res);
                        finalres = saveToDB(res);
                    }
                }
            }
        }
        if(finalres){
            return "success";
        }else{
            return "fail";
        }
    }


    /**
     * 对文件夹解压、更名、获取xml信息、最终报存到数据库
     */
    public boolean saveToDB(String filepath) throws IOException {
        String unzippath = filepath.substring(0,filepath.indexOf(".tar"));
        Landsat landsat = new Landsat();
        String newfilepath = null;
        int flag = 0 ;
        //解压、更名、获取信息
        try {
            if(readTarFile(filepath)){ //解压前先判断是不是T1数据，如果是执行下面步骤，不是则删除下载的压缩包
                TarArchiveInputStream tais = new TarArchiveInputStream(new FileInputStream(new File(filepath)));
                deTarFile(unzippath, tais);//解压
                tais.close();
                deleteFileOrDirectory(filepath);//删除压缩包
                newfilepath = renamefile(unzippath);//文件夹重命名
                String newfilename = newfilepath.substring(newfilepath.lastIndexOf("LC"));
                String doc = readJsonFile(newfilepath+File.separator+newfilename+"_stac.json");//读取json文件获取信息
                landsat =  getLandsatInfo(doc,newfilepath);// 打包文件信息，变为landsat对象
                flag =  landsatMapper.insertLandsat(landsat);//插入表
            }else{
                deleteFileOrDirectory(filepath);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if(flag>0){
            try {
            if(newfilepath !=null){
//                generateThumb(newfilepath);//生成缩略图
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar calendarNow = Calendar.getInstance();
                String timeNow = format.format(calendarNow.getTime());
                submitRawImage(newfilepath,timeNow);//下载后提交初始文件
//                changeAuthority(newfilepath);//更改文件权限
            }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return  true;
        }else{
            return  false;
        }
    }



    /**
     * 用浏览器驱动进行登录获取网站登录cookie
     */
    public void getCookie() throws MalformedURLException {
        WebDriver driver = new RemoteWebDriver(new URL("http://172.16.100.2:5555/"), DesiredCapabilities.internetExplorer());
        driver.manage().window().maximize();
//        System.setProperty("webdriver.chrome.driver", "C:\\Users\\chenlu\\Desktop\\chromedriver.exe");
//        WebDriver driver = new ChromeDriver();
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
        //将获取的cookie保存到txt文件下
        DataCenterUtils.clearInfoForFile(cookiePath);
        DataCenterUtils.writeTxt(cookiePath,cookieStr);
    }


    /**
     * 年月日转化为当年的第多少天（闰年+1）
     * 20200105 = 5
     */
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

    /**
     * 对文件进行解压
     */
    public static void deTarFile(String destPath, TarArchiveInputStream tais) throws Exception {

        TarArchiveEntry tae = null;
        File saveFile = new File(destPath);
        if (!saveFile.exists()) {
            saveFile.mkdirs();
        }
        while ((tae = tais.getNextTarEntry()) != null) {
            if (tae.isDirectory()) {
                System.out.println("文件夹");
            } else {
                String dir = destPath + File.separator + tae.getName();//tar档中文件
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
    }

    /**
     * 更改文件名称
     */
    public static String renamefile(String destPath) {
        File f = new File(destPath);
        File list[] = f.listFiles();
        String filename = null;
        for (int i = 0; i < list.length; i++) {
            if (list[i].isFile()) {
                String temp = list[i].getName();
                if(temp.substring(temp.lastIndexOf("_")).equals("_stac.json")){
                  filename = temp.substring(0,temp.indexOf("_stac.json"));
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

    /**
     * 读取解压后文件夹中xml文件中的影像元信息
     */
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
            res.setImageID(jsonObject.getString("id"));
            res.setSensorID("OLI_TIRS");
            res.setSpacecraftID(properties.getString("platform"));
            res.setDate(str2Instant(properties.getString("datetime")).plusSeconds(8*60*60));
            res.setCloudcover(Float.valueOf(properties.getString("eo:cloud_cover")));
            res.setImageSize("30");
            res.setEllipsoid("WGS84");
            res.setImageType(properties.getString("landsat:correction"));
            res.setThumburl(jsonObject.getJSONObject("assets").getJSONObject("reduced_resolution_browse").getString("href"));
            res.setPath(Integer.valueOf(properties.getString("landsat:wrs_path")));
            res.setRow(Integer.valueOf(properties.getString("landsat:wrs_row")));
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

    public List<Landsat> getFilePath(Instant startTime, Instant endTime,String type) {
        return landsatMapper.getFilePath(0,null,startTime,endTime,type);
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



//    @Scheduled(cron = "0 * * * * *")
//    public void autoThumb(){
//        generateThumb("/data/Ai-Sensing/ProcessCenter/TestData/landsat/LC08_L1TP_123039_20200108_20200824_02_T1");
//    }

    public static void generateThumb(String filePath) {
        String[] params = new String[3];
        params[0] = "/bin/sh";
        params[1] = "-c";
        String thumb432 = "srun /home/deployadmin/anaconda3/envs/modis/bin/gdal_merge.py -separate -o merged_432.tif *T1_B{4,3,2}.TIF && /home/deployadmin/anaconda3/envs/modis/bin/gdal_translate -of JPEG -scale -co worldfile=yes merged_432.tif merged_432_translate.jpg";
        String thumb753 = "srun /home/deployadmin/anaconda3/envs/modis/bin/gdal_merge.py -separate -o merged_753.tif *T1_B{7,5,3}.TIF && /home/deployadmin/anaconda3/envs/modis/bin/gdal_translate -of JPEG -scale -co worldfile=yes merged_753.tif merged_753_translate.jpg";
        params[2] = "cd " + filePath + " && " + thumb432 + " && " + thumb753;
        String result = exe(params);
        System.out.println(result);
    }

//    public static void changeAuthority(String filePath) {
//        String[] params = new String[1];
//        params[0] = "chown -R 1030:1030 " + filePath;
//        String result = exe(params);
//        System.out.println(result);
//    }

    private static String exe(String[] params) {
        StringBuilder sb = new StringBuilder();
        Process proc;
        try {
            proc = Runtime.getRuntime().exec(params);
            proc.waitFor();
            //用输入输出流来截取结果
            SequenceInputStream sis = new SequenceInputStream(proc.getInputStream(), proc.getErrorStream());
            InputStreamReader isr = new InputStreamReader(sis, Charset.forName("GBK"));
            BufferedReader in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            proc.destroy();
            in.close();
            isr.close();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return  sb.toString();
    }

    private void submitRawImage(String filepath,String time) throws IOException {

        String url = "http://ai-ecloud.whu.edu.cn/gateway/ai-sensing-open-service/product/submitRawImage";
        JSONObject param = new JSONObject();
        List<String> fieldList = new ArrayList<>();
        fieldList.add(filepath);
        param.put("type", "LandSat");
        param.put("fieldList", fieldList);
        param.put("details",URLEncoder.encode(time +"接入","utf-8"));
        try {
            okHttpUtil.doPostJson(url,param.toString());
            System.out.println("原始影像产品生成成功！！！");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("原始影像产品生成失败！！！");
        }
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



    /**
     * 删除文件或文件夹
     *
     * @param fileName 文件名
     * @return 删除成功返回true,失败返回false
     */
    public static boolean deleteFileOrDirectory(String fileName) {
        File file = new File(fileName);  // fileName是路径或者file.getPath()获取的文件路径
        if (file.exists()) {
            if (file.isFile()) {
                return deleteFile(fileName);  // 是文件，调用删除文件的方法
            } else {
                return deleteDirectory(fileName);  // 是文件夹，调用删除文件夹的方法
            }
        } else {
            System.out.println("文件或文件夹删除失败：" + fileName);
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     * @return 删除成功返回true,失败返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            file.delete();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除文件夹
     * 删除文件夹需要把包含的文件及文件夹先删除，才能成功
     *
     * @param directory 文件夹名
     * @return 删除成功返回true,失败返回false
     */
    public static boolean deleteDirectory(String directory) {
        // directory不以文件分隔符（/或\）结尾时，自动添加文件分隔符，不同系统下File.separator方法会自动添加相应的分隔符
        if (!directory.endsWith(File.separator)) {
            directory = directory + File.separator;
        }
        File directoryFile = new File(directory);
        // 判断directory对应的文件是否存在，或者是否是一个文件夹
        if (!directoryFile.exists() || !directoryFile.isDirectory()) {
            System.out.println("文件夹删除失败，文件夹不存在" + directory);
            return false;
        }
        boolean flag = true;
        // 删除文件夹下的所有文件和文件夹
        File[] files = directoryFile.listFiles();
        for (int i = 0; i < files.length; i++) {  // 循环删除所有的子文件及子文件夹
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {  // 删除子文件夹
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }

        if (!flag) {
            System.out.println("删除失败");
            return false;
        }
        // 最后删除当前文件夹
        if (directoryFile.delete()) {
            System.out.println("删除成功：" + directory);
            return true;
        } else {
            System.out.println("删除失败：" + directory);
            return false;
        }
    }



    /**
     * 重载递归解压读取文件
     * @param filepath
     * @throws IOException
     */
    public boolean readTarFile(String filepath) throws IOException {
        File zf = new File(filepath);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zf));
        TarArchiveInputStream zis = new TarArchiveInputStream(bis);
        TarArchiveEntry ze;
        while ((ze = zis.getNextTarEntry()) != null) {
            if (ze.isDirectory()) {
                System.out.println("A directory entry is");
            } else {
                if (ze.getName().endsWith("T1_stac.json") || ze.getName().endsWith("T1_ST_stac.json")) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Landsat> getLandsat(String type,Instant startTime, Instant endTime) {
        return landsatMapper.getLandsat(type,startTime,endTime);

    }

    public static Instant string2Instant(String date) {
        Date time = null;
        try {
            time = new SimpleDateFormat("yyyyMMdd").parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (time!=null) {
            return time.toInstant();
        }
        return null;
    }

}

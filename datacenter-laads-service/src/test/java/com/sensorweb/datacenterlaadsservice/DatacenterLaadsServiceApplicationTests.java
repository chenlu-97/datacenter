package com.sensorweb.datacenterlaadsservice;

import com.sensorweb.datacenterlaadsservice.dao.EntryMapper;
import com.sensorweb.datacenterlaadsservice.dao.GLDASMapper;
import com.sensorweb.datacenterlaadsservice.service.InsertGLDASService;
import com.sensorweb.datacenterlaadsservice.service.InsertGPM_3IMERGDE;
import com.sensorweb.datacenterlaadsservice.service.InsertLAADSService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@SpringBootTest
class DatacenterLaadsServiceApplicationTests {

    @Autowired
    private InsertLAADSService insertLAADSService;
    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private InsertGLDASService insertGLDASService;
    @Autowired
    private GLDASMapper gldasMapper;
    @Autowired
    private InsertGPM_3IMERGDE insertGPM_3IMERGDE;
    @Test
    void contextLoads() throws Exception {
        //测试
//        String startTime = "2021-11-15 00:00:00";
//        String endTime = "2021-11-18 00:00:00";
//        String bbox = "90.55,24.5,112.417,34.75";
//        String productName = "MOD11A1";
//        String test = insertLAADSService.insertData2(startTime, endTime, bbox, productName);
//        System.out.println(test);
        //测试
//          boolean test = insertLAADSService.insertData3("2021-09-01T00:00:00Z");
//          System.out.println(test);

        //测试
//        String time = entryMapper.selectNew().getStart().plusSeconds(24*60*60).toString();
//        System.out.println("time = " + time.substring(0, time.indexOf("T")).replace("-",""));


        //测试
//        List<Entry> entryList = new ArrayList<>();
//        Entry entry = new Entry();
//        entry.setSatellite("1");
//        entry.setEntryId("12");
//        entry.setBbox("12133");
//        entry.setSummary("1213");
//        entry.setWkt("POLYGON((119.88921295665621 32.43809123148852,120.7378487376959 32.272775667771676,121.52392173553908 32.11392926097289,121.52713013170285 32.1250148794685,121.59554547834652 32.36691622530554,121.6659985358775 32.61475609245087,121.73618880488671 32.86067740609684,121.80761210153415 33.10986251355583,121.88699656332598 33.38531657669484,121.94369387763581 33.58111802621447,122.01792679103042 33.83646201756913,122.01790658055839 33.838230418830506,121.5224570788418 33.93729939581788,120.67528383639105 34.10151678294821,120.32548043822243 34.1674169945179,120.007483607093 34.22636121664424,119.89051945454977 33.78834710068756,119.77884510492372 33.36646510952478,119.66808149921208 32.94452360765576,119.55290998356116 32.50211881560613,119.55432986776898 32.5015650616325,119.88921295665621 32.43809123148852))");
//        entryList.add(entry);
//        entryList.add(entry);
//        entryMapper.insertDataBatch(entryList);

//            String time = "2021-09-21T00:00:00Z";
//            String time1 = time.replace("T"," ").replace("Z","");
//            String pattern = "yyyy-MM-dd HH:mm:ss";
//            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
//            dateTimeFormatter.withZone(ZoneId.of("Asia/Shanghai"));
//            LocalDateTime localDateTime = LocalDateTime.parse(time1, dateTimeFormatter);
//            System.out.println(localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant());
//
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DATE, -1);
//        String stop = format.format(calendar.getTime());
//        calendar.add(Calendar.DATE, -1);
//        String start = format.format(calendar.getTime()).replace("00:00:00", "23:59:59");
//        System.out.println("stop = " + stop);
//        System.out.println("start = " + start);
//        insertGLDASService.insertDataByCookie("5358371643-download.txt");
//        insertGLDASService.getCookie("https://hydro1.gesdisc.eosdis.nasa.gov/data/GLDAS/GLDAS_NOAH025_3H_EP.2.1/2022/039/GLDAS_NOAH025_3H_EP.A20220208.0000.021.nc4");
//          insertGLDASService.getGLDASByBasic("2022-02-01T00:00:00Z");



//        try {
//            String time = null;
//            if (gldasMapper.selectNew() != null) {
//                time = gldasMapper.selectNew().getTime().plusSeconds(12*60*60).toString();
//                System.out.println("time = " + time);
//            }
//            boolean flag = insertGLDASService.insertData(time);
//            if (flag) {
////                log.info("GLDAS接入时间: " + time + "Status: Success");
//                System.out.println("GLDAS接入时间: " + time + "Status: Success");
//            }else{
////                log.error("GLDAS还未更新数据  "+time);
//                System.out.println("GLDAS还未更新数据  "+time );
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
////            log.error(e.getMessage());
////            log.error("GLDAS接入时间: " + calendar.getTime().toString() + "Status: Fail");
////            System.out.println("GLDAS接入时间: " + calendar.getTime().toString() + "Status: Fail");
//        }

//        insertGLDASService.update();


//        String startTime = "2022-03-01 00:00:00";
//        String endTime = "2022-03-13 00:00:00";
//
//        SimpleDateFormat format= new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Calendar start = Calendar.getInstance();
//        Calendar end = Calendar.getInstance();
//        try {
//            start.setTime(format.parse(startTime));
//            end.setTime(format.parse(endTime));
//        } catch (java.text.ParseException e) {
//            e.printStackTrace();
//        }
//        while(start.before(end))
//        {
//            System.out.println("start = " + format.format(start.getTime()));
//            start.add(Calendar.DAY_OF_MONTH,1);
//        }
//            String[] products = new String[]{"MOD11A1", "MOD11A2", "MYD11A1", "MOD13A2", "MCD19A2"};
//            String[] products = new String[]{"MOD11A1"};
//            boolean flag;
//            LocalDateTime dateTime = LocalDateTime.now();
//            DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");
//            String strDate3 = dtf3.format(dateTime);
//            dateTime = LocalDateTime.parse(strDate3, dtf3);
//            for (String product : products) {
//                try {
//                        Instant timeNew = entryMapper.selectMaxTimeData(product).get(0).getStart();
//                        Instant timeNow = dateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().minusSeconds(24*60*60);
//                        while (timeNew.isBefore(timeNow)) {
//                            String bbox = "95,24,123,35";//长江流域经纬度范围
//                            String code = null;
//                            try {
//                                String start = timeNew.plusSeconds(24 * 60 * 60).toString();
//                                start = start.substring(0, start.indexOf("T")) + " 00:00:00";
//                                String stop = timeNew.plusSeconds(48 * 60 * 60).toString();
//                                stop = stop.substring(0, stop.indexOf("T")) + " 00:00:00";
//                                System.out.println("start = " + start);
//                                System.out.println("stop = " + stop);
////                                code = insertLAADSService.insertData2(start, stop, bbox, product);
////                                System.out.println(product+"----LAADS接入时间: " + timeNew + "-----Status: -----" + code);
//                            } catch (Exception e) {
//                                System.out.println(e.getMessage());
//                                System.out.println(product+"----LAADS接入时间----: " + timeNew + "-----Status: Fail----" + code);
//                            }
//                            timeNew = timeNew.plusSeconds(48 * 60 * 60);
//                        }
//                    }catch (Exception e) {
//                     System.out.println(e.getMessage());
//                }
//            }

//        insertGLDASService.insertData("2022-05-01T00:00:00Z");
        insertGPM_3IMERGDE.insertData("2022-06-01T00:00:00Z");
    }
}

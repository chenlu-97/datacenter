package com.sensorweb.datacentergeeservice;

import com.sensorweb.datacentergeeservice.dao.LandsatMapper;
import com.sensorweb.datacentergeeservice.entity.Landsat;
import com.sensorweb.datacentergeeservice.service.LandsatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class DatacenterGeeServiceApplicationTests {

    @Autowired
    private LandsatService googleService;

    @Autowired
    LandsatMapper landsatMapper;

    @Test
    void contextLoads() throws Exception {
////        String path = "D:\\OneDrive\\GoogleEarthEngine\\GetGeoInfo-d670c0a18233.json";
//        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/GetGeoInfo-d670c0a18233.json"));
////        GoogleCredentials googleCredentials = googleService.getCredentials();

//        System.out.println("googleService.downloadLandsat(\"\") = " + googleService.downloadLandsat(""));
//        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
//        Calendar calendarNow = Calendar.getInstance();
//        System.out.println("calendarNow.getTime() = " + format.format(calendarNow.getTime()));

//
//        googleService.renamefile("E:\\landsat\\LC81300372022062LGN00");

//        System.out.println( googleService.readJsonFile("E:\\landsat\\LC08_L2SP_130037_20220303_20220309_02_T1\\LC08_L2SP_130037_20220303_20220309_02_T1_ST_stac.json"));

//        googleService.downloadLandsat("20220306");

//        List<Landsat> landsats =  landsatMapper.selectNew();
//        String time = landsats.get(0).getDate().toString();
//        time = time.replace("T", " ").replace("Z", "").substring(0, time.indexOf("T")).replace("-", "");

//        googleService.saveToDB("/Users/chenlu/Desktop/landsat/LC08_L2SP_025032_20220404_20220412_02_T1.tar");
    }

}

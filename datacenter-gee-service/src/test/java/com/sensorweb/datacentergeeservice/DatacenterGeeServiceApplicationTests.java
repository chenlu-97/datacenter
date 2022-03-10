package com.sensorweb.datacentergeeservice;

import com.sensorweb.datacentergeeservice.service.LandsatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class DatacenterGeeServiceApplicationTests {

    @Autowired
    private LandsatService googleService;
    @Test
    void contextLoads() throws IOException {
////        String path = "D:\\OneDrive\\GoogleEarthEngine\\GetGeoInfo-d670c0a18233.json";
//        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/GetGeoInfo-d670c0a18233.json"));
////        GoogleCredentials googleCredentials = googleService.getCredentials();

        System.out.println("googleService.downloadLandsat(\"\") = " + googleService.downloadLandsat(""));
//        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
//        Calendar calendarNow = Calendar.getInstance();
//        System.out.println("calendarNow.getTime() = " + format.format(calendarNow.getTime()));

//
//        //解压
//        try {
//            TarArchiveInputStream tais = new TarArchiveInputStream(new FileInputStream(new File("E:\\landsat\\LC81300372022062LGN00.tar")));
//            googleService.deTarFile("E:\\landsat\\LC81300372022062LGN00", tais);
//            tais.close();
//            System.out.println("解压成功");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        googleService.renamefile("E:\\landsat\\LC81300372022062LGN00");

//        System.out.println( googleService.readJsonFile("E:\\landsat\\LC08_L2SP_130037_20220303_20220309_02_T1\\LC08_L2SP_130037_20220303_20220309_02_T1_ST_stac.json"));



    }

}

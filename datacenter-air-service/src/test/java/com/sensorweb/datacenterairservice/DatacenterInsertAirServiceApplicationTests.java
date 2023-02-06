package com.sensorweb.datacenterairservice;

import com.sensorweb.datacenterairservice.service.InsertAirService;
import com.sensorweb.datacenterairservice.service.NewInsertChinaAirService;
import com.sensorweb.datacenterairservice.service.SuperStationService;
import com.sensorweb.datacenterairservice.service.WaterQualityWaterstationHourlyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DatacenterInsertAirServiceApplicationTests {
//    @Autowired
//    private ObsFeignClient obsFeignClient;
//
//    @Autowired
//    private ChinaAirQualityHourMapper chinaAirQualityHourMapper;
//    @Autowired
//    private AirQualityHourMapper airQualityHourMapper;
//    @Autowired
//    private AirStationMapper airStationMapper;

    @Test
    void contextLoads() throws Exception {
//        String bbox = "180 0,180 0";
//        boolean isWH = obsFeignClient.insertBeforeSelectWHSpa(bbox);
//        boolean isCJ = obsFeignClient.insertBeforeSelectCJSpa(bbox);
//        System.out.println(isCJ);
//        System.out.println(isWH);
//        AirQualityHour airQualityHour = new AirQualityHour();
//        airQualityHour.setUniqueCode("123");
//        int status = airQualityHourMapper.insertData(airQualityHour);
//        System.out.println(status);

//        List<AirStationModel> stations = airStationMapper.selectByAll();
//        for(AirStationModel station:stations){
//            String wkt = "POINT(" +station.getLon() + " " + station.getLat() + ")";
//            System.out.println(wkt);
//            boolean res = airStationMapper.updateGeom(wkt,station.getLon(),station.getLat());
//            System.out.println(res);
////        }
//            DataCenterUtils.sendMessage("CH_AIR", "全国空气质量","这是一条获取的全国空气质量数据");

//        LocalDateTime begin_date = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
//        LocalDateTime end_date = begin_date.plusSeconds(24*60*60);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Shanghai"));
//        String begin = formatter.format(begin_date);
//        String end = formatter.format(end_date);
//        System.out.println("begin = " + begin);
//        System.out.println("end = " + end);


//        insertAirService.insertDataByHour();
//        insertAirService.insertDataByHour2();
//        Thread.sleep(2 * 60 * 1000);
//        insertAirService.insertHourDataByHour("2022-07-07 10:00:00");
//        superStationService.accessinforming12313();
    }


}

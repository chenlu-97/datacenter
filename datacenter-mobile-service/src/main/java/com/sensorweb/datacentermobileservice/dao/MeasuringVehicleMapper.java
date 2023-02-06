package com.sensorweb.datacentermobileservice.dao;


import com.sensorweb.datacentermobileservice.entity.MeasuringVehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface MeasuringVehicleMapper {



    int insertVocsData(MeasuringVehicle measuringVehicle);

    int insertAirData(MeasuringVehicle measuringVehicle);

    int insertPMData(MeasuringVehicle measuringVehicle);

    int insertSPMSData(MeasuringVehicle measuringVehicle);

    int insertHTData(MeasuringVehicle measuringVehicle);

//    List<MeasuringVehicle> selectByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    int selectNum1();

    int selectNum2();

    int selectNum3();

    int selectNum4();

    int selectNum5();

    int selectNumByTime1(@Param("begin") Instant begin, @Param("end") Instant end);
    int selectNumByTime2(@Param("begin") Instant begin, @Param("end") Instant end);
    int selectNumByTime3(@Param("begin") Instant begin, @Param("end") Instant end);
    int selectNumByTime4(@Param("begin") Instant begin, @Param("end") Instant end);
    int selectNumByTime5(@Param("begin") Instant begin, @Param("end") Instant end);



    int insertRadioData(MeasuringVehicle measuringVehicle);

    List<MeasuringVehicle> selectByTime(@Param("begin")Instant begin, @Param("end") Instant end);



    List<Map> getVocsByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("start")Instant start, @Param("end")Instant end);

    List<Map> getHTByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("start")Instant start,  @Param("end")Instant end);

    List<Map> getPMByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("start")Instant start,  @Param("end")Instant end);

    List<Map> getAirByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("start")Instant start,  @Param("end")Instant end);

    List<Map> getSPMSByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("start")Instant start,  @Param("end")Instant end);

    int getDataNum1(@Param("start") Instant start, @Param("end") Instant end,@Param("spa") String spa,@Param("id") String id);

    int getDataNum2(@Param("start") Instant start, @Param("end") Instant end,@Param("spa") String spa,@Param("id") String id);

    int getDataNum3(@Param("start") Instant start, @Param("end") Instant end,@Param("spa") String spa,@Param("id") String id);

    int getDataNum4(@Param("start") Instant start, @Param("end") Instant end,@Param("spa") String spa,@Param("id") String id);

    int getDataNum5(@Param("start") Instant start, @Param("end") Instant end,@Param("spa") String spa,@Param("id") String id);
}

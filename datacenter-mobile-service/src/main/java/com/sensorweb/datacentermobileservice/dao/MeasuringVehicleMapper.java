package com.sensorweb.datacentermobileservice.dao;


import com.sensorweb.datacentermobileservice.entity.MeasuringVehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

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

    List<MeasuringVehicle> getVocsByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("start")Instant start,  @Param("end")Instant end);

    List<MeasuringVehicle> getHTByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("start")Instant start,  @Param("end")Instant end);

    List<MeasuringVehicle> getPMByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("start")Instant start,  @Param("end")Instant end);

    List<MeasuringVehicle> getAirByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("start")Instant start,  @Param("end")Instant end);

    List<MeasuringVehicle> getSPMSByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("start")Instant start,  @Param("end")Instant end);

}

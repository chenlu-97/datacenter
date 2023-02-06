package com.sensorweb.datacenterairservice.dao;


import com.sensorweb.datacenterairservice.entity.AirQualityHour;
import com.sensorweb.datacenterairservice.entity.RawDataSuperStationHourly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
* @author Amour
* @description 针对表【raw_data_super_station_hourly】的数据库操作Mapper
* @createDate 2022-06-22 11:23:38
* @Entity boot.bean.RawDataSuperStationHourly
*/

@Repository
@Mapper
public interface RawDataSuperStationHourlyMapper {
    int store(RawDataSuperStationHourly rawDataSuperStationHourly);

    int store2(RawDataSuperStationHourly rawDataSuperStationHourly);

    List<RawDataSuperStationHourly> selectMaxTimeData();

    int selectNum();

    int selectByTime(@Param("begin") Instant begin, @Param("end") Instant end);

    int insertDataBatch(List<RawDataSuperStationHourly> rawDataSuperStationHourly);
}

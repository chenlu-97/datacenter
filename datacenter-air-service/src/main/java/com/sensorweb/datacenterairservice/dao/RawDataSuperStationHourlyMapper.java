package com.sensorweb.datacenterairservice.dao;


import com.sensorweb.datacenterairservice.entity.RawDataSuperStationHourly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
* @author Amour
* @description 针对表【raw_data_super_station_hourly】的数据库操作Mapper
* @createDate 2022-06-22 11:23:38
* @Entity boot.bean.RawDataSuperStationHourly
*/
@Mapper
public interface RawDataSuperStationHourlyMapper {
    int store(RawDataSuperStationHourly rawDataSuperStationHourly);

    List<RawDataSuperStationHourly> selectMaxTimeData();

    int selectNum();

    int selectByTime(@Param("begin") Instant begin, @Param("end") Instant end);
}

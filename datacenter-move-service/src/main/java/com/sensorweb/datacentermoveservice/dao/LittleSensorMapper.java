package com.sensorweb.datacentermoveservice.dao;

import com.sensorweb.datacentermoveservice.entity.LittleSensor;
import com.sensorweb.datacentermoveservice.entity.LittleSensor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface LittleSensorMapper {
    int insertData(LittleSensor entry);
    int insertDataBatch(List<LittleSensor> entries);

    LittleSensor selectById(int id);

    int selectNum();

    int selectNum2(@Param("start") Instant start, @Param("end") Instant end);

    int selectNumByTime(@Param("begin") Instant begin, @Param("end") Instant end);

    List<LittleSensor> selectByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize,@Param("start") Instant start, @Param("end") Instant end);

    List<LittleSensor> selectByIds(@Param("deviceid") List<String> deviceid,@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    List<Map> getSuperStationByTime(@Param("start") Instant start, @Param("end") Instant end);

    List<Map> getWaterStationByTime(@Param("start") Instant start, @Param("end") Instant end);
}

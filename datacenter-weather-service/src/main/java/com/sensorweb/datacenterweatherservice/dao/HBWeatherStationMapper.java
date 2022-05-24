package com.sensorweb.datacenterweatherservice.dao;

import com.sensorweb.datacenterweatherservice.entity.HBWeatherStation;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@Mapper
public interface HBWeatherStationMapper {
    int insertData(HBWeatherStation hbWeatherStation);

    List<HBWeatherStation> selectByPage(int pageNum, int pageSize);

    int selectNum();

    List<HBWeatherStation> selectMaxTimeData();
}

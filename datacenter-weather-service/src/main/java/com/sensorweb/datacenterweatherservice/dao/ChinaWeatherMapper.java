package com.sensorweb.datacenterweatherservice.dao;

import com.sensorweb.datacenterweatherservice.entity.ChinaWeather;
import com.sensorweb.datacenterweatherservice.entity.ChinaWeather2;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
@Mapper
public interface ChinaWeatherMapper {

    int insertData(ChinaWeather2 chinaWeather2);

    List<ChinaWeather2> selectMaxTimeData();

    int selectNum();
}

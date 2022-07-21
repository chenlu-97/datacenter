package com.sensorweb.datacenterairservice.dao;


import com.sensorweb.datacenterairservice.entity.WaterQualityWaterstationHourly;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
* @author Amour
* @description 针对表【water_quality_waterstation_hourly 】的数据库操作Mapper
* @createDate 2022-06-21 16:26:58
* @Entity boot.bean.WaterQualityWaterstationHourly 
*/
@Repository
@Mapper
public interface WaterQualityWaterstationHourlyMapper {
    int store(WaterQualityWaterstationHourly waterQualityWaterstationHourly);

    List<WaterQualityWaterstationHourly> selectMaxTimeData();
}

package com.sensorweb.datacenterairservice.dao;


import com.sensorweb.datacenterairservice.entity.WaterStation;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author Amour
* @description 针对表【water_station】的数据库操作Mapper
* @createDate 2022-06-20 17:19:30
* @Entity boot.bean.WaterStation
*/

@Repository
@Mapper
public interface WaterStationMapper {

    void store(WaterStation waterStation);

    List<WaterStation> quaryMnId();
}

package com.sensorweb.datacentergeeservice.dao;

import com.sensorweb.datacentergeeservice.entity.Landsat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@Mapper
public interface LandsatMapper {


    List<Landsat> selectByattribute(@Param("spacecraftID") String spacecraftID, @Param("Date") String Date, @Param("Cloudcover") String Cloudcover, @Param("imageType") String imageType);

    List<Landsat> selectAll();

    List<Landsat> selectByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    int selectNum();

    List<Landsat> selectByIds(@Param("imageid") List<String> imageid,@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    int insertLandsat(Landsat landsat);


    List<Landsat> getFilePath(@Param("cloudcover") float cloudcover, @Param("wkt")String wkt, @Param("start")Instant start, @Param("end")Instant end,@Param("type")String type);

    List<Landsat> selectNew();

    List<Landsat> getLandsat( @Param("type")String type,@Param("startTime")Instant startTime, @Param("endTime")Instant endTime);

    List<Landsat> getLandsat2(@Param("type")String type, @Param("row")int row,  @Param("path")int path,  @Param("startTime")Instant startTime,@Param("endTime")Instant endTime);
}
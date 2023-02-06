package com.sensorweb.datacentermoveservice.controller;

import com.sensorweb.datacentermoveservice.dao.LittleSensorMapper;
import com.sensorweb.datacentermoveservice.entity.LittleSensor;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@RestController
@CrossOrigin
public class LittleSensorController {

    @Autowired
    private LittleSensorMapper littleSensorMapper;



    @ApiOperation("所有超级站点")
    @GetMapping(path = "getSuperStationByTime1")
    @ResponseBody
    public List<Map> getSuperStationByTime(@Param("startTime") String startTime, @Param("endTime") String endTime) {
        Instant start = string2Instant(startTime.replace("T", " ").replace("Z", ""));
        Instant stop = string2Instant(endTime.replace("T", " ").replace("Z", ""));
        List<Map> res = littleSensorMapper.getSuperStationByTime(start,stop);
        return res;
    }




    @ApiOperation("所有水站点")
    @GetMapping(path = "getWaterStationByTime1")
    @ResponseBody
    public List<Map> getWaterStationByTime(@Param("startTime") String startTime, @Param("endTime") String endTime) {
        Instant start = string2Instant(startTime.replace("T", " ").replace("Z", ""));
        Instant stop = string2Instant(endTime.replace("T", " ").replace("Z", ""));
        List<Map> res = littleSensorMapper.getWaterStationByTime(start,stop);
        return res;
    }



    public static Instant string2Instant(String date) {
        Date time = null;
        try {
            time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (time!=null) {
            return time.toInstant();
        }
        return null;
    }



}

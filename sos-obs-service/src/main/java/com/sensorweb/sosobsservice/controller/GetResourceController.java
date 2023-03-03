package com.sensorweb.sosobsservice.controller;

import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import com.sensorweb.sosobsservice.dao.ObservationMapper;
import com.sensorweb.sosobsservice.entity.Observation;
import com.sensorweb.sosobsservice.feign.*;
import com.sensorweb.sosobsservice.service.GetObservationExpandService;
import com.sensorweb.sosobsservice.util.ObsConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Api("观测数据查询相关API")
@CrossOrigin
@RestController
@Slf4j
public class GetResourceController implements ObsConstant {
    @Autowired
    private GetObservationExpandService getObservationExpandService;

    @Autowired
    private ObservationMapper observationMapper;

    @Autowired
    private AirFeignClient airFeignClient;
    @Autowired
    private WeatherFeignClient weatherFeignClient;
    @Autowired
    private HimawariFeignClient himawariFeignClient;
    @Autowired
    private GeeFeignClient geeFeignClient;
    @Autowired
    private LaadsFeignClient laadsFeignClient;
    @Autowired
    private OfflineFeignClient offlineFeignClient;
    @Autowired
    private LittleSensorFeignClient littleSensorFeignClient;

    @Autowired
    private DiscoveryClient discoveryClient;




    @ApiOperation("分页查询Data数据")
    @GetMapping(path = "getDataByPage")
    public Map<String, Object> getDataByPage(@ApiParam(name = "pageNum", value = "当前页码") @Param("pageNum") int pageNum,
                                                 @ApiParam(name = "pageSize", value = "每页的数据条目数") @Param("pageSize") int pageSize,
                                                 @ApiParam(name = "dataType", value = "需要查询的数据类型") @Param("dataType") String dataType,
                                                 @RequestParam(value="startTime" ,required=false)String startTime,
                                                 @RequestParam(value="endTime" ,required=false)String endTime,
                                                 @RequestParam(value="region" ,required=false)String region,
                                                 @RequestParam(value="id" ,required=false)String id) {
        Map<String, Object> res = new HashMap<>();
        List<String> stationId = new ArrayList<>();

        if(dataType.equals("HB_Air")){
            stationId = observationMapper.getDataBySpa("HB_AIR",region,id);
        }if(dataType.equals("HB_SuperAir")){
           stationId = observationMapper.getDataBySpa("SuperAirStation",region,id);
        }if(dataType.equals("HB_Water")){
            stationId = observationMapper.getDataBySpa("HB_Water",region,id);
        }if(dataType.equals("HB_WaterPollution")){
            stationId = observationMapper.getDataBySpa("hb_Water_pollution",region,id);
        }if(dataType.equals("HB_WaterAuto")){
            stationId = observationMapper.getDataBySpa("wh_1+8_Water",region,id);
        }if(dataType.equals("CH_Air")){
            stationId = observationMapper.getDataBySpa("CH_AIR",region,id);
        } if(dataType.equals("TW_Air")){
            stationId = observationMapper.getDataBySpa("TW_EPA_AIR",region,id);
        }if(dataType.equals("WH_Weather")){
            stationId = observationMapper.getDataBySpa("wh_1+8_weather",region,id);
        }if(dataType.equals("HB_Weather")){
            stationId = observationMapper.getDataBySpa("HB_WEATHER",region,id);
        }if(dataType.equals("CH_Weather")){
            stationId = observationMapper.getDataBySpa("CH_WEATHER",region,id);
        }

        Instant start = null;
        Instant end = null;
        if(endTime!=null && startTime!=null ){
            start = DataCenterUtils.string2Instant(startTime);
            end = DataCenterUtils.string2Instant(endTime);
        }

        List<Map> info =  observationMapper.getDataByPage(pageNum, pageSize,dataType, start, end, region, stationId);

        int count = observationMapper.getDataNum(dataType, start, end, region, stationId);
        Object num = new Integer(count);
        res.put("Info", info);
        res.put("num",num);
        return res;
    }



    @ApiOperation("分页查询统计报表的数据")
    @GetMapping(path = "getStatisticsByPage")
    public Map<String, Object> getStatisticsByPage(@ApiParam(name = "pageNum", value = "当前页码") @Param("pageNum") int pageNum,
                                                   @ApiParam(name = "pageSize", value = "每页的数据条目数") @Param("pageSize") int pageSize) {
        Map<String, Object> res = new HashMap<>();

        List<Map> info =  observationMapper.getStatisticsByPage(pageNum, pageSize);

        int count = observationMapper.getStatisticsNum();
        Object num = new Integer(count);
        res.put("Info", info);
        res.put("num",num);
        return res;
    }

}

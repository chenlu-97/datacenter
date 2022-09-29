package com.sensorweb.datacentermobileservice.controller;


import com.sensorweb.datacentermobileservice.dao.MeasuringVehicleMapper;
import com.sensorweb.datacentermobileservice.dao.SurveyingVesselMapper;
import com.sensorweb.datacentermobileservice.entity.MeasuringVehicle;
import com.sensorweb.datacentermobileservice.service.MeasuringVehicleService;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@CrossOrigin
public class MeasuringVehicleController {

    @Autowired
    MeasuringVehicleService  measuringVehicleService;

    @Autowired
    MeasuringVehicleMapper  measuringVehicleMapper;

    @Autowired
    SurveyingVesselMapper surveyingVesselMapper;




    @ApiOperation("获取车的数据总数")
    @GetMapping(path = "getVehicleNumber")
    public int getVehicleNumber() {
       int num1 = measuringVehicleMapper.selectNum1();
        int num2 = measuringVehicleMapper.selectNum2();
        int num3 = measuringVehicleMapper.selectNum3();
        int num4 = measuringVehicleMapper.selectNum4();
        int num5 = measuringVehicleMapper.selectNum5();
        int num = num1+num2+num3+num4+num5;
       return num;
    }

    @ApiOperation("获取车的数据总数")
    @GetMapping(path = "getVesselNumber")
    public int getVesselNumber() {
        int num = surveyingVesselMapper.selectNum();
        return num;
    }



    @ApiOperation("获取船的数据总数")
    @GetMapping(path = "getVesselNumberByTime")
    public int getVesselNumberByTime(@Param("startTime")String startTime, @Param("endTime")String endTime) {

        SimpleDateFormat format= new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar startloop = Calendar.getInstance();
        Calendar endloop = Calendar.getInstance();
        try {
            startloop.setTime(format.parse(startTime.replace("T", " ").replace("Z", "")));
            endloop.setTime(format.parse(endTime.replace("T", " ").replace("Z", "")));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Instant start = DataCenterUtils.string2Instant(format.format(startloop.getTime()));
        Instant end = DataCenterUtils.string2Instant(format.format(startloop.getTime()));
        int num = surveyingVesselMapper.selectNumByTime(start,end);
        return num;
    }

    @ApiOperation("获取车的数据总数")
    @GetMapping(path = "getVehicleNumberByTime")
    public int getVehicleNumberByTime(@Param("startTime")String startTime, @Param("endTime")String endTime) {
        SimpleDateFormat format= new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar startloop = Calendar.getInstance();
        Calendar endloop = Calendar.getInstance();
        try {
            startloop.setTime(format.parse(startTime.replace("T", " ").replace("Z", "")));
            endloop.setTime(format.parse(endTime.replace("T", " ").replace("Z", "")));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Instant start = DataCenterUtils.string2Instant(format.format(startloop.getTime()));
        Instant end = DataCenterUtils.string2Instant(format.format(startloop.getTime()));
        int num1 = measuringVehicleMapper.selectNumByTime1(start,end);
        int num2 = measuringVehicleMapper.selectNumByTime2(start,end);
        int num3 = measuringVehicleMapper.selectNumByTime3(start,end);
        int num4 = measuringVehicleMapper.selectNumByTime4(start,end);
        int num5 = measuringVehicleMapper.selectNumByTime5(start,end);
        int num = num1+num2+num3+num4+num5;
        return num;
    }



    @ApiOperation("分页查询数据")
    @GetMapping(path = "getVocsByPage")
    public Map<String, Object> getVocsByPage(@ApiParam(name = "pageNum", value = "当前页码") @Param("pageNum") int pageNum,
                                                       @ApiParam(name = "pageSize", value = "每页的数据条目数") @Param("pageSize") int pageSize) {
        Map<String, Object> res = new HashMap<>();
        List<MeasuringVehicle> info = measuringVehicleService.getVocsByPage(pageNum, pageSize);
        res.put("Info", info);
        return res;
    }
    @ApiOperation("分页查询数据")
    @GetMapping(path = "getHTByPage")
    public Map<String, Object> getHTByPage(@ApiParam(name = "pageNum", value = "当前页码") @Param("pageNum") int pageNum,
                                                         @ApiParam(name = "pageSize", value = "每页的数据条目数") @Param("pageSize") int pageSize) {
        Map<String, Object> res = new HashMap<>();
        List<MeasuringVehicle> info = measuringVehicleService.getHTByPage(pageNum, pageSize);
        res.put("Info", info);
        return res;
    }
    @ApiOperation("分页查询数据")
    @GetMapping(path = "getPMByPage")
    public Map<String, Object> getPMByPage(@ApiParam(name = "pageNum", value = "当前页码") @Param("pageNum") int pageNum,
                                                         @ApiParam(name = "pageSize", value = "每页的数据条目数") @Param("pageSize") int pageSize) {
        Map<String, Object> res = new HashMap<>();
        List<MeasuringVehicle> info = measuringVehicleService.getPMByPage(pageNum, pageSize);
        res.put("Info", info);
        return res;
    }
    @ApiOperation("分页查询数据")
    @GetMapping(path = "getAirByPage")
    public Map<String, Object> getAirByPage(@ApiParam(name = "pageNum", value = "当前页码") @Param("pageNum") int pageNum,
                                                         @ApiParam(name = "pageSize", value = "每页的数据条目数") @Param("pageSize") int pageSize) {
        Map<String, Object> res = new HashMap<>();
        List<MeasuringVehicle> info = measuringVehicleService.getAirByPage(pageNum, pageSize);
        res.put("Info", info);
        return res;
    }

    @ApiOperation("分页查询数据")
    @GetMapping(path = "getSPMSByPage")
    public Map<String, Object> getSPMSByPage(@ApiParam(name = "pageNum", value = "当前页码") @Param("pageNum") int pageNum,
                                                         @ApiParam(name = "pageSize", value = "每页的数据条目数") @Param("pageSize") int pageSize) {
        Map<String, Object> res = new HashMap<>();
        List<MeasuringVehicle> info = measuringVehicleService.getSPMSByPage(pageNum, pageSize);
        res.put("Info", info);
        return res;
    }
}

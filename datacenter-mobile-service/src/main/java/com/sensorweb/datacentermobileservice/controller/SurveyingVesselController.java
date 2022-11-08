package com.sensorweb.datacentermobileservice.controller;


import com.sensorweb.datacentermobileservice.dao.SurveyingVesselMapper;
import com.sensorweb.datacentermobileservice.entity.SurveyingVessel;
import com.sensorweb.datacentermobileservice.service.SurveyingVesselService;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@CrossOrigin
public class SurveyingVesselController {

    @Autowired
    SurveyingVesselService surveyingVesselService;

    @Autowired
    SurveyingVesselMapper surveyingVesselMapper;

    @ApiOperation("分页查询数据")
    @GetMapping(path = "getSurveyingVesselByPage")
    @ResponseBody
    public Map<String, Object> getSurveyingVesselByPage(@RequestParam(value="pageNum", required=false) Integer pageNum,
                                                        @RequestParam(value="pageSize", required=false) Integer pageSize,
                                                        @RequestParam(value="startTime" ,required=false)String startTime,
                                                        @RequestParam(value="endTime" ,required=false)String endTime,
                                                        @RequestParam(value="region" ,required=false)String region,
                                                        @RequestParam(value="id" ,required=false)String id) {
        Map<String, Object> res = new HashMap<>();
        List<SurveyingVessel> info;
        Instant start = null;
        Instant end = null;
        if(endTime!=null && startTime!=null ){
            start = DataCenterUtils.string2Instant(startTime);
            end = DataCenterUtils.string2Instant(endTime);
        }
        info =  surveyingVesselMapper.selectByPage2(pageNum,pageSize,start,end);
        res.put("Info", info);
        return res;
    }

    @ApiOperation("插入经纬度和时间")
    @GetMapping(path = "insertTimeAndPosition")
    public boolean insertTimeAndPosition(@Param("lon") float lon, @Param("lat") float lat) {
        SurveyingVessel surveyingVessel = new SurveyingVessel();
        surveyingVessel.setLon(lon);
        surveyingVessel.setLat(lat);
        SimpleDateFormat sd = new SimpleDateFormat();// 格式化时间
        sd.applyPattern("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();// 获取当前时间
//        System.out.println("现在时间：" + sd.format(date));
        String time = sd.format(date);
        Instant datatime = DataCenterUtils.string2LocalDateTime3(time).toInstant(ZoneOffset.ofHours(+8));
        surveyingVessel.setDataTime(datatime);
        int res =surveyingVesselMapper.insertPosition(surveyingVessel);
        return res>0;
    }
}

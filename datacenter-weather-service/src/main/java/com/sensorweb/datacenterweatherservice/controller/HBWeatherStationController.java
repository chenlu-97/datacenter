package com.sensorweb.datacenterweatherservice.controller;

import com.sensorweb.datacenterweatherservice.dao.HBWeatherStationMapper;
import com.sensorweb.datacenterweatherservice.entity.HBWeatherStation;
import com.sensorweb.datacenterweatherservice.service.HBWeatherStationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HBWeatherStationController {
    @Autowired
    HBWeatherStationService hbWeatherStationService;
    @Autowired
    HBWeatherStation hbWeatherStation;
    @Autowired
    HBWeatherStationMapper hbWeatherStationMapper;
    @GetMapping(value = "insertHBWeatherStation")
    public void insertHBWeatherStation(@ApiParam(name = "time", value = "当前页码") @Param("time") String time)
    {

        try {
            String document = hbWeatherStationService.getApiDocument(time);
            boolean res = hbWeatherStationService.getIOTInfo(document);
            System.out.println(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @ApiOperation("分页查询数据")
    @GetMapping(path = "getHBWeatherByPage")
    public Map<String, Object> getHBWeatherByPage(@ApiParam(name = "pageNum", value = "当前页码") @Param("pageNum") int pageNum,
                                                       @ApiParam(name = "pageSize", value = "每页的数据条目数") @Param("pageSize") int pageSize) {
        Map<String, Object> res = new HashMap<>();
        List<HBWeatherStation> info =  hbWeatherStationService.getDataByPage(pageNum, pageSize);
        int count =hbWeatherStationMapper.selectNum();
        Object num = new Integer(count);
        res.put("Info", info);
        res.put("num",num);
        return res;
    }

}

package com.sensorweb.datacenterairservice.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "datacenter-move-service")
public interface moveClient {

    @GetMapping(path = "getSuperStationByTime1")
    List<Map> getSuperStationByTime(@RequestParam(value = "startTime")String startTime, @RequestParam(value = "endTime")String endTime);

    @GetMapping(path = "getWaterStationByTime1")
    List<Map> getWaterStationByTime(@RequestParam(value = "startTime")String startTime, @RequestParam(value = "endTime")String endTime);
}

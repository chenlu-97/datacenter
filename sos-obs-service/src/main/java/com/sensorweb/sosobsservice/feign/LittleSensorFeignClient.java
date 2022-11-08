package com.sensorweb.sosobsservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@FeignClient(value = "datacenter-littleSensor-service")
public interface LittleSensorFeignClient {

    @GetMapping(path = "getLittleSensorNumber")
    int getLittleSensorNumber();

    @GetMapping(path = "getNumberByTime")
    int getNumberByTime(@RequestParam(value = "startTime")String startTime, @RequestParam(value = "endTime")String endTime);
}

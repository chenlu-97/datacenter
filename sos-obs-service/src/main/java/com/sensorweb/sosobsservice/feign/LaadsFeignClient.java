package com.sensorweb.sosobsservice.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@FeignClient(value = "datacenter-laads-service")
public interface LaadsFeignClient {

    @GetMapping(path = "getModisNumber")
    int getModisNum();

    @GetMapping(path = "getGLDASNumber")
    int getGLDASNum();

    @GetMapping(path = "getGPMNumber")
    int getGPMNum();

    @GetMapping(path = "getlaadsNumberByTime")
    int getlaadsNumberByTime(@RequestParam(value = "startTime")Instant startTime, @RequestParam(value = "endime")Instant endTime);

}

package com.sensorweb.sosobsservice.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@FeignClient(value = "datacenter-gee-service")
public interface GeeFeignClient {
    @GetMapping(path = "getLandsatNumber")
    int getLandsatNum();

    @GetMapping(path = "getGeeNumberByTime")
    int getGeeNumberByTime(@RequestParam(value = "startTime")Instant startTime, @RequestParam(value = "endime")Instant endTime);

}

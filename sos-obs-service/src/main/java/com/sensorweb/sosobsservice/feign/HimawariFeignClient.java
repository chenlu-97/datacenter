package com.sensorweb.sosobsservice.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@FeignClient(value = "datacenter-himawari-service")
public interface HimawariFeignClient {

    @GetMapping(path = "getHimawariNumber")
    int getHimawariNum();

    @GetMapping(path = "getHimawariNumberByTime")
    int getHimawariNumberByTime(@RequestParam(value = "startTime")Instant startTime, @RequestParam(value = "endime")Instant endTime);

}

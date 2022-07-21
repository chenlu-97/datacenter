package com.sensorweb.datacenterairservice;

import com.sensorweb.datacenterairservice.thread.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DatacenterAirServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatacenterAirServiceApplication.class, args);
//          new Thread(new downloadSuperAirStation()).start();
//            new Thread(new downloadWater()).start();
            new Thread(new downloadWater1()).start();
//        new Thread(new downloadWater2()).start();
//        new Thread(new downloadWater3()).start();
//            new Thread(new downloadAir()).start();
    }

}

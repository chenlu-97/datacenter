package com.sensorweb.datacenterairservice;

import com.sensorweb.datacenterairservice.thread.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class DatacenterAirServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatacenterAirServiceApplication.class, args);
//          new Thread(new downloadSuperAirStation()).start();
//          new Thread(new downloadSuperAirStation1()).start();
//            new Thread(new downloadWater()).start();
            new Thread(new downloadAir()).start();
//            new Thread(new downloadAir1()).start();
//            new Thread(new downloadAir2()).start();
    }

}

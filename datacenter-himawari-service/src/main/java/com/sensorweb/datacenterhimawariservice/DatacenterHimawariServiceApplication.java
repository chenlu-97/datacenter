package com.sensorweb.datacenterhimawariservice;

import com.sensorweb.datacenterhimawariservice.service.downloadHimawari;
import com.sensorweb.datacenterhimawariservice.service.downloadHimawari1;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class DatacenterHimawariServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatacenterHimawariServiceApplication.class, args);
//        new Thread(new downloadHimawari()).start();
//        new Thread(new downloadHimawari1()).start();
    }

}

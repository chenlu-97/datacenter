package com.sensorweb.datacenterlaadsservice;

import com.sensorweb.datacenterlaadsservice.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class DatacenterLaadsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatacenterLaadsServiceApplication.class, args);
//        new Thread(new downloadmodis()).start();
//        new Thread(new downloadmodis1()).start();
//        new Thread(new downloadmodis2()).start();
//        new Thread(new downloadmodis3()).start();
//        new Thread(new downloadmodis4()).start();
//        new Thread(new downloadmodis5()).start();
          new Thread(new downloadmodis6()).start();
    }

}

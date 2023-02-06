package com.sensorweb.datacenterlaadsservice;

import com.sensorweb.datacenterlaadsservice.download.*;
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
//        new Thread(new downloadmodis6()).start();
//        new Thread(new downloadmodis7()).start();
//        new Thread(new downloadmodis8()).start();
//        new Thread(new downloadmodis9()).start();
//        new Thread(new downloadmodis10()).start();
//        new Thread(new downloadmodis11()).start();
//        new Thread(new downloadmodis12()).start();


        new Thread(new downloadMOD09GA1()).start();
//        new Thread(new downloadMOD09GA2()).start();
//        new Thread(new downloadMOD09GA3()).start();
//        new Thread(new downloadMOD09GA4()).start();
//        new Thread(new downloadMOD09GA5()).start();
//        new Thread(new downloadMOD09GA6()).start();
//        new Thread(new downloadMOD09GA7()).start();
//        new Thread(new downloadMOD09GA8()).start();
//        new Thread(new downloadMOD09GA9()).start();
//        new Thread(new downloadMOD09GA10()).start();
//        new Thread(new downloadMOD09GA11()).start();

    }

}

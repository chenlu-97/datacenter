package com.sensorweb.datacentergeeservice;

import com.sensorweb.datacentergeeservice.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatacenterGeeServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(DatacenterGeeServiceApplication.class, args);
        new Thread(new downloadlandsat1() ).start();
        new Thread(new downloadlandsat2() ).start();
        new Thread(new downloadlandsat3() ).start();
        new Thread(new downloadlandsat4() ).start();
        new Thread(new downloadlandsat5() ).start();
    }

}

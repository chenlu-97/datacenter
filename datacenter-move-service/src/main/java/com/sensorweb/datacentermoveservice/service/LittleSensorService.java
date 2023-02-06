package com.sensorweb.datacentermoveservice.service;

import com.sensorweb.datacentermoveservice.dao.LittleSensorMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
public class LittleSensorService {

    @Autowired
    private LittleSensorMapper littleSensorMapper;



}

package com.sensorweb.datacenterlaadsservice.controller;


import com.sensorweb.datacenterlaadsservice.dao.GLDASMapper;
import com.sensorweb.datacenterlaadsservice.dao.GPM_3IMERGDEMapper;
import com.sensorweb.datacenterlaadsservice.entity.GLDAS;
import com.sensorweb.datacenterlaadsservice.entity.GPM_3IMERGDE;
import com.sensorweb.datacenterlaadsservice.service.InsertGLDASService;
import com.sensorweb.datacenterlaadsservice.service.InsertGPM_3IMERGDE;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin
public class GPMController {
    @Autowired
    private InsertGPM_3IMERGDE insertGPM_3IMERGDE;

    @Autowired
    private GPM_3IMERGDEMapper gpm_3IMERGDEMapper;

    @GetMapping(value = "getGPM_3IMERGDE")
    @ResponseBody
    public Map<String, Object> getGPM_3IMERGDE(@Param("startTime")String startTime, @Param("endTime")String endTime) {

        Map<String, Object> res = new HashMap<>();
        Map<String, String> path = new HashMap<>();
        try {
            Instant start = DataCenterUtils.string2Instant(startTime);
            Instant end = DataCenterUtils.string2Instant(endTime);
            List<GPM_3IMERGDE> gldas = gpm_3IMERGDEMapper.getFilePath(start,end);
            if(gldas.size()>0){
                for(int i=0;i<gldas.size();i++) {
                    String filepath = gldas.get(i).getFilePath();
                    String time = gldas.get(i).getTime().toString().replace("T"," ").replace("Z","");
                    path.put(time ,filepath);
                }
                res.put("status", "success");
                res.put("filePath",path);
            }else{
                    res.put("status", "sucess");
                    res.put("filePath","时间范围内，暂无数据");
                }
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "failed");
        }
        return res;
    }


    @GetMapping(value = "downloadGPM_3IMERGDE")
    @ResponseBody
    public Map<String, Object> downloadGPM_3IMERGDE( @Param("startTime")String startTime, @Param("endTime")String endTime) {
        Map<String, Object> res = new HashMap<>();
        boolean flag = false;
        try {
            Instant start = DataCenterUtils.string2Instant(startTime);
            Instant end = DataCenterUtils.string2Instant(endTime);
            while(start.isBefore(end)){
                flag =insertGPM_3IMERGDE.insertData(start.toString());
                start.plusSeconds(3*60*60);
            }
            if(flag){
                res.put("status", "success");
            }else{
                res.put("status", "failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "failed");
        }
        return res;
    }


//    @GetMapping(value = "downloadGLDASByFile")
//    @ResponseBody
//    public Map<String, Object> downloadGLDASByFile( @Param("filepath")String filepath) {
//        Map<String, Object> res = new HashMap<>();
//        boolean flag = false;
//        try {
//            flag =insertGLDASService.insertDataByCookie(filepath);
//            if(flag){
//                res.put("status", "success");
//            }else{
//                res.put("status", "failed");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            res.put("status", "failed");
//        }
//        return res;
//    }





}

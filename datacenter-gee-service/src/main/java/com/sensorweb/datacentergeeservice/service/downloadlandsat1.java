package com.sensorweb.datacentergeeservice.service;


import com.sensorweb.datacentergeeservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
public class downloadlandsat1 implements Runnable{
    @Override
    public void run() {
        System.out.println("-----20200101-----------20200630-------------");
        try {
            //起始时间
            String str="20200101";
            //结束时间
            String str1="20200630";
            SimpleDateFormat format= new  SimpleDateFormat("yyyyMMdd");
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            try {
                start.setTime(format.parse(str));
                end.setTime(format.parse(str1));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            while(start.before(end))
            {
                LandsatService landsatService = (LandsatService) ApplicationContextUtil.getBean("landsatService");
                log.info("------ 开始下载" + format.format(start.getTime()) +"的影像-----");
                String statue =  landsatService.downloadLandsat(format.format(start.getTime()));
                if (statue.equals("cookie error")){
                    log.info("------ cookie error 过期了，要更换cookie-----");
                } else if(statue.equals("fail")){
                    log.info("------ landsat8下载失败-----");
                }else if(statue.equals("success")){
                    log.info(format.format(start.getTime())+"-----获取影像成功！！！！！！！----");
                    DataCenterUtils.sendMessage("Landsat-8"+ format.format(start.getTime()), "Landsat-8","USGS获取的Landsat-8影像成功");
                }
                start.add(Calendar.DAY_OF_MONTH,1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.info("USGS获取Landsat-8影像 " + "Status: Fail");
            System.out.println(e.getMessage());
        }
    }
}
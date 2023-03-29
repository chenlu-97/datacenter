package com.sensorweb.datacentergeeservice.service;


import com.sensorweb.datacentergeeservice.entity.Landsat;
import com.sensorweb.datacentergeeservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

@Slf4j
public class downloadlandsat1 implements Runnable{
    @Override
    public void run() {

        try {
            //起始时间
            String str="20200101";
            //结束时间
            String str1="20210101";
            System.out.println("-----"+str+"----------"+str1+"--------------Landsat8 L1/L2影像");
            LandsatService landsatService = (LandsatService) ApplicationContextUtil.getBean("landsatService");
            SimpleDateFormat format= new  SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat format1= new  SimpleDateFormat("yyyy-MM-dd 00:00:00");
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

                    log.info("------ 开始下载" + format.format(start.getTime()) + "的影像-----");
                    String statue2 = landsatService.downloadLandsat_old(format.format(start.getTime()),"https://earthexplorer.usgs.gov/download/5e83d14fec7cae84/","L2SP");
                    if (statue2.equals("cookie error")) {
                        log.info("------ cookie error 过期了，要更换cookie-----");
                    } else if (statue2.equals("fail")) {
                        log.info("------ landsat8下载失败-----");
                    } else if (statue2.equals("success")) {
                        log.info(format.format(start.getTime()) + "-------获取影像成功！！！！！！！----");
                        DataCenterUtils.sendMessage("Landsat-8" + format.format(start.getTime()), "Landsat-8", "USGS获取的Landsat-8影像成功",1);
//                    }
                }

                    log.info("------ 开始下载" + format.format(start.getTime()) + "的影像-----");
    //                    String statue1 = "success";
                    String statue1 = landsatService.downloadLandsat_old(format.format(start.getTime()),"https://earthexplorer.usgs.gov/download/5e81f14f92acf9ef/","L1TP");
                    if (statue1.equals("cookie error")) {
                        log.info("------ cookie error 过期了，要更换cookie-----");
                    } else if (statue1.equals("fail")) {
                        log.info("------ landsat8下载失败-----");
                    } else if (statue1.equals("success")) {
                        log.info(format.format(start.getTime()) + "-------获取影像成功！！！！！！！----");
                        DataCenterUtils.sendMessage("Landsat-8" + format.format(start.getTime()), "Landsat-8", "USGS获取的Landsat-8影像成功",1);
                    }
//                }
                start.add(Calendar.DAY_OF_MONTH,1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.info("USGS获取Landsat-8影像 " + "Status: Fail");
        }
    }
}
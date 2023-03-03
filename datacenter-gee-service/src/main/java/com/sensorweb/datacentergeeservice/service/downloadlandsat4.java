package com.sensorweb.datacentergeeservice.service;


import com.sensorweb.datacentergeeservice.entity.Landsat;
import com.sensorweb.datacentergeeservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

@Slf4j
public class downloadlandsat4 implements Runnable{

    @Autowired
    LandsatService landsatService;

    @Override
    public void run() {

        try {
            //起始时间
            String str="20230101";
            //结束时间
            String str1="20230201";
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
//                Instant startTime = DataCenterUtils.string2Instant(format1.format(start.getTime()));
//                Instant endTime = DataCenterUtils.string2Instant(format1.format(start.getTime())).plusSeconds(24*60*60);
//                List<Landsat> L2SP = landsatService.getLandsat("L2SP",startTime,endTime);
//                List<Landsat> L1TP = landsatService.getLandsat("L1TP",startTime,endTime);
//                if(L2SP.size()>0){
//                    log.info(startTime.plusSeconds(8*60*60)+"的 SPL2已存在该数据，无需下载！！！");
//                }else {
                    log.info("------ 开始下载" + format.format(start.getTime()) + "的影像-----");
//                    String statue1 = "success";
                    String statue2 = landsatService.downloadLandsat(format.format(start.getTime()),"https://earthexplorer.usgs.gov/download/632210d4770592cf/","L2SP");
                    if (statue2.equals("cookie error")) {
                        log.info("------ cookie error 过期了，要更换cookie-----");
                    } else if (statue2.equals("fail")) {
                        log.info("------ landsat8下载失败-----");
                    } else if (statue2.equals("success")) {
                        log.info(format.format(start.getTime()) + "-------获取影像成功！！！！！！！----");
                        DataCenterUtils.sendMessage("Landsat-8" + format.format(start.getTime()), "Landsat-8", "USGS获取的Landsat-8影像成功",1);
//                    }
                }
//                if(L1TP.size()>0){
//                    log.info(startTime.plusSeconds(8*60*60)+"的 TPL1已存在该数据，无需下载！！！");
//                }else {
                    log.info("------ 开始下载" + format.format(start.getTime()) + "的影像-----");
//                    String statue1 = "success";
                    String statue1 = landsatService.downloadLandsat(format.format(start.getTime()),"https://earthexplorer.usgs.gov/download/632211e26883b1f7/","L1TP");
                    if (statue1.equals("cookie error")) {
                        log.info("------ cookie error 过期了，要更换cookie-----");
                    } else if (statue1.equals("fail")) {
                        log.info("------ landsat8下载失败-----");
                    } else if (statue1.equals("success")) {
                        log.info(format.format(start.getTime()) + "-------获取影像成功！！！！！！！----");
                        DataCenterUtils.sendMessage("Landsat-8" + format.format(start.getTime()), "Landsat-8", "USGS获取的Landsat-8影像成功",1);
//                    }
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

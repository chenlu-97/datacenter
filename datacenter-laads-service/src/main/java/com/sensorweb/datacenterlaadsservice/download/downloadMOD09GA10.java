package com.sensorweb.datacenterlaadsservice.download;


import com.sensorweb.datacenterlaadsservice.dao.EntryMapper;
import com.sensorweb.datacenterlaadsservice.entity.Entry;
import com.sensorweb.datacenterlaadsservice.service.InsertLAADSService;
import com.sensorweb.datacenterlaadsservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterlaadsservice.util.LAADSConstant;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

@Slf4j
public class downloadMOD09GA10 implements Runnable{

    @Override
    public void run() {
        System.out.println("-----2018-01-01---------2020-01-01 --------MOD09GA----");
        InsertLAADSService insertLAADSService = (InsertLAADSService) ApplicationContextUtil.getBean("insertLAADSService");
        EntryMapper entryMapper = (EntryMapper) ApplicationContextUtil.getBean("entryMapper");
        try {
            String startTime = "2018-01-01 00:00:00";
            String endTime = "2020-01-01 00:00:00";

//          String bbox = "95,24,123,35"; //长江流域
//          String bbox = "113.8,29.9,115,32"; //1+8城市圈
//            String bbox = "73,18,136,54"; //全国
            String bbox = "90,21.1,122.9,36"; //长江流域
            SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            Calendar startloop = Calendar.getInstance();
            Calendar endloop = Calendar.getInstance();
            try {
                startloop.setTime(format.parse(startTime));
                endloop.setTime(format.parse(endTime));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            while(startloop.before(endloop))
            {
                String[] products = new String[]{"MOD09GA","MOD09A1"};
                for (String product : products) {
                    Instant start = DataCenterUtils.string2Instant(format.format(startloop.getTime()));
                    Instant end = DataCenterUtils.string2Instant(format.format(startloop.getTime())).plusSeconds(24 * 60 * 60).minusSeconds(1);
                    String flag = null;
                    try {
                        flag = insertLAADSService.insertData4( start,end,bbox, product, LAADSConstant.LAADS_DOWNLOAD_TOKEN4);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println("获取modis产品：" + product + "接入时间：" + start.plusSeconds(8 * 60 * 60).toString() + "状态：" + flag);
                    log.info("获取modis产品：" + product + "接入时间：" + start.plusSeconds(8 * 60 * 60).toString() + "状态：" + flag);
                    Thread.sleep(5 * 1000); //下载太快会断开连接，这里休息10s
                }
                startloop.add(Calendar.DAY_OF_MONTH,1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.sensorweb.datacenterlaadsservice.download;


import com.sensorweb.datacenterlaadsservice.dao.EntryMapper;
import com.sensorweb.datacenterlaadsservice.entity.Entry;
import com.sensorweb.datacenterlaadsservice.service.InsertLAADSService;
import com.sensorweb.datacenterlaadsservice.util.ApplicationContextUtil;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

@Slf4j
public class downloadmodis1 implements Runnable{

    @Override
    public void run() {
        System.out.println("-----20200101---------20220315--------MOD11A2----");
        InsertLAADSService insertLAADSService = (InsertLAADSService) ApplicationContextUtil.getBean("insertLAADSService");
        EntryMapper entryMapper = (EntryMapper) ApplicationContextUtil.getBean("entryMapper");
        try {
            String startTime = "2020-01-01 00:00:00";
            String endTime = "2022-07-25 00:00:00";
            String product = "MOD11A2";
            //          String bbox = "95,24,123,35"; //长江流域
//          String bbox = "113.8,29.9,115,32"; //1+8城市圈
            String bbox = "73,18,136,54"; //全国
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
                Instant start = DataCenterUtils.string2Instant(format.format(startloop.getTime()));
                Instant end = DataCenterUtils.string2Instant(format.format(startloop.getTime())).plusSeconds(24*60*60);
//                List<Entry> entrys = entryMapper.getFilePath(product,start,end);
//                if(entrys.size()>0){
//                        System.out.println(product+start.toString()+"已存在该数据，无需下载！！！");
//                        log.info(product+start.toString()+"已存在该数据，无需下载！！！");
//                }else{
                    String flag = insertLAADSService.insertData2(start.toString().replace("T"," ").replace("Z",""), end.toString().replace("T"," ").replace("Z",""), bbox, product);
                    System.out.println( "获取modis产品："+product+"接入时间："+start.toString()+ "状态："+flag);
                    log.info("获取modis产品："+product+"接入时间："+start.toString()+ "状态："+flag);
                    Thread.sleep(60 * 1000); //下载太快会断开连接，这里休息1分钟
//                }
                startloop.add(Calendar.DAY_OF_MONTH,1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

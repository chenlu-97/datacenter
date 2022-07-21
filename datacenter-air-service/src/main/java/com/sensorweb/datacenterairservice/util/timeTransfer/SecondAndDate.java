package com.sensorweb.datacenterairservice.util.timeTransfer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SecondAndDate {

    public static Date sceondtodata(long sceond) {
        long milliSceond = sceond * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSceond);
        Date sdate = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
        String startTime = format.format(sdate);
        try {
            Date res = format.parse(startTime);
            return res;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long datatosecond(String time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date secondtime = format.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(secondtime);
            long milliSceond = calendar.toInstant().toEpochMilli();
            long second = milliSceond / 1000;
            return second;
        } catch (ParseException e) {
            e.printStackTrace();
        }
            return 0;
    }


}

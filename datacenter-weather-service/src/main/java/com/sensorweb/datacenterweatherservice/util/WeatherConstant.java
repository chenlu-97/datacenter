package com.sensorweb.datacenterweatherservice.util;

public interface WeatherConstant {
    String CHINA_WEATHER_URL = "http://api.weatherdt.com/common/";

//    String CHINA_WEATHER_KEY = "cf477689ee37f14790a4d6359bfa5915";
    String CHINA_WEATHER_KEY = "2ebb6c472b311a005aa894247676c515";

    String BAIDU_ADDRESS_API = "http://api.map.baidu.com/geocoding/v3/";

    String BAIDU_AK = "DUljkTDcLCQ00d6ZofaN8FgTnRf4YWAu";




    String TianXingZhou_URL = "http://iot.ainongye.cn/json";



    /**
     * 2022.7.13全国气象站点 数据请求URL
     */
        String CHINA_WEATHER_URL_2 = "http://api.data.cma.cn:8090/api";
        String user_id = "657630347734WX3gV";
        String pwd = "r1Lh6HR";
        String element ="Station_Id_C,Year,Mon,Day,Hour,PRS,PRS_Sea,PRS_Max,PRS_Min,TEM,TEM_Max,TEM_Min,RHU,RHU_Min,VAP,PRE_1h,WIN_D_INST_Max,WIN_S_Max,WIN_D_S_Max,WIN_S_Avg_2mi,WIN_D_Avg_2mi,WIN_S_Inst_Max,windpower";




    /**
     * 湖北气象站点 数据请求URL
     */
    String GET_HBWEATHER_STATION_URL = "http://61.183.207.181:8008/cimiss-web/api";

    /**
     * 湖北气象站点 user_id
     */
    String HBWEATHER_STATION_ID = "BCWH_QXT_WHUZHY";

    /**
     * 湖北气象站点 password
     */
    String HBWEATHER_STATION_PASSWORD = "whuzhy_210323";

    /**
     * 湖北气象站点 interfaceid 接口代码
     */
    String HBWEATHER_STATION_INTERFACEID = "getSurfSixEle";

    /**
     * 湖北气象站点 times
     */
    String HBWEATHER_STATION_TIMES = "20211125000000";

    /**
     * 湖北气象站点 dataCode
     */
    String HBWEATHER_STATION_DATACODE = "SURF_CHN_MUL_HOR";

    /**
     * 湖北气象站点 limitCnt
     */
    int FY4A_AWX_USER_LIMITCNT = 30;

    /**
     * 湖北气象站点 dataFormat
     */
    String HBWEATHER_STATION_DATAFORMAT = "json";


}

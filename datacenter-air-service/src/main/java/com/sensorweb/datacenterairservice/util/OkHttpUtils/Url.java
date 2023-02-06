package com.sensorweb.datacenterairservice.util.OkHttpUtils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public interface Url {



    String clickToken = "http://10.42.52.135:21000/sky-api/rate-auth/auth/clientToken";
//    String clickToken = "http://219.140.164.18:21000/sky-api/rate-auth/auth/clientToken";


    String WaterQualityWaterstationHourly = "http://10.42.52.135:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=c6a08ad7-ca68-49fe-b7c7-acb342e0fa1f";
//  String WaterQualityWaterstationHourly = "http://219.140.164.18:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=c6a08ad7-ca68-49fe-b7c7-acb342e0fa1f";


    String AirStation = "http://10.42.52.135:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=f7f352b4-5ced-4c11-be62-1833fdd8a2d6";
//    String AirStation = "http://219.140.164.18:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=f7f352b4-5ced-4c11-be62-1833fdd8a2d6";


    String RawDataAirStationHourly = " http://10.42.52.135:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=a8a9101d-0a8c-4c00-92f4-7df1097bc13c";
//    String RawDataAirStationHourly = "http://219.140.164.18:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=a8a9101d-0a8c-4c00-92f4-7df1097bc13c";


    String SuperStation = "http://10.42.52.135:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=e42904ac-6a9a-4924-9ba1-968f8748082f";
//    String SuperStation = "http://219.140.164.18:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=e42904ac-6a9a-4924-9ba1-968f8748082f";


    String RawDataSuperStationHourly = "http://10.42.52.135:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=8c033527-aa3d-4071-a087-2e5fb8eb63a0";
//    String RawDataSuperStationHourly = "http://219.140.164.18:21000/sky-api/rate-qualitymonitor-business/api/sqlparam/select?iid=8c033527-aa3d-4071-a087-2e5fb8eb63a0";

    List SuperStationCode = new ArrayList(Arrays.asList(
            "SW4200001",
            "SW4201001",
            "SW4202001",
            "SW4205001" ,
            "SW4206001" ,
            "SW4207001",
            "SW4208001",
            "SW4209001",
            "SW4209002",
            "SW4211001",
            "SW4212001"));

    String SuperStationid = "SW4200001,SW4201001,SW4202001,SW4205001,SW4206001,SW4207001,SW4208001,SW4209001,SW4209002,SW4211001,SW4212001";

}

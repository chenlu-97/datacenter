package com.sensorweb.datacenterairservice.util.OkHttpUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterairservice.util.OkHttpUtils.Url;
import lombok.Data;
import okhttp3.*;

import java.io.IOException;
import java.text.SimpleDateFormat;

@Data
public class OkHttpRequest implements Url {

    private static String token;

    public static String getToken() {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n    \"clientId\":\"6a84a017969b10ad\",\r\n    \"clientSecret\":\"3ab91a689212479aa58e90d611003e63\"\r\n}");
        Request request = new Request.Builder()
                .url(clickToken)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response;

        {
            try {
                response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                String res = responseBody.string();
                JSONObject re = (JSONObject) JSON.parse(res);
                String token = re.getString("access_token");
                return token;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //水站信息
    public static JSONArray accessWaterData(JSONObject body_content) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");

            RequestBody body = RequestBody.create(mediaType, body_content.toJSONString());
            Request request = new Request.Builder()
                    .url(WaterQualityWaterstationHourly)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Connection","keep-alive")
                    .addHeader("Cookie", "JSESSIONID=GgMIsUHVBOGBes8mzsZ_ULVWFmfvjU7AOSNVT1yl")
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
//                System.out.println(response.message().equals("Unauthorized"));
                if(response.message().equals("Unauthorized")){
                    token = getToken();
                    return accessWaterData(body_content);
                }
                ResponseBody responseBody = response.body();
                String res = responseBody.string();
//                System.out.println(res);
                JSONObject resJson = (JSONObject) JSONObject.parse(res);
                JSONObject dataJson = (JSONObject) resJson.get("data");
                if(dataJson!=null){
                    JSONArray station = (JSONArray) dataJson.get("list");
                    return station;
                }else{
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


    public static JSONArray accessAirStation(JSONObject body_content){

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");

            RequestBody body = RequestBody.create(mediaType, body_content.toJSONString());
            Request request = new Request.Builder()
                    .url(AirStation)
                    .method("POST",body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Cookie", "JSESSIONID=_uTby0DvxEbpItiEMkmStLSC3hfSFYgkmVh5qROW")
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                if(response.message().equals("Unauthorized")){
                    token = getToken();
                    return accessAirStation(body_content);
                }
                ResponseBody responseBody = response.body();
                String res = responseBody.string();
                JSONObject resJson = (JSONObject) JSONObject.parse(res);
                JSONObject dataJson = (JSONObject) resJson.get("data");
                if(dataJson!=null){
                    JSONArray station = (JSONArray) dataJson.get("list");
                    return station;
                }else{
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    //空气站站信息
    public static JSONArray accessAirStationData(JSONObject body_content){

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");

            RequestBody body = RequestBody.create(mediaType, body_content.toJSONString());
            Request request = new Request.Builder()
                    .url(RawDataAirStationHourly)
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Cookie", "JSESSIONID=_uTby0DvxEbpItiEMkmStLSC3hfSFYgkmVh5qROW")
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                if(response.message().equals("Unauthorized")){
                    token = getToken();
                    return accessAirStationData(body_content);
                }
                ResponseBody responseBody = response.body();
                String res = responseBody.string();
                JSONObject resJson = (JSONObject) JSONObject.parse(res);
                JSONObject dataJson = (JSONObject) resJson.get("data");
                if(dataJson!=null){
                    JSONArray station = (JSONArray) dataJson.get("list");
                    return station;
                }else{
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONArray accessSuperStation(JSONObject body_content){

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");

            RequestBody body = RequestBody.create(mediaType, body_content.toJSONString());
            Request request = new Request.Builder()
                    .url(SuperStation)
                    .method("POST",body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Cookie", "JSESSIONID=_uTby0DvxEbpItiEMkmStLSC3hfSFYgkmVh5qROW")
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                if(response.message().equals("Unauthorized")){
                    token = getToken();
                    return accessSuperStation(body_content);
                }
                ResponseBody responseBody = response.body();
                String res = responseBody.string();
                JSONObject resJson = (JSONObject) JSONObject.parse(res);
                JSONObject dataJson = (JSONObject) resJson.get("data");
                JSONArray station = (JSONArray) dataJson.get("list");

                return station;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONArray accessSuperStationData(JSONObject body_content){

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");

            RequestBody body = RequestBody.create(mediaType, body_content.toJSONString());
            Request request = new Request.Builder()
                    .url(RawDataSuperStationHourly)
                    .method("POST",body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Cookie", "JSESSIONID=_uTby0DvxEbpItiEMkmStLSC3hfSFYgkmVh5qROW")
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                if(response.message().equals("Unauthorized")){
                    token = getToken();
                    return accessSuperStationData(body_content);
                }
                ResponseBody responseBody = response.body();
                String res = responseBody.string();
                JSONObject resJson = (JSONObject) JSONObject.parse(res);
                JSONObject dataJson = (JSONObject) resJson.get("data");
                JSONArray station = (JSONArray) dataJson.get("list");
                return station;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}

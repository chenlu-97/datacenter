package com.sensorweb.datacenterofflineservice.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sensorweb.datacenterofflineservice.dao.WaterPollutionStationMapper;
import com.sensorweb.datacenterofflineservice.entity.WaterPollutionStation;
import com.sensorweb.datacenterofflineservice.util.OfflineConstant;
import com.sensorweb.datacenterutil.utils.DataCenterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@EnableScheduling
public class GetWaterPollutionStationService {
    @Autowired
    WaterPollutionStationMapper waterPollutionStationMapper;


    public List<WaterPollutionStation> getStationInfoByExcel(File excel) {
        List<WaterPollutionStation> stations = new ArrayList<>();
        try {
            if (excel.isFile() && excel.exists()) {   //判断文件是否存在
                String[] split = excel.getName().split("\\.");  //.是特殊字符，需要转义！！！！！
                Workbook wb;
                //根据文件后缀（xls/xlsx）进行判断
                if ( "xls".equals(split[1])){
                    FileInputStream fis = new FileInputStream(excel);   //文件流对象
                    wb = new HSSFWorkbook(fis);
                }else if ("xlsx".equals(split[1])){
                    wb = new XSSFWorkbook(excel);
                }else {
                    System.out.println("文件类型错误!");
                    return null;
                }
                //开始解析
                Sheet sheet = wb.getSheetAt(0);     //读取sheet 0

                int firstRowIndex = sheet.getFirstRowNum()+1;   //第一行是列名，所以不读
                int lastRowIndex = sheet.getLastRowNum();

                for(int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
                    WaterPollutionStation waterPollutionStation = new WaterPollutionStation();
                    Row row = sheet.getRow(rIndex);
                    if (row != null) {
                        Cell companyNameCell = row.getCell(0);
                        if (companyNameCell!=null) {
                            waterPollutionStation.setCompanyName(companyNameCell.getStringCellValue());
                        }
                        Cell stationNameCell = row.getCell(5);
                        if (stationNameCell!=null) {
                            waterPollutionStation.setStationName(stationNameCell.getStringCellValue());
                        }

                        Cell provinceCell = row.getCell(1);
                        if (provinceCell!=null) {
                            waterPollutionStation.setProvince(provinceCell.getStringCellValue());
                        }
                        Cell cityCell = row.getCell(2);
                        if (cityCell!=null) {
                            waterPollutionStation.setCity(cityCell.getStringCellValue());
                        }
                        Cell townshipCell = row.getCell(3);
                        if (townshipCell!=null) {
                            waterPollutionStation.setTownship(townshipCell.getStringCellValue());
                        }
                        Cell receivingWaterCell = row.getCell(6);
                        if (receivingWaterCell!=null) {
                            waterPollutionStation.setReceivingWater(receivingWaterCell.getStringCellValue());
                        }
                        JSONObject jsonObject = null;
                        if (companyNameCell!=null) {
                            jsonObject = getGeoAddress(companyNameCell.getStringCellValue());
                        }
                        if (jsonObject!=null) {
                            Object result = jsonObject.get("result");
                            if (result!=null) {
                                Object location = ((JSONObject) result).get("location");
                                Float lon= ((JSONObject) location).getFloatValue("lng");
                                Float lat = ((JSONObject) location).getFloatValue("lat");
                                waterPollutionStation.setLon(lon);
                                waterPollutionStation.setLat(lat);
                                waterPollutionStation.setGeom("POINT(" + lon.toString() + ' ' + lat.toString() + ")");
                            }
                        }
//                        Double lon= 0.1;
//                        Double lat = 0.1;
//                        waterPollutionStation.setLon(lon);
//                        waterPollutionStation.setLat(lat);
//                        waterPollutionStation.setGeom("POINT(" + lon.toString() + ' ' + lat.toString() + ")");
                        waterPollutionStation.setStationType("hb_Water_pollution");
                        stations.add(waterPollutionStation);
                    }
                }
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stations;
    }

    public JSONObject getGeoAddress(String address) throws IOException {
        String param = "address=" + URLEncoder.encode(address, "utf-8") + "&output=json" + "&ak=" + OfflineConstant.BAIDU_AK;
        String document = DataCenterUtils.doGet(OfflineConstant.BAIDU_ADDRESS_API, param);
        if (document!=null) {
            return JSON.parseObject(document);
        }
        return null;
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertStationInfoBatch(List<WaterPollutionStation> waterPollutionStation) {
        int status = waterPollutionStationMapper.insertDataBatch(waterPollutionStation);
        return status > 0;
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertStationInfoBatchInStation(List<WaterPollutionStation> waterPollutionStation) {
        int status = waterPollutionStationMapper.insertDataBatchInStation(waterPollutionStation);
        return status > 0;
    }






    public Object getfromExcel() throws IOException, InvalidFormatException {
        String tmpDir = "/Users/chenlu/Desktop/data.xls";
        File excel = new File(tmpDir);
        String[] split = excel.getName().split("\\.");  //.是特殊字符，需要转义！！！！！
        Workbook wb;
        OutputStream out = null;
        //根据文件后缀（xls/xlsx）进行判断
        if ("xls".equals(split[1])) {
            FileInputStream fis = new FileInputStream(excel);   //文件流对象
            wb = new HSSFWorkbook(fis);
        } else if ("xlsx".equals(split[1])) {
            wb = new XSSFWorkbook(excel);
        } else {
            System.out.println("文件类型错误!");
            return null;
        }
        //开始解析
        Sheet sheet = wb.getSheetAt(0);     //读取sheet 0

        int firstRowIndex = sheet.getFirstRowNum() + 1;   //第一行是列名，所以不读
        int lastRowIndex = sheet.getLastRowNum();

        for(int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
            WaterPollutionStation waterPollutionStation = new WaterPollutionStation();
            Row row = sheet.getRow(rIndex);
            String name = null;
            String station = null;
            JSONObject jsonObject = null;
            if (row != null) {
                Cell NameCell = row.getCell(0);
                if (NameCell != null) {
                   name =  NameCell.getStringCellValue();
                }
                Cell StationCell = row.getCell(1);
                if (StationCell != null) {
                    station =  StationCell.getStringCellValue();
                }
                System.out.println("name+station = " + name + station);
                jsonObject = getGeoAddress(name+station);
                if (jsonObject!=null) {
                    Object result = jsonObject.get("result");
                    if (result!=null) {
                        Object location = ((JSONObject) result).get("location");
                        Object lon= ((JSONObject) location).getFloatValue("lng");
                        Object lat = ((JSONObject) location).getFloatValue("lat");

                        Cell first = row.createCell(6);
                        first.setCellValue(lon.toString());

                        Cell second = row.createCell(7);
                        second.setCellValue(lat.toString());
//                        System.out.println("lon = " + lon);
//                        System.out.println("lat = " + lat);
                    }
                }

            }
        }
        out =  new FileOutputStream(tmpDir);
        wb.write(out);

        return null;
    }





}

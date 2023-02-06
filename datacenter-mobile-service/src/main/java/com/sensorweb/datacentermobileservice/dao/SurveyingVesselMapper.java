package com.sensorweb.datacentermobileservice.dao;


import com.sensorweb.datacentermobileservice.entity.SurveyingVessel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@Mapper
public interface SurveyingVesselMapper {

    int insertData(SurveyingVessel surveyingVessel);

    List<SurveyingVessel> selectByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);


    List<SurveyingVessel> selectByPage2(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize,  @Param("start")Instant start,  @Param("end")Instant end);


    int selectNum();

    int insertPosition(SurveyingVessel surveyingVessel);

    int UpdateDataByTime(SurveyingVessel surveyingVessel);

    int UpdateSpaByTime(SurveyingVessel surveyingVessel);

    List<SurveyingVessel> getTime(SurveyingVessel surveyingVessel);

    int updateDataByTime(SurveyingVessel surveyingVessel);

    int selectNumByTime(@Param("begin") Instant begin, @Param("end") Instant end);

    int getDataNum(@Param("start") Instant start, @Param("end") Instant end,@Param("spa") String spa,@Param("id") String id);
}

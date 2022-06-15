package com.sensorweb.datacenterlaadsservice.dao;


import com.sensorweb.datacenterlaadsservice.entity.GPM_3IMERGDE;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@Mapper
public interface GPM_3IMERGDEMapper {

    List<GPM_3IMERGDE> getFilePath(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    int insertData(GPM_3IMERGDE gldas);

    int insertDataBatch(List<GPM_3IMERGDE> gldaslist);

    int selectNum();

    List<GPM_3IMERGDE> selectByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    List<GPM_3IMERGDE> selectByIds(@Param("satellite") List<String> satellite,@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    GPM_3IMERGDE selectNew();

    List<GPM_3IMERGDE> getall();

    void updateFileName(@Param("id") String id, @Param("newPath")String newPath);
}

package com.sensorweb.datacenterlaadsservice.dao;

import com.sensorweb.datacenterlaadsservice.entity.GLDAS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@Mapper
public interface GLDASMapper {

    List<GLDAS> getFilePath(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    int insertData(GLDAS gldas);

    int insertDataBatch(List<GLDAS> gldaslist);

    GLDAS selectById(int id);

    int selectNum();

    List<GLDAS> selectByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    List<GLDAS> selectByIds(@Param("satellite") List<String> satellite,@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    GLDAS selectNew();
}

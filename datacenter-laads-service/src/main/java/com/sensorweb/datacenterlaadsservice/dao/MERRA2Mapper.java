package com.sensorweb.datacenterlaadsservice.dao;

import com.sensorweb.datacenterlaadsservice.entity.MERRA2;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@Mapper
public interface MERRA2Mapper {

    List<MERRA2> getFilePath(@Param("product") String product, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    int insertData(MERRA2 merra2);
    int insertDataBatch(List<MERRA2> entries);

    MERRA2 selectById(int id);

    int selectNum();

    List<MERRA2> selectByPage(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    List<MERRA2> selectByIds(@Param("satellite") List<String> satellite,@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    List<MERRA2> selectNew();

    List<MERRA2> selectMaxTimeData(String product);

    List<MERRA2> getByFileName(@Param("fileName") String fileName);
}

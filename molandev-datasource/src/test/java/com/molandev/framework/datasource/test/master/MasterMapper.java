package com.molandev.framework.datasource.test.master;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface MasterMapper {
    @Select("SELECT COUNT(*) FROM master_table")
    int countMaster();

    @Select("SELECT COUNT(*) FROM slave_table")
    int countSlaveInMaster();
    
    @Insert("INSERT INTO master_table (id, name) VALUES (#{id}, #{name})")
    void insertMaster(int id, String name);
}

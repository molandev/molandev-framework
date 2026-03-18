package com.molandev.framework.datasource.test.slave;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface SlaveMapper {
    @Select("SELECT COUNT(*) FROM slave_table")
    int countSlave();

    @Select("SELECT COUNT(*) FROM master_table")
    int countMasterInSlave();
    
    @Insert("INSERT INTO slave_table (id, name) VALUES (#{id}, #{name})")
    void insertSlave(int id, String name);
}

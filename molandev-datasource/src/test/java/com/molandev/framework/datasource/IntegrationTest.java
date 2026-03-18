package com.molandev.framework.datasource;

import com.molandev.framework.datasource.test.master.MasterMapper;
import com.molandev.framework.datasource.test.slave.SlaveMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = IntegrationTest.TestApplication.class, properties = {
        "molandev.datasource.master.url=jdbc:h2:mem:master_db;DB_CLOSE_DELAY=-1",
        "molandev.datasource.master.username=sa",
        "molandev.datasource.master.password=",
        "molandev.datasource.master.driver-class-name=org.h2.Driver",
        "molandev.datasource.master.primary=true",
        "molandev.datasource.master.packages[0]=com.molandev.framework.datasource.test.master",
        
        "molandev.datasource.slave.url=jdbc:h2:mem:slave_db;DB_CLOSE_DELAY=-1",
        "molandev.datasource.slave.username=sa",
        "molandev.datasource.slave.password=",
        "molandev.datasource.slave.driver-class-name=org.h2.Driver",
        "molandev.datasource.slave.packages[0]=com.molandev.framework.datasource.test.slave"

//        "spring.datasource.url=jdbc:h2:mem:master_db;DB_CLOSE_DELAY=-1",
//        "spring.datasource.username=sa",
//        "spring.datasource.password=",
//        "spring.datasource.driver-class-name=org.h2.Driver",

})
public class IntegrationTest {

    @Autowired
    private MasterMapper masterMapper;

    @Autowired
    private SlaveMapper slaveMapper;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        // 在两个库中分别建立不同的表
        DataSource masterDs = (DataSource) ((DynamicDataSource) dataSource).getTargetDataSourcesMap().get("master");
        JdbcTemplate masterJdbc = new JdbcTemplate(masterDs);
        masterJdbc.execute("CREATE TABLE IF NOT EXISTS master_table (id INT PRIMARY KEY)");
        
        DataSource slaveDs = (DataSource) ((DynamicDataSource) dataSource).getTargetDataSourcesMap().get("slave");
        JdbcTemplate slaveJdbc = new JdbcTemplate(slaveDs);
        slaveJdbc.execute("CREATE TABLE IF NOT EXISTS slave_table (id INT PRIMARY KEY)");
    }

    @Test
    void testDynamicRouting() {
        // 1. 验证 masterMapper 能查到 master_table
        assertThat(masterMapper.countMaster()).isZero();
        
        // 2. 验证 slaveMapper 能查到 slave_table
        assertThat(slaveMapper.countSlave()).isZero();

        // 3. 验证交叉访问会报错（证明确实切换了物理库）
        // masterMapper (路由到 master 库) 尝试访问 slave_table 应该抛出异常
        assertThatThrownBy(() -> masterMapper.countSlaveInMaster())
                .rootCause()
                .hasMessageContaining("SLAVE_TABLE");

        // slaveMapper (路由到 slave 库) 尝试访问 master_table 应该抛出异常
        assertThatThrownBy(() -> slaveMapper.countMasterInSlave())
                .rootCause()
                .hasMessageContaining("MASTER_TABLE");
    }

    @SpringBootApplication
    @MapperScan("com.molandev.framework.datasource.test")
    @Import(DynamicDataSourceAutoConfiguration.class)
    public static class TestApplication {
    }
}

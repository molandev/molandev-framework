package com.molandev.framework.datasource;

import com.molandev.framework.datasource.test.master.MasterMapper;
import com.molandev.framework.datasource.test.slave.SlaveMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 连接代理模式的多数据源事务测试
 */
@SpringBootTest(classes = ProxyTransactionTest.TestApplication.class, properties = {
        "molandev.datasource.master.url=jdbc:h2:mem:proxy_master_db;DB_CLOSE_DELAY=-1",
        "molandev.datasource.master.username=sa",
        "molandev.datasource.master.password=",
        "molandev.datasource.master.driver-class-name=org.h2.Driver",
        "molandev.datasource.master.primary=true",
        "molandev.datasource.master.packages[0]=com.molandev.framework.datasource.test.master",
        
        "molandev.datasource.slave.url=jdbc:h2:mem:proxy_slave_db;DB_CLOSE_DELAY=-1",
        "molandev.datasource.slave.username=sa",
        "molandev.datasource.slave.password=",
        "molandev.datasource.slave.driver-class-name=org.h2.Driver",
        "molandev.datasource.slave.packages[0]=com.molandev.framework.datasource.test.slave"
})
public class ProxyTransactionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProxyTransactionTestService testService;

    @BeforeEach
    void setUp() {
        DynamicDataSource dynamicDataSource = (DynamicDataSource) dataSource;
        
        // 准备 master 库的表
        DataSource masterDs = (DataSource) dynamicDataSource.getTargetDataSourcesMap().get("master");
        JdbcTemplate masterJdbc = new JdbcTemplate(masterDs);
        masterJdbc.execute("DROP TABLE IF EXISTS master_table");
        masterJdbc.execute("CREATE TABLE master_table (id INT PRIMARY KEY, name VARCHAR(50))");
        
        // 准备 slave 库的表
        DataSource slaveDs = (DataSource) dynamicDataSource.getTargetDataSourcesMap().get("slave");
        JdbcTemplate slaveJdbc = new JdbcTemplate(slaveDs);
        slaveJdbc.execute("DROP TABLE IF EXISTS slave_table");
        slaveJdbc.execute("CREATE TABLE slave_table (id INT PRIMARY KEY, name VARCHAR(50))");
    }

    /**
     * 测试：使用代理连接，在同一事务内访问多个数据源并成功回滚
     */
    @Test
    void testCrossDataSourceTransactionWithProxy() {
        System.out.println("\\n=== 测试：代理模式下的跨数据源事务 ===");
        
        // 执行跨数据源操作并触发回滚
        try {
            testService.insertBothDataSourcesWithRollback(1, "test1");
        } catch (RuntimeException e) {
            System.out.println("预期的异常: " + e.getMessage());
        }
        
        // 验证结果
        int masterCount = testService.countMaster();
        int slaveCount = testService.countSlave();
        
        System.out.println("\\nMaster count after rollback: " + masterCount);
        System.out.println("Slave count after rollback: " + slaveCount);
        
        // 如果两个都是 0，说明代理模式成功实现了跨数据源事务
        assertThat(masterCount).describedAs("Master 应该回滚").isEqualTo(0);
        assertThat(slaveCount).describedAs("Slave 应该回滚").isEqualTo(0);
        
        System.out.println("\\n✅ 结论：使用连接代理模式，两个数据源都成功回滚了！");
    }

    /**
     * 测试：验证数据确实插入到了不同的物理数据库
     */
    @Test
    void testDataInsertedToCorrectDatabases() {
        System.out.println("\\n=== 测试：验证数据插入到正确的数据库 ===");
        
        testService.insertBothDataSourcesWithCommit(1, "test1");
        
        int masterCount = testService.countMaster();
        int slaveCount = testService.countSlave();
        
        System.out.println("Master count: " + masterCount);
        System.out.println("Slave count: " + slaveCount);
        
        assertThat(masterCount).isEqualTo(1);
        assertThat(slaveCount).isEqualTo(1);
        
        System.out.println("\\n✅ 结论：数据成功插入到各自的物理数据库！");
    }

    @Service
    public static class ProxyTransactionTestService {
        
        @Autowired
        private MasterMapper masterMapper;
        
        @Autowired
        private SlaveMapper slaveMapper;
        
        @Transactional
        public void insertBothDataSourcesWithRollback(int id, String name) {
            System.out.println("\\n开始跨数据源事务操作...");
            
            // 先操作 master
            masterMapper.insertMaster(id, name);
            System.out.println("✓ 已插入 master: id=" + id);
            int masterCountBefore = masterMapper.countMaster();
            System.out.println("  Master 当前计数: " + masterCountBefore);
            
            // 再操作 slave
            slaveMapper.insertSlave(id, name);
            System.out.println("✓ 已插入 slave: id=" + id);
            int slaveCountBefore = slaveMapper.countSlave();
            System.out.println("  Slave 当前计数: " + slaveCountBefore);
            
            // 抛出异常触发回滚
            System.out.println("\\n触发异常，测试回滚...");
            throw new RuntimeException("故意抛出异常触发回滚");
        }
        
        @Transactional
        public void insertBothDataSourcesWithCommit(int id, String name) {
            masterMapper.insertMaster(id, name);
            slaveMapper.insertSlave(id, name);
        }
        
        public int countMaster() {
            return masterMapper.countMaster();
        }
        
        public int countSlave() {
            return slaveMapper.countSlave();
        }
    }

    @SpringBootApplication
    @MapperScan("com.molandev.framework.datasource.test")
    @Import({DynamicDataSourceAutoConfiguration.class, ProxyTransactionTest.ProxyTransactionTestService.class})
    public static class TestApplication {
    }
}

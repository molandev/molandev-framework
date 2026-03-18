package com.molandev.framework.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DruidDataSourceTest.TestApplication.class, properties = {
        "molandev.datasource.master.url=jdbc:h2:mem:druid_db;DB_CLOSE_DELAY=-1",
        "molandev.datasource.master.username=sa",
        "molandev.datasource.master.password=",
        "molandev.datasource.master.driver-class-name=org.h2.Driver",
        "molandev.datasource.master.type=com.alibaba.druid.pool.DruidDataSource",
        "molandev.datasource.master.pool.initial-size=5",
        "molandev.datasource.master.pool.max-active=20",
        "molandev.datasource.master.pool.min-idle=2",
        "molandev.datasource.master.pool.validation-query=SELECT 1"
})
public class DruidDataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDruidConfiguration() {
        assertThat(dataSource).isInstanceOf(DynamicDataSource.class);
        DynamicDataSource dynamicDataSource = (DynamicDataSource) dataSource;
        
        DataSource masterDs = (DataSource) dynamicDataSource.getTargetDataSourcesMap().get("master");
        assertThat(masterDs).isInstanceOf(DruidDataSource.class);
        
        DruidDataSource druidDs = (DruidDataSource) masterDs;
        assertThat(druidDs.getInitialSize()).isEqualTo(5);
        assertThat(druidDs.getMaxActive()).isEqualTo(20);
        assertThat(druidDs.getMinIdle()).isEqualTo(2);
        assertThat(druidDs.getValidationQuery()).isEqualTo("SELECT 1");
    }

    @SpringBootApplication
    @Import(DynamicDataSourceAutoConfiguration.class)
    public static class TestApplication {
    }
}

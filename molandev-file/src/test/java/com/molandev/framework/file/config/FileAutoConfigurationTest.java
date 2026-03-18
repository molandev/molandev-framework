package com.molandev.framework.file.config;

import com.molandev.framework.file.FileStorage;
import com.molandev.framework.file.storage.LocalFileStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileAutoConfiguration 单元测试
 *
 * @author molandev
 */
class FileAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FileAutoConfiguration.class));

    @Test
    void testLocalStorageAutoConfiguration() {
        contextRunner
                .withPropertyValues(
                        "molandev.file.type=local",
                        "molandev.file.local.base-path=./test-files"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(FileStorage.class);
                    assertThat(context.getBean(FileStorage.class))
                            .isInstanceOf(LocalFileStorage.class);
                });
    }

    @Test
    void testDefaultConfiguration() {
        contextRunner.run(context -> {
            // 默认应该是 local 存储
            assertThat(context).hasSingleBean(FileStorage.class);
            assertThat(context.getBean(FileStorage.class))
                    .isInstanceOf(LocalFileStorage.class);
        });
    }

    @Test
    void testPropertiesBinding() {
        contextRunner
                .withPropertyValues(
                        "molandev.file.type=local",
                        "molandev.file.default-bucket=my-bucket",
                        "molandev.file.local.base-path=/data/files"
                )
                .run(context -> {
                    FileProperties properties = context.getBean(FileProperties.class);
                    assertThat(properties.getType()).isEqualTo(FileProperties.StorageType.LOCAL);
                    assertThat(properties.getDefaultBucket()).isEqualTo("my-bucket");
                    assertThat(properties.getLocal().getBasePath()).isEqualTo("/data/files");
                });
    }
}

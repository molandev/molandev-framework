package com.molandev.framework.encrypt.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {DbEncryptTest.TestConfig.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "molandev.encrypt.db.enabled=true",
        "molandev.encrypt.db.key=12345678",
        "mybatis-flex.configuration.map-underscore-to-camel-case=true",
        "spring.sql.init.schema-locations=classpath:schema.sql",
        "spring.sql.init.mode=always"
}, locations = {"classpath:common_mybatis.yml"})
@DisplayName("@Enc注解测试")
class DbEncryptTest {

    @Autowired
    private TestUserMapper testUserMapper;

    @Autowired
    private TestUserNotEncryptMapper testUserNotEncryptMapper;

    @Autowired
    private DbEncryptService dbEncryptService;

    @SpringBootApplication
    @MapperScan("com.molandev.framework.encrypt.db")
    @ComponentScan(basePackages = {"com.molandev.framework.encrypt.db"})
    @Import({DbEncryptAutoConfiguration.class})
    static class TestConfig {

    }

    @Nested
    @DisplayName("实体类加密解密测试")
    class EntityEncryptionDecryptionTest {

        @Test
        @DisplayName("测试实体类字段加密存储和解密读取")
        void testEntityFieldEncryptionAndDecryption() {
            // 创建测试用户实体
            TestUser user = new TestUser();
            user.setName("张三");
            user.setPhone("13812345678");
            user.setIdCard("110101199001011234");
            user.setEmail("zhangsan@example.com");

            // 插入数据
            testUserMapper.insert(user);

            // 验证ID已生成
            assertNotNull(user.getId());

            // 通过ID查询用户
            TestUser foundUser = testUserMapper.selectOneById(user.getId());

            // 验证解密后的数据正确性
            assertNotNull(foundUser);
            assertEquals("张三", foundUser.getName());
            assertEquals("13812345678", foundUser.getPhone());
            assertEquals("110101199001011234", foundUser.getIdCard());
            assertEquals("zhangsan@example.com", foundUser.getEmail());
        }

        @Test
        @DisplayName("测试更新加密字段")
        void testUpdateEncryptedFields() {
            // 创建测试用户实体
            TestUser user = new TestUser();
            user.setName("李四");
            user.setPhone("13987654321");
            user.setIdCard("110101199002023456");
            user.setEmail("lisi@example.com");

            // 插入数据
            testUserMapper.insert(user);
            String userId = user.getId();

            // 查询插入后的数据
            user = testUserMapper.selectOneById(userId);
            
            // 更新用户信息
            user.setPhone("13712345678");
            user.setEmail("lisi_new@example.com");
            testUserMapper.update(user);

            // 通过未加密的Mapper查询，验证更新后数据库中存储的是新的加密数据
            TestUserNotEncrypt encryptedUser = testUserNotEncryptMapper.selectOneById(userId);
            assertNotNull(encryptedUser);
            assertNotEquals("13712345678", encryptedUser.getPhone(), "更新后数据库中的手机号应该是新的密文");
            assertNotEquals("13987654321", encryptedUser.getPhone(), "更新后数据库中的手机号不应该是旧的明文");
            assertNotEquals("lisi_new@example.com", encryptedUser.getEmail(), "更新后数据库中的邮箱应该是新的密文");
            assertNotEquals("lisi@example.com", encryptedUser.getEmail(), "更新后数据库中的邮箱不应该是旧的明文");

            // 通过带加密注解的Mapper查询，验证解密后的数据正确性
            TestUser updatedUser = testUserMapper.selectOneById(userId);
            assertNotNull(updatedUser);
            assertEquals("李四", updatedUser.getName());
            assertEquals("13712345678", updatedUser.getPhone(), "解密后应该是更新后的手机号");
            assertEquals("110101199002023456", updatedUser.getIdCard(), "身份证未修改，应该保持原值");
            assertEquals("lisi_new@example.com", updatedUser.getEmail(), "解密后应该是更新后的邮箱");
        }
    }

    @Nested
    @DisplayName("DataEncDecService测试")
    class DataEncDecServiceTest {

        @Test
        @DisplayName("测试加密服务加密方法")
        void testEncryptServiceEncryption() {
            // 创建测试用户实体
            TestUser user = new TestUser();
            user.setPhone("13812345678");

            // 执行加密
            dbEncryptService.encrypt(user);

            // 验证手机号已被加密（不等于原始值）
            assertNotNull(user.getPhone());
            assertNotEquals("13812345678", user.getPhone());
        }

        @Test
        @DisplayName("测试加密服务解密方法")
        void testEncryptServiceDecryption() {
            // 创建测试用户实体并手动设置加密值
            TestUser user = new TestUser();
            user.setPhone("13812345678");
            
            // 先加密
            dbEncryptService.encrypt(user);
            String encryptedPhone = user.getPhone();

            // 再解密
            dbEncryptService.decrypt(user);

            // 验证解密后的数据正确性
            assertEquals("13812345678", user.getPhone());
        }
    }
}
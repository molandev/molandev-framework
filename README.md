# MolanDev Framework

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-green.svg" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.0+-blue.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
  <a href="https://github.com/molandev/molandev-framework"><img src="https://img.shields.io/badge/GitHub-molandev--framework-black.svg" alt="GitHub"></a>
</p>

## 🚀 一套代码，单体与微服务自由切换

**MolanDev Framework** 是一套为极致开发者打造的 Java 后端核心组件库。它不仅仅是一个工具集，更是一套**革命性双模驱动架构方案**，解决项目初期微服务过度设计与后期重构推倒重来的两难困境。

### 🎯 核心价值
- **项目初期**: 单体模式极速开发，本地调试如丝般顺滑
- **业务扩展**: 一行配置切换微服务，业务代码零改动
- **持续演进**: 按需拆分服务，无需推翻重写
- **双向自由**: 微服务降本可切回单体，性能提升 10 倍

同时提供**全链路安全、分布式锁、多数据源**等企业级核心组件。

---

## ✨ 核心亮点

### 🔄 双模驱动架构（核心创新）
一套代码，单体与微服务自由切换。
- **接口即服务**: Feign 接口直接实现，无需编写 Controller
- **智能路由**: 单体模式本地调用，微服务模式远程调用
- **一行切换**: `molandev.cloud.mode=single/cloud` 即可切换架构
- **零重构成本**: 业务代码完全不用改，只需调配置

### 🧪 工业级稳定性
我们深知“工具类”在系统中的基石作用。
- **高覆盖率单元测试**：每一个核心方法都经过严格的单元测试验证，确保在各种边界条件下依然稳如磐石。
- **完善的文档注释**：代码注释率高，每一个 API 都有清晰的 Javadoc 说明，让你“见名知意”，上手即用。

### 🔐 全链路安全保障
不再担心接口被截获或数据被篡改。
- **混合加密通信**：RSA + AES 双层加密机制，动态密钥协商，抓包亦无可奈何。
- **接口防篡改**：MD5 签名 + 时间戳 + Nonce 随机数，彻底杜绝重放攻击与非法串改。
- **数据库透明加解密**：基于 MyBatis 拦截器，字段级存储加密，对业务代码完全透明。
- **智能数据脱敏**：一行注解，自动处理手机号、身份证、地址等敏感信息的展示脱敏。

### ⚡ 高性能分布式锁
解决分布式环境下的竞态条件，比传统的实现更轻量、更智能。
- **分布式可重入锁**：深度优化本地线程状态检查，大幅降低 Redis 交互成本。
- **多策略实现**：内置 Redis 实现与内存实现，无缝切换，应对不同业务规模。
- **注解式声明**：优雅的 AOP 切面支持，让锁的使用像 `@Transactional` 一样简单。

### 🗄️ 智能多数据源（双模适配）
完美适配双模架构，单体模式跨库事务，微服务模式数据隔离。
- **包名自动路由**: 根据 Mapper 包名自动切换数据源
- **跨库事务支持**: 单体模式下多数据源事务一致性
- **零侵入设计**: 无需任何注解，自动识别和路由
- **微服务合并**: 多服务合并为单体，降低运维成本

### 🛠️ 工业级工具库
告别重复造轮子，每一个工具方法都经过生产级打磨。
- **线程安全设计**: 弃用 SimpleDateFormat，全面拥抱 Java 8 `java.time`
- **Java 21 支持**: 自动识别虚拟线程，为高性能架构预留可能
- **全能加密集**: AES、RSA、MD5、SHA、DES 一应俱全

### 📩 统一事件总线（双模适配）
轻量级事件驱动架构，支持本地与跨服务通信。
- **类型安全**: 直接使用类对象作为主题，支持继承冒泡
- **静态发布**: `EventUtil.publish(event)` 极致便捷
- **双模驱动**: 单体模式纯内存分发，微服务模式自动切换 RabbitMQ
- **广播/争抢**: 支持广播模式和争抢模式，灵活适配不同场景

---

## 📦 模块预览

| 模块名称 | 功能描述 | 核心特性 |
| :--- | :--- | :--- |
| `molandev-rpc` | **双模切换核心** | 接口即服务、智能路由、一行配置切换 |
| `molandev-event` | **统一事件总线** | 类型安全、静态发布、双模适配 |
| `molandev-datasource` | **智能多数据源** | 包名路由、跨库事务、双模适配 |
| `molandev-encrypt` | **全链路安全** | 混合加密、签名校验、DB加密、脱敏 |
| `molandev-lock` | **分布式锁** | 可重入锁、本地线程优化、幂等性 |
| `molandev-file` | **文件存储** | 本地/S3切换、流式上传、职责分离 |
| `molandev-util` | **底层基石** | 日期、字符串、加密、反射等工具 |
| `molandev-spring` | **Spring 集成** | Spring工具、XSS过滤、树形结构、JSON增强 |


---

## 📖 官方文档与社区

想要了解更多关于 MolanDev Framework 的高级用法、架构设计与最佳实践？

👉 **立即访问官方主页： [molandev.com](https://molandev.com)**

在这里，你可以找到：
- 📘 详尽的 API 文档与原理剖析
- 🚀 快速集成指南
- 💡 常见场景的实战案例
- 👥 加入我们的开发者社区

---

## 🛠️ 快速集成

### 方式一：按需引入模块（推荐）

根据你的需求，引入对应的模块依赖：

```xml
<!-- 工具类基础模块 -->
<dependency>
    <groupId>com.molandev</groupId>
    <artifactId>molandev-util</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- 双模驱动架构核心 -->
<dependency>
    <groupId>com.molandev</groupId>
    <artifactId>molandev-rpc</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- 其他模块：molandev-event、molandev-datasource、molandev-encrypt、molandev-lock、molandev-file、molandev-spring -->
```

### 方式二：使用 BOM 统一版本

在 `pom.xml` 的 `<dependencyManagement>` 中引入 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.molandev</groupId>
            <artifactId>molandev-dependencies</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

然后引入需要的模块（无需指定版本）：

```xml
<dependency>
    <groupId>com.molandev</groupId>
    <artifactId>molandev-util</artifactId>
</dependency>
```

---

## 🤝 贡献与反馈

我们期待你的声音！无论是功能建议、Bug 反馈还是代码贡献，MolanDev Framework 的成长离不开每一位开发者的参与。

- **官方网站**: [molandev.com](https://molandev.com)
- **源码仓库**: [Gitee](https://gitee.com/molandev/molandev-framework) / [GitHub](https://github.com/molandev/molandev-framework)

---

## 📄 开源协议

本项目采用 **MIT** 协议开源，你可以自由地在商业项目中使用、修改或分发，无任何限制。

---

<p align="center">
  <b>让开发更简单，让系统更安全。</b><br>
  Designed with ❤️ by MolanDev Team
</p>

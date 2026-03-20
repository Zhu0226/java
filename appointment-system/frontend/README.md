## 智慧医疗预约挂号平台 - 启动与代码说明（前后端一体）

本项目是一个 **前后端分离** 的智慧医疗预约挂号平台，后端基于 **Spring Boot + MyBatis + ShardingSphere + Redis + RabbitMQ**，前端基于 **Vue 3 + TypeScript + Vite + Element Plus**。  
下面按“环境准备 → 启动步骤 → 业务流程 → 关键代码设计”的顺序，一步步说明。

---

## 一、项目整体结构

- **`backend/`**：Java 后端
  - 核心入口：`BackendApplication`（`com.hospital.backend.BackendApplication`）
  - 业务包：`com.hospital.appointment`（controller / service / mapper / model / common / config / aspect / mq 等）
  - 配置文件：`backend/src/main/resources/application.properties`
- **`frontend/`**（当前目录）：Vue 前端
  - 入口：`src/main.ts`
  - 路由：`src/router`
  - 状态管理：`pinia`
  - UI 组件：`element-plus`
  - 本地开发代理配置：`vite.config.ts`

前端在开发环境通过 Vite 的 **代理** 将 `/api/**` 请求转发到后端的 `http://localhost:8080`。

---

## 二、运行环境准备

后端启动依赖的基础中间件如下（默认端口均可在 `application.properties` 中修改）：

- **JDK 17**
- **Maven 3.8+**
- **MySQL**
  - 地址：`127.0.0.1:3306`
  - 数据库：`hospital_appointment_pro`
  - 账号密码默认：`root / 1234`（可通过 `spring.datasource.username/password` 覆盖）
- **Redis**
  - 地址：`127.0.0.1:6379`
- **RabbitMQ**（非所有功能都强依赖，但推荐安装）
  - 地址：`127.0.0.1:5672`
  - 账号密码默认：`guest / guest`
- **Node.js 18+ 与 npm**

所有这些地址与账号均在后端的 `application.properties` 中有明确配置：

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/hospital_appointment_pro?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:1234}

spring.data.redis.host=127.0.0.1
spring.data.redis.port=6379

spring.rabbitmq.host=127.0.0.1
spring.rabbitmq.port=5672
spring.rabbitmq.username=${RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:guest}
```

---

## 三、数据库初始化（按代码一步步推断出的必备表）

ShardingSphere 的异常日志中，如果出现：

> `Table or view \`biz_schedule\` does not exist.`

说明数据库尚未初始化。根据实体与 Mapper 代码，至少需要如下几张核心表：

1. **排班表 `biz_schedule`**  
   对应实体：`Schedule`（`model.entity.Schedule`）  
   对应 Mapper：`AdminScheduleMapper`、`ScheduleMapper`

```sql
CREATE DATABASE IF NOT EXISTS hospital_appointment_pro DEFAULT CHARSET utf8mb4;
USE hospital_appointment_pro;

CREATE TABLE IF NOT EXISTS biz_schedule (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id     BIGINT       NOT NULL,
  dept_id       BIGINT       NOT NULL,
  work_date     DATE         NOT NULL,
  shift_type    TINYINT      NOT NULL COMMENT '1-上午, 2-下午, 3-夜诊',
  total_num     INT          NOT NULL,
  available_num INT          NOT NULL,
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1-启用, 0-停用',
  is_deleted    TINYINT      NOT NULL DEFAULT 0,
  create_time   DATETIME              DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

2. **号源池表 `biz_ticket_pool`**  
   对应实体：`TicketPool`（`model.entity.TicketPool`）  
   由 `AdminScheduleMapper.batchInsertTicketPool` 批量插入：

```sql
CREATE TABLE IF NOT EXISTS biz_ticket_pool (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  schedule_id BIGINT       NOT NULL,
  ticket_no   INT          NOT NULL,
  start_time  TIME         NOT NULL,
  end_time    TIME         NOT NULL,
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0-未放号,1-可预约,2-锁定,3-已售出',
  version     INT          NOT NULL DEFAULT 0,
  is_deleted  TINYINT      NOT NULL DEFAULT 0,
  create_time DATETIME              DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

3. **医生表 `sys_doctor`**  
   对应实体：`SysDoctor`（`model.entity.SysDoctor`）  
   被 `ScheduleMapper` 和 `AdminScheduleMapper.selectAllActiveDoctors` 使用：

```sql
CREATE TABLE IF NOT EXISTS sys_doctor (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  dept_id          BIGINT       NOT NULL,
  doctor_name      VARCHAR(50)  NOT NULL,
  title            VARCHAR(50),
  consultation_fee DECIMAL(10,2) DEFAULT 0,
  status           TINYINT      NOT NULL DEFAULT 1,
  is_deleted       TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_doctor (dept_id, doctor_name, title, consultation_fee, status, is_deleted)
VALUES (1, '示例医生', '主任医师', 50.00, 1, 0);
```

实际项目中还包含患者、用户、订单等表（`SysPatient`、`SysUser`、`Order` 等实体），请根据实际 SQL 初始化脚本一次性导入，以确保所有功能可用。

---

## 四、后端启动流程（从入口类到配置）

### 4.1 入口类 `BackendApplication`

```java
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
@ComponentScan(basePackages = {"com.hospital"})
@MapperScan("com.hospital.appointment.mapper")
@EnableScheduling
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

- **排除 `DataSourceAutoConfiguration`**：启用自定义的 ShardingSphere 数据源配置。
- **排除 `SecurityAutoConfiguration`**：关闭 Spring Security 默认登录拦截，所有接口鉴权由自定义逻辑控制。
- **`@MapperScan`**：自动扫描 `com.hospital.appointment.mapper` 下的 MyBatis Mapper。
- **`@EnableScheduling`**：启用定时任务（比如自动生成排班等）。

### 4.2 数据源与 Redis 配置

- 数据源在 `ShardingDataSourceConfig` 中基于 `shardingsphere-jdbc-core` 配置，实现对订单等表的分库分表。
- `RedisConfig` 统一指定 `RedisTemplate` 的序列化方式，避免默认 JDK 序列化带来的二进制乱码。

### 4.3 全局异常处理与统一返回

- `GlobalExceptionHandler` 拦截：
  - `AuthException` → 返回 401，触发前端重新登录或刷新 Token。
  - `BusinessException` → 直接将业务信息返回给前端。
  - 其他异常 → 统一返回 “系统繁忙，请稍后再试”，并在日志中打印堆栈。
- `Result` 类封装统一的响应结构（`code`、`msg`、`data`），所有 Controller 返回该类型。

---

## 五、核心业务链路概览（按代码调用顺序说明）

### 5.1 患者个人中心信息（示例：`PatientController`）

入口类：`com.hospital.appointment.controller.PatientController`

1. `GET /api/patient/profile/info`
2. 通过 `UserContext.getUserId()` 获取当前登录患者 ID（登录时在 `AuthServiceImpl` 签发的 JWT 里写入）。
3. 使用 `PatientMapper.findById` 查询患者基础信息。
4. 根据 `birthDate` 动态计算年龄（`Period.between(...).getYears()`）。
5. 对手机号和身份证号做脱敏处理（中间段替换为 `****` 和 `**********`）。
6. 通过 `OrderMapper.countOrdersByStatus` 分别统计待支付 / 待就诊 / 已完成订单数。
7. 封装到 `PatientProfileVO` 返回给前端。

**前端效果**：患者首页会看到自己的姓名、性别、年龄、脱敏后的手机号/身份证号以及各类订单数量。

### 5.2 排班与挂号链路（简要）

- 管理端通过 `AdminScheduleController` 和 `AdminScheduleServiceImpl`：
  - 先调用 `AdminScheduleMapper.checkScheduleExists` 防止重复生成同一天同医生同班次的排班。
  - 插入 `biz_schedule` 记录后，批量生成对应的 `biz_ticket_pool` 号源记录。
- 患者端查询排班：
  - `ScheduleMapper.selectAvailableSchedulesByDept` 连表查询 `biz_schedule` 与 `sys_doctor`，只返回 `status=1` 且 `is_deleted=0` 的可用排班。
- 患者提交挂号订单：
  - `OrderServiceImpl.createAppointmentOrder` 通过 Redisson 分布式锁（`RedissonClient`）+ Redis 列表 + MySQL 乐观锁，防止超卖与重复挂号。
  - 订单创建成功后，使用 `RabbitTemplate` 将订单号投递到延迟队列，超时未支付则由 `OrderTimeoutListener` 自动取消并回收号源。

---

## 六、前端开发与运行方式

### 6.1 安装依赖

```bash
cd frontend/hospital-frontend
npm install
```

### 6.2 启动前端开发服务器

```bash
npm run dev
```

默认会在浏览器中自动打开（或手动访问）：  
`http://localhost:3000`

### 6.3 接口代理配置（`vite.config.ts`）

```ts
export default defineConfig({
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 如果后端实际接口不包含 /api 前缀，则需要打开下面这行：
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
});
```

- 前端代码只需要请求 `/api/**`，Vite 会将其反向代理到后端。
- 如后端 Controller 的 `@RequestMapping` 没有 `/api` 前缀时，请务必开启 `rewrite` 以去掉前缀。

---

## 七、后端运行步骤汇总

1. **确保中间件已启动**
   - MySQL（包含 `hospital_appointment_pro` 库与表）
   - Redis（6379）
   - RabbitMQ（5672，可选）
2. **启动后端**

```bash
cd backend
mvn spring-boot:run
```

控制台看到 `Tomcat initialized with port 8080` 且进程持续运行，说明后端启动成功。

3. **启动前端**

```bash
cd frontend/hospital-frontend
npm run dev
```

4. **访问系统**
   - 浏览器打开 `http://localhost:3000` 进入前端界面。
   - 所有业务操作（挂号、排班、订单等）通过 `/api/**` 调用后端。

---

## 八、常见问题排查

- **前端提示“系统繁忙，请稍后再试”**
  - 查看后端日志，如果看到 `Table or view \`xxx\` does not exist`：
    - 说明数据库缺少相应表，请根据实体 / Mapper 代码补齐建表 SQL。
  - 如果是 Redis / RabbitMQ 连接异常：
    - 检查对应服务是否启动，端口是否被占用，`application.properties` 中的地址是否正确。
- **访问接口 404**
  - 检查 Vite 代理是否正确（端口、`/api` 前缀、`rewrite` 配置）。
  - 检查后端 Controller 上的 `@RequestMapping` 与方法 `@GetMapping/@PostMapping` 路径是否与前端调用一致。

通过以上说明，你可以从环境搭建、数据库初始化，到前后端启动与核心业务链路，都顺利跑通并逐步理解整个项目的设计。 

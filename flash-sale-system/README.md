## 本地手动提交到 GitHub（备忘）

```bash
# 1. 进入项目根目录
cd D:\Develop\project\project\flash-sale-system

# 2. 查看当前修改
git status

# 3. 添加所有修改（也可以只 add 某些文件）
git add .

# 4. 提交（把 提交说明 换成本次修改的描述）
git commit -m "提交说明"

# 5. 推送到远程 main 分支
git push origin HEAD:main
```

---

## 高并发限时秒杀系统（整体说明）

这是一个典型的**前后端分离高并发限时秒杀系统**，用于演示：

- 如何用 **Spring Boot + MyBatis-Plus + Redis + MySQL + RabbitMQ** 搭建秒杀后台；
- 如何用 **Vue 3 + Vite** 搭建前端页面（用户端 + 后台管理）；
- 在「一人一单、库存扣减、防刷、防超卖、异步下单、监控」等场景下的完整实现。

下面从「目录结构 → 启动方式 → 一次完整抢购请求在代码中的流转」来一步步分析。

---

## 一、项目结构（从仓库根目录看）

```text
flash-sale-system/
  backend/                 # 后端：Spring Boot 秒杀服务
    pom.xml
    src/
      main/
        java/com/seckill/  # 业务代码
        resources/         # 配置、脚本
      test/                # 单元测试
    sql/                   # 建表 & 初始化 SQL
    README.md              # 后端详细阅读路线（更偏后端学习）

  frontend/                # 前端：Vue 3 + Vite 单页应用
    src/
      api/                 # axios 封装、前后端接口映射
      router/              # 前端路由
      views/               # 各个页面
    README.md              # 前端说明

  .gitignore
  .gitattributes
  README.md                # 你现在看到的这个整体说明
```

- **面试官/读代码的人** 推荐先看根 `README.md` 把全貌搞清楚，再进入 `backend/` / `frontend/` 看细节。

---

## 二、如何本地跑起来（按顺序来）

### 1. 安装依赖

- **后端环境**
  - JDK 17
  - Maven 3.9+
  - MySQL 8.x
  - Redis（本地单机即可）
  - RabbitMQ（可选：开启异步下单功能时需要）
- **前端环境**
  - Node.js 16+（推荐 18+）
  - npm 或 pnpm

### 2. 初始化数据库（看代码建表）

在 MySQL 中执行 `backend/sql/` 目录下的脚本：

- `schema.sql`：根据实体 `SeckillGoods` / `SeckillOrder` 等建表；
- `init_data.sql`：插入一些初始商品数据。

如果你打开 `backend/src/main/java/com/seckill/entity/SeckillGoods.java` 和 `SeckillOrder.java`，可以一一对上字段：

- `SeckillGoods` 对应表 `seckill_goods`，字段包括：
  - `goods_name`、`price`、`stock`、`start_time`、`end_time`；
  - `on_shelf`（是否上架）、`activity_tag`（活动标签）等；
- `SeckillOrder` 对应表 `seckill_order`，字段包括：
  - `user_id`、`goods_id`、`status`（0 待支付、1 已支付、2 已取消）；
  - 唯一索引 `uk_user_goods (user_id, goods_id)` 实现**一人一单**。

扩展字段（如上架 / 标签、订单 `update_time`）的变更 SQL 在：

- `backend/sql/alter_add_columns.sql`

### 3. 配置后端连接信息

打开 `backend/src/main/resources/application.yml`，主要关注几块：

- **server 端口**
  - `server.port: 8081`（前端 Vite 代理也是指向这个端口）。
- **数据源配置**
  - `spring.datasource.url: jdbc:mysql://localhost:3306/flash_sale?...`
  - `spring.datasource.username` / `spring.datasource.password`。
- **Redis**
  - `spring.data.redis.host: localhost`
  - `spring.data.redis.port: 6379`
  - 如果无密码，可以去掉或注释 `password`。
- **RabbitMQ（可选）**
  - 在 `RabbitMQConfig` 中配置队列、交换机；
  - 在 `application.yml` 中有 `seckill.order.async` 开关（false=同步写库，true=异步下单）。
- **后台 JWT 配置**
  - `admin.jwt-secret`、`admin.jwt-expire-minutes`、`admin.login-username`、`admin.login-password`。

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

看到类似日志代表启动成功：

- `Tomcat started on port 8081`
- `Started FlashSaleSystemApplication...`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认访问 `http://localhost:5173`，Vite 会把 `/seckill` 和 `/admin` 请求代理到 `http://localhost:8081`。

---

## 三、从「用户抢购一次」看代码怎么走

这一小节按**真实一次抢购**来，配合后端核心代码逐步看：

### 步骤 1：用户在前端看到商品列表

- 页面：`frontend/src/views/GoodsList.vue`
- 前端调用：
  - `frontend/src/api/seckill.js` 中的 `listGoods()`；
  - 实际请求：`GET /seckill/goods`。
- 后端入口：
  - `backend/src/main/java/com/seckill/controller/SeckillController.java` 中的 `listGoods()`。
  - 主要逻辑：调用 `SeckillService.listSeckillGoods()`，只返回 `onShelf = 1` 的商品。

到这里你可以在 IDE 中找到 `SeckillController.listGoods()`，配合前端的表格组件一起看。

### 步骤 2：进入商品详情页

- 页面：`GoodsDetail.vue`
- 前端会调用多几个接口：
  - `GET /seckill/goods/{goodsId}` → 商品基础信息；
  - `GET /seckill/status/{goodsId}` → 活动是否开始 / 结束、库存是否还有；
- 后端：
  - `SeckillController.goodsDetail()` → `SeckillService.getGoodsDetail(goodsId)`；
  - `SeckillController.status()` → `SeckillService.getSeckillStatus(goodsId)`：
    - 基于当前时间、商品的 `startTime` / `endTime` 和库存，返回「未开始 / 进行中 / 已结束」等状态枚举。

这一层你可以重点关注的是：**Controller 只做“接参数+调 Service+返回统一响应”**。

### 步骤 3：后台运营预热库存（写入 Redis）

在真正高并发前，需要先把库存从 MySQL 预热到 Redis。

- 后端接口：
  - `POST /seckill/preheat/{goodsId}/{stock}`（后台管理页面也会调用）；
- Controller：
  - `SeckillController.preheat()`；
- Service：
  - `SeckillService.preHeatStock(goodsId, stock)`：
    - 校验商品存在；
    - 校验预热库存 `stock > 0` 且不超过 DB 中该商品的真实库存；
    - 使用 `RedisTemplate` 把库存数量写到 Redis，key 前缀在 `SeckillService` 里以 `PREFIX_` 常量形式维护。

Redis 连接和 Lua 脚本执行方式在：

- `backend/src/main/java/com/seckill/config/RedisConfig.java`
- `backend/src/main/resources/seckill.lua`

### 步骤 4：用户获取「动态秒杀地址」

为了防止别人直接脚本刷固定 URL，系统采用了**动态路径**：

- 接口：
  - `GET /seckill/path?goodsId=1`
  - Header 中必须带 `userId`。
- Controller：
  - `SeckillController.getPath(goodsId, userId)`（方法名可能类似）
  - 上面会挂一个自定义注解 `@RateLimit(time=xx, count=xx)`，防止用户频繁刷新获取 path。
- Service：
  - `SeckillService.createSeckillPath(userId, goodsId)`：
    - 校验商品是否存在；
    - 校验活动是否在时间窗口内；
    - 生成一串随机字符串（例如 MD5 / UUID）；
    - 写入 Redis，key 里会包含 `userId + goodsId`；
    - 返回这串 path 给前端。

注解 `@RateLimit` 的定义和拦截逻辑：

- 注解定义：`backend/src/main/java/com/seckill/annotation/RateLimit.java`
- 拦截器：`backend/src/main/java/com/seckill/interceptor/RateLimitInterceptor.java`
  - 在 `preHandle` 中：
    - 解析方法上的 `@RateLimit`；
    - 从 Header 取 `userId`；
    - 拼 Redis key（如 `rate_limit:userId:接口名`）；
    - 使用 Redis 自增判断当前时间窗口次数是否超限；
    - 超限则直接返回「请求太频繁」，不进入 Controller。
- 拦截器注册：`WebMvcConfig` 中添加到 Spring MVC。

### 步骤 5：用户真正发起秒杀请求

当前端拿到 path 后，会调用：

- URL 形如：`POST /seckill/{path}/doSeckill?goodsId=1`
- Header 中仍然带 `userId`。

后端流程：

1. **Controller 层**：`SeckillController.doSeckill(path, goodsId, userId)`
   - 从 Header / Path / Query 中取出 `userId`、`goodsId` 和 path；
   - 直接调用 `SeckillService.executeSeckill(userId, goodsId, path)`。
2. **Service 核心方法**：`executeSeckill`（位于 `SeckillService`）
   - **校验参数 & 路径是否合法**（从 Redis 取出之前写进去的 path 对比）；
   - **检查一人一单**（通常会在 Redis 和数据库两层做防重）；
   - **Lua 脚本扣减库存**：
     - 使用在 `RedisConfig` 中注入的 `DefaultRedisScript`，执行 `seckill.lua`；
     - Lua 中会按照：
       - 若 key 不存在或库存 <= 0，返回失败码；
       - 否则 `decr` 一次，返回成功码；
   - **根据 Lua 结果返回**：
     - 若库存不足或已售罄，直接返回对应错误码（`ErrorCode` 枚举里有定义）；
     - 若库存扣减成功，再走后续下单逻辑（同步写库或异步 MQ）。

**这一段是整个项目最核心的逻辑**，建议你按如下顺序阅读：

- 先看 `executeSeckill` 代码，把每一个 `return` 的含义记在纸上；
- 对照 README 中的错误码表 `ErrorCode`；
- 最后打开 `seckill.lua`，搞清楚 Redis 层在做什么。

### 步骤 6：订单落库（同步 / 异步）

根据配置 `seckill.order.async` 决定采用哪种模式：

- **同步模式（默认 false）**
  - Redis 扣库存成功后，Service 里直接通过 `SeckillOrderMapper` 向 MySQL 插入订单；
  - `SeckillOrder` 实体 + 唯一索引确保一人一单；
  - 失败时抛出对应业务异常（`BusinessException`），由全局异常处理器统一包装。

- **异步模式（true + RabbitMQ 已启动）**
  - Service 不直接插入数据库，而是构造 `SeckillOrderMessage`（包含 `userId`、`goodsId`、timestamp），通过：
    - `SeckillOrderProducer` 发送到 MQ；
  - 消费者 `SeckillOrderConsumer` 监听队列，真正执行插入操作；
  - 主队列绑定了死信队列 `seckill.order.dlq`：
    - 消费失败时消息会进入 DLQ，由 `SeckillOrderDlqConsumer` 记录错误日志；
    - 便于后续排查与手工补偿。

MQ 的完整配置在：

- `backend/src/main/java/com/seckill/config/RabbitMQConfig.java`

### 步骤 7：查询抢购结果 & 订单列表

用户完成秒杀后，前端会轮询或手动点击：

- `GET /seckill/orders/result?goodsId=1` → 查询该用户对某商品是否有订单；
- `GET /seckill/orders` → 查看我的所有订单；
- `POST /seckill/orders/{orderId}/pay` / `cancel` → 支付 / 取消；

对应后端：

- Controller 中的订单相关接口（通常仍在 `SeckillController` 或专门订单 Controller 中）；
- Service 中的 `payOrder` / `cancelOrder` 方法：
  - 使用 `OrderStatus` 枚举控制状态流转（只允许 0→1、0→2）。

---

## 四、后台管理 & 鉴权（Admin 侧）

后台管理主要在这几个类：

- 控制器：`backend/src/main/java/com/seckill/controller/AdminSeckillController.java`
  - 前缀：`/admin/seckill/goods`
  - 支持商品的增删改查、设置活动时间、上架状态、活动标签等；
- 登录控制器：`AdminLoginController`，接口：
  - `POST /admin/login`：传入用户名密码，返回 JWT token。

JWT 相关：

- `JwtProperties`：从 `application.yml` 读取 JWT 秘钥、过期时间等；
- `JwtUtils`：生成 / 校验 token；
- `AdminAuthFilter`：从 `Authorization: Bearer xxx` 中解析 token 并校验；
- `AdminAuthConfig`：注册过滤器，只放行 `/admin/login`，其余 `/admin/**` 必须带合法 token。

权限注解 `@RequiresPermission` 也在 `annotation` 包下定义，可对某些接口做更细粒度控制（示例逻辑在拦截器 / AOP 中）。

---

## 五、统一返回结构、错误码与日志

### 1. 统一返回结构 `ApiResponse`

所有接口统一返回：

```json
{
  "code": 0,
  "msg": "OK",
  "data": { }
}
```

- 封装类：`backend/src/main/java/com/seckill/common/ApiResponse.java`
- 常见错误枚举：`ErrorCode` 枚举
  - 包含「参数错误、资源不存在、请求太频繁、活动未开始 / 已结束、一人一单已抢过、库存不足」等。

全局异常处理：

- `GlobalExceptionHandler` 通过 `@RestControllerAdvice` 拦截 `BusinessException`、其他异常，统一转换为 `ApiResponse`。

### 2. 日志与监控

- 日志配置：`backend/src/main/resources/logback-spring.xml`
  - `com.seckill` 包日志同时输出到控制台和 `logs/seckill.log`；
  - 按天滚动，默认保留 7 天。
- 在 `SeckillService.executeSeckill` 等关键路径中，打了结构化的文本日志：
  - 如 `step=... userId=... goodsId=... reason=... durationMs=...`；
  - 方便以后接入 ELK 做检索、统计。
- 监控指标：
  - 依赖：`spring-boot-starter-actuator` + `micrometer-registry-prometheus`（见 `pom.xml`）；
  - 配置：`management.endpoints.web.exposure.include: health,info,prometheus`；
  - 访问：`GET /actuator/prometheus`，可以对接 Prometheus + Grafana。

---

## 六、前端代码如何映射到后端接口

前端目录：`frontend/src/`，重点看三块：

- `api/`：
  - `request.js`：封装 axios 实例、统一注入 JWT、处理 401 跳转等；
  - `seckill.js`：C 端秒杀相关接口封装（listGoods、goodsDetail、status、path、doSeckill、result、metrics 等）；
  - `admin.js`：后台管理接口封装（登录、商品 CRUD、预热库存）。
- `views/`：
  - `GoodsList.vue`：秒杀首页；
  - `GoodsDetail.vue`：商品详情 + 抢购按钮；
  - `MyOrders.vue`：我的订单；
  - `Metrics.vue`：秒杀指标；
  - `AdminLogin.vue`：后台登录；
  - `AdminGoods.vue`：后台商品管理。
- `router/`：
  - 定义路由与组件的映射，比如 `/goods/:id` 映射到 `GoodsDetail.vue`。

页面与接口的对照表可以在 `frontend/README.md` 中看到，这里只是强调：**前端所有核心行为都能在后端 Controller / Service 中找到一一对应的方法**，便于你在面试时从任意一端讲回整个链路。

---

## 七、如果你要系统学习这份代码（建议路线）

1. **第一轮（体验流程）**
   - 跑通项目（按「如何本地跑起来」一节）；
   - 在前端完整做一遍：预热 → 获取 path → 秒杀 → 查订单 → 看指标。
2. **第二轮（读 Controller）**
   - 按 `SeckillController` 上的方法顺序，对照接口一条条看；
   - 弄清楚每个 URL 对应哪个 Service 方法。
3. **第三轮（钻 Service）**
   - 重点阅读 `SeckillService`：
     - `listSeckillGoods`、`getGoodsDetail`、`getSeckillStatus`、`preHeatStock`；
     - 最后攻克 `executeSeckill`。
4. **第四轮（Redis + Lua + MQ）**
   - 看 `RedisConfig` + `seckill.lua`，理解不超卖的核心实现；
   - 看 `RabbitMQConfig` + `SeckillOrderProducer` + `SeckillOrderConsumer` + DLQ 。
5. **第五轮（限流 + 鉴权 + 监控）**
   - 看 `@RateLimit` 注解、`RateLimitInterceptor`；
   - 看 `JwtUtils` + `AdminAuthFilter`；
   - 看日志与 Prometheus 暴露的指标。

当你能从「前端按钮点击」一路讲到「Redis / MQ / MySQL 的变化」，再讲回「监控和日志里会看到什么」，这套秒杀系统对你来说就真正不是黑盒了。


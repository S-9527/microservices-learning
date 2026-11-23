# 微服务学习之旅 (microservices-learning)

> 一个**从零开始、可运行、文档驱动**的 Spring Cloud 微服务学习仓库。
> 技术栈选用当前主流组合：**Nacos + Sentinel + Spring Cloud Gateway + OpenFeign + MySQL + MyBatis-Plus**。
> **Docs First · 中文为主 · 保留英文技术术语 · 干净的 Git 提交历史**

---

## 🎯 这个项目要解决什么

大多数微服务 demo 的问题是：代码和文档脱节、多轮对话后大模型丢失上下文、提交历史混乱无法按步骤复习。

本仓库用三件事解决：

1. **[大量结构化文档](./docs/README.md)** —— 每个主题一篇，能独立阅读，是项目的「长期记忆」。
2. **[AGENTS.md](./AGENTS.md)** —— 约束大模型：改代码必须改文档、问题必须记录、提交必须原子化。
3. **细粒度 Conventional Commits** —— 一个模块一个提交，在 GitHub 提交页就能当复习材料看。

---

## 🧩 技术栈与版本（锁定）

| 组件 | 选型 | 版本 | 为什么 |
|------|------|------|--------|
| JDK | Java | 25 LTS | 环境已装 |
| 框架 | Spring Boot | 4.0.10 | 当前稳定补丁版 |
| 微服务套件 | Spring Cloud | 2025.1.5 | 与 Boot 4.0.x 匹配 |
| 注册/配置中心 | **Nacos** | Server v3.1.1 / Client 3.1.1 | 当前主流，注册与配置二合一，控制台直观 |
| 容错限流 | **Sentinel** | 1.8.9 + Dashboard | 主流、带可视化控制台 |
| 网关 | Spring Cloud Gateway | 5.0.5（WebFlux） | 官方网关 |
| 服务调用 | OpenFeign | 5.0.4 | 声明式 HTTP 客户端 |
| 数据库 | **MySQL** | 8.0 | 真实数据库，三库隔离 |
| 持久层 | **MyBatis-Plus** | 3.5.18（Boot4 starter） | 国内主流，XML/注解都支持 |
| 链路追踪 | Micrometer + Zipkin | 1.6.9 / 3.5 | traceId 贯穿全链路 |
| 容器 | Docker Compose | v2 | 一条命令起全栈 |

> 版本兼容矩阵与踩坑记录见 [AGENTS.md 第 4 节](./AGENTS.md#4-版本锁定改动前必须写-adr) 和 [ADR-0001](./docs/decisions/0001-技术栈与版本选型.md)。**不要随意升级。**

---

## 🧩 模块一览

| 模块 | 端口 | 数据库 | 职责 |
|------|------|--------|------|
| [`common`](./common) | — | — | 统一响应体、错误码、全局异常、跨服务 DTO |
| [`api-gateway`](./api-gateway) | 8080 | — | 统一入口、路由、traceId 透传 |
| [`user-service`](./user-service) | 8081 | `user_db` | 用户领域 |
| [`product-service`](./product-service) | 8082 | `product_db` | 商品领域、真实库存扣减 |
| [`order-service`](./order-service) | 8083 | `order_db` | 订单领域、Feign 编排 + Sentinel 降级 |

基础设施（Docker）：MySQL `3306`、Nacos `8848`、Sentinel Dashboard `8090`、Zipkin `9411`。

---

## 🚀 快速开始

### 前置要求

- Docker + Docker Compose v2（推荐）
- 若本地跑：JDK 25 + Maven 3.9+ + 本地 MySQL/Nacos/Sentinel

### 启动全栈（两步）

```bash
# 1. 先在宿主机构建 jar（依赖已缓存，几秒完成）
mvn -q -DskipTests package

# 2. 构建镜像并启动全部容器
docker compose up -d --build
```

> 镜像只负责把 jar 装进 JRE，不在容器里重复下载 Maven 依赖，因此构建很快。
> 改了代码后重新执行这两步即可。

启动顺序由 Compose 的 healthcheck + `depends_on` 保证：
`MySQL → Nacos → Nacos 配置初始化 → 业务服务 → Gateway`

访问入口：

| 地址 | 说明 |
|------|------|
| http://localhost:8088 | Nacos 控制台（Nacos 3.x 控制台在 8080，宿主映射到 8088；无鉴权） |
| http://localhost:8090 | Sentinel Dashboard |
| http://localhost:9411 | Zipkin UI |
| **http://localhost:3000** | **购物页面（React 前端，见 [docs/14](./docs/14-可观测性实验台.md)）** |
| http://localhost:8080/api/users/1 | 经网关查询用户 |
| http://localhost:8080/api/products/1 | 经网关查询商品 |
| `POST http://localhost:8080/api/orders?userId=1&productId=1&quantity=1` | 下单（完整跨服务链路） |

### 本地编译 / 测试

```bash
mvn -q -DskipTests compile   # 全量编译
mvn test                     # 全量测试（不依赖外部中间件）
```

---

## 📚 学习路线

从 [docs/00-学习路线](./docs/00-学习路线.md) 开始，完整索引见 [docs/README.md](./docs/README.md)。

| 阶段 | 主题 | 状态 |
|------|------|------|
| 0 | 环境准备与全局认知 | ✅ |
| 1 | 微服务基础 / 服务拆分 | ✅ |
| 2 | Nacos 服务注册与发现 | ✅ |
| 3 | OpenFeign 服务间通信 | ✅ |
| 4 | Gateway 网关 | ✅ |
| 5 | Nacos 配置中心 | ✅ |
| 6 | Sentinel 熔断限流降级 | ✅ |
| 7 | Micrometer + Zipkin 链路追踪 | ✅ |
| 8 | MySQL + MyBatis-Plus 数据持久化 | ✅ |
| 9 | Docker Compose 编排 + GitHub Actions | ✅ |
| 10 | 进阶：分布式事务 / 消息 / 安全 | ⬜ 待展开 |

> 技术选型理由见 [ADR-0002（Nacos）](./docs/decisions/0002-选择Nacos作为注册与配置中心.md)、
> [ADR-0003（MySQL）](./docs/decisions/0003-选择MySQL与MyBatis-Plus.md)、
> [ADR-0004（Sentinel）](./docs/decisions/0004-选择Sentinel作为容错组件.md)。

---

## 🤝 与大模型协作

1. 让它**先读 [AGENTS.md](./AGENTS.md)**；
2. 每轮结束检查 `docs/` 是否更新；
3. 有疑问要求写入 `docs/questions.md`，而不是猜答案；
4. 提交前要求执行 `mvn -q -DskipTests compile`。

---

## 📄 License

MIT（仅用于学习）

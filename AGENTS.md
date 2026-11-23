# AGENTS.md — 本项目的大模型协作规范

> 这个文件是给 **AI 助手（大模型）** 读的「项目宪法」。
> 人类读者请从 [README.md](./README.md) 开始。
> 任何 AI 助手在本仓库执行任何操作之前，**必须先完整阅读本文件**。

---

## 0. 最重要的三句话

1. **这是一个「学习项目」，不是生产系统。** 代码的首要目标是讲清概念，其次才是跑得起来。可读性 > 抽象 > 性能。
2. **文档是项目的「长期记忆」。** 用户会多轮对话，上下文会丢失。设计决策、踩过的坑、未解答的问题必须落盘到 `docs/`。
3. **用户会在 GitHub 网页上阅读代码。** 因此**一个模块一个提交**，每个提交必须是「一个完整、可解释的变更」。用户已明确反馈：一次生成太多文件不好阅读。

---

## 1. 硬性规则（不可违反）

| # | 规则 | 原因 |
|---|------|------|
| R1 | **不得随意删除或整体重写 `docs/` 下的已有文档**；只能追加或局部修订。只有用户明确要求「抹除某项废弃技术栈」时才可删除对应文档，并在 ADR 中记录决策 | 文档是复习资料 |
| R2 | **代码变更必须同步更新对应文档**；禁止「只改代码不改文档」 | 避免代码/文档漂移 |
| R3 | **每提出/发现一个问题，必须记录到 [`docs/questions.md`](./docs/questions.md)**（含状态：待解答 / 已解答 / 已放弃） | 用户明确要求 |
| R4 | **Conventional Commits**：`feat|fix|docs|build|ci|refactor|test|chore(scope): 中文描述`。**一个模块一个提交**，提交前检查 `git diff --stat`，超过 ~10 个文件要拆分 | 用户已反馈粒度问题 |
| R5 | **提交前必须验证**：至少 `mvn -q -DskipTests compile`；新增模块时要逐模块把 `<module>` 加入父 POM，保证**每个提交都能编译** | 学习仓库随时可运行 |
| R6 | **禁止 `git push --force`、`git rebase` 已推送的提交、`--amend` 已推送的提交** | 保护公开历史 |
| R7 | **禁止提交真实密钥/token/密码**。Nacos token 与 MySQL 密码仅为本地学习值，已在 `.env.example` 和 compose 中明确标注；生产必须替换 | 安全 |
| R8 | **不要擅自升级依赖版本**。版本锁定见第 4 节；升级先写 ADR | Boot 4 / Cloud 2025.1 组合脆弱 |
| R9 | **不要引入更重的生产组件**（K8s、Seata、SkyWalking、RabbitMQ 等）来炫技；如需扩展先写 ADR 并与用户确认 | 保持学习曲线平缓 |
| R10 | **新增模块必须同时提供**：`pom.xml`、启动类、`application.yml`、Dockerfile、文档段落 | 结构一致 |
| R11 | **不确定的技术细节不许编造**：先查 Maven Central / 官方 jar 元数据 / 官方文档，验证过程记入 ADR 或排错文档 | 避免误导 |
| R12 | **每轮对话结束前**，新认知更新 [`docs/12-常见问题与排错.md`](./docs/12-常见问题与排错.md) 或 `questions.md`；并简要告诉用户「本轮改了什么、下一步做什么」 | 防止上下文丢失 |

---

## 2. 用户画像与偏好

- 系统学习微服务，要求**当前主流技术栈 + 真实实现**：Nacos（注册+配置）、Sentinel（容错）、真实 MySQL（不用内存模拟）。
- 多轮对话继续项目，文档必须能独立承载上下文。
- **在 GitHub 网页读代码** → 提交粒度小、提交信息有语义、文档用相对路径链接。
- 语言：**中文为主，技术术语保留英文**（Service Discovery、Circuit Breaker、traceId 等）。
- 希望问题被及时提出并记录。
- `gh` 已登录（账号 `S-9527`），公开仓库 `microservices-learning`。

---

## 3. 项目地图

| 模块 | 端口 | 数据库 | 职责 | 关键文档 |
|------|------|--------|------|----------|
| `common` | — | — | 响应体、错误码、异常、DTO | [02-架构总览](./docs/02-项目架构总览.md) |
| `api-gateway` | 8080 | — | 路由、traceId 透传 | [05-Gateway](./docs/05-API网关-Gateway.md) |
| `user-service` | 8081 | `user_db` | 用户领域 | [03-Nacos](./docs/03-Nacos服务注册与发现.md) |
| `product-service` | 8082 | `product_db` | 商品、库存扣减 | [09-MySQL+MyBatis-Plus](./docs/09-MySQL与MyBatis-Plus.md) |
| `order-service` | 8083 | `order_db` | 订单编排 + Sentinel | [07-Sentinel](./docs/07-Sentinel熔断限流.md) |

基础设施（Docker Compose）：MySQL `3306`、Nacos `8848/9848`、Sentinel Dashboard `8090`、Zipkin `9411`。

主链路：`客户端 → Gateway(8080) → order-service(8083) → [user-service(8081) + product-service(8082)]`，
服务注册/配置在 Nacos，容错由 Sentinel，traceId 上报 Zipkin，数据落在三个独立 MySQL schema。

---

## 4. 版本锁定（改动前必须写 ADR）

| 组件 | 版本 | 备注 |
|------|------|------|
| JDK | 25 LTS | 环境已装 |
| Lombok | 1.18.48 | Boot BOM 管理；父 POM 全局引入并配置 annotationProcessorPaths |
| Spring Boot | 4.0.10 | 父 POM 管理 |
| Spring Cloud | 2025.1.5 | BOM 管理 |
| Spring Cloud Alibaba | 2025.1.0.0 | 对应 Cloud 2025.1.x / Boot 4.0.x（官方 README 确认） |
| Nacos Server | v3.1.1 | 与 BOM 内置 nacos-client 3.1.1 对齐 |
| Sentinel | 1.8.9 | 与 Dashboard 镜像 tag 对齐 |
| Gateway | 5.0.5 | artifact：`spring-cloud-starter-gateway-server-webflux` |
| MyBatis-Plus | 3.5.18 | artifact：`mybatis-plus-spring-boot4-starter` |
| MySQL | 8.0 | `mysql:8.0` 镜像 |
| Zipkin | 3.5 | `openzipkin/zipkin:3.5` |

**关键 API / 配置陷阱：**

1. Gateway 配置前缀：`spring.cloud.gateway.server.webflux.*`；老前缀 `spring.cloud.gateway.*` 不生效。
2. Gateway artifact：`spring-cloud-starter-gateway-server-webflux`；老的 `spring-cloud-starter-gateway` 不存在。
3. Boot 4 移除了 `spring-boot-starter-aop`；需要 AOP 时显式加 `org.aspectj:aspectjweaver`。
4. Nacos Config 用 `spring.config.import: nacos:<dataId>?refreshEnabled=true`（不是 bootstrap.yml）。
5. Nacos 3.x 鉴权：`NACOS_AUTH_TOKEN` 必须 Base64 且 ≥32 字节；学习环境显式 `NACOS_AUTH_ENABLE=false`。
6. `@SentinelResource` 注解包：`com.alibaba.csp.sentinel.annotation.SentinelResource`。
7. `ApiResponse` 是 record，访问器是 `data()` 不是 `getData()`。
8. OpenFeign 的 groupId 是 `org.springframework.cloud`，不是 `com.alibaba.cloud`。

---

## 5. 新功能工作流（按顺序）

```
1. 读文档   → README / AGENTS / 相关 docs
2. 先提问   → 有歧义先用 question 工具问用户，或记入 questions.md
3. 写文档   → Docs First
4. 写代码   → 简洁、中文注释、包结构一致；实体用 `@Data`，日志用 `@Slf4j`，记录类用 `record`
5. 写测试   → 至少 1 个核心测试
6. 验证     → mvn compile + test；能启动的服务用 curl 冒烟
7. 更新索引 → README / docs/README.md / 进度看板
8. 提交     → 一个模块一个原子提交
9. 同步     → 用户确认后再 push（不要擅自 push）
```

---

## 6. 文档规范

- 位置 `docs/`，命名 `NN-主题-关键词.md`。
- 结构：`为什么 → 核心概念 → 本项目实现 → 动手验证 → 常见坑 → 延伸思考/相关文档`。
- Mermaid 画图，表格做对比，中文讲解 + 英文术语。
- 每篇末尾必须有「相关文档」相对链接区。
- ADR 写到 `docs/decisions/NNNN-标题.md`：背景 / 备选方案 / 决策 / 后果。

---

## 7. 问题记录协议

在 `docs/questions.md` 维护。格式见该文件顶部模板。以下情形**必须**新增条目：
- 用户对技术选型提出质疑；
- 报错排查超过一轮；
- 文档之间自相矛盾；
- 出现「先这样，以后再优化」的临时方案。

---

## 8. 常用命令

```bash
# 编译 / 测试
mvn -q -DskipTests compile
mvn test

# 一键全栈（先构建 jar，再起容器）
mvn -q -DskipTests package
docker compose up -d --build
docker compose ps
docker compose logs -f order-service
docker compose down

# 冒烟验证
curl http://localhost:8080/api/users/1
curl -X POST 'http://localhost:8080/api/orders?userId=1&productId=1&quantity=1'
curl http://localhost:8088   # Nacos 3.x 控制台
curl http://localhost:8848/nacos/v1/ns/service/list?pageNo=1\&pageSize=20  # API
curl http://localhost:8090
curl http://localhost:9411
```

---

## 9. 已知坑（持续补充）

见 [`docs/12-常见问题与排错.md`](./docs/12-常见问题与排错.md) 与 [`docs/questions.md`](./docs/questions.md)。

---

## 10. 本文件的维护

每次发现新的陷阱或规则必须追加到对应章节。修改本文件用提交信息 `docs: 更新 AGENTS.md 协作规范`。

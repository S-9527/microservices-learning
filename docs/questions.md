# 问题台账（Questions Log）

> 用户明确要求：**过程中有什么问题及时提出并记录**。这里是所有疑问的唯一入口。
> 状态：`待解答` / `已解答` / `已放弃` / `已转为后续阶段`。

---

## 登记格式

```markdown
### Q-000 简短问题
- 状态：待解答
- 提出日期：YYYY-MM-DD
- 背景：（为什么会有这个问题）
- 结论：（引用文档/ADR 链接；待解答时写「待补」）
```

---

## 台账

### Q-001 Spring Boot 4 / Spring Cloud 版本怎么配才不会崩？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：环境是 JDK 25（很新），Boot 3.x 的 Spring Framework 6 对 Java 25 支持存疑；Boot 4 又移除了一些老 starter。
- 结论：实测 Maven Central 元数据确认 [ADR-0001](./decisions/0001-技术栈与版本选型.md)：Boot 4.0.10 + Cloud 2025.1.5 + Alibaba 2025.1.0.0。Gateway artifact 改名为 `spring-cloud-starter-gateway-server-webflux`，配置前缀改为 `spring.cloud.gateway.server.webflux.*`；`spring-boot-starter-aop` 被移除，需显式加 `aspectjweaver`。

### Q-002 为什么选 Nacos 而不是 Consul / K8s Service？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：注册中心与配置中心的技术选型。
- 结论：Nacos 是当前国内主流，注册与配置二合一，控制台直观，且与 Boot 4 兼容的 Alibaba BOM 已存在。见 [ADR-0002](./decisions/0002-选择Nacos作为注册与配置中心.md)。

### Q-003 为什么用真实 MySQL 而不是内存数据？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：用户明确要求「真实，可以直接使用数据库而不是模拟」。
- 结论：三个服务各持一个 schema（user_db/product_db/order_db），用 MyBatis-Plus 读写；库存扣减用条件 UPDATE 防超卖。见 [ADR-0003](./decisions/0003-选择MySQL与MyBatis-Plus.md) 与 [09-MySQL](./09-MySQL与MyBatis-Plus.md)。

### Q-004 容错为什么用 Sentinel？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：用户要求主流技术栈，Sentinel 带可视化控制台。
- 结论：见 [ADR-0004](./decisions/0004-选择Sentinel作为容错组件.md)。规则默认存内存，持久化到 Nacos datasource 属进阶主题。

### Q-005 Nacos 配置拉取失败，业务服务还能启动吗？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：学习时常单独启动某个服务调试。
- 结论：当前 import 未加 `optional:`，配置缺失会快速失败——这是刻意的，便于发现配置错误。完整启动时 Compose 会等 `nacos-config-init` 成功后再启动业务服务。如需本地单独调试，可自行加 `optional:nacos:` 前缀，但要知道可能静默使用本地旧配置。

### Q-006 熔断（Circuit Breaker）和限流（Rate Limiter）有什么区别？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：两者都「拒绝请求」，容易混淆。
- 结论：熔断针对**下游依赖故障**（保护自己不被拖垮），限流针对**上游调用方过量**（保护自己不被打垮）。Sentinel 中分别由 DegradeRule 与 FlowRule 实现，见 [07-Sentinel](./07-Sentinel熔断限流.md)。

### Q-007 是否需要引入 Service Mesh（Istio）？
- 状态：已放弃（当前阶段）
- 提出日期：2026-11-23
- 背景：进阶话题。
- 结论：现阶段用 Spring Cloud + Nacos 把原理跑通即可；Service Mesh 是另一套学习曲线。按 AGENTS.md R9 不引入。

### Q-008 用户会在 GitHub 网页读代码，文档链接怎么处理？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：用户明确说会在 GitHub 网页阅读代码。
- 结论：所有文档必须使用相对路径链接（如 `[01](./01-...md)`），GitHub 会自动渲染成可点击链接。已在 AGENTS.md 第 6 节固化。

---

### Q-009 老教程里的 Eureka、Hystrix、Resilience4j 还能用吗？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：学习资料里经常出现这些组件，容易和新版技术栈混淆。
- 结论：**Eureka、Hystrix 已停止维护，不推荐在新项目中使用**（Spring Cloud 新版本也已移除集成）；**Resilience4j 库本身仍在维护，但本项目不采用**，因为主流中文生态更常用 Sentinel，且 Sentinel 带可视化控制台。老教程中的 Zuul 1.x、Spring Cloud Config（Git 后端）同样已被 Gateway / Nacos 取代。汇总见 [01-基础概念 §5.1](./01-微服务基础概念.md)。

---

### Q-010 `ip: ${SERVICE_IP:}` 是 Nacos 的内置变量吗？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：用户发现三个服务的 `application.yml` 里有 `ip: ${SERVICE_IP:}`，怀疑是框架固有变量。
- 结论：**不是内置变量**。`${SERVICE_IP:}` 只是 Spring 占位符语法（读环境变量 `SERVICE_IP`，默认空字符串），`SERVICE_IP` 这个名字是自定义的，且 Compose 从未设置它，因此永远解析为空。Nacos Client 在 `ip` 为空时会自动探测容器 IP（实测注册为 `172.18.0.7:8081`），功能正常，但配置本身是死代码且注释误导。已从三个服务中删除，并改为说明自动探测的注释。

---

### Q-011 Sentinel Dashboard 登录用户名密码是什么？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：访问 `http://localhost:8090` 出现登录页（原生 Sentinel 没有登录页）。
- 结论：`bladex/sentinel-dashboard:1.8.9` 镜像加了鉴权，默认账号密码为 **`sentinel` / `sentinel`**（从 jar 内 `application.properties` 的 `auth.username` / `auth.password` 查到，并已 curl 验证登录成功）。生产环境可用 `JAVA_OPTS=-Dauth.username=xxx -Dauth.password=xxx` 覆盖。

### Q-012 为什么需要前端实验台？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：用户反馈熔断限流与链路追踪概念难以从代码理解，希望有页面辅助。
- 结论：新增 `frontend/` 模块（React + Vite + TypeScript + pnpm + Tailwind CSS），通过 `/lab/status` 读真实熔断状态、`/admin/fault` 一键注入故障、Zipkin API 画瀑布图。见 [14-可观测性实验台](./14-可观测性实验台.md)。

---

### Q-013 网关已经配了 CORS，为什么后端服务还要各自配 LabCorsConfig？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：用户指出「一切请求都应从网关出去」，质疑后端各自配 CORS 的必要性。
- 结论：**用户的架构直觉是对的，原实现是缺陷**。当时前端 `/lab/status` 直连 8083、`/admin/fault` 直连 8082，因为网关只配了 `/api/**` 路由，导致必须给两个后端各打一个 CORS 补丁。已重构：网关新增 `/lab/**`、`/admin/**` 路由，前端全部改走 GATEWAY，删除两个 `LabCorsConfig`，CORS 由网关统一处理。Zipkin 作为独立监控系统保留直连（其自身返回 `access-control-allow-origin: *`）。

---

### Q-014 微服务是不是就是「按领域拆分 + Feign 通信 + Nacos 注册配置 + Gateway 入口 + 其余组件治理」？
- 状态：已解答
- 提出日期：2026-11-23
- 背景：用户在学完全部组件后做整体认知确认。
- 结论：**主干正确，需补三点**：
  ① 按领域拆分的本质是**数据自治**（Database per Service），否则只是分布式单体（见 ADR-0003）；
  ② Feign 只是**同步** HTTP 调用，真实系统还有异步消息（RocketMQ/Kafka）与最终一致；
  ③ Gateway 是**统一入口**而非出口，负责路由 + CORS/traceId/鉴权等横切逻辑 + 隐藏内部拓扑。
  本质：用运维复杂度换「独立演进」——业务代码只占一小块，Nacos/Sentinel/Zipkin/Docker 这一整套是为了让分布式系统可控。
  架构图见 [02-项目架构总览](./02-项目架构总览.md)。

---

### Q-015 项目从 workspace 移到子目录后，Docker Compose 要注意什么？
- 状态：已解答
- 提出日期：2026-11-24
- 背景：用户把项目从 `/home/anguish/workspace` 移到 `/home/anguish/workspace/microservices-learning`，会话也随之移动。
- 结论：
  ① **compose 项目名默认取目录名**，从 `workspace` 变成 `microservices-learning`，旧容器/网络/卷不会被复用；若不固定项目名，应接受「重新初始化」并清理旧资源。也可在 `docker-compose.yml` 顶部加 `name: xxx` 固定（本次用户选择接受新项目名）。
  ② **git 历史完整保留**——直接 `mv` 含 `.git` 的目录即可。
  ③ **会话用 `session.move` 工具跟随**到新目录（工具路径是 `tools.opencode.session.move`，不是 `session_move`）。
  ④ 移动后重新 `mvn package` + `pnpm build` + `docker compose up -d --build`，镜像名会变成 `<新目录名>-*`。

---

### Q-016 服务降级是不是就是前端直接显示「不可用」？那不是直接影响功能了吗？
- 状态：已解答
- 提出日期：2026-11-24
- 背景：用户看到 fallback 返回 503、前端 toast 显示失败，质疑降级是否等于功能没了。
- 结论：**降级 ≠ 一定显示不可用**，它有三层：
  ① **有损服务**（最优）：返回缩水但可用的结果（如推荐挂了返回热门商品），用户无感；
  ② **异步补偿**：先接请求后处理（如下单先记 PENDING，稍后补扣库存），用户觉得成功；
  ③ **友好失败**（本项目）：返回明确错误提示 + 保留购物车，体验可控。
  本项目用 ③ 是教学取舍——做 ①/② 需引入 RocketMQ 等进阶组件（AGENTS.md R9）。
  **降级的真正价值是故障隔离**：牺牲单个功能、快速释放线程，避免一个慢依赖拖垮整个商城（雪崩）。详见 [07-Sentinel](./07-Sentinel熔断限流.md)。

---

## 待办 / 开放问题

> 暂无。遇到新问题请按上方格式追加，不要修改已有条目的结论（只能追加补充说明）。

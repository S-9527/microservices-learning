# 06 · Nacos 配置中心

> 前置：[02-项目架构总览](./02-项目架构总览.md) · [ADR-0002](./decisions/0002-选择Nacos作为注册与配置中心.md)

---

## 1. 为什么把配置放进 Nacos

- 配置与代码分离，改配置不用重新打包；
- Nacos 注册中心与配置中心合二为一，少一个中间件；
- 带控制台，可在线编辑、发布、回滚（学习环境用文件初始化 + 控制台修改）。

---

## 2. 配置模型

| 概念 | 本项目取值 | 说明 |
|------|-----------|------|
| dataId | `user-service.yml` 等 | 配置的唯一标识，通常与 `spring.application.name` 对应 |
| Group | `DEFAULT_GROUP` | 默认分组 |
| Namespace | `public` | 默认命名空间 |

本项目共有 5 份配置，由 [`docker/nacos-init/configs/`](../docker/nacos-init/configs/) 在启动时推送：

| dataId | 内容 |
|--------|------|
| `shared-config.yml` | Feign 超时、Actuator、链路追踪、日志格式、MyBatis-Plus |
| `user-service.yml` | 用户服务的数据源（连 `user_db`） |
| `product-service.yml` | 商品服务的数据源（连 `product_db`） |
| `order-service.yml` | 订单数据源 + Sentinel Dashboard 地址 |
| `api-gateway.yml` | **网关路由规则** |

---

## 3. 客户端接入：`spring.config.import`

Boot 2.4 之后不再推荐 `bootstrap.yml`。当前写法：

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER:localhost:8848}
  config:
    import:
      - nacos:shared-config.yml
      - nacos:user-service.yml?refreshEnabled=true
```

要点：

1. `nacos:<dataId>` 是 Spring Cloud Alibaba 提供的 Config Data 协议；
2. `?refreshEnabled=true` 开启运行时刷新；
3. 多个 import 按顺序加载，**后面的覆盖前面的**——所以 shared 在前、服务专属在后；
4. 本地 `application.yml` 只保留端口、服务名、Nacos 地址这些「必须本地确定」的项。

---

## 4. 配置自动推送初始化

[`docker/nacos-init/init-configs.sh`](../docker/nacos-init/init-configs.sh)：

1. 等待 Nacos 健康检查通过；
2. 遍历 `configs/*.yml`，用 `curl --data-urlencode` 推送到 Nacos；
3. 业务服务通过 `depends_on: nacos-config-init: condition: service_completed_successfully` 等待配置就绪。

> 这是学习环境的便利做法。生产环境应通过 CI/CD 或运维平台管理配置，且配置变更需要审计。

---

## 5. 动态刷新

```bash
# 1. 在 Nacos 控制台修改 user-service.yml 的 app.greeting 并发布
# 2. 触发刷新（需暴露 refresh 端点，已在 shared-config.yml 中开启）
curl -X POST http://localhost:8081/actuator/refresh
# 3. 验证
curl -s http://localhost:8081/actuator/env | grep -A2 app.greeting
```

需要刷新的 Bean 标注 `@RefreshScope`。注意：数据源、连接池等基础设施配置**不适合**运行时刷新。

---

## 6. 常见坑

| 现象 | 原因 | 处理 |
|------|------|------|
| 服务启动报找不到 dataId | 初始化脚本未执行 / dataId 拼错 | 看 `nacos-config-init` 容器日志 |
| 改了 Nacos 配置不生效 | 没开 `refreshEnabled=true` 或没 refresh | 检查 import 参数 |
| 网关路由不生效 | 路由写在本地而非 Nacos | 路由在 `api-gateway.yml`，前缀 `spring.cloud.gateway.server.webflux.*` |
| 容器内连不上 Nacos | 用了 localhost | 用 `nacos:8848` |
| 数据源密码报错 | `MYSQL_PASSWORD` 未注入 | Compose 中已设置，本地运行需 export |

---

## 7. 延伸思考

- Namespace 与 Group 分别解决什么隔离问题？
- 配置变更如何做灰度与回滚？
- 为什么数据库连接池参数不建议运行时刷新？

---

## 相关文档

- [03-Nacos服务注册与发现](./03-Nacos服务注册与发现.md)
- [05-API网关-Gateway](./05-API网关-Gateway.md)
- [ADR-0002-选择Nacos](./decisions/0002-选择Nacos作为注册与配置中心.md)
- [12-常见问题与排错](./12-常见问题与排错.md)

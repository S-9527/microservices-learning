# ADR-0004 · 选择 Sentinel 作为容错组件

- 状态：已接受
- 日期：2026-11-23

---

## 1. 背景

微服务中依赖必然失败，需要流控、熔断、降级保护。用户要求「主流」选型。

---

## 2. 备选方案

| 方案 | 国内主流度 | 可视化控制台 | Boot 4 兼容 | 结论 |
|------|-----------|-------------|------------|------|
| **Sentinel 1.8.9** | 高 | 有 Dashboard | Alibaba 2025.1.0.0 支持 | ✅ 采用 |
| Hystrix | 已淘汰 | 有 | ❌ | 排除 |

---

## 3. 决策

- 使用 **Sentinel 1.8.9**（BOM 管理），Dashboard 镜像 `bladex/sentinel-dashboard:1.8.9`。
- 用 `@SentinelResource` 的 `blockHandler`（限流拒绝）与 `fallback`（下游故障）区分两类降级。
- 基础规则用 Java 代码注册（`SentinelRuleConfig`），保证不开控制台也受保护。
- Boot 4 无 `spring-boot-starter-aop`，显式引入 `org.aspectj:aspectjweaver`。

---

## 4. 后果

### 正面
- 有可视化控制台，可实时看 QPS、异常比例并改规则。
- 注解式接入简单，适合教学演示。

### 负面
- 多一个 Dashboard 容器，需配置通信端口（8719）。
- 规则默认存内存，重启失效；持久化到 Nacos datasource 留作进阶。

---

## 5. 相关文档

- [07-Sentinel熔断限流](../07-Sentinel熔断限流.md)

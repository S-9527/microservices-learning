# ADR-0002 · 选择 Nacos 作为注册与配置中心

- 状态：已接受
- 日期：2026-11-23

---

## 1. 背景

微服务需要 Service Registry 解决「实例地址动态变化」，也需要配置中心把配置与代码分离。
用户要求使用**当前主流**技术栈，并要求真实实现（非模拟）。

---

## 2. 备选方案

| 方案 | 维护状态 | 注册+配置 | 额外中间件 | 结论 |
|------|----------|-----------|-----------|------|
| **Nacos 3.x** | 活跃（Alibaba） | 二合一 | Nacos Server（Docker 一条命令） | ✅ 采用 |
| Consul | 活跃 | 注册为主 | Consul Agent | 生态偏运维，国内教材少 |
| Kubernetes Service | 平台内置 | 仅注册 | 需 K8s 集群 | 离本地学习太远 |
| ZooKeeper | 活跃 | 仅注册/协调 | 集群部署 | 过重 |

---

## 3. 决策

- 注册与配置统一用 **Nacos**。
- Server 版本 `v3.1.1`，与 Spring Cloud Alibaba 2025.1.0.0 内置的 nacos-client `3.1.1` 对齐。
- Alibaba BOM 2025.1.0.0 经官方 README 确认对应 Cloud 2025.1.x / Boot 4.0.x，无需降级。
- 学习环境显式 `NACOS_AUTH_ENABLE=false`，但保留 Base64 token 与 server identity 配置。
- 业务配置（含网关路由、数据源）放在 Nacos，由 `docker/nacos-init/` 自动初始化。

---

## 4. 后果

### 正面
- 与当前国内主流实践一致，资料丰富。
- 注册与配置二合一，少维护一个组件；控制台可在线查看实例与改配置。

### 负面
- 多一个 Docker 容器。
- Nacos 3.x 鉴权配置较复杂（token 需 Base64 且 ≥32 字节）。
- 控制台端口/API 在不同小版本有变化，需按锁定版本查证。

---

## 5. 相关文档

- [03-Nacos服务注册与发现](../03-Nacos服务注册与发现.md)
- [06-Nacos配置中心](../06-Nacos配置中心.md)

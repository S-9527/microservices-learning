# 文档索引

> 本目录是项目的「长期记忆」。每个主题一篇，按编号顺序学习。
> 规则见 [`../AGENTS.md`](../AGENTS.md)。问题记录见 [`questions.md`](./questions.md)。
>
> 技术栈：**Nacos**（注册+配置）、**Sentinel**（容错）、**Spring Cloud Gateway**、**OpenFeign**、
> **MySQL + MyBatis-Plus**（真实持久化）、**Micrometer + Zipkin**（链路追踪）。

---

## 学习主线（按顺序读）

| # | 文档 | 主题 | 前置 |
|---|------|------|------|
| 00 | [00-学习路线](./00-学习路线.md) | 全局路线图与验收标准 | — |
| 01 | [01-微服务基础概念](./01-微服务基础概念.md) | 单体 vs 微服务、服务边界、CAP、常见误区 | 00 |
| 02 | [02-项目架构总览](./02-项目架构总览.md) | 模块地图、请求链路、端口、版本 | 01 |
| 03 | [03-Nacos服务注册与发现](./03-Nacos服务注册与发现.md) | 注册/心跳/拉取/服务列表 | 02 |
| 04 | [04-服务间通信-OpenFeign](./04-服务间通信-OpenFeign.md) | 声明式 HTTP 客户端、负载均衡、错误处理 | 03 |
| 05 | [05-API网关-Gateway](./05-API网关-Gateway.md) | Route / Predicate / Filter、新配置前缀 | 03 |
| 05b | [05b-Gateway通俗指南](./05b-Gateway通俗指南.md) | 官方文档说人话版：快递分拣中心类比 + 项目实例 | 05 |
| 06 | [06-Nacos配置中心](./06-Nacos配置中心.md) | dataId、spring.config.import、动态刷新 | 02 |
| 07 | [07-Sentinel熔断限流](./07-Sentinel熔断限流.md) | 限流 / 熔断 / 降级 / Dashboard | 04 |
| 08 | [08-链路追踪-Micrometer-Zipkin](./08-链路追踪-Micrometer-Zipkin.md) | traceId、span、采样、Zipkin UI | 04 |
| 09 | [09-MySQL与MyBatis-Plus](./09-MySQL与MyBatis-Plus.md) | 三库隔离、条件 UPDATE 防超卖 | 02 |
| 09b | [09b-容器化与编排-Docker](./09b-容器化与编排-Docker.md) | Dockerfile、Compose、健康检查 | 02 |

## 工程化与实践

| # | 文档 | 主题 |
|---|------|------|
| 10 | [10-CI-CD-GitHub-Actions](./10-CI-CD-GitHub-Actions.md) | 自动化构建与测试 |
| 11 | [11-测试策略](./11-测试策略.md) | 单元 / 集成 / 契约测试的边界 |
| 12 | [12-常见问题与排错](./12-常见问题与排错.md) | 踩坑记录（持续追加） |
| 13 | [13-实验手册](./13-实验手册.md) | 可动手的验证步骤与故障注入 |
| 14 | [14-可观测性实验台](./14-可观测性实验台.md) | React 前端页面：熔断/限流/链路可视化 |

## 协作与元信息

| 文档 | 用途 |
|------|------|
| [questions.md](./questions.md) | 疑问台账：待解答 / 已解答 / 已放弃 |
| [decisions/](./decisions/) | ADR：架构决策记录 |
| [../AGENTS.md](../AGENTS.md) | 大模型协作规范（**AI 必读**） |
| [../README.md](../README.md) | 项目总览与快速开始 |

---

## 文档写作约定

1. 文件命名：`NN-主题-关键词.md`，NN 为两位数字，表示建议学习顺序。
2. 每篇文档结构：`为什么 → 核心概念 → 本项目实现 → 动手验证 → 常见坑 → 延伸思考/相关文档`。
3. 使用 Mermaid 画架构/流程图；使用表格做对比。
4. 中文讲解，技术术语保留英文。
5. **只追加不删除**（见 AGENTS.md R1）；只有用户明确要求抹除某技术栈时才删除相关文档。
6. 每篇末尾必须有「相关文档」相对链接区，保证 GitHub 上可点击跳转。

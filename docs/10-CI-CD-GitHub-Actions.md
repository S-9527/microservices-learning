# 10 · CI/CD（GitHub Actions）

> 解决「每次 push 自动验证」。前置：[09-Docker](./09-容器化与编排-Docker.md)

---

## 1. CI 与 CD 的区别

| | CI（Continuous Integration） | CD（Continuous Delivery/Deployment） |
|--|------------------------------|--------------------------------------|
| 触发 | push / PR | 合并到 main / 打 tag |
| 做什么 | 编译、测试、代码检查、构建镜像 | 部署到测试/生产环境 |
| 本项目 | ✅ 已实现 | ⬜ 暂不实现（学习项目无需真实部署） |

---

## 2. 工作流文件

[`.github/workflows/ci.yml`](../.github/workflows/ci.yml)：

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          distribution: zulu
          java-version: '25'
          cache: maven
      - name: Compile & Test
        run: mvn -B -q test
      - name: Build jars
        run: mvn -B -q -DskipTests package
```

要点：

1. **固定 JDK 版本与发行版**：`zulu` + `25`，与本地环境一致（环境里就是 Zulu 25）。
2. **`cache: maven`**：缓存 `~/.m2`，第二次开始快很多。
3. **先 test 后 package**：失败时留下清晰的错误阶段。
4. `-B` 批处理模式：CI 日志干净。

---

## 3. 本地复现 CI

CI 失败不要只盯着网页日志，本地复现更快：

```bash
mvn -B clean test
mvn -B -DskipTests package
```

> 注意 CI 环境是全新的，没有本地 `target/` 缓存和已下载的依赖，所以务必用 `clean`。

---

## 4. 状态检查（Status Checks）

在 GitHub 仓库 **Settings → Branches → Branch protection rules** 里可以配置：

- 要求 CI 通过才能合并 PR；
- 要求 1 个 approval。

本项目是公开学习仓库，可按需开启。**不要**在公开仓库里配置需要 secret 的部署步骤。

---

## 5. 常见坑

| 现象 | 原因 | 处理 |
|------|------|------|
| CI 报 JDK 版本不支持 | setup-java 版本与 pom 的 `java.version` 不一致 | 统一 25 |
| 本地能过 CI 挂 | 本地没 clean，残留旧 class | 本地也跑 `mvn clean test` |
| 依赖下载慢/失败 | Maven Central 网络波动 | 用 `cache: maven`；必要时重试 |
| CI 里 Docker build 失败 | 多阶段 Dockerfile 的相对路径不对 | 确认 context 是仓库根 |

---

## 6. 延伸思考

- 如何在 CI 里跑集成测试（需要 Nacos / MySQL）？（Testcontainers）
- 如何把镜像推到 GitHub Container Registry（ghcr.io）？需要什么权限？
- 为什么要固定 `actions/checkout@v4` 这样的版本？

---

## 相关文档

- [09-容器化与编排-Docker](./09-容器化与编排-Docker.md)
- [11-测试策略](./11-测试策略.md)
- [README](../README.md)

# 05b · Gateway 操作手册与特性清单

> 这份是**照着做**的手册 + **你还没用上的特性清单**，不是概念科普（概念见 [05](./05-API网关-Gateway.md)）。
> 基于 Spring Cloud Gateway **5.0.5** 官方文档（Cloud 2025.1.5）。
> 所有配置都放在 **Nacos 的 `api-gateway.yml`**，不是本地文件。

---

## 第一部分：日常操作（照着做）

### 操作 1：改一条已有路由的目标地址

**场景**：user-service 换端口了，或想临时把流量导到另一个实例。

**步骤**：

1. 打开 [`docker/nacos-init/configs/api-gateway.yml`](../docker/nacos-init/configs/api-gateway.yml)，找到：
   ```yaml
   - id: user-service
     uri: lb://user-service      # ← 改这里
   ```
2. 改 `uri`（`lb://服务名` 走注册中心，或 `http://ip:port` 写死）
3. 推送配置到 Nacos 并重启网关：
   ```bash
   docker compose run --rm --no-deps nacos-config-init
   docker compose restart api-gateway
   ```
4. 验证：
   ```bash
   curl -i http://localhost:8080/api/users/1
   ```

> ⚠️ 只改文件不推 Nacos 不生效——网关读的是 Nacos 里的配置。

---

### 操作 2：新增一条路由

**场景**：加了一个新服务 `payment-service`，想通过 `/api/payments/**` 暴露。

**步骤**：

1. 在 `api-gateway.yml` 的 `routes:` 列表末尾加：
   ```yaml
   - id: payment-service                 # 唯一名字
     uri: lb://payment-service          # 目标服务（需已注册到 Nacos）
     predicates:
       - Path=/api/payments/**          # 匹配路径
     filters:
       - StripPrefix=1                  # 去掉 /api 前缀
   ```
2. 推送 + 重启（同上）
3. 验证：
   ```bash
   curl -i http://localhost:8080/api/payments/1
   ```

**字段速查**：

| 字段 | 作用 | 本项目写法 |
|------|------|-----------|
| `id` | 路由名，唯一即可 | `user-service` |
| `uri` | 转发目标 | `lb://user-service` |
| `predicates` | 匹配条件（数组，多条是「且」） | `Path=/api/users/**` |
| `filters` | 转发前加工（数组） | `StripPrefix=1` |

---

### 操作 3：让下游收到不同的路径（StripPrefix / RewritePath）

**StripPrefix**（删掉路径前 N 段）：

```yaml
filters:
  - StripPrefix=1     # /api/users/1 → 下游收到 /users/1
```
官方例子：`/name/blue/red` + `StripPrefix=2` → `/red`。

**RewritePath**（正则替换，更灵活）：

```yaml
filters:
  # /api/users/1 → /users/1
  - RewritePath=/api/(?<segment>.*), /$\{segment}
```
> 注意 YAML 里 `$` 要写成 `$\`，这是官方特别说明的转义坑。

---

### 操作 4：加请求头 / 响应头

```yaml
filters:
  - AddRequestHeader=X-Request-Source, gateway
  - AddResponseHeader=X-Powered-By, spring-cloud-gateway
```
官方支持在值里用路径变量（如 `Path=/red/{segment}` 时 `Blue-{segment}` 会替换成实际值）。

---

### 操作 5：打开 actuator 看路由（调试用）

1. 在 `docker/nacos-init/configs/shared-config.yml` 的 `include` 里加 `gateway`：
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,info,env,configprops,refresh,metrics,gateway
   ```
2. 推送 + 重启网关
3. 验证：
   ```bash
   curl http://localhost:8080/actuator/gateway/routes | jq '.[].route_id'
   ```

---

### 操作 6：配 CORS（两种方式，选一个）

**方式 A：Java 配置（本项目当前用的）**

文件 [`GatewayCorsConfig.java`](../api-gateway/src/main/java/com/example/microservices/gateway/config/GatewayCorsConfig.java)：
```java
config.setAllowedOriginPatterns(List.of("http://localhost:*"));
config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
```

**方式 B：官方 yml 配置（更集中，想改可用这个）**

在 `api-gateway.yml` 加：
```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          globalcors:
            cors-configurations:
              '[/**]':
                allowedOrigins: "http://localhost:3000"
                allowedMethods: [GET, POST]
                allowedHeaders: "*"
```
> 改成方式 B 时记得删掉 `GatewayCorsConfig.java`，否则两套并存。

验证（浏览器跨域关键，curl 必须带 Origin 头才能测出来）：
```bash
curl -i -H 'Origin: http://localhost:3000' http://localhost:8080/api/users/1 | grep -i access-control
```

---

### 操作 7：写一个自己的 GlobalFilter（对所有路由生效）

参考 [`TraceIdGlobalFilter.java`](../api-gateway/src/main/java/com/example/microservices/gateway/filter/TraceIdGlobalFilter.java)：

```java
@Component
public class MyFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 转发前：改请求头
        ServerHttpRequest req = exchange.getRequest().mutate()
            .header("X-Foo", "bar").build();
        // 注意：响应头必须在这里设，chain.filter 之后响应已提交，不能再改
        exchange.getResponse().getHeaders().set("X-Bar", "foo");
        return chain.filter(exchange.mutate().request(req).build());
    }
    @Override public int getOrder() { return 0; }  // 越小越先执行
}
```
加 `@Component` 即可自动生效，**不用写进 yml**。

---

## 第二部分：特性清单 —— 你用了哪些，漏了哪些

官方共有 **14 个 Predicate**、**37 个 GatewayFilter**、**10+ 个内置 GlobalFilter**。
下表 ✅ = 本项目已用，⬜ = 未用（「不知道的特性」就在这里）。

### 2.1 Route Predicate Factories（匹配条件）

| Predicate | 作用 | 状态 |
|-----------|------|------|
| `Path` | 路径匹配 | ✅ `Path=/api/users/**` |
| `Method` | HTTP 方法匹配 | ⬜ |
| `Header` | 请求头匹配（支持正则） | ⬜ |
| `Query` | URL 参数匹配 | ⬜ |
| `Cookie` | Cookie 匹配 | ⬜ |
| `Host` | 主机名匹配 | ⬜ |
| `RemoteAddr` | 客户端 IP 匹配 | ⬜ |
| `After` / `Before` / `Between` | 按时间生效（可做定时开关） | ⬜ |
| `Weight` | 按权重分流（灰度发布） | ⬜ |
| `XForwardedRemoteAddr` | 根据 X-Forwarded-For 匹配 | ⬜ |
| `ReadBody` | 请求体内容匹配 | ⬜ |
| `Version` | 版本路由 | ⬜ |

> 多条 Predicate 同时写是「且」关系（全部满足才匹配）。

### 2.2 GatewayFilter Factories（路由级加工，共 37 个）

只列最常用的，完整 37 个见[官方页面](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/gatewayfilter-factories.html)。

| Filter | 作用 | 状态 |
|--------|------|------|
| `StripPrefix` | 删路径前 N 段 | ✅ |
| `AddRequestHeader` / `AddResponseHeader` | 加请求/响应头 | ⬜ |
| `SetRequestHeader` / `SetResponseHeader` | 覆盖头 | ⬜ |
| `RemoveRequestHeader` / `RemoveResponseHeader` | 删头 | ⬜ |
| `RewritePath` / `SetPath` | 改路径 | ⬜ |
| `RequestRateLimiter` | **网关层限流**（基于 Redis + KeyResolver，返回 429） | ⬜ 见下方说明 |
| `CircuitBreaker` | **网关层熔断**（下游挂时降级） | ⬜ 我们用 Sentinel 在 order-service 做 |
| `Retry` | 失败自动重试 | ⬜ |
| `RequestSize` | 限制请求体大小 | ⬜ |
| `ModifyRequestBody` / `ModifyResponseBody` | 改请求/响应体 | ⬜ |
| `SecureHeaders` | 加安全响应头 | ⬜ |
| `RedirectTo` | 重定向 | ⬜ |
| `SetStatus` | 设置响应状态码 | ⬜ |
| `PreserveHostHeader` | 保留原始 Host 头 | ⬜ |
| `DedupeResponseHeader` | 去重响应头 | ⬜ |
| `TokenRelay` | OAuth2 令牌透传 | ⬜ |
| 其余（JSON→gRPC、缓存、Session 等） | 见官方 | ⬜ |

### 2.3 内置 GlobalFilter（全局生效，共 10+）

| GlobalFilter | 作用 | 状态 |
|--------------|------|------|
| `NettyRoutingFilter` | 真正发 HTTP 请求到下游 | ✅ 内置自动生效 |
| `NettyWriteResponseFilter` | 把下游响应写回客户端 | ✅ 内置 |
| `ReactiveLoadBalancerClientFilter` | 解析 `lb://` 服务名 | ✅ 内置（`lb://` 靠它） |
| `RouteToRequestUrlFilter` | 把 Route 转成实际 URL | ✅ 内置 |
| `ForwardRoutingFilter` | `forward://` 本地转发 | ⬜ |
| `WebsocketRoutingFilter` | WebSocket 路由 | ⬜ |
| `GatewayMetricsFilter` | 路由指标（配 actuator 后可用） | ⬜ |
| `LocalCacheResponseFilter` | 本地响应缓存 | ⬜ |
| `TraceIdGlobalFilter` | 我们自己的 | ✅ 自定义 |

---

## 第三部分：重点「漏掉的特性」详解

### 3.1 网关层限流 `RequestRateLimiter`（和 Sentinel 限流的区别）

官方文档要点：
- 基于 `RateLimiter` 实现，超限返回 **HTTP 429 Too Many Requests**（状态码可配）
- 必须提供一个 `KeyResolver` Bean 决定「按什么维度限流」（如按用户、按 IP）
- **不能用快捷写法**，必须用完整 `name/args` 写法
- 默认 KeyResolver 是 `PrincipalNameKeyResolver`（需要 Spring Security）

配置示例（官方，需 Redis）：
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10    # 每秒补充 10 个令牌
      redis-rate-limiter.burstCapacity: 20    # 令牌桶容量 20
      key-resolver: "#{@userKeyResolver}"     # SpEL 引用 Bean
```

**和 Sentinel 的区别**：

| | Gateway RequestRateLimiter | Sentinel（本项目用的） |
|--|---------------------------|----------------------|
| 位置 | 网关层（所有流量入口） | 服务层（order-service 内部） |
| 需要 Redis | 是 | 否 |
| 维度 | KeyResolver 自定义 | 资源名 + 规则 |
| 本项目为何没用 | 不想引入 Redis（AGENTS.md R9） | 已有 Sentinel |

> 想做「网关层按 IP 限流」时再启用它。

### 3.2 网关层熔断 `CircuitBreaker` Filter

官方提供网关层熔断 Filter，可以和 Resilience4j 配合，下游挂时在网关就降级，请求根本不用进业务服务。
本项目选择在 order-service 用 Sentinel 做熔断（更贴近业务），所以没用网关层的。

### 3.3 按权重分流 `Weight`（灰度发布）

```yaml
predicates:
  - Weight=group1, 8    # 这组里 80% 流量走这条
```
配合另一条 `Weight=group1, 2` 可实现灰度。想做蓝绿/灰度时再用。

### 3.4 服务发现Locator（自动路由）

官方有 `DiscoveryClientRouteDefinitionLocator`：开启后**不用手写路由**，自动为每个注册服务生成 `/服务名/**` 的路由。
本项目配置里 `discovery.locator.enabled: false`（显式关闭），因为我们手写路由更可控、路径更语义化（`/api/users` 而不是 `/user-service`）。

### 3.5 Fluent Java Routes API（代码定义路由）

官方支持不写 yml，用 Java DSL 定义路由（`@Bean RouteLocator`）。适合路由需要动态计算的场景。本项目用 yml + Nacos，方便在线改，没用这个。

---

## 第四部分：验证命令速查

```bash
# 改完配置推送到 Nacos
docker compose run --rm --no-deps nacos-config-init
docker compose restart api-gateway

# 看路由（需先开 actuator 的 gateway 端点）
curl http://localhost:8080/actuator/gateway/routes | jq '.[].route_id'

# 测路由是否生效
curl -i http://localhost:8080/api/users/1

# 测 CORS（必须带 Origin 头，否则测不出浏览器问题）
curl -i -H 'Origin: http://localhost:3000' http://localhost:8080/api/users/1

# 看网关日志
docker compose logs -f api-gateway
```

---

## 相关文档

- [05-API网关-Gateway](./05-API网关-Gateway.md) —— 概念与版本陷阱
- [03-Nacos服务注册与发现](./03-Nacos服务注册与发现.md) —— `lb://` 背后的服务发现
- [07-Sentinel熔断限流](./07-Sentinel熔断限流.md) —— 本项目熔断方案
- [12-常见问题与排错](./12-常见问题与排错.md)
- [官方文档](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux.html)

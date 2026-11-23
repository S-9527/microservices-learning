# ADR-0003 · 选择 MySQL 与 MyBatis-Plus

- 状态：已接受
- 日期：2026-11-23

---

## 1. 背景

用户明确要求「真实，可以直接使用数据库而不是模拟」。因此不使用内存 Map，
每个服务接入真实关系型数据库并实践 Database per Service。

---

## 2. 备选方案

| 方案 | 真实度 | 国内主流度 | 学习成本 | 结论 |
|------|--------|-----------|----------|------|
| 内存 Map | 低 | — | 低 | ❌ 用户明确拒绝 |
| **MySQL 8 + MyBatis-Plus** | 高 | ★★★★★ | 中 | ✅ 采用 |
| MySQL 8 + JPA | 高 | ★★★ | 中 | 备选 |
| MySQL 8 + 原生 JDBC | 高 | ★★ | 高（样板代码多） | 不适合主线 |
| PostgreSQL + MyBatis-Plus | 高 | ★★★ | 中 | 备选 |

---

## 3. 决策

- 数据库：**MySQL 8.0**（`mysql:8.0` Docker 镜像）。
- 持久层：**MyBatis-Plus 3.5.18**，使用 Boot 4 专用 starter。
- 数据隔离：`user_db` / `product_db` / `order_db` 三个 schema（同一容器，学习简化）。
- 表结构由 `sql/init.sql` 在容器首次启动时初始化。
- 金额用 `DECIMAL(10,2)` ↔ `BigDecimal`。
- 库存扣减用条件 `UPDATE ... WHERE stock >= qty` 原子防超卖。

---

## 4. 后果

### 正面
- 数据真实可验证，可用 `mysql` 客户端直接查。
- 演示了 Database per Service、连接池、条件更新防超卖等工程点。

### 负面
- 本地启动需 MySQL（推荐直接用 Compose）。
- 跨服务无本地事务，订单与库存是最终一致 —— 已知教学取舍，分布式事务留作进阶。

---

## 5. 相关文档

- [09-MySQL与MyBatis-Plus](../09-MySQL与MyBatis-Plus.md)

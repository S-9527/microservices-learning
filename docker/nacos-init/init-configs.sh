#!/bin/sh
# 把 configs/ 目录下的 yml 推送到 Nacos，作为服务启动前的配置初始化。
# 仅供本地学习使用；生产环境应由运维流程管理配置。
set -e

NACOS=${NACOS_URL:-http://nacos:8848}
GROUP=DEFAULT_GROUP
DIR=/configs

echo "等待 Nacos 就绪: $NACOS"
attempt=0
# Nacos 3.x 的旧 readiness 端点已返回 410，改用控制台根路径判断就绪
until curl -fsS "$NACOS/nacos/" >/dev/null 2>&1; do
  attempt=$((attempt + 1))
  if [ "$attempt" -ge 60 ]; then
    echo "Nacos 在 60 次重试后仍未就绪，退出"
    exit 1
  fi
  sleep 2
done
echo "Nacos 已就绪"

for file in "$DIR"/*.yml; do
  # 保留 .yml 后缀：客户端 spring.config.import 使用的是 nacos:<dataId>
  name=$(basename "$file")
  echo "推送配置 dataId=$name"
  # Nacos 3.x 兼容 v1 管理接口；若失败则尝试 v3 接口
  if ! curl -fsS -X POST "$NACOS/nacos/v1/cs/configs" \
      --data-urlencode "dataId=$name" \
      --data-urlencode "group=$GROUP" \
      --data-urlencode "content@$file" >/dev/null; then
    curl -fsS -X POST "$NACOS/nacos/v3/admin/cs/config" \
      --data-urlencode "dataId=$name" \
      --data-urlencode "groupName=$GROUP" \
      --data-urlencode "content@$file" >/dev/null
  fi
done

echo "Nacos 配置初始化完成"

# 镜像只托管本地构建好的 dist（先在宿主执行 pnpm build，依赖已缓存，秒级完成）
FROM nginx:1.27-alpine
COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

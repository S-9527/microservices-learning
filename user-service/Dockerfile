# 镜像只负责打包：先在宿主执行 mvn -q -DskipTests package（依赖已缓存，秒级完成）
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY user-service/target/user-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

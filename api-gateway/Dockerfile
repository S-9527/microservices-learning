# 镜像只负责打包：先在宿主执行 mvn -q -DskipTests package
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

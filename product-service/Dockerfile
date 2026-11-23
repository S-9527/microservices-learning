# 镜像只负责打包：先在宿主执行 mvn -q -DskipTests package
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY product-service/target/product-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

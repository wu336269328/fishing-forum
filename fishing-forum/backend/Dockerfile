# 阶段1: Maven 构建
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -q

# 阶段2: JRE 运行（alpine 瘦身镜像）
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# JVM 调优：按容器 limits 自动算堆，单核用 Serial GC，限制 Metaspace/CodeCache
# 部署到更大机器时可通过环境变量 JAVA_TOOL_OPTIONS 覆盖
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:InitialRAMPercentage=30 \
    -XX:+UseSerialGC \
    -XX:MaxMetaspaceSize=128m \
    -XX:ReservedCodeCacheSize=64m \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

COPY --from=build /app/target/*.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh && mkdir -p /app/uploads/images/fish /app/uploads/avatars
EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]

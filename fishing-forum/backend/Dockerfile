# 阶段1: Maven 构建
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -q

# 阶段2: JDK 运行
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh && mkdir -p /app/uploads/images/fish /app/uploads/avatars
EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]

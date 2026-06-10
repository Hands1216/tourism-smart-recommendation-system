# 后端镜像：直接使用宿主机已构建好的 fat jar
# 构建上下文为 ./tourism-backend/tourism-main，需在容器外先执行：
#   cd tourism-ai && mvn clean install -DskipTests
#   cd ../tourism-algorithm && mvn clean install -DskipTests
#   cd ../tourism-backend && mvn clean package -DskipTests
FROM eclipse-temurin:17-jre

WORKDIR /app

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms512m -Xmx1024m"

# 上传目录（与 application-prod.yml 中 upload.path 对应）
RUN mkdir -p /app/uploads /app/logs

COPY target/tourism-main-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

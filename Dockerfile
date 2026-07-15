FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -q && \
    ls -lh target/

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=production \
    JAVA_OPTS="-XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+ParallelRefProcEnabled \
    -XX:+AlwaysPreTouch \
    -Xms384m \
    -Xmx512m \
    -XX:MetaspaceSize=64m \
    -XX:MaxMetaspaceSize=128m \
    -XX:CompressedClassSpaceSize=64m \
    -Djava.awt.headless=true \
    -Dspring.jpa.open-in-view=false \
    -Dspring.main.lazy-initialization=false"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

CMD ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]


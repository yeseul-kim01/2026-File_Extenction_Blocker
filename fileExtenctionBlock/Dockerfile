# =========================
# Stage 1: Build
# =========================
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew clean build -x test --no-daemon


# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

# ECS/CloudWatch 로그용
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

EXPOSE 8089

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

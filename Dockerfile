# ---------- Stage 1: build jar ----------
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /build

# cache dependency dulu supaya build berikutnya lebih cepat
COPY pom.xml .
RUN mvn -B -q dependency:go-offline -Pproduction || true

COPY src ./src
RUN mvn -B clean package -Pproduction -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

RUN mkdir -p /app/uploads
ENV UPLOAD_DIR=/app/uploads
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

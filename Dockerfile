FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/*.jar app.jar
ENV UPLOAD_DIR=/app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

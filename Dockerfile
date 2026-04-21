FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar
ENTRYPOINT ["java", "-jar", "build/libs/demo-0.0.1-SNAPSHOT.jar"]

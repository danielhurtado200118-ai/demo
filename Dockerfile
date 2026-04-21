FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar
ENTRYPOINT ["sh", "-c", "java -jar build/libs/*[0-9].jar"]

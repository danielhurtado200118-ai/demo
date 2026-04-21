FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build -x test
ENTRYPOINT ["sh", "-c", "java -jar build/libs/*.jar"]

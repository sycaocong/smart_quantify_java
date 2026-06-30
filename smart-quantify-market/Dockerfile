FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY settings.xml /root/.m2/settings.xml
COPY . .
RUN mvn clean package -DskipTests -pl smart-quantify-market -am -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/smart-quantify-market/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
FROM eclipse-temurin:26.0.2_10-jdk-ubi10-minimal AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:26.0.2_10-jre-ubi10-minimal
WORKDIR /app
COPY --from=build /workspace/target/CSDL-0.0.1-SNAPSHOT.jar /app/app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]

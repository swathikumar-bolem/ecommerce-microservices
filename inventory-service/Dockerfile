FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src src
RUN mvn -q -DskipTests package
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -r -u 10001 appuser
COPY --from=build /src/target/*.jar app.jar
USER appuser
ENTRYPOINT ["java","-jar","/app/app.jar"]

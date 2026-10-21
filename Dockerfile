FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd --system --create-home bank

COPY --from=build /app/target/*.jar app.jar

USER bank
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]
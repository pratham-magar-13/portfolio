# Production image: builds with Maven on JDK 21, runs on a slim JRE 21.
# Run:  docker build -t portfolio .
#       docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod \
#         -e DB_URL='jdbc:mysql://db:3306/portfolio?useSSL=true&serverTimezone=UTC' \
#         -e DB_USERNAME=portfolio -e DB_PASSWORD=secret portfolio

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

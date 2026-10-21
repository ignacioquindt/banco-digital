FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sh mvnw -B dependency:go-offline
COPY src src
RUN sh mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system banco && useradd --system --gid banco banco
COPY --from=build /build/target/banco-digital-0.0.1-SNAPSHOT.jar /app/app.jar
USER banco
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

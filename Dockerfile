#Build (test saltati siccome non vi è alcun test da eseguire e faila la build)
FROM maven:3.9.9-amazoncorretto-23 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

#JAR Execution
FROM openjdk:23-jdk
WORKDIR /app
COPY --from=builder /app/target/NearYou-0.0.1-SNAPSHOT.jar /app
CMD ["java", "-jar", "NearYou-0.0.1-SNAPSHOT.jar"]
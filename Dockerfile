#Spring Boot App
FROM openjdk:23-jdk
WORKDIR /app
COPY target/NearYou-0.0.1-SNAPSHOT.jar /app
CMD ["java", "-jar", "NearYou-0.0.1-SNAPSHOT.jar"]
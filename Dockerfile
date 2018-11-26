FROM openjdk:8-jdk-alpine

LABEL maintainer="ostrzejszy@gmail.com"

VOLUME /tmp

EXPOSE 8080

ARG JAR_FILE=/build/libs/spring-boot-demo-0.0.1-SNAPSHOT.jar

ADD ${JAR_FILE} spring-boot-demo.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/spring-boot-demo.jar"]
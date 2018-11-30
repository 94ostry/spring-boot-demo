FROM openjdk:8-jdk-alpine

LABEL maintainer="ostrzejszy@gmail.com"

VOLUME /tmp

EXPOSE 8080

ARG JAR_FILE=/build/libs/post-service-0.0.1-SNAPSHOT.jar

ADD ${JAR_FILE} post-service.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/post-service.jar"]
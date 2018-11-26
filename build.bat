echo "Build Gradle"
call gradlew clean build -x test

echo "Remove docker container"
docker rm -f spring-boot-demo

echo "Remove docker image"
docker rmi spring-boot-demo:1.0.0

echo "Build docker image"
docker build --no-cache -t spring-boot-demo:1.0.0 .

echo "Run docker container"
docker run -p 5000:8080 --name spring-boot-demo spring-boot-demo:1.0.0

PAUSE
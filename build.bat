echo "Build Gradle"
rem call gradlew clean build
call gradlew clean build -x test

echo "Build-Run Post-service"

docker rm -f post-service
docker rmi post-service:1.0.0
docker build --no-cache -t post-service:1.0.0 ./post-service
docker run -p 5000:8080 --name post-service post-service:1.0.0

PAUSE
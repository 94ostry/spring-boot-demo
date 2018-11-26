1. IntelliJ run

link : http://localhost:8080/h2-console

url : jdbc:h2:mem:testdb
user : sa
password :

2. Docker run

    docker build -t spring-boot-demo .
 
    docker run -p 5000:8080 spring-boot-demo

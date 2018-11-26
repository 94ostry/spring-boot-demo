1. IntelliJ run


    link:
    http://localhost:8080/h2-console

    properties:
    url : jdbc:h2:mem:testdb
    user : sa
    password :

2. Docker run

    docker build -t spring-boot-demo:1.0.0 .
 
    docker run -p 5000:8080 --name spring-boot-demo spring-boot-demo:1.0.0


    link:
    http://localhost:5000/h2-console
    
3. one command


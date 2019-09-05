FROM openjdk:8-alpine

COPY target/uberjar/swagger-service.jar /swagger-service/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/swagger-service/app.jar"]

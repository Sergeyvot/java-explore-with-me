FROM amazoncorretto:11
COPY target/*.jar stats-server.jar
ENTRYPOINT ["./wait-for-it.sh", "stats-db:5432", "--", "java", "-jar", "stats-server.jar"]
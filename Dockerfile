FROM maven:3.8.2-jdk-8

# copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY target/user-center-backend-0.0.1.jar  ./target/user-center-backend-0.0.1.jar
# Builder a release artifact.
#RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/target/user-center-backend-0.0.1.jar","--spring.profiles.active=prod"]

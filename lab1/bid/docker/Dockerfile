FROM eclipse-temurin:17-jdk-alpine as builder
# Set environment variables.
ENV HOME /root
WORKDIR /app

COPY ../mvnw .
COPY ../.mvn .mvn
COPY ../pom.xml .
COPY ../src src

# build the project
RUN chmod +x ./mvnw
RUN ./mvnw install -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

COPY --from=builder /app/target/bid-service-*.jar bid-service.jar

ENTRYPOINT exec java $JAVA_OPTS -jar ./bid-service.jar
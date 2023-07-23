# Step 1: Choose a base image with Java (OpenJDK) and Scala SBT
FROM hseeberger/scala-sbt:8u302_1.5.5_2.13.6 AS builder

# Step 2: Set the working directory in the container
WORKDIR /app

# Step 3: Copy the project files into the container
COPY . .

# Step 4: Build the Scala API (sbt assembly will create a fat JAR)
RUN sbt assembly

# Step 5: Create a new lightweight image to run the application
FROM openjdk:8-jre-slim

# Step 6: Set the working directory in the final image
WORKDIR /app

# Step 7: Copy the fat JAR from the builder image into the final image
COPY --from=builder /app/target/scala-2.13/star-wars-proxy-api.jar ./star-wars-proxy-api.jar

# Step 8: Set the command to run the application
CMD ["java", "-jar", "star-wars-proxy-api.jar"]

# Step 9: Expose the port your application is listening on
EXPOSE 8080

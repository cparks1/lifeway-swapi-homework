# Lifeway Homework - SWAPI Proxy

This project implements a simple Star Wars API that allows users to search for character information. It is built using Akka HTTP, Akka Actors, and Akka Streams.

## Dependencies

To run this project, you need the following dependencies:

- Scala 2.13.8

## Installation

1. Clone the repository:
`git clone <repository_url>`


2. Ensure you have Scala and SBT (Scala Build Tool) installed on your system.

3. Navigate to the project directory and run the following command:
`sbt`

4. Within the sbt terminal, run the following command: `compile`

## Running the API proxy server
To start the server on port 8080, run the following command within the sbt terminal: `run`


The server will be accessible at `http://localhost:8080`.

## Running the API proxy server via Docker
1. Pull the Docker image: Run `docker pull ghcr.io/cparks1/star-wars-proxy-api:<latest>`, where `latest` is the latest version of that package.
2. Run the Docker container: Run `docker run -p 8080:8080 cparks1/star-wars-proxy-api:<latest>`, where `latest` is the latest version of that package.
3. The Docker container should now be running and you can now access the API at [http://localhost:8080](http://localhost:8080). If you want to stop running the Docker container, press `Ctrl + C` in the terminal where the container is running.

### Building a Docker image of the proxy API
1. From the root of the project directory, run the command `sbt assembly`
2. In the same path, run `docker build -t star-wars-proxy-api .`

### Pushing the new Docker image to GitHub
1. Find the image ID of the newly built Docker image via `docker image ls`.
2. Run `docker tag <image_id> ghcr.io/<github_username>/star-wars-proxy-api:1.0.1`, where 1.0.1 is the next version following semantic versioning specification.
3. Login to the GitHub container registry by running `docker login ghcr.io -u <github_username>`. The password it expects is your personal access token with Package write permissions.
4. Run `docker push ghcr.io/<github_username>/<image_name>:<tag>`.

## API Endpoints

### Get Character Info

- Endpoint: `GET /characters/search`
- Query Parameter: `name` (required) - The name of the character to search for.

This endpoint retrieves information about a character by their name. If the character is found, it returns detailed information such as name, height, mass, hair color, birth year, species information, films appeared in, and starships flown in.

If the character is not found, the endpoint returns a 404 Not Found response.

## Testing

The project includes unit tests to verify the functionality of the Star Wars API.

To run the tests, use the following command within the sbt terminal:
`test`

## Running the front end via Docker
1. Pull the Docker image: Run `docker pull ghcr.io/cparks1/star-wars-proxy-front-end:<latest>`, where `latest` is the latest version of that package.
2. Run the Docker container: Run `docker run -p 3000:3000 ghcr.io/cparks1/star-wars-proxy-front-end:<latest>`, where `latest` is the latest version of that package.
3. The Docker container should now be running and you can now access the front end via [http://localhost:3000](http://localhost:3000). If you want to stop running the Docker container, press `Ctrl + C` in the terminal where the container is running.

### Using the front end
1. Type a Star Wars character name into the search bar.
2. Press the `ENTER` key on your keyboard or press `Search`.

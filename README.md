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
1. Pull the Docker image: Run `docker pull cparks1/star-wars-proxy-api:latest`
2. Run the Docker container: Run `docker run -p 8080:8080 cparks1/star-wars-proxy-api:latest`
3. The Docker container should now be running and you can now access the API. If you want to stop running the Docker container, press `Ctrl + C` in the terminal where the container is running.

### Building a Docker image of the proxy API
1. From the root of the project directory, run the command `sbt assembly`
2. In the same path, run `docker build -t star-wars-proxy-api .`

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

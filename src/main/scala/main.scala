import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher

import StarWarsAPI._

object Main extends App {
  // Create the required implicits for ActorSystem, ActorMaterializer, and ExecutionContext
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  // Define CORS settings with multiple allowed origins
  val corsSettings: CorsSettings = CorsSettings.defaultSettings
    .withAllowedOrigins(HttpOriginMatcher("http://localhost:3000", "http://localhost:80"))

  // Add CORS directives to your route
  val corsRoute: Route = cors(corsSettings) {
    route // Your existing route from StarWarsAPI.route
  }

  // Start the server on port 8080
  val binding = Http().newServerAt("0.0.0.0", 8080).bind(corsRoute)

  binding.onComplete {
    case Success(value) =>
      println("Server is listening on http://localhost:8080")
    case Failure(exception) =>
      println(s"Failure: $exception")
      system.terminate()
  }
}

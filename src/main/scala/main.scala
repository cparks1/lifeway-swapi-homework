import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure}

import StarWarsAPI._

object Main extends App {
  // Create the required implicits for ActorSystem, ActorMaterializer, and ExecutionContext
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  // Start the server on port 8080
  val binding = Http().newServerAt("0.0.0.0", 8080).bind(route)

  binding.onComplete {
    case Success(value) =>
      println("Server is listening on http://localhost:8080")
    case Failure(exception) =>
      println(s"Failure: $exception")
      system.terminate()
  }
}

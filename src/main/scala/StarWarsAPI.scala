import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import spray.json._

object StarWarsAPI extends DefaultJsonProtocol {
  implicit val swapiCharacterSearchResultFormat: RootJsonFormat[SWAPICharacterSearchResult] = jsonFormat16(SWAPICharacterSearchResult)
  implicit val swapiFilmInfoFormat: RootJsonFormat[SWAPIFilmInfo] = jsonFormat14(SWAPIFilmInfo)
  implicit val swapiShipInfoFormat: RootJsonFormat[SWAPIShipInfo] = jsonFormat18(SWAPIShipInfo)
  implicit val SWAPISpeciesInfoFormat: RootJsonFormat[SWAPISpeciesInfo] = jsonFormat15(SWAPISpeciesInfo)
  implicit val proxyAPISpeciesInfoFormat: RootJsonFormat[ProxyAPISpeciesInfo] = jsonFormat4(ProxyAPISpeciesInfo.apply)
  implicit val proxyAPIResultFormat: RootJsonFormat[ProxyAPIResult] = jsonFormat8(ProxyAPIResult.apply)

  def route(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Route = path("characters" / "search") {
    parameters("name") { name =>
      // Call the makeSearchRequest function to get the character info
      val characterSearchResultFuture = makeCharacterSearchRequest(name)

      // Handle the result
      onComplete(characterSearchResultFuture) {
        case Success(characterSearchResult) if characterSearchResult.name.nonEmpty =>
          // The character exists, retrieve the film info, starship info, and species info
          val filmInfoFuture = makeFilmAPIRequests(characterSearchResult)
          val starshipInfoFuture = makeShipAPIRequests(characterSearchResult)
          val speciesInfoFuture = makeSpeciesAPIRequests(characterSearchResult)
          // Combine the futures using for-comprehensions to get the required data
          val resultFuture = for {
            filmInfo <- filmInfoFuture
            starshipInfo <- starshipInfoFuture
            speciesInfo <- speciesInfoFuture
          } yield ProxyAPIResult.fromSWAPIData(characterSearchResult, speciesInfo, starshipInfo, filmInfo)
          // Complete with the ProxyAPIResult when all data is available
          onSuccess(resultFuture) { proxyAPIResult =>
            complete(proxyAPIResult)
          }
        case Success(characterSearchResult) =>
          // The character doesn't exist, return 404 with "Character not found"
          complete(StatusCodes.NotFound, "Character not found")
        case Failure(_) =>
          // An error occurred, return 500 Internal Server Error
          complete(StatusCodes.InternalServerError, "Internal server error")
      }
    }
  }

  def makeFilmAPIRequests(searchResult: SWAPICharacterSearchResult)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Seq[SWAPIFilmInfo]] = {
    val filmURIs = searchResult.films

    // Use Future.sequence to process the list of film URIs asynchronously
    val filmInfoFutures: List[Future[SWAPIFilmInfo]] = filmURIs.map { filmUri =>
      val request = HttpRequest(uri = filmUri)
      for {
        response <- Http().singleRequest(request)
        entity <- Unmarshal(response.entity).to[SWAPIFilmInfo]
      } yield entity
    }

    // Convert the List[Future[SWAPIFilmInfo]] to Future[List[SWAPIFilmInfo]]
    // and then map it to Seq[SWAPIFilmInfo]
    Future.sequence(filmInfoFutures).map(_.toSeq)
  }

  def makeShipAPIRequests(searchResult: SWAPICharacterSearchResult)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Seq[SWAPIShipInfo]] = {
    val shipURIs = searchResult.starships

    // Use Future.sequence to process the list of ship URIs asynchronously
    val shipInfoFutures: List[Future[SWAPIShipInfo]] = shipURIs.map { shipUri =>
      val request = HttpRequest(uri = shipUri)
      for {
        response <- Http().singleRequest(request)
        entity <- Unmarshal(response.entity).to[SWAPIShipInfo]
      } yield entity
    }

    Future.sequence(shipInfoFutures).map(_.toSeq)
  }

  def makeSpeciesAPIRequests(searchResult: SWAPICharacterSearchResult)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Seq[SWAPISpeciesInfo]] = {
    val speciesURIs = if (searchResult.species.nonEmpty) searchResult.species else List("https://swapi.dev/api/species/1/")

    // Use Future.sequence to process the list of species URIs asynchronously
    val speciesInfoFutures: List[Future[SWAPISpeciesInfo]] = speciesURIs.map { speciesUri =>
      val request = HttpRequest(uri = speciesUri)
      for {
        response <- Http().singleRequest(request)
        entity <- Unmarshal(response.entity).to[SWAPISpeciesInfo]
      } yield entity
    }

    Future.sequence(speciesInfoFutures).map(_.toSeq)
  }

  def makeCharacterSearchRequest(name: String)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[SWAPICharacterSearchResult] = {
    val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
    val url = s"https://swapi.dev/api/people/?search=$encodedName"

    val request: HttpRequest = HttpRequest(uri = url)
    for {
      response <- Http().singleRequest(request)
      characterInfo <- unmarshalCharacterSearchEntity(response)
    } yield characterInfo
  }

  // Update the unmarshalEntity function to return SWAPICharacterSearchResult directly
  def unmarshalCharacterSearchEntity(response: HttpResponse)(implicit ec: ExecutionContext, materializer: ActorMaterializer): Future[SWAPICharacterSearchResult] = {
    // Extract the JSON entity from the HttpResponse
    val jsonBytes: Future[ByteString] = response.entity.withoutSizeLimit().dataBytes.runReduce(_ ++ _)

    // Convert the ByteString to a JSON string
    val jsonString: Future[String] = jsonBytes.map(_.utf8String)

    // Parse the JSON string into a Spray JSON AST (Abstract Syntax Tree)
    val jsonAst: Future[JsValue] = jsonString.map(_.parseJson)

    // Check if the 'results' field exists and contains at least one element
    val characterInfo: Future[SWAPICharacterSearchResult] = jsonAst.flatMap { jsValue =>
      // Convert the first result to a SWAPICharacterSearchResult object
      jsValue match {
        case JsObject(fields) if fields.contains("results") =>
          val results = fields("results")
          results match {
            case JsArray(Vector(firstResult)) =>
              Future.successful(firstResult.convertTo[SWAPICharacterSearchResult])
            case JsArray(Vector()) =>
              // If 'results' is an empty array, return a default empty SWAPICharacterSearchResult
              Future.successful(SWAPICharacterSearchResult("", "", "", "", "", "", "", "", "", Nil, Nil, Nil, Nil, "", "", ""))
            case _ =>
              // Handle unexpected 'results' format or multiple results here
              Future.failed(new RuntimeException("Unexpected 'results' format or multiple results"))
          }
        case _ =>
          // Handle unexpected JSON format here
          Future.failed(new RuntimeException("Unexpected JSON format"))
      }
    }

    characterInfo // Return the Future[SWAPICharacterSearchResult] without the closing curly brace
  }
}

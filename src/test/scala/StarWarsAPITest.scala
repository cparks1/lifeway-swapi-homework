import StarWarsAPI.route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.stream.ActorMaterializer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}


import StarWarsAPI.proxyAPIResultFormat


class StarWarsAPITest extends AnyWordSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  implicit val routeTestTimeout = RouteTestTimeout(10.seconds)

  implicit override val materializer: ActorMaterializer = ActorMaterializer()

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(10, Seconds), interval = Span(5, Millis))

  val validSWAPICharacterSearchResult = SWAPICharacterSearchResult(
    name = "Luke Skywalker",
    height = "172",
    mass = "77",
    hair_color = "blond",
    skin_color = "fair",
    eye_color = "blue",
    birth_year = "19BBY",
    gender = "male",
    homeworld = "https://swapi.dev/api/planets/1/",
    films = List(
      "https://swapi.dev/api/films/1/",
      "https://swapi.dev/api/films/2/",
      "https://swapi.dev/api/films/3/",
      "https://swapi.dev/api/films/6/"
    ),
    species = List.empty,
    vehicles = List(
      "https://swapi.dev/api/vehicles/14/",
      "https://swapi.dev/api/vehicles/30/"
    ),
    starships = List(
      "https://swapi.dev/api/starships/12/",
      "https://swapi.dev/api/starships/22/"
    ),
    created = "2014-12-09T13:50:51.644000Z",
    edited = "2014-12-20T21:17:56.891000Z",
    url = "https://swapi.dev/api/people/1/"
  )

  val validChewbaccaSWAPICharacterSearchResult = SWAPICharacterSearchResult(
    name = "Chewbacca",
    height = "228",
    mass = "112",
    hair_color = "brown",
    skin_color = "unknown",
    eye_color = "blue",
    birth_year = "200BBY",
    gender = "male",
    homeworld = "https://swapi.dev/api/planets/14/",
    films = List(
      "https://swapi.dev/api/films/1/",
      "https://swapi.dev/api/films/2/",
      "https://swapi.dev/api/films/3/",
      "https://swapi.dev/api/films/6/"
    ),
    species = List("https://swapi.dev/api/species/3/"),
    vehicles = List("https://swapi.dev/api/vehicles/19/"),
    starships = List(
      "https://swapi.dev/api/starships/10/",
      "https://swapi.dev/api/starships/22/"
    ),
    created = "2014-12-10T16:42:45.066000Z",
    edited = "2014-12-20T21:17:50.332000Z",
    url = "https://swapi.dev/api/people/13/"
  )

  val emptySWAPICharacterSearchResult = SWAPICharacterSearchResult(
    name = "",
    height = "",
    mass = "",
    hair_color = "",
    skin_color = "",
    eye_color = "",
    birth_year = "",
    gender = "",
    homeworld = "",
    films = List.empty,
    species = List.empty,
    vehicles = List.empty,
    starships = List.empty,
    created = "",
    edited = "",
    url = ""
  )

  val expectedHappyPathProxyAPIResult = ProxyAPIResult(
    name = "Luke Skywalker",
    height = "172",
    mass = "77",
    hair_color = "blond",
    birth_year = "19BBY",
    species_info = List(ProxyAPISpeciesInfo(
      name = "Human",
      classification = "mammal",
      designation = "sentient",
      language = "Galactic Basic"
    )),
    starships_flown_in = List(
      "X-wing",
      "Imperial shuttle"
    ),
    films_appeared_in = List(
      "A New Hope",
      "The Empire Strikes Back",
      "Return of the Jedi",
      "Revenge of the Sith"
    )
  )



  // Test cases
  "StarWarsAPI" should {
    "return character info when character exists" in {
      Get("/characters/search?name=Luke") ~> route ~> check {
        status shouldBe StatusCodes.OK

        val characterInfo: ProxyAPIResult = responseAs[ProxyAPIResult]
        characterInfo shouldBe expectedHappyPathProxyAPIResult
      }
    }

    "return 404 Not Found when character does not exist" in {
      Get("/characters/search?name=NonExistentCharacter") ~> route ~> check {
        status shouldBe StatusCodes.NotFound
        responseAs[String] shouldBe "Character not found"
      }
    }

    "return SWAPICharacterSearchResult when character exists" in {
      // Call the makeSearchRequest function with the character name
      val searchResultFuture = StarWarsAPI.makeCharacterSearchRequest("Luke")

      whenReady(searchResultFuture) { characterSearchResult =>
        characterSearchResult.name should not be empty
        characterSearchResult.height should not be empty
        characterSearchResult.mass should not be empty
        characterSearchResult.hair_color should not be empty
        characterSearchResult.birth_year should not be empty
        characterSearchResult.films should not be empty
        characterSearchResult.starships should not be empty
        characterSearchResult.species.isEmpty shouldBe true // SWAPI calls for Luke Skywalker give an empty species list.
      }
    }

    "return an empty SWAPICharacterSearchResult when character doesn't exist" in {
      val searchResultFuture = StarWarsAPI.makeCharacterSearchRequest("idontexist")

      whenReady(searchResultFuture) { characterSearchResult =>
        characterSearchResult shouldBe emptySWAPICharacterSearchResult
      }
    }

    "return SWAPIFilmInfo list when given a valid SWAPICharacterSearchResult" in {
      val expectedFilmTitles = List( // Verifying just film titles since the full film data for 4 films would be a lot
        "A New Hope",
        "The Empire Strikes Back",
        "Return of the Jedi",
        "Revenge of the Sith"
      )
      val filmResultFuture = StarWarsAPI.makeFilmAPIRequests(validSWAPICharacterSearchResult)
      whenReady(filmResultFuture) { filmResults =>
        filmResults should not be empty

        // Extract the "title" field from each SWAPIFilmInfo object
        val titles: Seq[String] = filmResults.map(_.title)

        // Check if all the titles in the list match the expected titles
        val allTitlesMatch: Boolean = titles.forall(expectedFilmTitles.contains)

        // Assert that all titles match the expected values
        allTitlesMatch should be(true)
      }
    }

    "return SWAPIShipInfo list when given a valid SWAPICharacterSearchResult" in {
      val expectedStarshipNames = List( // Verifying just ship names since the full ship data would be a lot
        "X-wing",
        "Imperial shuttle"
      )
      val shipResultFuture = StarWarsAPI.makeShipAPIRequests(validSWAPICharacterSearchResult)
      whenReady(shipResultFuture) { shipResults =>
        shipResults should not be empty

        // Extract the "name" field from each SWAPIShipInfo object
        val names: Seq[String] = shipResults.map(_.name)

        // Check if all the names in the list match the expected names
        val allNamesMatch: Boolean = names.forall(expectedStarshipNames.contains)

        // Assert that all titles match the expected values
        allNamesMatch should be(true)
      }
    }

    "return SWAPISpeciesInfo list containing Human when given a SWAPICharacterSearchResult corresponding to a human" in {
      val speciesResultFuture = StarWarsAPI.makeSpeciesAPIRequests(validSWAPICharacterSearchResult)
      whenReady(speciesResultFuture) { speciesResults =>
        speciesResults should have size 1 // Ensure only one species is returned

        // Extract the "name" field from the single SWAPISpeciesInfo object
        val name: String = speciesResults.head.name

        // Check if the species name is "Human"
        name should be("Human")
      }
    }

    "return SWAPISpeciesInfo list containing Wookie when given a SWAPICharacterSearchResult corresponding to Wookie" in {
      val speciesResultFuture = StarWarsAPI.makeSpeciesAPIRequests(validChewbaccaSWAPICharacterSearchResult)
      whenReady(speciesResultFuture) { speciesResults =>
        speciesResults should have size 1 // Ensure only one species is returned

        // Extract the "name" field from the single SWAPISpeciesInfo object
        val name: String = speciesResults.head.name

        // Check if the species name is "Wookie"
        name should be("Wookie")
      }
    }
  }
}

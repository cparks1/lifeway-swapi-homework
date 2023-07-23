case class ProxyAPIResult(
                         name: String,
                         height: String,
                         mass: String,
                         hair_color: String,
                         birth_year: String,
                         species_info: Seq[ProxyAPISpeciesInfo],
                         starships_flown_in: Seq[String],
                         films_appeared_in: Seq[String]
                         )

object ProxyAPIResult {
  def fromSWAPIData(characterSearchResult: SWAPICharacterSearchResult, speciesInfo: Seq[SWAPISpeciesInfo], starshipInfo: Seq[SWAPIShipInfo], filmInfo: Seq[SWAPIFilmInfo]) = {
    ProxyAPIResult(
      name = characterSearchResult.name,
      height = characterSearchResult.height,
      mass = characterSearchResult.mass,
      hair_color = characterSearchResult.hair_color,
      birth_year = characterSearchResult.birth_year,
      species_info = speciesInfo.map(ProxyAPISpeciesInfo.fromSWAPISpeciesInfo),
      starships_flown_in = starshipInfo.map(_.name),
      films_appeared_in = filmInfo.map(_.title)
    )
  }
}

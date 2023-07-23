case class ProxyAPISpeciesInfo(
                                name: String,
                                classification: String,
                                designation: String,
                                language: String
                              )

object ProxyAPISpeciesInfo {
  def fromSWAPISpeciesInfo(swapiSpeciesInfo: SWAPISpeciesInfo): ProxyAPISpeciesInfo = {
    ProxyAPISpeciesInfo(
      name = swapiSpeciesInfo.name,
      classification = swapiSpeciesInfo.classification,
      designation = swapiSpeciesInfo.designation,
      language = swapiSpeciesInfo.language
    )
  }
}

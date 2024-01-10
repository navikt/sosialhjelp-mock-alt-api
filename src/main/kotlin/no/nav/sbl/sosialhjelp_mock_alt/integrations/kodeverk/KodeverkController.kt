package no.nav.sbl.sosialhjelp_mock_alt.integrations.kodeverk

import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.KommuneInfoService
import no.nav.sbl.sosialhjelp_mock_alt.integrations.kodeverk.dto.KodeverkDto
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class KodeverkController(
    kommuneInfoService: KommuneInfoService,
) {
  private val log by logger()

  private val kommuner: KodeverkDto = lesKodeverk("kommuner")
  private val landkoder: KodeverkDto = lesKodeverk("landkoder")
  private val postnummer: KodeverkDto = lesKodeverk("postnummer")

  init {
    kommuner.betydninger.keys.forEach { kommuneInfoService.addKommunieInfo(it) }
    kommuneInfoService.addSvarutKommuneInfo("3801")
  }

  private fun lesKodeverk(navn: String): KodeverkDto {
    val string: String =
        this::class.java.classLoader.getResource("kodeverk/kodeverk_$navn.json")!!.readText()
    return objectMapper.readValue(string, KodeverkDto::class.java)
  }

  @GetMapping("/kodeverk/{kodeverknavn}/koder/betydninger")
  fun hentKodeverk(@PathVariable kodeverknavn: String): ResponseEntity<KodeverkDto> {
    log.debug("Kodeverk request: $kodeverknavn")
    if (kodeverknavn == "Kommuner") {
      return ResponseEntity.ok(kommuner)
    }
    if (kodeverknavn == "Landkoder") {
      return ResponseEntity.ok(landkoder)
    }
    if (kodeverknavn == "Postnummer") {
      return ResponseEntity.ok(postnummer)
    }
    return ResponseEntity.notFound().build()
  }
}

package no.nav.sbl.sosialhjelp_mock_alt.integrations.kodeverk

import no.nav.sbl.sosialhjelp_mock_alt.integrations.kodeverk.dto.KodeverkDto
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class KodeverkController {
    final val kommuner: KodeverkDto
    final val landkoder: KodeverkDto
    final val postnummer: KodeverkDto

    init {
        kommuner = lesKodeverk("kommuner")
        landkoder = lesKodeverk("landkoder")
        postnummer = lesKodeverk("postnummer")
    }

    private fun lesKodeverk(navn: String): KodeverkDto {
        val string: String = this::class.java.classLoader.getResource("kodeverk/kodeverk_${navn}.json")!!.readText()
        return objectMapper.readValue(string, KodeverkDto::class.java)
    }

    companion object {
        private val log by logger()
    }

    @GetMapping("/kodeverk/api/v1/kodeverk/{kodeverknavn}/koder/betydninger")
    fun hentKodeverk(@PathVariable kodeverknavn: String): ResponseEntity<KodeverkDto> {
        log.debug("Kodeverk request: ${kodeverknavn}")
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

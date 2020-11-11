package no.nav.sbl.sosialhjelp_mock_alt.integrations.kodeverk

import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KodeverkController {
    final val kommuner: String
    final val landkoder: String
    final val postnummer: String

    init {
        kommuner = lesKodeverk("Kommuner")
        landkoder = lesKodeverk("Landkoder")
        postnummer = lesKodeverk("Postnummer")
    }

    private fun lesKodeverk(navn: String): String {
        val json: String? = this::class.java.classLoader.getResource("kodeverk/${navn}.xml")!!.readText()
        return json!!
    }

    companion object {
        val log by logger()
    }

    @GetMapping("/kodeverk")
    fun loggUkjentRequest(@RequestParam kodeverknavn: String): ResponseEntity<String> {
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

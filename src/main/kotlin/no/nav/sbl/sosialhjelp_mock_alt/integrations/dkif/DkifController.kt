package no.nav.sbl.sosialhjelp_mock_alt.integrations.dkif

import no.nav.sbl.sosialhjelp_mock_alt.integrations.dkif.model.DigitalKontaktinfo
import no.nav.sbl.sosialhjelp_mock_alt.integrations.dkif.model.DigitalKontaktinfoBolk
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DkifController {
    companion object {
        val log by logger()
    }
    private val kontaktinfoer = mutableMapOf<String, DigitalKontaktinfoBolk>()

    @RequestMapping("/dkif/v1/personer/kontaktinformasjon")
    fun getKontaktinfo(@RequestHeader headers: HttpHeaders): ResponseEntity<DigitalKontaktinfoBolk> {
        val fnr = hentFnrFraToken(headers)
        var kontaktinfo = kontaktinfoer[fnr]
        if(kontaktinfo == null) {
            kontaktinfo = DigitalKontaktinfoBolk(defaultKontaktinfo(fnr), null)
        }
        val returnValue = objectMapper.writeValueAsString(kontaktinfo)
        log.info("Henter dkif kontaktinfo:\n$returnValue")
        return ResponseEntity.ok(kontaktinfo)
    }

    private fun defaultKontaktinfo(fnr: String): Map<String, DigitalKontaktinfo> {
        return mapOf(fnr to randomKontaktinfo())
    }

    private fun randomKontaktinfo(): DigitalKontaktinfo {
        return DigitalKontaktinfo(randomInt(8).toString())
    }
}

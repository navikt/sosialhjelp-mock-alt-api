package no.nav.sbl.sosialhjelp_mock_alt.integrations.dkif

import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.DkifService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.model.DigitalKontaktinfo
import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.model.DigitalKontaktinfoBolk
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraHeaders
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class DkifController(private val dkifService: DkifService) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/dkif/v1/personer/kontaktinformasjon")
    fun getKontaktinfo(@RequestHeader headers: HttpHeaders): ResponseEntity<DigitalKontaktinfoBolk> {
        val fnr = hentFnrFraHeaders(headers)
        var kontaktinfo = dkifService.getDigitalKontaktinfoBolk(fnr)
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

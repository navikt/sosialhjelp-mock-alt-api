package no.nav.sbl.sosialhjelp.mock.alt.integrations.krr

import no.nav.sbl.sosialhjelp.mock.alt.datastore.krr.KrrService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.krr.model.DigitalKontaktinformasjon
import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class KrrController(
    private val krrService: KrrService,
) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/krr/rest/v1/person")
    fun getPerson(
        @RequestHeader(name = "Nav-Personident") ident: String,
    ): DigitalKontaktinformasjon {
        log.info("Henter KRR data for id: $ident")
        return krrService.hentKonfigurasjon(ident)
    }

    @GetMapping("/krr/rest/ping")
    fun getPing(): String = "OK"
}

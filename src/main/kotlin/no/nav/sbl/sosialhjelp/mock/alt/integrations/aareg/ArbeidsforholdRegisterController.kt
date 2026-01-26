package no.nav.sbl.sosialhjelp.mock.alt.integrations.aareg

import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.AaregService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.ArbeidsforholdResponseDto
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper
import no.nav.sbl.sosialhjelp.mock.alt.utils.hentFnrFraHeaders
import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class ArbeidsforholdRegisterController(
    private val aaregService: AaregService,
) {
    companion object {
        private val log by logger()
    }

    // ?sporingsinformasjon=false&regelverk=A_ORDNINGEN&ansettelsesperiodeFom=2020-07-29&ansettelsesperiodeTom=2020-10-29
    @PostMapping("/aareg/v2/arbeidstaker/arbeidsforhold")
    fun getArbeidsforhold(
        @RequestHeader headers: HttpHeaders,
    ): ResponseEntity<List<ArbeidsforholdResponseDto>> {
        val fnr = hentFnrFraHeaders(headers)
        val arbeidsforhold = aaregService.getArbeidsforhold(fnr)
        log.info(
            "Henter aareg arbeidsforhold liste: ${objectMapper.writeValueAsString(arbeidsforhold)}",
        )
        return ResponseEntity.ok(arbeidsforhold)
    }
}

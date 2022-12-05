package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.soknadApi

import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SvarUtService
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date

@RestController
class SoknadApiController(
    private val svarUtService: SvarUtService,
    @Value("\${soknad-frontend-baseurl}") private val soknadFrontendBaseUrl: String
) {
    /**
     * Endepunktet kalles kun av innsyn-api ved lokal kjøring
     */
    @GetMapping("soknad-api/soknadoversikt/soknader")
    @ResponseBody
    fun soknadoversikt(@RequestHeader headers: HttpHeaders): ResponseEntity<List<SaksListeDto>> {
        val ident = hentFnrFraToken(headers)
        val svarUtSoknader = svarUtService.getSvarUtSoknader(ident)
            .map {
                val soknadId = it.first.eksternReferanse.removePrefix("-")
                SaksListeDto(
                    fiksDigisosId = null,
                    soknadTittel = "Økonomisk sosialhjelp ($soknadId)",
                    sistOppdatert = Date.from(LocalDateTime.parse(it.second.innsendingstidspunkt, DateTimeFormatter.ISO_DATE_TIME).toInstant(ZoneOffset.UTC)),
                    kilde = "soknad-api",
                    url = "$soknadFrontendBaseUrl/skjema/$soknadId/ettersendelse"
                )
            }
        return ResponseEntity.ok(svarUtSoknader)
    }
}

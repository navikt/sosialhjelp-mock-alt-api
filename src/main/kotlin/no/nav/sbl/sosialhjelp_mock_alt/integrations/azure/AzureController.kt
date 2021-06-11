package no.nav.sbl.sosialhjelp_mock_alt.integrations.azure

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.integrations.azure.model.AzureAdBruker
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraCookieNoDefault
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraHeadersNoDefault
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class AzureController(val pdlService: PdlService) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/azuread/graph/me")
    fun getCurrentAzureBruker(
        @RequestHeader headers: HttpHeaders,
        @CookieValue(name = "localhost-idtoken") cookie: String?,
    ): AzureAdBruker {
        val id = hentFnrFraHeadersNoDefault(headers) ?: hentFnrFraCookieNoDefault(cookie) ?: throw MockAltException("Klarte ikke å finne id.")
        return getAzureBruker(id)
    }

    @GetMapping("/azuread/graph/users/{id}")
    fun getAzureBruker(@PathVariable id: String): AzureAdBruker {
        val personalia = pdlService.getPersonalia(id)
        log.info("Henter azureAd bruker med id $id")
        return AzureAdBruker(
            personalia.fnr,
            "${personalia.navn.fornavn} ${personalia.navn.mellomnavn} ${personalia.navn.etternavn}"
                .replace("  ", " ").trim(),
            "${personalia.navn.fornavn} ${personalia.navn.mellomnavn}".trim(),
            personalia.navn.etternavn
        )
    }
}

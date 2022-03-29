package no.nav.sbl.sosialhjelp_mock_alt.integrations.azure

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.integrations.azure.model.AzureAdBruker
import no.nav.sbl.sosialhjelp_mock_alt.integrations.azure.model.AzureAdBrukere
import no.nav.sbl.sosialhjelp_mock_alt.integrations.azure.model.AzureAdGruppe
import no.nav.sbl.sosialhjelp_mock_alt.integrations.azure.model.AzureAdGrupper
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraCookieNoDefault
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraHeadersNoDefault
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraTokenNoDefault
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    ): ResponseEntity<AzureAdBruker> {
        val id = hentFnrFraTokenNoDefault(headers)
            ?: hentFnrFraHeadersNoDefault(headers)
            ?: hentFnrFraCookieNoDefault(cookie)
        return getAzureBruker(id ?: throw MockAltException("Klarte ikke å finne id i request."))
    }

    @GetMapping("/azuread/graph/users/{id}")
    fun getAzureBruker(@PathVariable id: String): ResponseEntity<AzureAdBruker> {
        log.info("Henter azureAd bruker med id $id")
        return try {
            val personalia = pdlService.getPersonalia(id)
            ResponseEntity.ok(AzureAdBruker(personalia))
        } catch (e: MockAltException) {
            log.info("Feil ved henting av bruker: ${e.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @GetMapping("/azuread/graph/me/memberOf")
    fun getInnloggetAzureBrukersGrupper(@RequestHeader headers: HttpHeaders): ResponseEntity<AzureAdGrupper> {
        val id = hentFnrFraToken(headers)
        log.info("Henter azureAd brukers grupper for \"me\" (id: $id)")
        return try {
            val grupper = hentGrupper(id)
            ResponseEntity.ok(AzureAdGrupper(value = grupper))
        } catch (e: MockAltException) {
            log.info("Feil ved henting av brukers grupper: ${e.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @GetMapping("/azuread/graph/users/{id}/memberOf")
    fun getAzureBrukersGrupper(@PathVariable id: String): ResponseEntity<AzureAdGrupper> {
        log.info("Henter azureAd brukers grupper for brukerId $id")
        return try {
            val grupper = hentGrupper(id)
            ResponseEntity.ok(AzureAdGrupper(value = grupper))
        } catch (e: MockAltException) {
            log.info("Feil ved henting av brukers grupper: ${e.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    private fun hentGrupper(id: String): List<AzureAdGruppe> {
        val personalia = pdlService.getPersonalia(id)
        val grupper = if (personalia.navn.fornavn == "Admin") {
            if (personalia.navn.etternavn == "Admin") {
                listOf(
                    AzureAdGruppe("0000-MOCK-sosialhjelp-dialog-veileder", "Veiledere"),
                    AzureAdGruppe("0000-MOCK-sosialhjelp-dialog-admin", "Administratorer"),
                )
            } else if (personalia.navn.etternavn == "Arkiv") {
                listOf(
                    AzureAdGruppe("0000-MOCK-sosialhjelp-dialog-arkiv", "Teknisk-arkiv"),
                )
            } else if (personalia.navn.etternavn == "Innsikt") {
                listOf(
                    AzureAdGruppe("0000-MOCK-sosialhjelp-dialog-innsikt", "Innsikktere"),
                )
            } else {
                listOf(
                    AzureAdGruppe("0000-MOCK-sosialhjelp-dialog-veileder", "Veiledere"),
                    AzureAdGruppe("0000-MOCK-sosialhjelp-dialog-admin", "Administratorer"),
                    AzureAdGruppe("0000-MOCK-sosialhjelp-dialog-arkiv", "Teknisk-arkiv"),
                    AzureAdGruppe("0000-MOCK-sosialhjelp-dialog-innsikt", "Innsikktere"),
                )
            }
        } else if (personalia.navn.fornavn == "Tyske") {
            emptyList() // Ikke veileder
        } else {
            listOf(AzureAdGruppe("0000-MOCK-sosialhjelp-dialog-veileder", "Veiledere"))
        }
        return grupper
    }

    @GetMapping("/azuread/graph/groups/{id}/members")
    fun getAzureGruppeBrukere(@PathVariable id: String): AzureAdBrukere {
        val personaListe = pdlService.getPersonListe()
        log.info("Henter azureAd brukere i gruppe $id")

        return AzureAdBrukere(
            value = personaListe.map { AzureAdBruker(it) }
        )
    }
}

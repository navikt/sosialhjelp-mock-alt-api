package no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl

import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.Kjoenn
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlFoedselsdato
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlInnsynHentPerson
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlInnsynPerson
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlInnsynPersonResponse
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlKjoenn
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlModiaHentPerson
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlModiaPerson
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlModiaPersonResponse
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlPersonNavn
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlTelefonnummer
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PdlController {
    companion object {
        private val log by logger()
    }

    @PostMapping("/pdl_endpoint_url")
    fun dummyEndpoint(@RequestParam parameters: MultiValueMap<String, String>, @RequestBody body: String): String {
        log.info("Henter pdl_endpoint_url")

        return decideResponse(body)
    }

    private fun decideResponse(body: String): String {
        return when {
            body.contains(Regex("(navn)|(kjoenn)|(telefonnummer)|(foedsel)")) -> {
                objectMapper.writeValueAsString(defaultResponseModia())
            }
            body.contains(Regex("(adressebeskyttelse)")) -> {
                objectMapper.writeValueAsString(defaultResponseInnsyn())
            }
            else -> "OK"
        }
    }

    private fun defaultResponseModia() =
            PdlModiaPersonResponse(
                    errors = emptyList(),
                    data = PdlModiaHentPerson(
                            hentPerson = PdlModiaPerson(
                                    navn = listOf(PdlPersonNavn("Person", null, "Testperson")),
                                    kjoenn = listOf(PdlKjoenn(Kjoenn.KVINNE)),
                                    foedsel = listOf(PdlFoedselsdato("1945-10-26")),
                                    telefonnummer = listOf(PdlTelefonnummer("+47","11112222", 1))
                            )
                    )
            )

    private fun defaultResponseInnsyn() =
            PdlInnsynPersonResponse(
                    errors = emptyList(),
                    data = PdlInnsynHentPerson(
                            hentPerson = PdlInnsynPerson(
                                    adressebeskyttelse = emptyList()
                            )
                    )
            )
}

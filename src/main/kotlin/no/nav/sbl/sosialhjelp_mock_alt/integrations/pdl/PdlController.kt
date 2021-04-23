package no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl

import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlRequest
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PdlController(
    private val pdlService: PdlService,
    private val feilService: FeilService,
) {
    @PostMapping("/pdl_endpoint_url", produces = ["application/json;charset=UTF-8"])
    fun pdlEndpoint(
        @RequestParam parameters: MultiValueMap<String, String>,
        @RequestBody body: String,
        @RequestHeader headers: HttpHeaders,
    ): String {
        val tokenFnr = hentFnrFraToken(headers)
        val pdlRequest = objectMapper.readValue(body, PdlRequest::class.java)
        if (tokenFnr != pdlRequest.variables.ident) {
            log.warn("Token matcher ikke request! $tokenFnr != ${pdlRequest.variables.ident}")
        }
        return decideResponse(pdlRequest)
    }

    /**
     * forelderBarnRelasjon -> kun del av person-request fra soknad-api
     * folkeregisterpersonstatus -> kun del av barn-request fra soknad-api
     * bostedsadresse -> del av ektefelle-request fra soknad-api (gjelder også de 2 over, men denne inneholder verken forelderBarnRelasjon eller folkeregisterpersonstatus)
     * kjoenn -> kun del av request fra modia-api
     * navn -> del av request fra innsyn-api (gjelder også 3 av de over, men denne inneholder verken forelderBarnRelasjon, folkeregisterpersonstatus eller bostedsadresse)
     * adressebeskyttelse -> tilgangskontroll-sjekk kall fra soknad-api
     */
    private fun decideResponse(pdlRequest: PdlRequest): String {
        val fnr = pdlRequest.variables.ident
        return when {
            pdlRequest.query.contains(Regex("(forelderBarnRelasjon)")) -> {
                feilService.eventueltLagFeil(fnr, "PdlController", "getSoknadPerson")
                objectMapper.writeValueAsString(pdlService.getSoknadPersonResponseFor(fnr))
            }
            pdlRequest.query.contains(Regex("(folkeregisterpersonstatus)")) -> {
                feilService.eventueltLagFeil(fnr, "PdlController", "getSoknadBarn")
                objectMapper.writeValueAsString(pdlService.getSoknadBarnResponseFor(fnr))
            }
            pdlRequest.query.contains(Regex("(bostedsadresse)")) -> {
                feilService.eventueltLagFeil(fnr, "PdlController", "getSoknadEktefelle")
                objectMapper.writeValueAsString(pdlService.getSoknadEktefelleResponseFor(fnr))
            }
            pdlRequest.query.contains(Regex("(kjoenn)")) -> {
                feilService.eventueltLagFeil(fnr, "PdlController", "getModia")
                objectMapper.writeValueAsString(pdlService.getModiaResponseFor(fnr))
            }
            pdlRequest.query.contains(Regex("(navn)")) -> {
                feilService.eventueltLagFeil(fnr, "PdlController", "getInnsyn")
                objectMapper.writeValueAsString(pdlService.getInnsynResponseFor(fnr))
            }
            pdlRequest.query.contains(Regex("(adressebeskyttelse)")) -> {
                feilService.eventueltLagFeil(fnr, "PdlController", "getSoknadAdressebeskyttelse")
                objectMapper.writeValueAsString(pdlService.getSoknadAdressebeskyttelseResponseFor(fnr))
            }
            else -> "OK"
        }
    }

    companion object {
        private val log by logger()
    }
}

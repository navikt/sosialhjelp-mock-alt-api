package no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl

import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlRequest
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PdlController(
        private val pdlService: PdlService,
        private val feilService: FeilService,
) {
    companion object {
        private val log by logger()
    }

    @PostMapping("/pdl_endpoint_url")
    fun pdlEndpoint(
            @RequestParam parameters: MultiValueMap<String, String>,
            @RequestBody body: String,
    ): String {
        val pdlRequest = objectMapper.readValue(body, PdlRequest::class.java)
        return decideResponse(pdlRequest)
    }

    /**
     * forelderBarnRelasjon -> kun del av person-request fra soknad-api
     * folkeregisterpersonstatus -> kun del av barn-request fra soknad-api
     * bostedsadresse -> del av ektefelle-request fra soknad-api (gjelder også de 2 over, men denne inneholder verken forelderBarnRelasjon eller folkeregisterpersonstatus)
     * adressebeskyttelse -> del av request fra innsyn-api (gjelder også de 3 over, men denne inneholder verken forelderBarnRelasjon, folkeregisterpersonstatus eller bostedsadresse)
     * kjoenn -> kun del av request fra modia-api
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
            pdlRequest.query.contains(Regex("(adressebeskyttelse)")) -> {
                feilService.eventueltLagFeil(fnr, "PdlController", "getInnsyn")
                objectMapper.writeValueAsString(pdlService.getInnsynResponseFor(fnr))
            }
            pdlRequest.query.contains(Regex("(kjoenn)")) -> {
                feilService.eventueltLagFeil(fnr, "PdlController", "getModia")
                objectMapper.writeValueAsString(pdlService.getModiaResponseFor(fnr))
            }
            else -> "OK"
        }
    }
}

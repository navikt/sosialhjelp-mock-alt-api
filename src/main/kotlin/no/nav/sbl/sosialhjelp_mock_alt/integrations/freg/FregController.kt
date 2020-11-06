package no.nav.sbl.sosialhjelp_mock_alt.integrations.freg

import no.nav.sbl.sosialhjelp_mock_alt.integrations.freg.model.NavnDto
import no.nav.sbl.sosialhjelp_mock_alt.integrations.freg.model.OrganisasjonNoekkelinfoDto
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FregController {
    companion object {
        val log by logger()
    }

    @RequestMapping("/freg/v1/organisasjon/{orgnr}/noekkelinfo")
    fun getNokkelinfo(@PathVariable orgnr: String, @RequestHeader headers: HttpHeaders):
            ResponseEntity<OrganisasjonNoekkelinfoDto> {
        val nokkelinfo = OrganisasjonNoekkelinfoDto(
                navn = NavnDto("Mock navn"),
                organisasjonsnummer = orgnr,
        )
        log.info("Henter freg nøkkelinfo: ${objectMapper.writeValueAsString(nokkelinfo)}")
        return ResponseEntity.ok(nokkelinfo)
    }
}

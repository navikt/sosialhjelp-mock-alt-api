package no.nav.sbl.sosialhjelp_mock_alt.integrations.ereg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.EregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.model.NavnDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.model.OrganisasjonNoekkelinfoDto
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class EregController(private val eregService: EregService) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/freg/v1/organisasjon/{orgnr}/noekkelinfo")
    fun getNokkelinfo(@PathVariable orgnr: String, @RequestHeader headers: HttpHeaders):
            ResponseEntity<OrganisasjonNoekkelinfoDto> {
        val nokkelinfo = eregService.getOrganisasjonNoekkelinfo(orgnr) ?: OrganisasjonNoekkelinfoDto(
                    navn = NavnDto("Mock navn"),
                    organisasjonsnummer = orgnr,
            )
        log.info("Henter ereg nøkkelinfo: ${objectMapper.writeValueAsString(nokkelinfo)}")
        return ResponseEntity.ok(nokkelinfo)
    }
}

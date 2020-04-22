package no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks

import no.nav.sbl.sosialhjelp_mock_alt.datastore.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.domain.KommuneInfo
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FiksController(private val soknadService: SoknadService) {
    companion object {
        val log by logger()
    }

    @RequestMapping("/fiks/digisos/digisos/api/v1/soknader/soknader")
    fun listSoknader(@RequestParam parameters:MultiValueMap<String, String>): ResponseEntity<String> {
        val soknad = soknadService.listSoknader()
        return ResponseEntity.ok(soknad)
    }

    @RequestMapping("/fiks/digisos/digisos/api/v1/soknader/{digisosId}")
    fun hentSoknad(@PathVariable digisosId: String): ResponseEntity<String> {
        val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(soknad)
    }

    @RequestMapping("/fiks/digisos/digisos/api/v1/soknader/{digisosId}/dokumenter")
    fun hentDokumenter(@PathVariable digisosId: String): ResponseEntity<String> {
        val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(soknad)
    }

    @RequestMapping("/fiks/digisos/digisos/api/v1/soknader/{digisosId}/dokumenter/{dokumentlagerId}")
    fun hentDokumenterFraLager(@PathVariable digisosId: String, @PathVariable dokumentlagerId: String): ResponseEntity<String> {
        val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(soknad)
    }

    @RequestMapping("/fiks/digisos/digisos/api/v1/nav/kommuner/{kommunenummer}")
    fun hentKommuneInfo(@PathVariable kommunenummer: String): ResponseEntity<String> {
        val kommuneInfo = KommuneInfo(
                kommunenummer = kommunenummer,
                kanMottaSoknader = true,
                kanOppdatereStatus = true,
                harMidlertidigDeaktivertOppdateringer = false,
                harMidlertidigDeaktivertMottak = false,
                kontaktPersoner = null
        )
        log.info("Henter kommuneinfo: $kommuneInfo")
        return ResponseEntity.ok(objectMapper.writeValueAsString(kommuneInfo))
    }
}
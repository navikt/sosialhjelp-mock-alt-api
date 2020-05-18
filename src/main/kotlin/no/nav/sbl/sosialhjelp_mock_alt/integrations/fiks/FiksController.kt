package no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks

import no.nav.sbl.sosialhjelp_mock_alt.datastore.DokumentKrypteringsService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.KommuneInfo
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Collections

@RestController
class FiksController(private val soknadService: SoknadService, private val dokumentKrypteringsService: DokumentKrypteringsService) {
    companion object {
        val log by logger()
    }

    @RequestMapping("/fiks/digisos/api/v1/soknader/soknader")
    fun listSoknader(@RequestParam parameters: MultiValueMap<String, String>): ResponseEntity<String> {
        val soknadsListe = soknadService.listSoknader()
        return ResponseEntity.ok(soknadsListe)
    }

    @RequestMapping("/fiks/digisos/api/v1/soknader/{digisosId}")
    fun hentSoknad(@PathVariable digisosId: String): ResponseEntity<String> {
        val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(soknad)
    }

    // TODO: Modia versjon. Finn riktige URLer 2020-04-22
    @RequestMapping("/fiks/digisos/api/nav/v1/soknader")
    fun listSoknader2(@RequestParam parameters: MultiValueMap<String, String>): ResponseEntity<String> {
        val soknadsListe = soknadService.listSoknader()
        return ResponseEntity.ok(soknadsListe)
    }

    // TODO: Modia versjon. Finn riktige URLer 2020-04-22
    @RequestMapping("/fiks/digisos/api/v1/nav/soknader/{digisosId}")
    fun hentSoknad2(@PathVariable digisosId: String): ResponseEntity<String> {
        val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(soknad)
    }

    // TODO: Modia versjon. Finn riktige URLer 2020-04-22
    @RequestMapping("/fiks/digisos/api/v1/nav/soknader/{digisosId}/dokumenter/{dokumentlagerId}")
    fun hentDokumentFraLager2(@PathVariable digisosId: String,@PathVariable dokumentlagerId: String): ResponseEntity<String> {
        val dokumentString = soknadService.hentDokument(digisosId, dokumentlagerId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(dokumentString)
    }

    @RequestMapping("/fiks/digisos/api/v1/soknader/{digisosId}/dokumenter")
    fun hentDokumenterFraLager2(@PathVariable digisosId: String): ResponseEntity<String> {
        val dokumentListe = soknadService.hentDokumenter(digisosId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(dokumentListe)
    }

    @RequestMapping("/fiks/digisos/api/v1/soknader/{digisosId}/dokumenter/{dokumentlagerId}")
    fun hentDokumentFraLager(@PathVariable digisosId: String, @PathVariable dokumentlagerId: String): ResponseEntity<String> {
        val dokumentString = soknadService.hentDokument(digisosId, dokumentlagerId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(dokumentString)
    }

    @RequestMapping("/fiks/digisos/api/v1/nav/kommuner/{kommunenummer}")
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

    @RequestMapping("/fiks/digisos/api/v1/nav/kommuner")
    fun hentKommuneInfoListe(): ResponseEntity<String> {
        val kommuneInfo = KommuneInfo(
                kommunenummer = "1",
                kanMottaSoknader = true,
                kanOppdatereStatus = true,
                harMidlertidigDeaktivertOppdateringer = false,
                harMidlertidigDeaktivertMottak = false,
                kontaktPersoner = null
        )
        log.info("Henter kommuneinfo: $kommuneInfo")
        return ResponseEntity.ok(objectMapper.writeValueAsString(Collections.singletonList(kommuneInfo)))
    }

    @RequestMapping("/fiks/digisos/api/v1/dokumentlager-public-key")
    fun hentDokumentLagerNokkel(): ResponseEntity<ByteArray> {
        log.info("Henter dokumentlager public key:")
        return ResponseEntity.ok(dokumentKrypteringsService.publicCertificateBytes)
    }

    @RequestMapping("/fiks/digisos/api/v1/soknader/{kommunenummer}/{digisosId}/{navEksternRefId}")
    fun lastOppFiler(@PathVariable kommunenummer: String, @PathVariable digisosId: String, @PathVariable navEksternRefId: String): ResponseEntity<String> {
        log.info("Laster opp fil for kommune: ${kommunenummer} digisosId: ${digisosId} navEksternRefId: ${navEksternRefId}")
        return ResponseEntity.ok("OK")
    }
}
package no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks

import no.nav.sbl.sosialhjelp_mock_alt.datastore.DokumentKrypteringsService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.KommuneInfo
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.Collections

@RestController
class FiksController(private val soknadService: SoknadService, private val dokumentKrypteringsService: DokumentKrypteringsService) {
    companion object {
        private val log by logger()
    }

//    ======== Innsyn =========
    @RequestMapping("/fiks/digisos/api/v1/soknader/soknader")
    fun listSoknaderInnsyn(@RequestParam parameters: MultiValueMap<String, String>): ResponseEntity<String> {
        val soknadsListe = soknadService.listSoknader()
        return ResponseEntity.ok(soknadsListe)
    }

    @RequestMapping("/fiks/digisos/api/v1/soknader/{digisosId}")
    fun hentSoknadInnsyn(@PathVariable digisosId: String): ResponseEntity<String> {
        val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(soknad)
    }

    @RequestMapping("/fiks/digisos/api/v1/soknader/{digisosId}/dokumenter/{dokumentlagerId}")
    fun hentDokumentFraLagerInnsyn(@PathVariable digisosId: String, @PathVariable dokumentlagerId: String): ResponseEntity<String> {
        val dokumentString = soknadService.hentDokument(digisosId, dokumentlagerId)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(dokumentString)
    }

//    ======== Modia =========
    @PostMapping("/fiks/digisos/api/nav/v1/soknader/soknader")
    fun listSoknaderModia(@RequestBody body: String, @RequestParam(name = "sporingsId") sporingsId: String): ResponseEntity<String> {
        val soknadsListe = soknadService.listSoknader()
        return ResponseEntity.ok(soknadsListe)
    }

    @RequestMapping("/fiks/digisos/api/v1/nav/soknader/{digisosId}")
    fun hentSoknadModia(@PathVariable digisosId: String, @RequestParam(name = "sporingsId") sporingsId: String): ResponseEntity<String> {
        val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(soknad)
    }

    @RequestMapping("/fiks/digisos/api/v1/nav/soknader/{digisosId}/dokumenter/{dokumentlagerId}")
    fun hentDokumentFraLagerModia(@PathVariable digisosId: String, @PathVariable dokumentlagerId: String, @RequestParam(name = "sporingsId") sporingsId: String): ResponseEntity<String> {
        val dokumentString = soknadService.hentDokument(digisosId, dokumentlagerId)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(dokumentString)
    }

//    ======= KommuneInfo =======
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

//    ======== public-key dokumentlager ========
    @RequestMapping("/fiks/digisos/api/v1/dokumentlager-public-key")
    fun hentDokumentLagerNokkel(): ResponseEntity<ByteArray> {
        log.info("Henter dokumentlager public key:")
        return ResponseEntity.ok(dokumentKrypteringsService.publicCertificateBytes)
    }

//    ======== Last opp filer ========
    @RequestMapping("/fiks/digisos/api/v1/soknader/{kommunenummer}/{digisosId}/{navEksternRefId}")
    fun lastOppFiler(@PathVariable kommunenummer: String, @PathVariable digisosId: String, @PathVariable navEksternRefId: String): ResponseEntity<String> {
        log.info("Laster opp fil for kommune: ${kommunenummer} digisosId: ${digisosId} navEksternRefId: ${navEksternRefId}")
        return ResponseEntity.ok("OK")
    }

    @PostMapping("/{fiksDigisosId}/filOpplasting", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun filOpplasting(@PathVariable fiksDigisosId: String, @RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        val dokumentlagerId = soknadService.lastOppFil(fiksDigisosId, file)
        return ResponseEntity.ok(dokumentlagerId)
    }
}
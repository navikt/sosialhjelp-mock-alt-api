package no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks

import com.fasterxml.jackson.core.type.TypeReference
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.DokumentKrypteringsService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.DigisosApiWrapper
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.KommuneInfo
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.SakWrapper
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.VedleggMetadata
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.joda.time.DateTime
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.Collections
import java.util.UUID

@RestController
class FiksController(private val soknadService: SoknadService, private val dokumentKrypteringsService: DokumentKrypteringsService) {
    companion object {
        private val log by logger()
    }

    //    ======== Innsyn =========
    @GetMapping("/fiks/digisos/api/v1/soknader/soknader")
    fun listSoknaderInnsyn(@RequestParam parameters: MultiValueMap<String, String>): ResponseEntity<String> {
        val soknadsListe = soknadService.listSoknader()
        return ResponseEntity.ok(soknadsListe)
    }

    @GetMapping("/fiks/digisos/api/v1/soknader/{digisosId}")
    fun hentSoknadInnsyn(@PathVariable digisosId: String): ResponseEntity<String> {
        val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(soknad)
    }

    @GetMapping("/fiks/digisos/api/v1/soknader/{digisosId}/dokumenter/{dokumentlagerId}")
    fun hentDokumentFraLagerInnsyn(@PathVariable digisosId: String, @PathVariable dokumentlagerId: String): ResponseEntity<String> {
        val dokumentString = soknadService.hentDokument(digisosId, dokumentlagerId)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(dokumentString)
    }

    @PostMapping("/fiks/digisos/api/v1/{fiksOrgId}/{digisosId}/filer")
    fun lastOppFilerInnsyn(@PathVariable digisosId: String,
                           @PathVariable(required = false) fiksOrgId: String?,
                     @RequestParam body: LinkedMultiValueMap<String, Any>
    ): ResponseEntity<String> {
        return lastOppFiler("", digisosId, "", body)
    }

    @PostMapping("/fiks/digisos/api/v1/{fiksOrgId}/{fiksDigisosId}") // tar også /ny
    fun oppdaterSoknadInnsyn(@PathVariable fiksOrgId:String,
                             @PathVariable(required = false) fiksDigisosId:String?,
                             @RequestBody(required = false) body: String?): ResponseEntity<String> {
        var id = fiksDigisosId
        if (id == null || id.toLowerCase().contentEquals("ny")) {
            id = UUID.randomUUID().toString()
            val digisosApiWrapper = DigisosApiWrapper(SakWrapper(JsonDigisosSoker()), "")
            digisosApiWrapper.sak.soker.hendelser.add(JsonHendelse()
                    .withHendelsestidspunkt(DateTime.now().toDateTimeISO().toString())
                    .withType(JsonHendelse.Type.SOKNADS_STATUS))
            soknadService.oppdaterDigisosSak(fiksOrgId, id, digisosApiWrapper)
            return ResponseEntity.ok("$id")
        } else {
            val digisosApiWrapper = objectMapper.readValue(body, DigisosApiWrapper::class.java)
            soknadService.oppdaterDigisosSak(fiksOrgId, id, digisosApiWrapper)
            return ResponseEntity.ok("{\"fiksDigisosId\":\"$id\"}")
        }
    }

    //
    //    ======== Modia =========
    @PostMapping("/fiks/digisos/api/v1/nav/soknader/soknader")
    fun listSoknaderModia(@RequestBody body: String, @RequestParam(name = "sporingsId") sporingsId: String): ResponseEntity<String> {
        val soknadsListe = soknadService.listSoknader()
        return ResponseEntity.ok(soknadsListe)
    }

    @GetMapping("/fiks/digisos/api/v1/nav/soknader/{digisosId}")
    fun hentSoknadModia(@PathVariable digisosId: String, @RequestParam(name = "sporingsId") sporingsId: String): ResponseEntity<String> {
        val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(soknad)
    }

    @GetMapping("/fiks/digisos/api/v1/nav/soknader/{digisosId}/dokumenter/{dokumentlagerId}")
    fun hentDokumentFraLagerModia(@PathVariable digisosId: String, @PathVariable dokumentlagerId: String, @RequestParam(name = "sporingsId") sporingsId: String): ResponseEntity<String> {
        val dokumentString = soknadService.hentDokument(digisosId, dokumentlagerId)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(dokumentString)
    }

    //    ======= KommuneInfo =======
    @GetMapping("/fiks/digisos/api/v1/nav/kommuner/{kommunenummer}")
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

    @GetMapping("/fiks/digisos/api/v1/nav/kommuner")
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
    @GetMapping("/fiks/digisos/api/v1/dokumentlager-public-key")
    fun hentDokumentLagerNokkel(): ResponseEntity<ByteArray> {
        log.info("Henter dokumentlager public key:")
        return ResponseEntity.ok(dokumentKrypteringsService.publicCertificateBytes)
    }

    //    ======== Last opp filer ========
    @PostMapping("/fiks/digisos/api/v1/soknader/{kommunenummer}/{digisosId}/{navEksternRefId}")
    fun lastOppFiler(@PathVariable kommunenummer: String,
                     @PathVariable digisosId: String,
                     @PathVariable navEksternRefId: String,
                     @RequestParam body: LinkedMultiValueMap<String, Any>
    ): ResponseEntity<String> {
        log.info("Laster opp filer for kommune: $kommunenummer digisosId: $digisosId navEksternRefId: $navEksternRefId")
        val vedleggsInfoText:String = body["vedlegg.json"].toString()
        val vedleggsJson = objectMapper.readValue(vedleggsInfoText, object : TypeReference<List<JsonVedleggSpesifikasjon>>() {})
        body.keys.forEach {
            if(it.startsWith("vedleggSpesifikasjon")) {
                val json = body[it].toString()
                val vedleggMetadata = objectMapper.readValue(json, object : TypeReference<List<VedleggMetadata>>() {})
                soknadService.lastOppFil(digisosId, vedleggMetadata[0], vedleggsJson[0])
            }
        }
        return ResponseEntity.ok("OK")
    }

    @PostMapping("/{fiksDigisosId}/filOpplasting", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun filOpplasting(@PathVariable fiksDigisosId: String, @RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        log.info("Laster opp fil for fiksDigisosId: $fiksDigisosId")
        val dokumentlagerId = soknadService.lastOppFil(fiksDigisosId, file)
        return ResponseEntity.ok(dokumentlagerId)
    }
}
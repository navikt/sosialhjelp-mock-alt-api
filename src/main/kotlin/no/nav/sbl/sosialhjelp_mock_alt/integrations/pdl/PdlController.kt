package no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.PdlRequest
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PdlController(private val pdlService: PdlService) {
    companion object {
        private val log by logger()
    }

    @PostMapping("/pdl_endpoint_url")
    fun pdlEndpoint(@RequestParam parameters: MultiValueMap<String, String>, @RequestBody body: String): String {
        val pdlRequest = objectMapper.readValue(body, PdlRequest::class.java)
        return decideResponse(pdlRequest)
    }

    private fun decideResponse(pdlRequest: PdlRequest): String {
        return when {
            pdlRequest.query.contains(Regex("(adressebeskyttelse)")) -> {
                objectMapper.writeValueAsString(pdlService.getInnsynResponseFor(pdlRequest.variables.ident))
            }
            pdlRequest.query.contains(Regex("(navn)|(kjoenn)|(telefonnummer)|(foedsel)")) -> {
                objectMapper.writeValueAsString(pdlService.getModiaResponseFor(pdlRequest.variables.ident))
            }
            else -> "OK"
        }
    }

    // Frontend stuff:
    @PostMapping("/pdl/upload_url")
    fun pdlUpload(@RequestBody body: String): ResponseEntity<String> {
        log.info("Laster opp pdl data: $body")
        val personalia = objectMapper.readValue(body, Personalia::class.java)
        if (personalia.fnr.isEmpty()) {
            return ResponseEntity.badRequest().body("FNR må være satt!")
        }
        pdlService.leggTilPerson(personalia)
        return ResponseEntity.ok("OK")
    }

    @GetMapping("/pdl/download_url")
    fun pdlDownload(@RequestParam ident: String): ResponseEntity<Personalia> {
        val personalia = try {
            pdlService.getPersonalia(ident)
        } catch (e: Exception) {
            log.warn("Finner ikke personalia for fnr: $ident")
            return ResponseEntity.noContent().build()
        }
        log.info("Henter ned pdl data for fnr: $ident")
        return ResponseEntity.ok(personalia)
    }

    @GetMapping("/pdl/person_liste")
    fun personListe(): ResponseEntity<Collection<Personalia>> {
        val personListe = pdlService.getPersonListe()
        return ResponseEntity.ok(personListe)
    }
}

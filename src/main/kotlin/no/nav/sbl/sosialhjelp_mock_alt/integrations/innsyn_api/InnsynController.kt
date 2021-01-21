package no.nav.sbl.sosialhjelp_mock_alt.integrations.innsyn_api

import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.DigisosApiWrapper
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class InnsynController(private val soknadService: SoknadService) {

    @PostMapping("/innsyn-api/api/v1/digisosapi/oppdaterDigisosSak")
    fun oppdaterSoknad(
            @RequestParam(required = false) fiksDigisosId:String?,
            @RequestParam(required = false) fnr:String?,
            @RequestBody body: String,
            @RequestHeader headers: HttpHeaders): ResponseEntity<String> {
        var id = fiksDigisosId
        if (id == null) {
            id = UUID.randomUUID().toString()
        }
        val digisosApiWrapper = objectMapper.readValue(body, DigisosApiWrapper::class.java)

        val faktiskFnr = hentFnrFraTokenOrInput(fnr, headers)
        soknadService.oppdaterDigisosSak(kommuneNr = "0301", fiksOrgId = "11415cd1-e26d-499a-8421-751457dfcbd5",
                fnr = faktiskFnr, fiksDigisosIdInput = id, digisosApiWrapper = digisosApiWrapper)
        return ResponseEntity.ok("{\"fiksDigisosId\":\"$id\"}")
    }

    private fun hentFnrFraTokenOrInput(fnrInput: String?, headers: HttpHeaders): String {
        return fnrInput ?: hentFnrFraToken(headers)
    }
}

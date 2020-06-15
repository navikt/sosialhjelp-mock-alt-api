package no.nav.sbl.sosialhjelp_mock_alt.integrations.innsyn_api

import no.nav.sbl.sosialhjelp_mock_alt.datastore.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.DigisosApiWrapper
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class InnsynController(private val soknadService: SoknadService) {

    @PostMapping("/innsyn-api/api/v1/digisosapi/oppdaterDigisosSak")
    fun oppdaterSoknad(@RequestParam(required = false) fiksDigisosId:String?, @RequestBody body: String): ResponseEntity<String> {
        var id = fiksDigisosId
        if (id == null) {
            id = UUID.randomUUID().toString()
        }
        val digisosApiWrapper = objectMapper.readValue(body, DigisosApiWrapper::class.java)

        soknadService.oppdaterDigisosSak("11415cd1-e26d-499a-8421-751457dfcbd5", id, digisosApiWrapper)
        return ResponseEntity.ok("{\"fiksDigisosId\":\"$id\"}")
    }
}
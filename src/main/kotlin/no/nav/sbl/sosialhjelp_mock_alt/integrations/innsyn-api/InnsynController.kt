package no.nav.sbl.sosialhjelp_mock_alt.integrations.`innsyn-api`

import no.nav.sbl.sosialhjelp_mock_alt.datastore.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.DigisosApiWrapper
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

@RestController
class InnsynController(private val soknadService: SoknadService) {

    @RequestMapping("/innsyn-api/api/v1/digisosapi/oppdaterDigisosSak")
    fun oppdaterSoknad(@RequestParam parameters: MultiValueMap<String, String>, @RequestBody body: String): ResponseEntity<String> {
        val fiksDigisosId: String? = parameters.get("fiksDigisosId")?.get(0)
        if (fiksDigisosId == null) {
            throw RuntimeException("Missig fiksDigisosId parameter!")
        }
        val digisosApiWrapper = objectMapper.readValue(body, DigisosApiWrapper::class.java)

        soknadService.oppdaterDigisosSak(fiksDigisosId, digisosApiWrapper)
        return ResponseEntity.ok("{\"fiksDigisosId\":\"$fiksDigisosId\"}")
    }
}
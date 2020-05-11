package no.nav.sbl.sosialhjelp_mock_alt.integrations.abac

import no.nav.sbl.sosialhjelp_mock_alt.integrations.abac.model.AbacResponse
import no.nav.sbl.sosialhjelp_mock_alt.integrations.abac.model.Decision
import no.nav.sbl.sosialhjelp_mock_alt.integrations.abac.model.XacmlResponse
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AbacController {
    companion object {
        val log by logger()
    }

    @RequestMapping("/abac/application/authorize")
    fun getToken(@RequestParam parameters:MultiValueMap<String, String>): String {
        val authorization = XacmlResponse(response = listOf(AbacResponse(decision = Decision.Permit, associatedAdvice = emptyList())))
        log.info("Henter abac authorization: $authorization")
        return objectMapper.writeValueAsString(authorization)
    }
}
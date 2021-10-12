package no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten

import no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten.model.IdPortenAccessTokenResponse
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IdPortenController {

    @PostMapping("/idporten/idporten-oidc-provider/token")
    fun getToken(
        @RequestParam parameters: MultiValueMap<String, String>?,
        @RequestBody body: String?
    ): IdPortenAccessTokenResponse {
        val token = IdPortenAccessTokenResponse(
            accessToken = "",
            expiresIn = 999999,
            scope = "ks:fiks"
        )
        log.info("Henter token: $token")
        return token
    }

    companion object {
        private val log by logger()
    }
}

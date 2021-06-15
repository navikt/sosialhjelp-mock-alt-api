package no.nav.sbl.sosialhjelp_mock_alt.integrations.wellknown

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten.model.WellKnown
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class WellKnownController(
    @Value("\${host_address}") private val hostAddress: String,
    private val proxyAwareResourceRetriever: ProxyAwareResourceRetriever,
    private val mockOAuth2Server: MockOAuth2Server
) {

    @GetMapping("/well-known/{issuer}")
    fun getMockAltMetadata(
        @PathVariable(value = "issuer") issuer: String
    ): WellKnown {
        val wellknownUrl = mockOAuth2Server.wellKnownUrl(issuer)

        val metadata = proxyAwareResourceRetriever.retrieveResource(wellknownUrl.toUrl()).content
            .replace("http://view-localhost:4321/$issuer/jwks", "${hostAddress}sosialhjelp/mock-alt-api/jwks/$issuer")
            .replace("http://localhost:4321/$issuer/jwks", "${hostAddress}sosialhjelp/mock-alt-api/jwks/$issuer")

        val wellknown = objectMapper.readValue<WellKnown>(metadata)
        log.info("Metadata for issuer=$issuer: \n$wellknown")
        return wellknown
    }

    @GetMapping("/jwks/{issuer}")
    fun getMockAltJwks(
        @PathVariable(value = "issuer") issuer: String
    ): String {
        val jwksUrl = mockOAuth2Server.jwksUrl(issuer)
        val data = proxyAwareResourceRetriever.retrieveResource(jwksUrl.toUrl())
        log.info("Henter jwks for issuer=$issuer")
        return data.content
    }

    companion object {
        private val log by logger()
    }
}

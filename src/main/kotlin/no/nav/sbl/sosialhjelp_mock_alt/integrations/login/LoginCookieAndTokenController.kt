package no.nav.sbl.sosialhjelp_mock_alt.integrations.login

import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import no.nav.security.token.support.spring.test.MockLoginController
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@RestController
class LoginCookieAndTokenController(
    @Value("\${host_address}") private val host_address: String,
    @Value("\${cookie_domain}") private val cookie_domain: String,
    private val proxyAwareResourceRetriever: ProxyAwareResourceRetriever,
    private val mockOAuth2Server: MockOAuth2Server
) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/login/metadata/{issuer}")
    fun getMockAltMetadata(
        @PathVariable(value = "issuer") issuer: String
    ): String {
        val wellknownUrl = mockOAuth2Server.wellKnownUrl(issuer)
        val metadata = proxyAwareResourceRetriever.retrieveResource(wellknownUrl.toUrl()).content
            .replace("http://view-localhost:4321/$issuer/jwks", "${host_address}sosialhjelp/mock-alt-api/login/jwks/$issuer")
            .replace("http://localhost:4321/$issuer/jwks", "${host_address}sosialhjelp/mock-alt-api/login/jwks/$issuer")
        log.info("Metadata for issuer=$issuer: \n$metadata")
        return metadata
    }

    @GetMapping("/login/jwks/{issuer}")
    fun getMockAltJwks(
        @PathVariable(value = "issuer") issuer: String
    ): String {
        val jwksUrl = mockOAuth2Server.jwksUrl(issuer)
        val data = proxyAwareResourceRetriever.retrieveResource(jwksUrl.toUrl())
        log.info("Hetner jwks for issuer=$issuer")
        return data.content
    }

    @GetMapping("/login/cookie")
    fun addCookie(
        @RequestParam(value = "issuerId") issuerId: String?,
        @RequestParam(value = "audience") audience: String,
        @RequestParam(value = "subject", defaultValue = "12345678910") subject: String?,
        @RequestParam(value = "cookiename", defaultValue = "localhost-idtoken") cookieName: String,
        @RequestParam(value = "redirect", required = false) redirect: String?,
        @RequestParam(value = "expiry", required = false) expiry: String?,
        response: HttpServletResponse
    ): Cookie? {
        val token = mockOAuth2Server.issueToken(
            issuerId!!,
            MockLoginController::class.java.simpleName,
            DefaultOAuth2TokenCallback(
                issuerId,
                subject!!,
                listOf(audience),
                java.util.Map.of("acr", "Level4"),
                expiry?.toLong() ?: 3600
            )
        ).serialize()
        return createCookieAndAddToResponse(
            response,
            cookieName,
            token,
            redirect
        )
    }

    @PostMapping("/login/cookie/{issuerId}")
    fun addCookie(
        @PathVariable(value = "issuerId") issuerId: String,
        @RequestParam(value = "cookiename", defaultValue = "localhost-idtoken") cookieName: String,
        @RequestParam(value = "redirect", required = false) redirect: String?,
        @RequestBody claims: Map<String, Any>,
        response: HttpServletResponse
    ): Cookie? {
        val token = mockOAuth2Server.anyToken(
            mockOAuth2Server.issuerUrl(issuerId),
            claims
        ).serialize()
        return createCookieAndAddToResponse(
            response,
            cookieName,
            token,
            redirect
        )
    }

    private fun createCookieAndAddToResponse(
        response: HttpServletResponse,
        cookieName: String,
        token: String,
        redirect: String?
    ): Cookie? {
        val cookie = Cookie(cookieName, token)
        cookie.domain = cookie_domain
        cookie.path = "/"
        response.addCookie(cookie)
        if (redirect != null) {
            response.sendRedirect(redirect)
            return null
        }
        return cookie
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.integrations.login

import com.nimbusds.jose.JOSEObjectType
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.MockLoginController
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class LoginCookieController(
    @Value("\${cookie_domain}") private val cookie_domain: String,
    private val mockOAuth2Server: MockOAuth2Server
) {

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
        val claims = mutableMapOf<String, String>()
        claims["acr"] = "Level4"
        claims["pid"] = subject!! // idporten
        claims["oid"] = subject // azure ad
        val token = mockOAuth2Server.issueToken(
            issuerId!!,
            MockLoginController::class.java.simpleName,
            DefaultOAuth2TokenCallback(
                issuerId,
                subject,
                JOSEObjectType.JWT.type,
                listOf(audience),
                claims,
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

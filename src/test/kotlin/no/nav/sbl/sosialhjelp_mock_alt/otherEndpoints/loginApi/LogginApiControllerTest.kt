package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.loginApi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LogginApiControllerTest {

    @Test
    fun cookieTest() {
        val token = LogginApiController.extractToken(listOf(
                "other=0; " +
                        "localhost-idtoken=ourTokenString; " +
                        "something=1111111"))
        assertEquals("ourTokenString", token)
    }

    @Test
    fun cookieTest_firstPosittion() {
        val token = LogginApiController.extractToken(listOf(
                "localhost-idtoken=ourTokenString; " +
                        "other=0; " +
                        "something=1111111"))
        assertEquals("ourTokenString", token)
    }

    @Test
    fun cookieTest_lastPosittion() {
        val token = LogginApiController.extractToken(listOf(
                "other=0; " +
                        "something=1111111" +
                        "localhost-idtoken=ourTokenString; "))
        assertEquals("ourTokenString", token)
    }
}

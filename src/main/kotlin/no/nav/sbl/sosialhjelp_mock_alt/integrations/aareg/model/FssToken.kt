package no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model

import no.nav.security.token.support.test.JwtTokenGenerator

class FssToken(val access_token: String, val token_type: String, val expires_in: Long) {
    companion object {
        fun createToken(fnr: String): FssToken {
            val expiryTime = 120L
            val token = JwtTokenGenerator.createSignedJWT(fnr, expiryTime)
            return FssToken(
                access_token = token.serialize(),
                token_type = "FssToken",
                expires_in = expiryTime,
            )
        }
    }
}

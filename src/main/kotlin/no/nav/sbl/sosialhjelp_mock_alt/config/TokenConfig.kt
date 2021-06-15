package no.nav.sbl.sosialhjelp_mock_alt.config

import no.nav.security.token.support.spring.test.MockOAuth2ServerAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(MockOAuth2ServerAutoConfiguration::class)
class TokenConfig {

    // Mulig starte mockOAuth2Server manuelt fremfor med AutoConfiguration.
    //
    // WellKnownController
    // - wellknown for issuer -> /well-known/{issuer}  -> gå til discoveryurl for issuer i mockOauth2Server
    // - jwks for issuer      -> /jwks/{issuer}      -> gå til jwks url for issuer i mockOauth2Server
    //
    // LoginCookieController
    // - addCookie            -> /login/cookie/{issuer}    -> kopiert og tilpasset fra MockLoginController
}

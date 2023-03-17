package no.nav.sbl.sosialhjelp_mock_alt.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("digisos-ekstern")
class SwaggerConfig {
  @Bean
  fun customOpenAPI(): OpenAPI {
    val server = Server()
    server.url = "https://digisos.ekstern.dev.nav.no/sosialhjelp/mock-alt-api"
    return OpenAPI().servers(listOf(server))
  }
}

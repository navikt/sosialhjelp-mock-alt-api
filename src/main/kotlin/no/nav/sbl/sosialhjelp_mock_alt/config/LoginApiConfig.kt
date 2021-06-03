package no.nav.sbl.sosialhjelp_mock_alt.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class LoginApiConfig : WebMvcConfigurer {

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .requestFactory(SimpleClientHttpRequestFactory::class.java)
            .build()!!
    }
}

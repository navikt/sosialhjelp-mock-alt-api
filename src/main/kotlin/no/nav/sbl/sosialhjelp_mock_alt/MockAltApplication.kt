package no.nav.sbl.sosialhjelp_mock_alt

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.sosialhjelp_mock_alt.config.CORSFilter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class MockAltApplication {

	@Bean
	fun navCorsFilter(): CORSFilter {
		return CORSFilter()
	}
}

val objectMapper: ObjectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()
		.registerModules(JavaTimeModule(), KotlinModule())
		.configure(SerializationFeature.INDENT_OUTPUT, true)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

fun main(args: Array<String>) {
	runApplication<MockAltApplication>(*args)
}

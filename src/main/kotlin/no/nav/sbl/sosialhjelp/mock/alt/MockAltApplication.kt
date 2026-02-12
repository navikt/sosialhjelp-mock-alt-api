package no.nav.sbl.sosialhjelp.mock.alt

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.sosialhjelp.mock.alt.config.CORSFilter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.time.LocalDateTime
import java.time.ZoneId

@SpringBootApplication
@EnableScheduling
class MockAltApplication {
    @Bean
    fun navCorsFilter(): CORSFilter = CORSFilter()
}

fun main(args: Array<String>) {
    runApplication<MockAltApplication>(*args)
}

val objectMapper: JsonMapper =
    JsonSosialhjelpObjectMapper
        .createJsonMapperBuilder()
        .addModule(kotlinModule())
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .build()

fun LocalDateTime.toEpochMillis() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

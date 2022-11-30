package no.nav.sbl.sosialhjelp_mock_alt.datastore.enhetsregisteret

import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class EnhetsregisteretService {
    fun getEnhet(orgnr: String): String {
        log.info("Henter enhetsinformasjon for orgnr $orgnr")
        return this::class.java.classLoader.getResource("enhetsregisteret/kommune.json")!!.readText()
    }

    companion object {
        private val log by logger()
    }
}

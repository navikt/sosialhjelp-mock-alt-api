package no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer

import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.model.KontonummerDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Component

@Component
class KontonummerService {

    private val kontonummerMap: HashMap<String, KontonummerDto> = HashMap()

    fun getKontonummer(ident: String): KontonummerDto? {
        log.info("Henter kontonummer for $ident")
        return kontonummerMap[ident]
    }

    fun putKontonummer(ident: String, kontonummer: String) {
        kontonummerMap[ident] = KontonummerDto(kontonummer)
    }

    companion object {
        private val log by logger()
    }
}
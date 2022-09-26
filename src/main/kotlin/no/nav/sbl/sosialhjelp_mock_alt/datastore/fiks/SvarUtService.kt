package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks

import no.ks.fiks.svarut.klient.model.Forsendelse
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SvarUtService(
    @Value("\${filter_soknader_on_fnr}") private val filterSoknaderOnFnr: Boolean,
) {
    private val forsendelseSoknadMap: HashMap<String, Pair<Forsendelse, JsonSoknad>> = HashMap()

    fun addSvarUtSoknad(fnr: String, forsendelse: Forsendelse, jsonSoknad: JsonSoknad) {
        forsendelseSoknadMap[fnr] = forsendelse to jsonSoknad
    }

    fun getSvarUtSoknader(fnr: String?): MutableCollection<Pair<Forsendelse, JsonSoknad>> {
        if (fnr == null) {
            log.info("Henter søknadsliste. Antall SvarUt-soknader: ${forsendelseSoknadMap.size}")
            return forsendelseSoknadMap.values
        }
        if (filterSoknaderOnFnr) {
            return forsendelseSoknadMap.values
                .filter { it.second.data.personalia.personIdentifikator.verdi == fnr }
                .toMutableList()
                .also { log.info("Henter søknadsliste. Antall SvarUt-soknader for $fnr: ${it.size}") }
        }

        log.info("- returnerer fortsatt alle. Antall SvarUt-soknader: ${forsendelseSoknadMap.size}")
        return forsendelseSoknadMap.values
    }

    companion object {
        val log by logger()
    }
}

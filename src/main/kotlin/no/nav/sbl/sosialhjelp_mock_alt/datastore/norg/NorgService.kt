package no.nav.sbl.sosialhjelp_mock_alt.datastore.norg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.norg.model.NavEnhet
import org.springframework.stereotype.Service

@Service
class NorgService {

    private val navEnheter = mutableMapOf<String, NavEnhet>()

    init {
        leggTilNavenhet(navEnheter, "0301", "Sentrum, Oslo kommune")
        leggTilNavenhet(navEnheter, "0315", "Grünerløkka, Oslo kommune")
        leggTilNavenhet(navEnheter, "1208", "NAV Årstad, Årstad kommune")
        leggTilNavenhet(navEnheter, "1209", "NAV Bergenhus, Bergen kommune")
        leggTilNavenhet(navEnheter, "1210", "NAV Ytrebygda, Bergen kommune")
    }

    fun getNavenhet(enhetsnr: String): NavEnhet? {
        return navEnheter[enhetsnr] ?: lagMockNavEnhet(enhetsnr, "Generert mockenhet $enhetsnr, Mock kommune")
    }

    fun getAlleNavenheter(): Collection<NavEnhet> {
        return navEnheter.values
    }
}

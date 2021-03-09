package no.nav.sbl.sosialhjelp_mock_alt.datastore.norg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.norg.model.NavEnhet
import no.nav.sbl.sosialhjelp_mock_alt.integrations.norg.lagMockNavEnhet
import no.nav.sbl.sosialhjelp_mock_alt.integrations.norg.leggTilNavenhet
import org.springframework.stereotype.Service

@Service
class NorgService {

    private val navEnheter = mutableMapOf<String, NavEnhet>()

    init {
        leggTilNavenhet(navEnheter, "1234", "Mock bydel, mock kommune")
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

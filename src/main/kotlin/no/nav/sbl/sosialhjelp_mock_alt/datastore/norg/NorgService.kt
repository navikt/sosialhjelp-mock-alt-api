package no.nav.sbl.sosialhjelp_mock_alt.datastore.norg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.norg.model.NavEnhet
import org.springframework.stereotype.Service

@Service
class NorgService {

    private val navEnheter = mutableMapOf<String, NavEnhet>()
    private val gtNavEnheter = mutableMapOf<String, NavEnhet>()

    init {
        val sentrum = lagMockNavEnhet("0301", "Sentrum, Oslo kommune")
        val grunerokka = lagMockNavEnhet("0315", "Grünerløkka, Oslo kommune")
        val aarstad = lagMockNavEnhet("1208", "NAV Årstad, Årstad kommune")
        val bergenhus = lagMockNavEnhet("1209", "NAV Bergenhus, Bergen kommune")
        val ytrebygda = lagMockNavEnhet("1210", "NAV Ytrebygda, Bergen kommune")
        val horten = lagMockNavEnhet("0701", "NAV Horten")

        navEnheter[sentrum.enhetNr] = sentrum
        navEnheter[grunerokka.enhetNr] = grunerokka
        navEnheter[aarstad.enhetNr] = aarstad
        navEnheter[bergenhus.enhetNr] = bergenhus
        navEnheter[ytrebygda.enhetNr] = ytrebygda

        gtNavEnheter["0301"] = sentrum
        gtNavEnheter["4601"] = bergenhus
        gtNavEnheter["3801"] = horten
    }

    fun getNavenhet(enhetsnr: String): NavEnhet? {
        return navEnheter[enhetsnr] ?: lagMockNavEnhet(enhetsnr, "Generert mockenhet $enhetsnr, Mock kommune")
    }

    fun getNavEnhetForGt(geografiskTilknytning: String): NavEnhet {
        return gtNavEnheter[geografiskTilknytning] ?: lagMockNavEnhet(geografiskTilknytning, "Generert mockenhet for gt $geografiskTilknytning, Mock kommune")
    }

    fun getAlleNavenheter(): Collection<NavEnhet> {
        return navEnheter.values
    }
}

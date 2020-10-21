package no.nav.sbl.sosialhjelp_mock_alt.integrations.norg

import no.nav.sbl.sosialhjelp_mock_alt.integrations.norg.model.NavEnhet
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class NorgController {
    companion object {
        val log by logger()
    }

    private val navEnheter = mutableMapOf<String, NavEnhet>()

    init {
        leggTilNavenhet(navEnheter, "1234", "Mock bydel, mock kommune")
        leggTilNavenhet(navEnheter, "1208", "NAV Årstad, Årstad kommune")
        leggTilNavenhet(navEnheter, "1209", "NAV Bergenhus, Bergen kommune")
        leggTilNavenhet(navEnheter, "1210", "NAV Ytrebygda, Bergen kommune")
    }

    @RequestMapping("/norg_endpoint_url/enhet/{enhetsnr}")
    fun getToken(@PathVariable enhetsnr: String): String {
        val navEnhet = navEnheter[enhetsnr] ?: lagMockNavEnhet(enhetsnr, "Generert mockenhet $enhetsnr, Mock kommune")
        log.info("Henter nav enhet for id: $enhetsnr")
        return objectMapper.writeValueAsString(navEnhet)
    }

    @RequestMapping("/norg_endpoint_url/kodeverk/EnhetstyperNorg")
    fun hentEnhetstyperDummy(): String {
        log.info("Henter EnhetstyperNorg")
        return "OK"
    }
}

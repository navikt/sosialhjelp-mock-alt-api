package no.nav.sbl.sosialhjelp_mock_alt.integrations.norg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.norg.NorgService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class NorgController(val norgService: NorgService) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/norg_endpoint_url/enhet")
    fun getAlleEnheter(@RequestParam enhetStatusListe: String): String {
        val navEnheter = norgService.getAlleNavenheter()
        log.info("Henter alle nav enheter: ${navEnheter.size} status: $enhetStatusListe")
        return objectMapper.writeValueAsString(navEnheter)
    }

    @GetMapping("/norg_endpoint_url/enhet/{enhetsnr}")
    fun getEnhet(@PathVariable enhetsnr: String): String {
        val navEnhet = norgService.getNavenhet(enhetsnr)
        log.info("Henter nav enhet for id: $enhetsnr")
        return objectMapper.writeValueAsString(navEnhet)
    }

    @GetMapping("/norg_endpoint_url/enhet/navkontor/{geografiskTilknytning}", produces = ["application/json;charset=UTF-8"])
    fun getEnhetForGt(@PathVariable geografiskTilknytning: String): String {
        val navEnhet = norgService.getNavenhet(geografiskTilknytning)
        log.info("Henter nav enhet for gt: $geografiskTilknytning")
        return objectMapper.writeValueAsString(navEnhet)
    }

    @GetMapping("/norg_endpoint_url/kodeverk/EnhetstyperNorg")
    fun hentEnhetstyperDummy(): String {
        log.info("Henter EnhetstyperNorg")
        return "OK"
    }
}

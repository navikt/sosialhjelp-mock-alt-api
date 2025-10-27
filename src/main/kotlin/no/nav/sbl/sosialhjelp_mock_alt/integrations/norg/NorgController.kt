package no.nav.sbl.sosialhjelp_mock_alt.integrations.norg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.norg.NorgService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class NorgController(private val norgService: NorgService, private val feilService: FeilService) {
  companion object {
    private val log by logger()
  }

  @GetMapping("/norg_endpoint_url/enhet", produces = ["application/json;charset=UTF-8"])
  fun getAlleEnheter(
      @RequestParam enhetStatusListe: String,
      @RequestHeader headers: HttpHeaders,
  ): String {
    feilService.eventueltLagFeilMedFnrFraToken(headers, "NorgController", "getAlleEnheter")
    val navEnheter = norgService.getAlleNavenheter()
    log.info("Henter alle nav enheter: ${navEnheter.size} status: $enhetStatusListe")
    return objectMapper.writeValueAsString(navEnheter)
  }

  @GetMapping("/norg_endpoint_url/enhet/{enhetsnr}", produces = ["application/json;charset=UTF-8"])
  fun getEnhet(@PathVariable enhetsnr: String, @RequestHeader headers: HttpHeaders): String {
    feilService.eventueltLagFeilMedFnrFraToken(headers, "NorgController", "getEnhet")
    val navEnhet = norgService.getNavenhet(enhetsnr)
    log.info("Henter nav enhet for id: $enhetsnr")
    return objectMapper.writeValueAsString(navEnhet)
  }

  @GetMapping(
      "/norg_endpoint_url/enhet/navkontor/{geografiskTilknytning}",
      produces = ["application/json;charset=UTF-8"],
  )
  fun getEnhetForGt(
      @PathVariable geografiskTilknytning: String,
      @RequestHeader headers: HttpHeaders,
  ): String {
    feilService.eventueltLagFeilMedFnrFraToken(headers, "NorgController", "getEnhetForGt")
    val navEnhet = norgService.getNavEnhetForGt(geografiskTilknytning)
    log.info("Henter nav enhet for gt: $geografiskTilknytning")
    return objectMapper.writeValueAsString(navEnhet)
  }

  @GetMapping("/norg_endpoint_url/kodeverk/EnhetstyperNorg")
  fun hentEnhetstyperDummy(): String {
    log.info("Henter EnhetstyperNorg")
    return "OK"
  }
}

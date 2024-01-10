package no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl

import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlAdresseSokService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlGeografiskTilknytningService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.HentGeografiskTilknytningRequest
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.HentPersonRequest
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.SokAdresseRequest
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PdlController(
    private val pdlService: PdlService,
    private val pdlAdresseSokService: PdlAdresseSokService,
    private val pdlGeografiskTilknytningService: PdlGeografiskTilknytningService,
    private val feilService: FeilService,
) {
  @PostMapping("/pdl_endpoint_url", produces = ["application/json;charset=UTF-8"])
  fun pdlEndpoint(
      @RequestParam parameters: MultiValueMap<String, String>,
      @RequestBody body: String,
      @RequestHeader headers: HttpHeaders,
  ): String {
    val ident = hentFnrFraToken(headers)
    return handleRequest(body, ident)
  }

  private fun handleRequest(body: String, ident: String): String {
    if (body.contains("hentIdenter")) {
      return "{\"data\":{\"hentIdenter\":{\"identer\":[{\"ident\":\"$ident\"}]}}}"
    }
    if (body.contains("ident") && body.contains("historikk")) {
      val hentPersonRequest = objectMapper.readValue(body, HentPersonRequest::class.java)
      return handleHentPersonRequest(hentPersonRequest, ident)
    }
    if (body.contains("hentGeografiskTilknytning")) {
      val hentGeografiskTilknytningRequest =
          objectMapper.readValue(body, HentGeografiskTilknytningRequest::class.java)
      return handleHentGeografiskTilknytningRequest(hentGeografiskTilknytningRequest, ident)
    }
    if (body.contains("paging") && body.contains("criteria")) {
      val sokAdresseRequest = objectMapper.readValue(body, SokAdresseRequest::class.java)
      return handleSokAdresseRequest(sokAdresseRequest, ident)
    }
    return "OK"
  }

  private fun handleHentPersonRequest(hentPersonRequest: HentPersonRequest, ident: String): String {
    if (ident != hentPersonRequest.variables.ident) {
      log.warn(
          "Token matcher ikke request! $ident (token) != ${hentPersonRequest.variables.ident} (person request)")
    }
    return decideResponse(hentPersonRequest)
  }

  /**
   * forelderBarnRelasjon -> kun del av person-request fra soknad-api folkeregisterpersonstatus ->
   * kun del av barn-request fra soknad-api bostedsadresse -> del av ektefelle-request fra
   * soknad-api (gjelder også de 2 over, men denne inneholder verken forelderBarnRelasjon eller
   * folkeregisterpersonstatus) kjoenn -> kun del av request fra modia-api navn -> del av request
   * fra innsyn-api (gjelder også 3 av de over, men denne inneholder verken forelderBarnRelasjon,
   * folkeregisterpersonstatus eller bostedsadresse) adressebeskyttelse -> tilgangskontroll-sjekk
   * kall fra soknad-api
   */
  private fun decideResponse(hentPersonRequest: HentPersonRequest): String {
    val fnr = hentPersonRequest.variables.ident
    return when {
      hentPersonRequest.query.contains(Regex("(forelderBarnRelasjon)")) -> {
        feilService.eventueltLagFeil(fnr, "PdlController", "getSoknadPerson")
        objectMapper.writeValueAsString(pdlService.getSoknadPersonResponseFor(fnr))
      }
      hentPersonRequest.query.contains(Regex("(folkeregisterpersonstatus)")) -> {
        feilService.eventueltLagFeil(fnr, "PdlController", "getSoknadBarn")
        objectMapper.writeValueAsString(pdlService.getSoknadBarnResponseFor(fnr))
      }
      hentPersonRequest.query.contains(Regex("(bostedsadresse)")) -> {
        feilService.eventueltLagFeil(fnr, "PdlController", "getSoknadEktefelle")
        objectMapper.writeValueAsString(pdlService.getSoknadEktefelleResponseFor(fnr))
      }
      hentPersonRequest.query.contains(Regex("(kjoenn)")) -> {
        feilService.eventueltLagFeil(fnr, "PdlController", "getModia")
        objectMapper.writeValueAsString(pdlService.getModiaResponseFor(fnr))
      }
      hentPersonRequest.query.contains(Regex("(navn)")) -> {
        feilService.eventueltLagFeil(fnr, "PdlController", "getInnsyn")
        objectMapper.writeValueAsString(pdlService.getInnsynResponseFor(fnr))
      }
      hentPersonRequest.query.contains(Regex("(adressebeskyttelse)")) -> {
        feilService.eventueltLagFeil(fnr, "PdlController", "getSoknadAdressebeskyttelse")
        objectMapper.writeValueAsString(pdlService.getSoknadAdressebeskyttelseResponseFor(fnr))
      }
      else -> "OK"
    }
  }

  private fun handleHentGeografiskTilknytningRequest(
      hentGeografiskTilknytningRequest: HentGeografiskTilknytningRequest,
      ident: String
  ): String {
    if (ident != hentGeografiskTilknytningRequest.variables.ident) {
      log.warn(
          "Token matcher ikke request! $ident (token) != ${hentGeografiskTilknytningRequest.variables.ident} (GT request)")
    }

    val response =
        pdlGeografiskTilknytningService.getGeografiskTilknytning(
            hentGeografiskTilknytningRequest.variables.ident)
    feilService.eventueltLagFeil(ident, "PdlController", "getGeografiskTilknytning")
    return objectMapper.writeValueAsString(response)
  }

  private fun handleSokAdresseRequest(sokAdresseRequest: SokAdresseRequest, ident: String): String {
    val criteria = sokAdresseRequest.variables.criteria
    val isWildcardSok: Boolean =
        criteria
            .firstOrNull { it.fieldName == "vegadresse.adressenavn" }
            ?.searchRule
            ?.contains("wildcard") ?: false

    feilService.eventueltLagFeil(ident, "PdlController", "getSokAdresse")

    return if (isWildcardSok) {
      val wildcardSokestreng =
          criteria
              .firstOrNull { it.fieldName == "vegadresse.adressenavn" }
              ?.searchRule
              ?.get("wildcard")
              ?.removeSuffix("*") ?: ""
      objectMapper.writeValueAsString(pdlAdresseSokService.getAdresse(wildcardSokestreng))
    } else {
      val adressenavn =
          criteria
              .firstOrNull { it.fieldName == "vegadresse.adressenavn" }
              ?.searchRule
              ?.get("contains") ?: ""
      val husnummer =
          criteria.firstOrNull { it.fieldName == "vegadresse.husnummer" }?.searchRule?.get("equals")
              ?: ""
      val husbokstav =
          criteria
              .firstOrNull { it.fieldName == "vegadresse.husbokstav" }
              ?.searchRule
              ?.get("equals") ?: ""
      objectMapper.writeValueAsString(
          pdlAdresseSokService.getAdresse(adressenavn + husnummer + husbokstav))
    }
  }

  companion object {
    private val log by logger()
  }
}

package no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl

import graphql.parser.Parser
import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlAdresseSokService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlGeografiskTilknytningService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.HentGeografiskTilknytningRequest
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.HentPersonRequest
import no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model.MockGraphQLRequest
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

  private fun getQuery(document: graphql.language.Document): String? {
    val operations = document.definitions.filterIsInstance<graphql.language.OperationDefinition>()
    val selections = operations.flatMap { it.selectionSet.selections }
    return selections.firstOrNull()?.let { it as graphql.language.Field }.let { it?.name }
  }

  @PostMapping("/pdl_endpoint_url", produces = ["application/json;charset=UTF-8"])
  fun pdlEndpoint(
      @RequestParam parameters: MultiValueMap<String, String>,
      @RequestBody body: String,
      @RequestHeader headers: HttpHeaders,
  ): String {
    val ident = hentFnrFraToken(headers)
    val request = objectMapper.readValue(body, MockGraphQLRequest::class.java)
    val document = Parser().parseDocument(request.query)
    val query = getQuery(document)

    return when (query) {
      "hentIdenter" -> "{\"data\":{\"hentIdenter\":{\"identer\":[{\"ident\":\"$ident\"}]}}}"
      "hentPerson" -> handleHentPersonRequest(body, ident)
      "hentGeografiskTilknytning" -> handleHentGeografiskTilknytningRequest(body, ident)
      "sokAdresse" -> handleSokAdresseRequest(body, ident)
      "forslagAdresse" -> handleForslagAdresseRequest(request.variables)
      else -> "OK"
    }
  }

  private fun handleForslagAdresseRequest(variables: Map<String, Any>): String {
    val fritekst = variables["fritekst"] ?: error("Fritekst mangler")
    require(fritekst is String) { "Fritekst må være en streng" }
    return objectMapper.writeValueAsString(
        mapOf(
            "data" to mapOf("forslagAdresse" to pdlAdresseSokService.forslagAdresse(fritekst)),
            "errors" to emptyList<String>()))
  }

  private fun handleHentPersonRequest(body: String, ident: String): String {
    val hentPersonRequest = objectMapper.readValue(body, HentPersonRequest::class.java)

    if (ident != hentPersonRequest.variables.ident) {
      log.warn(
          "Token matcher ikke request! $ident (token) != ${hentPersonRequest.variables.ident} (person request)")
    }
    return decideResponse(hentPersonRequest)
  }

  /**
   * forelderBarnRelasjon -> kun del av person-request fra soknad-api
   *
   * folkeregisterpersonstatus -> kun del av barn-request fra soknad-api
   *
   * bostedsadresse -> del av ektefelle-request fra soknad-api (gjelder også de 2 over, men denne
   * inneholder verken forelderBarnRelasjon eller folkeregisterpersonstatus)
   *
   * kjoenn -> kun del av request fra modia-api
   *
   * navn -> del av request fra innsyn-api (gjelder også 3 av de over, men denne inneholder verken
   * forelderBarnRelasjon, folkeregisterpersonstatus eller bostedsadresse)
   *
   * adressebeskyttelse -> tilgangskontroll-sjekk kall fra soknad-api
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

  private fun handleHentGeografiskTilknytningRequest(body: String, ident: String): String {
    val hentGeografiskTilknytningRequest =
        objectMapper.readValue(body, HentGeografiskTilknytningRequest::class.java)
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

  private fun handleSokAdresseRequest(body: String, ident: String): String {
    val sokAdresseRequest = objectMapper.readValue(body, SokAdresseRequest::class.java)

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

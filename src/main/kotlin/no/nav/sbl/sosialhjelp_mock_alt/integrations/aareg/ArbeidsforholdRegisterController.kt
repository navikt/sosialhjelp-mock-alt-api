package no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.AaregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.ArbeidsforholdDto
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraHeaders
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class ArbeidsforholdRegisterController(
    private val aaregService: AaregService,
) {

  companion object {
    private val log by logger()
  }

  @GetMapping("/aareg/v1/arbeidstaker/arbeidsforhold")
  // ?sporingsinformasjon=false&regelverk=A_ORDNINGEN&ansettelsesperiodeFom=2020-07-29&ansettelsesperiodeTom=2020-10-29
  fun getArbeidsforhold(
      @RequestHeader headers: HttpHeaders
  ): ResponseEntity<List<ArbeidsforholdDto>> {
    val fnr = hentFnrFraHeaders(headers)
    val arbeidsforhold = aaregService.getArbeidsforhold(fnr)
    log.info(
        "Henter aareg arbeidsforhold liste: ${objectMapper.writeValueAsString(arbeidsforhold)}")
    return ResponseEntity.ok(arbeidsforhold)
  }
}

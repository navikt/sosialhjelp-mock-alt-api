package no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg

import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.ArbeidsforholdDto
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.FssToken
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ArbeidsforholdRegisterController {
    companion object {
        val log by logger()
    }

    @RequestMapping("/aareg/")
    fun getAaregSts(@RequestHeader headers: HttpHeaders): ResponseEntity<FssToken> {
        val fnr = hentFnrFraToken(headers)
        val fssToken = FssToken.createToken(fnr)
        log.info("Henter aareg token: ${objectMapper.writeValueAsString(fssToken)}")
        return ResponseEntity.ok(fssToken)
    }

    @RequestMapping("/aareg/v1/arbeidstaker/arbeidsforhold")
    //?sporingsinformasjon=false&regelverk=A_ORDNINGEN&ansettelsesperiodeFom=2020-07-29&ansettelsesperiodeTom=2020-10-29
    fun getArbeidsforhold(@RequestHeader headers: HttpHeaders): ResponseEntity<List<ArbeidsforholdDto>> {
        val fnr = hentFnrFraToken(headers)
        val arbaidsforhold = listOf(ArbeidsforholdDto.opprettDummyForhold(fnr))
        log.info("Henter areg arbeidsforhold liste: ${objectMapper.writeValueAsString(arbaidsforhold)}")
        return ResponseEntity.ok(arbaidsforhold)
    }
}

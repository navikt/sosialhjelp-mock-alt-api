package no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks

import com.fasterxml.jackson.module.kotlin.readValue
import no.ks.fiks.svarut.klient.model.Forsendelse
import no.ks.fiks.svarut.klient.model.ForsendelsesId
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SvarUtService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.util.UUID

@RestController
class SvarUtController(
    private val svarUtService: SvarUtService
) {
    companion object {
        private val log by logger()
    }

    @PostMapping("/svarut/tjenester/api/forsendelse/v1/sendForsendelse")
    fun sendSoknad(
        request: StandardMultipartHttpServletRequest
    ): ResponseEntity<ForsendelsesId> {

        val multiFileMap = request.multiFileMap["filer"]!!
        val jsonSoknadPart: MultipartFile = multiFileMap.first { it.originalFilename == "soknad.json" }
        val jsonSoknad = objectMapper.readValue(jsonSoknadPart.inputStream, JsonSoknad::class.java)
        val fnr = jsonSoknad.data.personalia.personIdentifikator.verdi

        val forsendelseString = request.getParameter("forsendelse")
        val forsendelse = objectMapper.readValue<Forsendelse>(forsendelseString)

        svarUtService.addSvarUtSoknad(fnr, forsendelse, jsonSoknad)
        log.info("SvarUt-soknad med soknadId ${forsendelse.eksternReferanse.removePrefix("-")} har blitt lastet opp")

        return ResponseEntity.ok(ForsendelsesId(UUID.randomUUID()))
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.datastore

import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.DigisosSak
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.DigisosSoker
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.DokumentInfo
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.EttersendtInfoNAV
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.OriginalSoknadNAV
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.DigisosApiWrapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sbl.sosialhjelp_mock_alt.utils.toLocalDateTime
import no.nav.sbl.sosialhjelp_mock_alt.utils.unixToLocalDateTime
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.collections.HashMap

@Service
class SoknadService {
    companion object {
        val log by logger()
    }

    val soknadsliste: HashMap<String, DigisosSak> = HashMap()

    fun hentSoknad(fiksDigisosId: String): String? {
        log.info("Henter søknad med fiksDigisosId: $fiksDigisosId")
        val soknad = soknadsliste.get(fiksDigisosId) ?: return null
        return objectMapper.writeValueAsString(soknad)
    }

    fun listSoknader(): String {
        log.info("Henter søknadsliste. Antll soknader: ${soknadsliste.size}")
        return objectMapper.writeValueAsString(soknadsliste.values)
    }

    fun oppdaterDigisosSak(fiksDigisosId: String?, digisosApiWrapper: DigisosApiWrapper): String? {
        val dokumentlagerId = UUID.randomUUID().toString()
        //fiksClientMock.postDokument(dokumentlagerId, digisosApiWrapper.sak.soker)
        var id = fiksDigisosId
        if (id == null) {
            id = UUID.randomUUID().toString()
        }

        if (soknadsliste.get(id) == null) {
            log.info("Oppretter søknad med id: $id")
            soknadsliste.put(id, DigisosSak(
                    fiksDigisosId = id,
                    sokerFnr = "01234567890",
                    fiksOrgId = "11415cd1-e26d-499a-8421-751457dfcbd5",
                    kommunenummer = "1",
                    sistEndret = System.currentTimeMillis(),
                    originalSoknadNAV = OriginalSoknadNAV(
                            navEksternRefId = "110000000",
                            metadata = "",
                            vedleggMetadata = "mock-soknad-vedlegg-metadata",
                            soknadDokument = DokumentInfo("", "", 0L),
                            vedlegg = Collections.emptyList(),
                            timestampSendt = femMinutterForMottattSoknad(digisosApiWrapper)),
                    ettersendtInfoNAV = EttersendtInfoNAV(Collections.emptyList()),
                    digisosSoker = null))
        } else {
            log.info("Oppdaterer søknad med id: $id")
            oppdaterOriginalSoknadNavHvisTimestampSendtIkkeErFoerTidligsteHendelse(id, digisosApiWrapper)
        }

        val digisosSak = hentSak(id)
        val updatedDigisosSak = digisosSak.updateDigisosSoker(DigisosSoker(dokumentlagerId, Collections.emptyList(), System.currentTimeMillis()))
        soknadsliste.put(id, updatedDigisosSak)
        log.debug("Lagret søknad med id: $id sak: {${updatedDigisosSak}}")
        return id
    }

    private fun hentSak(id: String?): DigisosSak {
        log.debug("Henter søknad med id: $id")
        return soknadsliste.get(id) ?: throw RuntimeException("Umulig exception! Finner ikke sak med id: $id")
    }

    private fun femMinutterForMottattSoknad(digisosApiWrapper: DigisosApiWrapper): Long {
        val mottattTidspunkt = digisosApiWrapper.sak.soker.hendelser.minBy { it.hendelsestidspunkt }!!.hendelsestidspunkt
        return try {
            mottattTidspunkt.toLocalDateTime().minusMinutes(5).atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            LocalDateTime.now().minusMinutes(5).atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()
        }
    }

    private fun oppdaterOriginalSoknadNavHvisTimestampSendtIkkeErFoerTidligsteHendelse(id: String, digisosApiWrapper: DigisosApiWrapper) {
        val digisosSak = hentSak(id)
        val timestampSendt = digisosSak.originalSoknadNAV!!.timestampSendt
        val tidligsteHendelsetidspunkt = digisosApiWrapper.sak.soker.hendelser.minBy { it.hendelsestidspunkt }!!.hendelsestidspunkt
        if (unixToLocalDateTime(timestampSendt).isAfter(tidligsteHendelsetidspunkt.toLocalDateTime())) {
            val oppdatertDigisosSak = digisosSak.updateOriginalSoknadNAV(digisosSak.originalSoknadNAV.copy(timestampSendt = femMinutterForMottattSoknad(digisosApiWrapper)))
            soknadsliste[id] = oppdatertDigisosSak
        }
    }

    fun DigisosSak.updateDigisosSoker(digisosSoker: DigisosSoker): DigisosSak {
        return this.copy(digisosSoker = digisosSoker)
    }

    fun DigisosSak.updateOriginalSoknadNAV(originalSoknadNAV: OriginalSoknadNAV): DigisosSak {
        return this.copy(originalSoknadNAV = originalSoknadNAV)
    }
}
package no.nav.sbl.sosialhjelp_mock_alt.datastore

import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.DigisosSak
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.DigisosSoker
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.DokumentInfo
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.EttersendtInfoNAV
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.OriginalSoknadNAV
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.VedleggMetadata
import no.nav.sbl.sosialhjelp_mock_alt.datastore.model.defaultJsonSoknad
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
    val dokumentLager: HashMap<String, String> = HashMap() // Lagres som rå json

    fun hentSoknad(fiksDigisosId: String): String? {
        log.info("Henter søknad med fiksDigisosId: $fiksDigisosId")
        val soknad = soknadsliste.get(fiksDigisosId) ?: return null
        log.debug(soknad.toString())
        return objectMapper.writeValueAsString(soknad)
    }

    fun listSoknader(): String {
        log.info("Henter søknadsliste. Antll soknader: ${soknadsliste.size}")
        return objectMapper.writeValueAsString(soknadsliste.values)
    }

    fun oppdaterDigisosSak(fiksDigisosIdInput: String?, digisosApiWrapper: DigisosApiWrapper): String? {
        var fiksDigisosId = fiksDigisosIdInput
        if (fiksDigisosId == null) {
            fiksDigisosId = UUID.randomUUID().toString()
        }

        val oldSoknad = soknadsliste.get(fiksDigisosId)
        if (oldSoknad == null) {
            log.info("Oppretter søknad med id: $fiksDigisosId")
            val vedleggMetadataId = UUID.randomUUID().toString()
            val digisosSak = DigisosSak(
                    fiksDigisosId = fiksDigisosId,
                    sokerFnr = "01234567890",
                    fiksOrgId = "11415cd1-e26d-499a-8421-751457dfcbd5",
                    kommunenummer = "1",
                    sistEndret = System.currentTimeMillis(),
                    originalSoknadNAV = OriginalSoknadNAV(
                            navEksternRefId = "110000000",
                            metadata = fiksDigisosId,
                            vedleggMetadata = vedleggMetadataId,
                            soknadDokument = DokumentInfo("", "", 0L),
                            vedlegg = Collections.emptyList(),
                            timestampSendt = femMinutterForMottattSoknad(digisosApiWrapper)),
                    ettersendtInfoNAV = EttersendtInfoNAV(Collections.emptyList()),
                    digisosSoker = null)
            val dokumentlagerId = UUID.randomUUID().toString()
            log.info("Lagrer søker dokument med dokumentlagerId: $dokumentlagerId")
            dokumentLager.put(dokumentlagerId, objectMapper.writeValueAsString(digisosApiWrapper.sak.soker))
            val updatedDigisosSak = digisosSak.updateDigisosSoker(DigisosSoker(dokumentlagerId, Collections.emptyList(), System.currentTimeMillis()))
            log.info("Lagrer søknad fiksDigisosId: $fiksDigisosId")
            log.debug(updatedDigisosSak.toString())
            soknadsliste.put(fiksDigisosId, updatedDigisosSak)
            log.info("Lagrer orginalsøknad (med bare default verdier) med dokumentlagerId: $fiksDigisosId")
            log.debug(defaultJsonSoknad.toString())
            val orginalSoknad = objectMapper.writeValueAsString(defaultJsonSoknad)
            dokumentLager.put(fiksDigisosId, orginalSoknad)
            log.info("Lagrer vedleggs metadata med dokumentlagerId: $vedleggMetadataId")
            val vedleggMetadata = VedleggMetadata("soknad.json", "application/json", orginalSoknad.length.toLong())
            dokumentLager.put(vedleggMetadataId, objectMapper.writeValueAsString(vedleggMetadata))
        } else {
            log.info("Oppdaterer søknad med id: $fiksDigisosId")
            oppdaterOriginalSoknadNavHvisTimestampSendtIkkeErFoerTidligsteHendelse(fiksDigisosId, digisosApiWrapper)
            val dokumentlagerId = UUID.randomUUID().toString()
            log.info("Lagrer/oppdaterer søker dokument med dokumentlagerId: ${dokumentlagerId}")
            dokumentLager.put(dokumentlagerId, objectMapper.writeValueAsString(digisosApiWrapper.sak.soker))
            val updatedDigisosSak = oldSoknad.updateDigisosSoker(DigisosSoker(dokumentlagerId, Collections.emptyList(), System.currentTimeMillis()))
            soknadsliste.replace(fiksDigisosId, updatedDigisosSak)
        }
        return fiksDigisosId
    }

    private fun hentSak(id: String?): DigisosSak {
        log.debug("Henter sak med id: $id")
        return soknadsliste.get(id) ?: throw RuntimeException("Finner ikke sak med id: $id")
    }

    fun hentDokumenter(digisosId: String): String? {
        log.debug("Henter alle dokumenter for søknad med id: $digisosId")
        val soknad = soknadsliste.get(digisosId) ?: throw RuntimeException("Finner ikke sak med id: $digisosId")
        return objectMapper.writeValueAsString(soknad.digisosSoker!!.dokumenter)
    }

    fun hentDokument(digisosId: String, dokumentlagerId: String): String? {
        log.debug("Henter dokument med id: $dokumentlagerId")
        return dokumentLager[dokumentlagerId] // Allerede lagret som json
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
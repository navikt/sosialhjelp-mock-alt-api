package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSaksStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.DigisosApiWrapper
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.SakWrapper
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.VedleggMetadata
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.defaultJsonSoknad
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sbl.sosialhjelp_mock_alt.utils.toLocalDateTime
import no.nav.sbl.sosialhjelp_mock_alt.utils.unixToLocalDateTime
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DigisosSoker
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import no.nav.sosialhjelp.api.fiks.Ettersendelse
import no.nav.sosialhjelp.api.fiks.EttersendtInfoNAV
import no.nav.sosialhjelp.api.fiks.OriginalSoknadNAV
import no.nav.sosialhjelp.api.fiks.Tilleggsinformasjon
import org.joda.time.DateTime
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.Collections
import java.util.UUID
import kotlin.collections.get
import kotlin.collections.set

const val SOKNAD_DEFAULT_TITTEL = "Søknad om økonomisk sosialhjelp"

@Service
class SoknadService {
    companion object {
        val log by logger()
    }

    final val ettersendelseFilnavn = "ettersendelse.pdf"
    val soknadsliste: HashMap<String, DigisosSak> = HashMap()
    val dokumentLager: HashMap<String, String> = HashMap() // Lagres som rå json
    val fillager: FixedFileStrorage = FixedFileStrorage()

    fun hentSoknad(fiksDigisosId: String): DigisosSak? {
        log.info("Henter søknad med fiksDigisosId: $fiksDigisosId")
        val soknad = soknadsliste[fiksDigisosId] ?: return null
        log.debug(soknad.toString())
        return soknad
    }

    fun listSoknader(fnr: String?): MutableCollection<DigisosSak> {
//        if(fnr == null) {
        log.info("Henter søknadsliste. Antall soknader: ${soknadsliste.size}")
        return soknadsliste.values
//        }
//        val soknadslisteForFnr = soknadsliste.values.filter{it.sokerFnr.equals(fnr)}
//        log.info("Henter søknadsliste. Antall soknader for $fnr: ${soknadslisteForFnr.size}")
//        return soknadslisteForFnr
    }

    fun opprettDigisosSak(fiksOrgId: String, kommuneNr: String, fnr: String, id: String) {
        val digisosApiWrapper = DigisosApiWrapper(SakWrapper(JsonDigisosSoker()), "")
        digisosApiWrapper.sak.soker.hendelser.add(JsonSoknadsStatus()
                .withHendelsestidspunkt(DateTime.now().toDateTimeISO().toString())
                .withType(JsonHendelse.Type.SOKNADS_STATUS).withStatus(JsonSoknadsStatus.Status.MOTTATT))
        oppdaterDigisosSak(kommuneNr = kommuneNr, fiksOrgId = fiksOrgId,
                fnr = fnr, fiksDigisosIdInput = id, digisosApiWrapper = digisosApiWrapper)
    }

    fun oppdaterDigisosSak(
            kommuneNr: String,
            fiksOrgId: String?,
            fnr: String,
            fiksDigisosIdInput: String?,
            digisosApiWrapper: DigisosApiWrapper,
            enhetsnummer: String = "0315",
            jsonSoknad: JsonSoknad? = null,
            jsonVedlegg: JsonVedleggSpesifikasjon? = null,
            dokumenter: MutableList<DokumentInfo> = mutableListOf(),
            soknadDokument: DokumentInfo? = null
    ): String? {
        var fiksDigisosId = fiksDigisosIdInput
        if (fiksDigisosId == null) {
            fiksDigisosId = UUID.randomUUID().toString()
        }

        val metadataId = UUID.randomUUID().toString()

        val oldSoknad = soknadsliste.get(fiksDigisosId)
        if (oldSoknad == null) {
            log.info("Oppretter søknad med id: $fiksDigisosId")
            val vedleggMetadataId = UUID.randomUUID().toString()
            val digisosSak = DigisosSak(
                    fiksDigisosId = fiksDigisosId,
                    sokerFnr = fnr,
                    fiksOrgId = fiksOrgId ?: "",
                    kommunenummer = kommuneNr,
                    sistEndret = System.currentTimeMillis(),
                    originalSoknadNAV = OriginalSoknadNAV(
                            navEksternRefId = "110000000",
                            metadata = metadataId,
                            vedleggMetadata = vedleggMetadataId,
                            soknadDokument = soknadDokument ?: DokumentInfo("", "", 0L),
                            vedlegg = Collections.emptyList(),
                            timestampSendt = femMinutterForMottattSoknad(digisosApiWrapper)),
                    ettersendtInfoNAV = EttersendtInfoNAV(Collections.emptyList()),
                    digisosSoker = null,
                    tilleggsinformasjon = Tilleggsinformasjon(
                            enhetsnummer = enhetsnummer
                    )
            )
            val dokumentlagerId = UUID.randomUUID().toString()
            log.info("Lagrer søker dokument med dokumentlagerId: $dokumentlagerId")
            dokumentLager[dokumentlagerId] = objectMapper.writeValueAsString(digisosApiWrapper.sak.soker)
            val updatedDigisosSak = digisosSak.updateDigisosSoker(DigisosSoker(dokumentlagerId, dokumenter, System.currentTimeMillis()))
            log.info("Lagrer søknad fiksDigisosId: $fiksDigisosId")
            log.debug(updatedDigisosSak.toString())
            soknadsliste[fiksDigisosId] = updatedDigisosSak
            log.info("Lagrer orginalsøknad (med bare default verdier) med dokumentlagerId: $fiksDigisosId")
            val soknad = jsonSoknad ?: defaultJsonSoknad(fnr)
            log.debug(soknad.toString())
            val orginalSoknad = objectMapper.writeValueAsString(soknad)
            dokumentLager[metadataId] = orginalSoknad
            log.info("Lagrer vedleggs metadata med dokumentlagerId: $vedleggMetadataId")
            val vedlegg = jsonVedlegg ?: defaultVedleggMetadata()
            dokumentLager[vedleggMetadataId] = objectMapper.writeValueAsString(vedlegg)
        } else {
            log.info("Oppdaterer søknad med id: $fiksDigisosId")
            oppdaterOriginalSoknadNavHvisTimestampSendtIkkeErFoerTidligsteHendelse(fiksDigisosId, digisosApiWrapper)
            val dokumentlagerId = UUID.randomUUID().toString()
            log.info("Lagrer/oppdaterer søker dokument med dokumentlagerId: $dokumentlagerId")
            dokumentLager[dokumentlagerId] = objectMapper.writeValueAsString(digisosApiWrapper.sak.soker)
            val updatedDigisosSak = oldSoknad.updateDigisosSoker(DigisosSoker(dokumentlagerId, Collections.emptyList(), System.currentTimeMillis()))
            soknadsliste.replace(fiksDigisosId, updatedDigisosSak)
        }
        return fiksDigisosId
    }

    private fun defaultVedleggMetadata(): JsonVedleggSpesifikasjon {
        return JsonVedleggSpesifikasjon()
    }

    private fun leggVedleggTilISak(id: String, nyttVedlegg: VedleggMetadata, dokumentId: String, timestamp: Long) {
        if (!nyttVedlegg.filnavn!!.contentEquals(ettersendelseFilnavn)) {
            val digisosSak = hentSak(id)
            val idNumber = (digisosSak.ettersendtInfoNAV!!.ettersendelser.size + 1).toString().padStart(4, '0')
            val navEksternRefId = "ettersendelseNavEksternRef$idNumber"
            val dokumentInfo = DokumentInfo(nyttVedlegg.filnavn, dokumentId, nyttVedlegg.storrelse)
            val ettersendelse = Ettersendelse(navEksternRefId, dokumentId, listOf(dokumentInfo), timestamp)
            val nyListe: List<Ettersendelse> = listOf(digisosSak.ettersendtInfoNAV!!.ettersendelser, listOf(ettersendelse)).flatten()

            val updatedDigisosSak = digisosSak.updateEttersendtInfoNAV(EttersendtInfoNAV(nyListe))
            soknadsliste[id] = updatedDigisosSak
        }
    }

    private fun hentSak(id: String?): DigisosSak {
        log.debug("Henter sak med id: $id")
        return soknadsliste[id] ?: throw MockAltException("Finner ikke sak med id: $id")
    }

    fun hentFil(dokumentlagerId: String): FileEntry? {
        log.debug("Henter fil med id: $dokumentlagerId")
        return fillager.find(dokumentlagerId)
    }

    fun hentDokument(digisosId: String?, dokumentlagerId: String): String? {
        log.debug("Henter dokument med id: $dokumentlagerId")
        return dokumentLager[dokumentlagerId] // Allerede lagret som json
    }

    private fun femMinutterForMottattSoknad(digisosApiWrapper: DigisosApiWrapper): Long {
        val mottattTidspunkt = digisosApiWrapper.sak.soker.hendelser.minByOrNull { it.hendelsestidspunkt }!!.hendelsestidspunkt
        return try {
            mottattTidspunkt.toLocalDateTime().minusMinutes(5).atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            LocalDateTime.now().minusMinutes(5).atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()
        }
    }

    private fun oppdaterOriginalSoknadNavHvisTimestampSendtIkkeErFoerTidligsteHendelse(id: String, digisosApiWrapper: DigisosApiWrapper) {
        val digisosSak = hentSak(id)
        val timestampSendt = digisosSak.originalSoknadNAV!!.timestampSendt
        val tidligsteHendelsetidspunkt = digisosApiWrapper.sak.soker.hendelser.minByOrNull { it.hendelsestidspunkt }!!.hendelsestidspunkt
        if (unixToLocalDateTime(timestampSendt).isAfter(tidligsteHendelsetidspunkt.toLocalDateTime())) {
            val oppdatertDigisosSak = digisosSak.updateOriginalSoknadNAV(digisosSak.originalSoknadNAV!!.copy(timestampSendt = femMinutterForMottattSoknad(digisosApiWrapper)))
            soknadsliste[id] = oppdatertDigisosSak
        }
    }

    fun DigisosSak.updateDigisosSoker(digisosSoker: DigisosSoker): DigisosSak {
        return this.copy(digisosSoker = digisosSoker)
    }

    fun DigisosSak.updateOriginalSoknadNAV(originalSoknadNAV: OriginalSoknadNAV): DigisosSak {
        return this.copy(originalSoknadNAV = originalSoknadNAV)
    }

    fun DigisosSak.updateEttersendtInfoNAV(ettersendtInfoNAV: EttersendtInfoNAV): DigisosSak {
        return this.copy(ettersendtInfoNAV = ettersendtInfoNAV)
    }

    fun lastOppFil(
            fiksDigisosId: String,
            vedleggMetadata: VedleggMetadata,
            vedleggsJson: JsonVedleggSpesifikasjon? = null,
            timestamp: Long = DateTime.now().millis,
            file: MultipartFile? = null,
    ): String {
        val vedleggsId = UUID.randomUUID().toString()
        var vedleggsInfo: JsonVedlegg? = null
        var sha512 = "dummySha512"
        if (vedleggsJson != null && !vedleggMetadata.filnavn!!.contentEquals(ettersendelseFilnavn)) {
            vedleggsInfo = vedleggsJson.vedlegg.firstOrNull { jsonVedlegg ->
                jsonVedlegg.filer.any { it.filnavn!!.contentEquals(vedleggMetadata.filnavn) }
            }
            if (vedleggsInfo != null) {
                sha512 = vedleggsInfo.filer.first { it.filnavn!!.contentEquals(vedleggMetadata.filnavn) }.sha512
            }
        }
        dokumentLager[vedleggsId] = objectMapper.writeValueAsString(
                JsonVedleggSpesifikasjon()
                        .withVedlegg(listOf(JsonVedlegg()
                                .withType(vedleggsInfo?.type ?: "annet")
                                .withTilleggsinfo(vedleggsInfo?.tilleggsinfo)
                                .withStatus(vedleggsInfo?.status ?: "LastetOpp")
                                .withFiler(listOf(JsonFiler()
                                        .withFilnavn(vedleggMetadata.filnavn)
                                        .withSha512(sha512)))
                        ))
        )
        leggVedleggTilISak(fiksDigisosId, vedleggMetadata, vedleggsId, timestamp)
        if (file != null) {
            fillager.add(vedleggsId, vedleggMetadata.filnavn ?: file.name, file.bytes)
        }
        log.info("Lastet opp fil fiksDigisosId: $fiksDigisosId, filnavn: ${vedleggMetadata.filnavn}, vedleggsId: $vedleggsId")
        return vedleggsId
    }

    fun leggInnIDokumentlager(
            filnavn: String,
            bytes: ByteArray,
            vedleggsId: String = UUID.randomUUID().toString(),
    ): String {
        fillager.add(vedleggsId, filnavn, bytes)
        return vedleggsId
    }

    fun hentSoknadstittel(fiksDigisosId: String): String {
        val digisosSak = soknadsliste[fiksDigisosId]
        val soknadString = dokumentLager[digisosSak!!.digisosSoker!!.metadata]
        val soknad = objectMapper.readValue(soknadString, JsonDigisosSoker::class.java)
        val saksTittelMap = HashMap<String, String>()
        soknad.hendelser.filter { it.type == JsonHendelse.Type.SAKS_STATUS }
                .forEach {
                    if (it is JsonSaksStatus) {
                        saksTittelMap[it.referanse] = it.tittel
                    }
                }
        if(saksTittelMap.isNotEmpty()) {
            return saksTittelMap.values.joinToString()
        }
        return SOKNAD_DEFAULT_TITTEL
    }
}

class FixedFileStrorage {
    private val maxSize = 200
    private val items: MutableList<FileEntry> = mutableListOf()

    fun add(key: String, fileName: String, bytes: ByteArray) {
        while (items.size >= maxSize) {
            items.removeAt(0)
        }
        items.add(FileEntry(key, fileName, bytes))
    }

    fun find(key: String): FileEntry? {
        return items.findLast { it.key == key }
    }
}

class FileEntry(val key: String, val filnavn: String, val bytes: ByteArray)

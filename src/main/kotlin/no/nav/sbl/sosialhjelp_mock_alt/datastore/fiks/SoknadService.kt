package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID
import kotlin.collections.set
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSaksStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.dokumentlager.Dokumentlager
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.DigisosApiWrapper
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.SakWrapper
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.VedleggMetadata
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.defaultJsonSoknad
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendVedlegg
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
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

const val SOKNAD_DEFAULT_TITTEL = "Søknad om økonomisk sosialhjelp"

@Service
class SoknadService(
    @Value("\${filter_soknader_on_fnr}") private val filter_soknader_on_fnr: Boolean,
    private val dokumentlager: Dokumentlager,
) {
    companion object {
        val log by logger()
    }

    private val ettersendelseFilnavn = "ettersendelse.pdf"
    private val soknadsliste: HashMap<String, DigisosSak> = HashMap()
    //  private val dokumentLager: HashMap<String, String> = HashMap() // Lagres som rå json
    private val fillager: FixedFileStorage = FixedFileStorage()
    private val ettersendelsePdfLager: FixedFileStorage = FixedFileStorage()

    fun hentSoknad(fiksDigisosId: String): DigisosSak? {
        log.info("Henter søknad med fiksDigisosId: $fiksDigisosId")
        val soknad = soknadsliste[fiksDigisosId] ?: return null
        log.debug(soknad.toString())
        return soknad
    }

    fun listSoknader(fnr: String?): MutableCollection<DigisosSak> {
        if (fnr == null) {
            log.info("Henter søknadsliste. Antall soknader: ${soknadsliste.size}")
            return soknadsliste.values
        }
        val soknadslisteForFnr = soknadsliste.values.filter { it.sokerFnr == fnr }.toMutableList()
        log.info("Henter søknadsliste. Antall soknader for $fnr: ${soknadslisteForFnr.size}")
        if (filter_soknader_on_fnr) {
            return soknadslisteForFnr
        }
        log.info("- returnerer fortsatt alle. Antall soknader: ${soknadsliste.size}")
        return soknadsliste.values
    }

    fun opprettDigisosSak(fiksOrgId: String, kommuneNr: String, fnr: String, id: String) {
        val digisosApiWrapper = DigisosApiWrapper(SakWrapper(JsonDigisosSoker()), "")
        var hendelsestidspunkt = ZonedDateTime.now(ZoneOffset.UTC)
        if (id == "15months") {
            hendelsestidspunkt = hendelsestidspunkt.minusMonths(15)
        }
        digisosApiWrapper.sak.soker.hendelser.add(
            JsonSoknadsStatus()
                .withHendelsestidspunkt(hendelsestidspunkt.format(DateTimeFormatter.ISO_INSTANT))
                .withType(JsonHendelse.Type.SOKNADS_STATUS)
                .withStatus(JsonSoknadsStatus.Status.MOTTATT))
        oppdaterDigisosSak(
            kommuneNr = kommuneNr,
            fiksOrgId = fiksOrgId,
            fnr = fnr,
            fiksDigisosIdInput = id,
            digisosApiWrapper = digisosApiWrapper)
    }

    fun hentVedlegg(sak: DigisosSak): List<FrontendVedlegg> {
        val initialVedlegg = sak.digisosSoker?.dokumenter?.map { toVedlegg(it) } ?: emptyList()
        val ettersendteVedlegg =
            sak.ettersendtInfoNAV?.ettersendelser?.flatMap { ettersendelse ->
                ettersendelse.vedlegg.map { toVedlegg(it) }
            } ?: emptyList()

        return initialVedlegg + ettersendteVedlegg
    }

    private fun toVedlegg(dokument: DokumentInfo): FrontendVedlegg {
        val kanLastesned = hentFil(dokument.dokumentlagerDokumentId) != null
        return FrontendVedlegg(
            dokument.filnavn, dokument.dokumentlagerDokumentId, dokument.storrelse, kanLastesned)
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
        soknadDokument: DokumentInfo? = null,
        isPapirSoknad: Boolean = false,
    ): String? {
        var fiksDigisosId = fiksDigisosIdInput
        if (fiksDigisosId == null) {
            fiksDigisosId = UUID.randomUUID().toString()
        }

        val metadataId = UUID.randomUUID().toString()

        val oldSoknad = soknadsliste[fiksDigisosId]
        if (oldSoknad == null) {
            log.info("Oppretter søknad med id: $fiksDigisosId")
            var sistEndret = System.currentTimeMillis()
            if (fiksDigisosId == "15months") {
                sistEndret = DateTime.now().minusMonths(15).millis
            }
            val vedleggMetadataId = UUID.randomUUID().toString()
            val digisosSak =
                DigisosSak(
                    fiksDigisosId = fiksDigisosId,
                    sokerFnr = fnr,
                    fiksOrgId = fiksOrgId ?: "",
                    kommunenummer = kommuneNr,
                    sistEndret = sistEndret,
                    originalSoknadNAV =
                        if (isPapirSoknad) {
                            null
                        } else {
                            OriginalSoknadNAV(
                                navEksternRefId = "110000000",
                                metadata = metadataId,
                                vedleggMetadata = vedleggMetadataId,
                                soknadDokument = soknadDokument ?: DokumentInfo("", "", 0L),
                                vedlegg = emptyList(),
                                timestampSendt = femMinutterForMottattSoknad(digisosApiWrapper))
                        },
                    ettersendtInfoNAV = EttersendtInfoNAV(emptyList()),
                    digisosSoker = null,
                    tilleggsinformasjon = Tilleggsinformasjon(enhetsnummer = enhetsnummer))
            val dokumentlagerId = UUID.randomUUID().toString()
            log.info("Lagrer søker dokument med dokumentlagerId: $dokumentlagerId")

            dokumentlager.save(dokumentlagerId, objectMapper.writeValueAsString(digisosApiWrapper.sak.soker))

            val oppdatertTidspunkt =
                unixToLocalDateTime(sistEndret)
                    .plusSeconds(1)
                    .atZone(ZoneId.of("Europe/Oslo"))
                    .toInstant()
                    .toEpochMilli()
            val updatedDigisosSak =
                digisosSak.updateDigisosSoker(
                    DigisosSoker(dokumentlagerId, dokumenter, oppdatertTidspunkt))
            log.info("Lagrer søknad fiksDigisosId: $fiksDigisosId")
            log.debug(updatedDigisosSak.toString())
            soknadsliste[fiksDigisosId] = updatedDigisosSak
            log.info(
                "Lagrer orginalsøknad (med bare default verdier) med dokumentlagerId: $fiksDigisosId")
            val soknad = jsonSoknad ?: defaultJsonSoknad(fnr)
            log.debug(soknad.toString())
            val orginalSoknad = objectMapper.writeValueAsString(soknad)

            dokumentlager.save(metadataId, orginalSoknad)

            log.info("Lagrer vedleggs metadata med dokumentlagerId: $vedleggMetadataId")
            val vedlegg = jsonVedlegg ?: defaultVedleggMetadata()

            dokumentlager.save(vedleggMetadataId, objectMapper.writeValueAsString(vedlegg))
        } else {
            log.info("Oppdaterer søknad med id: $fiksDigisosId")
            oppdaterOriginalSoknadNavHvisTimestampSendtIkkeErFoerTidligsteHendelse(
                fiksDigisosId, digisosApiWrapper)
            var dokumentlagerId = oldSoknad.digisosSoker?.metadata
            if (dokumentlagerId == null) {
                dokumentlagerId = UUID.randomUUID().toString()
                log.info("Lag nytt søker dokument med dokumentlagerId: $dokumentlagerId")

                dokumentlager.save(dokumentlagerId, objectMapper.writeValueAsString(digisosApiWrapper.sak.soker))

                val updatedDigisosSak =
                    oldSoknad.updateDigisosSoker(
                        DigisosSoker(dokumentlagerId, emptyList(), System.currentTimeMillis()))
                soknadsliste.replace(fiksDigisosId, updatedDigisosSak)
            } else {
                log.info("Oppdaterer søker dokument med dokumentlagerId: $dokumentlagerId")
                oldSoknad.digisosSoker?.copy(timestampSistOppdatert = System.currentTimeMillis())?.let {
                    soknadsliste.replace(fiksDigisosId, oldSoknad.updateDigisosSoker(it))
                }

                dokumentlager.save(dokumentlagerId, objectMapper.writeValueAsString(digisosApiWrapper.sak.soker))
            }
        }
        return fiksDigisosId
    }

    private fun defaultVedleggMetadata(): JsonVedleggSpesifikasjon {
        return JsonVedleggSpesifikasjon()
    }

    private fun leggVedleggTilISak(
        id: String,
        nyttVedlegg: VedleggMetadata,
        dokumentId: String,
        timestamp: Long
    ) {
        if (!nyttVedlegg.filnavn!!.contentEquals(ettersendelseFilnavn)) {
            val digisosSak = hentSak(id)
            val idNumber =
                (digisosSak.ettersendtInfoNAV!!.ettersendelser.size + 1).toString().padStart(4, '0')
            val navEksternRefId = "ettersendelseNavEksternRef$idNumber"
            val dokumentInfo = DokumentInfo(nyttVedlegg.filnavn, dokumentId, nyttVedlegg.storrelse)
            val ettersendelse =
                Ettersendelse(navEksternRefId, dokumentId, listOf(dokumentInfo), timestamp)
            val nyListe: List<Ettersendelse> =
                listOf(digisosSak.ettersendtInfoNAV!!.ettersendelser, listOf(ettersendelse)).flatten()

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

    fun hentEttersendelsePdf(fiksDigisosId: String): FileEntry? {
        log.debug("Henter $ettersendelseFilnavn for id: $fiksDigisosId")
        return ettersendelsePdfLager.find(fiksDigisosId)
    }

    fun hentDokument(digisosId: String?, dokumentlagerId: String): String? {
        log.debug("Henter dokument med id: $dokumentlagerId")
        return dokumentlager.get(dokumentlagerId) // Allerede lagret som json
    }

    private fun femMinutterForMottattSoknad(digisosApiWrapper: DigisosApiWrapper): Long {
        val mottattTidspunkt =
            digisosApiWrapper.sak.soker.hendelser
                .minByOrNull { it.hendelsestidspunkt }!!
                .hendelsestidspunkt
        return try {
            mottattTidspunkt
                .toLocalDateTime()
                .minusMinutes(5)
                .atZone(ZoneId.of("Europe/Oslo"))
                .toInstant()
                .toEpochMilli()
        } catch (e: DateTimeParseException) {
            LocalDateTime.now()
                .minusMinutes(5)
                .atZone(ZoneId.of("Europe/Oslo"))
                .toInstant()
                .toEpochMilli()
        }
    }

    private fun oppdaterOriginalSoknadNavHvisTimestampSendtIkkeErFoerTidligsteHendelse(
        id: String,
        digisosApiWrapper: DigisosApiWrapper,
    ) {
        val digisosSak = hentSak(id)
        val timestampSendt = digisosSak.originalSoknadNAV?.timestampSendt
        val tidligsteHendelsetidspunkt =
            digisosApiWrapper.sak.soker.hendelser
                .minByOrNull { it.hendelsestidspunkt }!!
                .hendelsestidspunkt
        if (timestampSendt != null &&
            unixToLocalDateTime(timestampSendt).isAfter(tidligsteHendelsetidspunkt.toLocalDateTime())) {
            val oppdatertDigisosSak =
                digisosSak.updateOriginalSoknadNAV(
                    digisosSak.originalSoknadNAV!!.copy(
                        timestampSendt = femMinutterForMottattSoknad(digisosApiWrapper)))
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
        if (vedleggsJson != null) {
            if (vedleggMetadata.filnavn!!.contentEquals(ettersendelseFilnavn)) {
                if (file != null) {
                    ettersendelsePdfLager.add(fiksDigisosId, ettersendelseFilnavn, file.bytes)
                }
            } else {
                vedleggsInfo =
                    vedleggsJson.vedlegg.firstOrNull { jsonVedlegg ->
                        jsonVedlegg.filer.any { it.filnavn!!.contentEquals(vedleggMetadata.filnavn) }
                    }
                if (vedleggsInfo != null) {
                    sha512 =
                        vedleggsInfo.filer
                            .first { it.filnavn!!.contentEquals(vedleggMetadata.filnavn) }
                            .sha512
                }
            }
        }
        objectMapper.writeValueAsString(
            JsonVedleggSpesifikasjon()
                .withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(vedleggsInfo?.type ?: "annet")
                            .withTilleggsinfo(vedleggsInfo?.tilleggsinfo)
                            .withStatus(vedleggsInfo?.status ?: "LastetOpp")
                            .withHendelseType(vedleggsInfo?.hendelseType)
                            .withFiler(
                                listOf(
                                    JsonFiler()
                                        .withFilnavn(vedleggMetadata.filnavn)
                                        .withSha512(sha512)))))
        ).also { dokumentlager.save(vedleggsId, it) }

        leggVedleggTilISak(fiksDigisosId, vedleggMetadata, vedleggsId, timestamp)
        if (file != null) {
            fillager.add(vedleggsId, vedleggMetadata.filnavn ?: file.name, file.bytes)
        }
        log.info(
            "Lastet opp fil fiksDigisosId: $fiksDigisosId, filnavn: ${vedleggMetadata.filnavn}, vedleggsId: $vedleggsId")
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

        val soknadString = dokumentlager.get(digisosSak!!.digisosSoker!!.metadata)

        val soknad = objectMapper.readValue(soknadString, JsonDigisosSoker::class.java)
        val saksTittelMap = HashMap<String, String>()
        soknad.hendelser
            .filter { it.type == JsonHendelse.Type.SAKS_STATUS }
            .forEach {
                if (it is JsonSaksStatus) {
                    saksTittelMap[it.referanse] = it.tittel ?: ""
                }
            }
        if (saksTittelMap.isNotEmpty()) {
            return saksTittelMap.values.joinToString()
        }
        return SOKNAD_DEFAULT_TITTEL
    }
}

class FixedFileStorage {
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

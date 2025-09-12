package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.dokumentlager.DokumentlagerService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.mellomlagring.MellomlagringService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FiksKlageService(
    private val mellomlagringService: MellomlagringService,
    private val dokumentlagerService: DokumentlagerService,
) {

    val klageStorage: KlageStorageHandler = KlageStorageHandler()

    fun hentKlager(personId: String): List<FiksKlageDto> {
        TODO("Finn klager basert på person + digisosId hvis den følger med")
    }

    fun handleMottattKlage(
        personId: String,
        digisosId: UUID,
        klageId: UUID,
        vedtakId: UUID,
        klageFiles: KlageFiles,
    ) {
        val klageJsonId = UUID.randomUUID()
            .also { dokumentlagerService.leggTilDokument(it.toString(), klageFiles.klageJson) }

        val vedleggJsonId = UUID.randomUUID()
            .also { dokumentlagerService.leggTilDokument(it.toString(), klageFiles.vedleggJson) }

        val klagePdfId = UUID.randomUUID()
            .also {
                dokumentlagerService.lagreFil(
                    it.toString(),
                    klageFiles.klagePdf.originalFilename ?: error("Mangler filnavn på klage.pdf"),
                    klageFiles.klagePdf.bytes
                )
            }

        FiksKlageDto(
            digisosId = digisosId,
            klageId = klageId,
            navEksternRefId = klageId,
            vedtakId = vedtakId,
            klageMetadata = klageJsonId,
            vedleggMetadata = vedleggJsonId,
            klageDokument = DokumentInfoDto(
                filnavn = klageFiles.klagePdf.originalFilename ?: error("Mangler filnavn på klage.pdf"),
                dokumentlagerDokumentId = klagePdfId,
                storrelse = klageFiles.klagePdf.size,
            ),
            sendtKvittering = SendtKvitteringDto(
                sendtKanal = FiksProtokoll.FIKS_IO,
                meldingId = UUID.randomUUID(),
                sendtStatus = SendtStatusDto(
                    status = SendtStatus.SENDT,
                    timestamp = System.currentTimeMillis(),
                ),
                statusListe = emptyList()
            ),
        )
            .also { klageStorage.createKlage(personId, it) }


        flyttFilerFraMellomlager(klageId, klageFiles.vedleggJson)
    }

    fun handleSendEttersendelse() {

    }

    fun handleTrekkKlage() {

    }

    private fun flyttFilerFraMellomlager(klageId: UUID, vedleggJson: String) {

        val jsonVedlegg = objectMapper.readValue(vedleggJson, JsonVedleggSpesifikasjon::class.java)
            .let { vedleggSpec -> vedleggSpec.vedlegg.find { it.type == "klage" && it.klageId == klageId.toString() } }
            ?: error("Fant ikke vedlegg spesifikasjon for klageId $klageId i vedleggJson")

        if (jsonVedlegg.filer.isEmpty()) return

        val mellomlagredeForKlage = mellomlagringService.getAll(klageId.toString())?.mellomlagringMetadataList
            ?: error("Finner ingen mellomlagrede filer for klageId $klageId")

        jsonVedlegg.validate(mellomlagredeForKlage.map { it.filnavn })

        mellomlagredeForKlage.forEach { dokumentDto ->
            val bytes = mellomlagringService.get(klageId.toString(), dokumentDto.filId)
            dokumentlagerService.lagreFil(dokumentDto.filId, dokumentDto.filnavn, bytes)
        }

        mellomlagringService.deleteAll(klageId.toString())
    }

    companion object {
        private val logger by logger()
    }
}

private fun JsonVedlegg.validate(mellomlagredeFilnavn: List<String>) {
    require(filer.all { mellomlagredeFilnavn.contains(it.filnavn) }) {
        "Manglende filer i mellomlager"
    }
}

data class KlageFiles (
    val klageJson: String,
    val vedleggJson: String,
    val klagePdf: MultipartFile,
)

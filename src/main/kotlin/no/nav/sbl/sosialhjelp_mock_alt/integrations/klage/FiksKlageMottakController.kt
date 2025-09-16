package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import java.util.UUID
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraTokenNoDefault
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class FiksKlageMottakController(
    private val klageService: FiksKlageService,
) {

    @GetMapping("/fiks/digisos/klage/api/v1/klager")
    fun returnerKlager(
        @RequestParam("digisosId") digisosId: UUID?,
        @RequestHeader headers: HttpHeaders,
    ): List<FiksKlageDto> {

        val personId = hentFnrFraTokenNoDefault(headers) ?: error("Mangler fnr i token")

        return klageService.hentKlager(personId, digisosId)
    }

    @PostMapping("/fiks/digisos/klage/api/v1/{digisosId}/{navEksternRefId}/{klageId}/{vedtakId}")
    fun mottaKlage(
        @PathVariable("digisosId") digisosId: UUID,
        @PathVariable("navEksternRefId") navEksternRefId: UUID,
        @PathVariable("klageId") klageId: UUID,
        @PathVariable("vedtakId") vedtakId: UUID,
        @RequestPart("klage.json") klageJson: String,
        @RequestPart("klage.pdf") klagePdf: MultipartFile,
        @RequestPart("vedlegg.json") vedleggJson: String,
        @RequestHeader headers: HttpHeaders,
    ) {
        val personId = hentFnrFraTokenNoDefault(headers) ?: error("Mangler fnr i token")

        klageService.handleMottattKlage(
            personId,
            digisosId,
            klageId,
            navEksternRefId,
            vedtakId,
            KlageFiles(
                klageJson,
                vedleggJson,
                klagePdf)
        )
    }

    @PostMapping("/fiks/digisos/klage/api/v1/{digisosId}/{navEksternRefId}/{klageId}/vedlegg")
    fun mottaEttersendelse(
        @PathVariable("digisosId") digisosId: UUID,
        @PathVariable("navEksternRefId") navEksternRefId: UUID,
        @PathVariable("klageId") klageId: UUID,
    ) {
        TODO("Send ettersendelse")
    }

    @PostMapping("/fiks/digisos/klage/api/v1/{digisosId}/{navEksternRefId}/{klageId}/trekk")
    fun mottaTrekkKlage(
        @PathVariable("digisosId") digisosId: UUID,
        @PathVariable("navEksternRefId") navEksternRefId: UUID,
        @PathVariable("klageId") klageId: UUID,
    ) {
        TODO ("Trekk klage")
    }
}

data class FiksKlageDto(
    val fiksOrgId: UUID = UUID.randomUUID(),
    val digisosId: UUID,
    val klageId: UUID,
    val vedtakId: UUID,
    val navEksternRefId: UUID,
    val klageMetadata: UUID, // id til klage.json i dokumentlager
    val vedleggMetadata: UUID, // id til vedlegg.json (jsonVedleggSpec) i dokumentlager
    val klageDokument: DokumentInfoDto, // id til klage.pdf i dokumentlager
    val trekkKlageInfo: TrekkKlageInfoDto? = null,
    val sendtKvittering: SendtKvitteringDto = SendtKvitteringDto(
        sendtKanal = FiksProtokoll.FIKS_IO,
        meldingId = UUID.randomUUID(),
        sendtStatus = SendtStatusDto(
            status = SendtStatus.SENDT,
            timestamp = System.currentTimeMillis(),
        ),
        statusListe = emptyList(),
    ),
    val ettersendtInfoNAV: EttersendtInfoNAVDto = EttersendtInfoNAVDto(emptyList()),
    val trukket: Boolean = false,
)

data class EttersendtInfoNAVDto(
    val ettersendelser: List<EttersendelseDto>,
)

data class EttersendelseDto(
    val navEksternRefId: UUID,
    val vedleggMetadata: UUID,
    val vedlegg: List<DokumentInfoDto>,
    val timestampSendt: Long,
)

data class DokumentInfoDto(
    val filnavn: String,
    val dokumentlagerDokumentId: UUID,
    val storrelse: Long,
)

data class TrekkKlageInfoDto(
    val navEksternRefId: UUID,
    val trekkPdfMetadata: UUID,
    val vedleggMetadata: UUID,
    val trekkKlageDokument: DokumentInfoDto,
    val vedlegg: List<DokumentInfoDto>,
    val sendtKvittering: SendtKvitteringDto,
)

data class SendtKvitteringDto(
    val sendtKanal: FiksProtokoll,
    val meldingId: UUID,
    val sendtStatus: SendtStatusDto,
    val statusListe: List<SendtStatusDto>,
)

data class SendtStatusDto(
    val status: SendtStatus,
    val timestamp: Long,
)

enum class SendtStatus {
    SENDT,
    BEKREFTET,
    TTL_TIDSAVBRUDD,
    MAX_RETRIESAVBRUDD,
    IKKE_SENDT,
    SVARUT_FEIL,
    STOPPET,
}

enum class FiksProtokoll {
    FIKS_IO,
    SVARUT,
}

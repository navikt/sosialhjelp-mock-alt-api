package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import java.time.LocalDateTime
import java.util.UUID
import java.util.UUID.randomUUID

data class DigisosKlagerMetadata(
    val id: UUID = randomUUID(),
    val fiksDigisosId: UUID = randomUUID(),
    val personId: String,
    val klager: List<DigisosKlage> = emptyList(),
    val sistEndret: LocalDateTime = LocalDateTime.now(),
    val hendelserStatusList: List<HendelserSvarUt> = emptyList()
)

data class DigisosKlage(
    val klageId: UUID = randomUUID(),
    val navEksternRefId: UUID = randomUUID(),
    // id til klage.json
    val metadata: UUID = randomUUID(),
    // id til vedlegg.json
    val vedleggMetadata: UUID = randomUUID(),
    val klageDokument: KlageDokument,
    val vedlegg: List<DigisosVedlegg>,
    val sendtKvittering: SendtKvittering,
    val trukket: Boolean,
)

data class HendelserSvarUt(
    val state: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)

data class KlageDokument(
    val filnavn: String,
    // id til klage.pdf
    val dokumenlagerDokumentId: UUID = randomUUID(),
    val storrelse: Long
)

data class DigisosVedlegg(
    val filnavn: String,
    val dokumentlagerDokumentId: UUID = randomUUID(),
    val storrelse: Long,
)


data class SendtKvittering(
    val sendtStatus: DigisosSendtStatus,
    val statusListe: List<DigisosSendtStatus>
)

data class DigisosSendtStatus(
    val status: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)

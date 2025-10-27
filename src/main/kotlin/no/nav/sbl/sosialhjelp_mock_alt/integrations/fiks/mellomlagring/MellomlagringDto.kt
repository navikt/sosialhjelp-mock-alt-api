package no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks.mellomlagring

data class MellomlagringDto(
    val navEksternRefId: String,
    val mellomlagringMetadataList: List<MellomlagringDokumentInfo>?,
)

data class MellomlagringDokumentInfo(
    val filnavn: String,
    val filId: String,
    val storrelse: Long,
    val mimetype: String,
)

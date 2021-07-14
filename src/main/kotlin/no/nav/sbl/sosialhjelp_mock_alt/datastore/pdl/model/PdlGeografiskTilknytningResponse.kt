package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model

data class PdlGeografiskTilknytningResponse(
    val errors: List<PdlError>?,
    val data: PdlHentGeografiskTilknytning?
) {
    companion object {
        fun defaultResponse(): PdlGeografiskTilknytningResponse {
            return PdlGeografiskTilknytningResponse(
                errors = null,
                data = PdlHentGeografiskTilknytning(
                    hentGeografiskTilknytning = PdlGeografiskTilknytning(
                        gtType = GtType.KOMMUNE,
                        gtKommune = "0301",
                        gtBydel = null,
                        gtUtland = null
                    )
                )
            )
        }
    }
}

data class PdlHentGeografiskTilknytning(
    val hentGeografiskTilknytning: PdlGeografiskTilknytning?
)

data class PdlGeografiskTilknytning(
    val gtType: GtType,
    val gtKommune: String?,
    val gtBydel: String?,
    val gtUtland: String?
)

enum class GtType {
    KOMMUNE, BYDEL, UTLAND, UDEFINERT
}

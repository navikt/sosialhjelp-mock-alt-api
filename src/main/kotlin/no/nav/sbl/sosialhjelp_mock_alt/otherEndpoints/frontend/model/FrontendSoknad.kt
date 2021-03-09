@file:Suppress("unused")

package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model

class FrontendSoknad(
        val sokerFnr: String,
        val sokerNavn: String,
        val fiksDigisosId: String,
        val tittel: String,
        val vedlegg: Collection<FrontendVedlegg>,
        val vedleggSomMangler: Int,
)

class FrontendVedlegg(
        val navn: String,
        val id: String,
        val size: Long,
        val kanLastesned: Boolean,
)

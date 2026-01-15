package no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl

import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.GtType
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlGeografiskTilknytning
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlGeografiskTilknytningResponse
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlHentGeografiskTilknytning
import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.stereotype.Component

@Deprecated("Brukes ikke lenger av Soknad-api")
@Component
class PdlGeografiskTilknytningService {
    private val gtMap: HashMap<String, PdlGeografiskTilknytningResponse> = HashMap()

    fun getGeografiskTilknytning(ident: String): PdlGeografiskTilknytningResponse {
        log.info("Henter PDL GT for ident: $ident")
        return gtMap[ident] ?: PdlGeografiskTilknytningResponse.defaultResponse()
    }

    fun putGeografiskTilknytning(
        ident: String,
        gt: String,
    ) {
        val type = if (gt.length == 6) GtType.BYDEL else GtType.KOMMUNE

        gtMap[ident] =
            PdlGeografiskTilknytningResponse(
                errors = null,
                data =
                    PdlHentGeografiskTilknytning(
                        hentGeografiskTilknytning =
                            PdlGeografiskTilknytning(
                                gtType = type,
                                gtKommune = if (type == GtType.KOMMUNE) gt else null,
                                gtBydel = if (type == GtType.BYDEL) gt else null,
                                gtLand = null,
                            ),
                    ),
            )
    }

    companion object {
        private val log by logger()
    }
}

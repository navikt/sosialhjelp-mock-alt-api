package no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.model

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker

class DigisosApiWrapper(
    val sak: SakWrapper,
    val type: String,
)

class SakWrapper(
    val soker: JsonDigisosSoker,
)

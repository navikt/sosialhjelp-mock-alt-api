package no.nav.sbl.sosialhjelp_mock_alt.utils

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker

class DigisosApiWrapper(val sak: SakWrapper, val type: String)

class SakWrapper(val soker: JsonDigisosSoker)
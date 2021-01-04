package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model

import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigPersonnummer
import org.joda.time.DateTime

data class Personalia(
        val fnr: String = genererTilfeldigPersonnummer(),
        val navn: PdlPersonNavn = PdlPersonNavn(),
        var addressebeskyttelse: Gradering = Gradering.UGRADERT,
        var sivilstand: String = "UOPPGITT",
        var starsborgerskap: String = "NOR",
        var locked: Boolean = false,
        var opprettetTidspunkt: Long = DateTime.now().millis
) {
    fun withNavn(fornavn: String, mellomnavn: String, etternavn: String): Personalia {
        navn.fornavn = fornavn
        navn.mellomnavn = mellomnavn
        navn.etternavn = etternavn
        return this
    }

    fun withAdressebeskyttelse(nyVerdi: Gradering): Personalia {
        addressebeskyttelse = nyVerdi
        return this
    }

    fun withSivilstand(nyVerdi: String): Personalia {
        sivilstand = nyVerdi
        return this
    }

    fun withStarsborgerskap(nyVerdi: String): Personalia {
        starsborgerskap = nyVerdi
        return this
    }

    fun locked(): Personalia {
        locked = true
        return this
    }

    fun withOpprettetTidspunkt(tidspunkt: Long): Personalia {
        opprettetTidspunkt = tidspunkt
        return this
    }
}

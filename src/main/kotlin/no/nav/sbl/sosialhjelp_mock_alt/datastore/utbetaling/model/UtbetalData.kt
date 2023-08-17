package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model

import java.math.BigDecimal
import java.time.LocalDate

data class UtbetalDataDto(
    val utbetaltTil: Aktoer? = Aktoer(),
    val utbetalingsdato: LocalDate? = LocalDate.now().minusDays(2),
    val ytelseListe: List<Ytelse>? = listOf(Ytelse()),
)

data class Ytelse(
    val ytelsestype: String? = "Barnetrygd",
    val ytelsesperiode: Periode? = Periode(),
    val ytelseNettobeloep: BigDecimal? = BigDecimal(1500.00),
    val rettighetshaver: Aktoer? = Aktoer(),
    val skattsum: BigDecimal? = BigDecimal(500.00),
    val trekksum: BigDecimal? = BigDecimal.ZERO,
    val ytelseskomponentersum: BigDecimal? = BigDecimal.ZERO,
    val ytelseskomponentListe: List<Ytelseskomponent>? = listOf(Ytelseskomponent()),
    val bilagsnummer: String? = "bilagsnummer",
)

data class Aktoer(
    val aktoertype: Aktoertype = Aktoertype.PERSON,
    val ident: String = "aktoerident",
    val navn: String? = "aktoernavn",
)

enum class Aktoertype {
  PERSON,
  ORGANISASJON,
  SAMHANDLER,
}

data class Ytelseskomponent(
    val ytelseskomponenttype: String? = "Ytelseskomponenttype",
    val satsbeloep: BigDecimal? = BigDecimal.ZERO,
    val satstype: String? = "satstype",
    val satsantall: Double? = 2.0,
    val ytelseskomponentbeloep: BigDecimal? = BigDecimal.ZERO,
)

data class Periode(
    val fom: LocalDate = LocalDate.now().minusDays(25),
    val tom: LocalDate = LocalDate.now().minusDays(5),
)

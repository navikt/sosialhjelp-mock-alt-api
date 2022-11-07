package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model

import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigOrganisasjonsnummer
import java.math.BigDecimal
import java.time.LocalDate

class UtbetalData {

    data class UtbetalDataDto(
        val utbetaltTil: Aktoer? = Aktoer(),
        val utbetalingsmetode: String? = "metode",
        val utbetalingsstatus: String? = "status",
        val posteringsdato: LocalDate? = LocalDate.now().minusDays(2),
        val forfallsdato: LocalDate? = LocalDate.now().minusDays(3),
        val utbetalingsdato: LocalDate? = LocalDate.now().minusDays(14),
        val utbetalingNettobeloep: BigDecimal? = BigDecimal("1500.00"),
        val utbetalingsmelding: String? = "melding",
        val utbetaltTilKonto: Bankkonto? = Bankkonto(),
        val ytelseListe: List<Ytelse>? = listOf(Ytelse()),
    )

    data class Ytelse(
        val ytelsestype: String? = "Barnetrygd",
        val ytelsesperiode: Periode? = Periode(),
        val ytelseNettobeloep: BigDecimal? = BigDecimal("1500.00"),
        val rettighetshaver: Aktoer? = Aktoer(
            Aktoertype.SAMHANDLER,
            genererTilfeldigOrganisasjonsnummer(),
            "organisasjonen"
        ),
        val skattsum: BigDecimal? = BigDecimal("500.00"),
        val trekksum: BigDecimal? = BigDecimal("0"),
        val ytelseskomponentersum: BigDecimal? = BigDecimal("2000"),
        val skattListe: List<Skatt>? = listOf(Skatt()),
        val trekkListe: List<Trekk>? = listOf(Trekk()),
        val ytelseskomponentListe: List<Ytelseskomponent>? = listOf(Ytelseskomponent()),
        val bilagsnummer: String? = "bilagsnummer",
        val refundertForOrg: Aktoer? = Aktoer(
            Aktoertype.ORGANISASJON,
            genererTilfeldigOrganisasjonsnummer(),
            "refundert organisasjon"
        ),
    )

    data class Aktoer(
        val aktoertype: Aktoertype = Aktoertype.PERSON,
        val ident: String = "aktoerident",
        val navn: String? = "aktoernavn",
    )

    data class Ytelseskomponent(
        val ytelseskomponenttype: String? = "Ytelseskomponenttype",
        val satsbeloep: BigDecimal? = BigDecimal("1500"),
        val satstype: String? = "satstype",
        val satsantall: Double? = 2.0,
        val ytelseskomponentbeloep: BigDecimal? = BigDecimal("2000"),
    )

    data class Skatt(
        val skattebeloep: BigDecimal? = BigDecimal("500")
    )

    data class Trekk(
        val trekktype: String? = "Trekktype",
        val trekkbeloep: BigDecimal? = BigDecimal("0"),
        val kreditor: String? = "Kreditor",
    )

    data class Bankkonto(
        val kontonummer: String? = "1234567811",
        val kontotype: String? = "kontotype",
    )

    data class Periode(
        val fom: LocalDate = LocalDate.now().minusDays(25),
        val tom: LocalDate = LocalDate.now().minusDays(5),
    )

    enum class Aktoertype {
        PERSON,
        ORGANISASJON,
        SAMHANDLER,
    }
}

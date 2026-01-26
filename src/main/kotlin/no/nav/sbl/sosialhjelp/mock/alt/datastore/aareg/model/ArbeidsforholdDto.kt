package no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model

import java.time.LocalDate
import java.util.UUID

data class ArbeidsforholdResponseDto(
    val id: String,
    val ansettelsesperiode: AnsettelsesperiodeDto,
    val ansettelsesdetaljer: List<AnsettelsesdetaljerDto>?,
    val opplysningspliktig: OpplysningspliktigDto?,
    val arbeidstaker: ArbeidstakerDto?,
    val arbeidssted: ArbeidsstedDto?,
) {
    companion object {
        fun createNyttArbeidsforhold(
            personId: String,
            start: LocalDate = LocalDate.now().minusYears(2),
            slutt: LocalDate? = null,
            arbeidssted: ArbeidsstedDto,
            stillingsprosent: Double,
        ): ArbeidsforholdResponseDto =
            ArbeidsforholdResponseDto(
                id = UUID.randomUUID().toString(),
                ansettelsesperiode =
                    AnsettelsesperiodeDto(
                        startdato = start,
                        sluttdato = slutt,
                    ),
                ansettelsesdetaljer =
                    listOf(
                        AnsettelsesdetaljerDto(
                            avtaltStillingsprosent = stillingsprosent,
                            ansettelsesform =
                                AnsettelsesformDto(
                                    kode = "fast",
                                    beskrivelse = "Fast stilling",
                                ),
                            rapporteringsmaaneder =
                                RapporteringsmaanederDto(
                                    fra = LocalDate.now().minusMonths(2).let { "${it.year}-${it.month}" },
                                    til = null,
                                ),
                        ),
                    ),
                opplysningspliktig = null,
                arbeidstaker =
                    ArbeidstakerDto(
                        identer =
                            listOf(
                                ArbeidstakerIdentDto(
                                    type = ArbeidstakerIdentType.FOLKEREGISTERIDENT,
                                    ident = personId,
                                    gjeldende = true,
                                ),
                            ),
                    ),
                arbeidssted = arbeidssted,
            )
    }
}

data class AnsettelsesperiodeDto(
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
)

data class AnsettelsesdetaljerDto(
    val avtaltStillingsprosent: Double,
    val ansettelsesform: AnsettelsesformDto?,
    val rapporteringsmaaneder: RapporteringsmaanederDto?,
)

data class AnsettelsesformDto(
    val kode: String?,
    val beskrivelse: String?,
)

data class RapporteringsmaanederDto(
    val fra: String,
    val til: String?,
)

data class OpplysningspliktigDto(
    val type: OpplysningspliktigType,
    val identer: List<IdentInfoDto>,
)

enum class OpplysningspliktigType {
    Hovedenhet,
    Person,
}

data class ArbeidsstedDto(
    val type: ArbeidsstedType,
    val identer: List<IdentInfoDto>,
)

enum class ArbeidsstedType {
    Underenhet,
    Person,
}

data class IdentInfoDto(
    val type: IdentInfoType,
    val ident: String,
    val gjeldende: Boolean?,
)

enum class IdentInfoType {
    AKTORID,
    FOLKEREGISTERIDENT,
    ORGANISASJONSNUMMER,
}

data class ArbeidstakerDto(
    val identer: List<ArbeidstakerIdentDto>,
)

data class ArbeidstakerIdentDto(
    val type: ArbeidstakerIdentType,
    val ident: String,
    val gjeldende: Boolean,
)

enum class ArbeidstakerIdentType {
    FOLKEREGISTERIDENT,
    ORGANISASJONSNUMMER,
    AKTORID,
}

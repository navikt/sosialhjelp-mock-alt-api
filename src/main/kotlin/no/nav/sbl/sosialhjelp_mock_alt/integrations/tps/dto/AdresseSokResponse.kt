package no.nav.sbl.sosialhjelp_mock_alt.integrations.tps.dto

data class AdresseSokResponse(
        val flereTreff: Boolean,
        val adresseDataList: List<AdresseData>
)

data class AdresseData(
        val kommunenummer: String?,
        val kommunenavn: String?,
        val adressenavn: String?,
        val husnummerFra: String?,
        val husnummerTil: String?,
        val postnummer: String?,
        val poststed: String?,
        val geografiskTilknytning: String?,
        val gatekode: String?,
        val bydel: String?,
        val husnummer: String?,
        val husbokstav: String?,
)


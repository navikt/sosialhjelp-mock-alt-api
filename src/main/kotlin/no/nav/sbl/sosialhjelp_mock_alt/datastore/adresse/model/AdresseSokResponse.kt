package no.nav.sbl.sosialhjelp_mock_alt.datastore.adresse.model

data class AdresseSokResponse(
        val flereTreff: Boolean,
        val adresseDataList: List<AdresseData>
) {
    companion object {
        fun defaultAdressesok() : AdresseSokResponse {
            return AdresseSokResponse(
                    flereTreff = false,
                    adresseDataList = listOf(
                            AdresseData(
                                    kommunenummer = "1000",
                                    kommunenavn = "Mock Kommune",
                                    adressenavn = "Mock veien",
                                    husnummerFra = "1",
                                    husnummerTil = "42",
                                    postnummer = "0101",
                                    poststed = "Mock by",
                                    geografiskTilknytning = "1234",
                                    gatekode = "gatekode",
                                    bydel = "bydel",
                                    husnummer = "2",
                                    husbokstav = ""
                            )
                    )
            )
        }

    }
}

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


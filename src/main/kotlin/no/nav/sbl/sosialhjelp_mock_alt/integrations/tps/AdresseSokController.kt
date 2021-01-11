package no.nav.sbl.sosialhjelp_mock_alt.integrations.tps


import no.nav.sbl.sosialhjelp_mock_alt.integrations.tps.dto.AdresseData
import no.nav.sbl.sosialhjelp_mock_alt.integrations.tps.dto.AdresseSokResponse
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AdresseSokController {
    companion object {
        private val log by logger()
    }

    @GetMapping("/tps/adressesoek", produces = ["application/json;charset=UTF-8"])
    fun adressesok(@RequestParam parameters: MultiValueMap<String, String>): String {
        return objectMapper.writeValueAsString(defaultAdressesok())
    }

    private fun defaultAdressesok() : AdresseSokResponse {
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

package no.nav.sbl.sosialhjelp_mock_alt.integrations.azure.model

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia

data class AzureAdBruker(
    val id: String,
    val userPrincipalName: String,
    val givenName: String,
    val surname: String
) {
    constructor(personalia: Personalia) : this(
        id = personalia.fnr,
        userPrincipalName = "${personalia.navn.fornavn} ${personalia.navn.mellomnavn} ${personalia.navn.etternavn}"
            .replace("  ", " ").trim(),
        givenName = "${personalia.navn.fornavn} ${personalia.navn.mellomnavn}".trim(),
        surname = personalia.navn.etternavn
    )
}

class AzureAdBrukere(val value: List<AzureAdBruker>)

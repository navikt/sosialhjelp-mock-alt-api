package no.nav.sbl.sosialhjelp_mock_alt.integrations.sts.model

data class STSResponse(
        val access_token: String,
        val token_type: String,
        val expires_in: Int
)
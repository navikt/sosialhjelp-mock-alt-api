package no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks

import org.springframework.stereotype.Service
import java.util.Base64

@Service
class DokumentKrypteringsService {
    private val publicCertificateString =
        "MIICKzCCAZQCCQDv9tI+XfqHGjANBgkqhkiG9w0BAQsFADBaMQswCQYDVQQGEwJu" +
            "bzENMAsGA1UECAwETW9jazENMAsGA1UEBwwETW9jazEMMAoGA1UECgwDTmF2MRAw" +
            "DgYDVQQLDAdEaWdpc29zMQ0wCwYDVQQDDARtb2NrMB4XDTIwMDQyMzA5MzUwM1oX" +
            "DTQ3MDkwODA5MzUwM1owWjELMAkGA1UEBhMCbm8xDTALBgNVBAgMBE1vY2sxDTAL" +
            "BgNVBAcMBE1vY2sxDDAKBgNVBAoMA05hdjEQMA4GA1UECwwHRGlnaXNvczENMAsG" +
            "A1UEAwwEbW9jazCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAoZNrSU4pVOAH" +
            "x1XGCbYsTOCyJXaOUHraQI4lUpB2RdPPV+fB65tB7D+NuKKnR3VTqF4RUqzFQtHh" +
            "mYLh4N4HLCbLAoLYn8D7VQFAQun271K5198ICZPCDSu3raxS523TgXXBvjxLwPq+" +
            "KvSoDlEqyP6l5ET6XbVn7Px1VKJN7gECAwEAATANBgkqhkiG9w0BAQsFAAOBgQA6" +
            "eYEGvR3DUfrZV60Yy+T9DxuFFZhj5JEC3QMba/CXR2mAObgnG8IeVeBk/gULUmYq" +
            "8G0U+1smjA2tCPKI6Aknq0SaqqkbrE9jOoYm3+rjl7NoAnWlxXj86OjydYsecTWJ" +
            "LoJpNjfb/NpzHaR/bUECrKIUZ2U04r9mH4fvDJoPow=="
    val publicCertificateBytes: ByteArray = Base64.getDecoder().decode(publicCertificateString)
}

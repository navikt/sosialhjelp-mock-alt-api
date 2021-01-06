package no.nav.sbl.sosialhjelp_mock_alt.datastore.feil

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelp_mock_alt.KonfigurertFeil
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class FeilService {
    private val feilsituasjoner = HashMap<String, Feilsituasjon>()

    fun legtilFeil(
            feilsituasjon: Feilsituasjon,
    ) {
        feilsituasjoner.put(feilsituasjon.fnr, feilsituasjon)
    }

    fun eventueltLagFeil(headers: HttpHeaders, className: String, functionName: String) {
        val fnr = hentFnrFraToken(headers)
        eventueltLagFeil(fnr, className, functionName)
    }

    fun eventueltLagFeil(fnr: String, className: String, functionName: String) {
        val feilsituasjon = feilsituasjoner[fnr]
        if (feilsituasjon != null) {
            if (feilsituasjon.className.contentEquals(className) || feilsituasjon.className.contentEquals("*")) {
                if (feilsituasjon.functionName.contentEquals(functionName) || feilsituasjon.functionName.contentEquals("*")) {
                    if(feilsituasjon.timeoutSansynlighet > randomInt(2)) {
                        var sleep = 0
                        while (sleep < feilsituasjon.timeout) {
                            runBlocking { delay(10_000) }
                            sleep += 10
                        }
                    }
                    if(feilsituasjon.feilkodeSansynlighet > randomInt(2)) {
                        if (feilsituasjon.feilkode != null && feilsituasjon.feilkode > 0) {
                            throw KonfigurertFeil(feilsituasjon.feilkode, feilsituasjon.feilmelding)
                        }
                    }
                }
            }
        }
    }

    fun hentFeil(fnr: String): Feilsituasjon? {
        return feilsituasjoner[fnr]
    }
}

class Feilsituasjon(
        val fnr: String,
        val timeout: Int, val timeoutSansynlighet: Int,
        val feilkode: Int?, val feilmelding: String, val feilkodeSansynlighet: Int,
        val className: String, val functionName: String,
)

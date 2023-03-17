package no.nav.sbl.sosialhjelp_mock_alt.utils

import java.time.LocalDate
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Kjoenn
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class FnrUtilKtTest {

  @Test
  fun generererEtTilfeldigPersonnummer() {
    val fnr = genererTilfeldigPersonnummer()
    Assertions.assertEquals(11, fnr.length)
  }

  @Test
  fun generererEtTilfeldigPersonnummer_BasertPaFodselsdato() {
    val fnr = genererTilfeldigPersonnummer(dato = LocalDate.of(1945, 10, 26))
    Assertions.assertEquals(11, fnr.length)
    Assertions.assertTrue(fnr.startsWith("261045"))
  }

  @Test
  fun generererEtTilfeldigPersonnummer_BasertPaKjonn() {
    val fnr = genererTilfeldigPersonnummer(kjoenn = Kjoenn.MANN)
    Assertions.assertEquals(11, fnr.length)
  }

  @Test
  fun validerKjoenn_usatt() {
    Assertions.assertTrue(validerKjoenn(123, null))
  }

  @Test
  fun validerKjoenn_mann() {
    Assertions.assertTrue(validerKjoenn(123, Kjoenn.MANN))
  }

  @Test
  fun validerKjoenn_mann_error() {
    Assertions.assertFalse(validerKjoenn(122, Kjoenn.MANN))
  }

  @Test
  fun validerKjoenn_kvinne() {
    Assertions.assertTrue(validerKjoenn(122, Kjoenn.KVINNE))
  }

  @Test
  fun validerKjoenn_kvinne_error() {
    Assertions.assertFalse(validerKjoenn(123, Kjoenn.KVINNE))
  }
}

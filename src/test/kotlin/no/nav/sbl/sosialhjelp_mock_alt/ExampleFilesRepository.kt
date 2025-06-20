package no.nav.sbl.sosialhjelp_mock_alt

import java.io.File

object ExampleFilesRepository {
  val PDF_FILE = getFile("sample.pdf")

  fun getFile(filename: String): File {
    val url = this.javaClass.classLoader.getResource("examplefiles/$filename")?.file
    return File(url!!)
  }
}

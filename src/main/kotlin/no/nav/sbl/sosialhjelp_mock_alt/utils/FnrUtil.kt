package no.nav.sbl.sosialhjelp_mock_alt.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun genererTilfeldigPersonnummer() : String {
    val year = Math.round(Math.random() * 100).toInt()
    val dato = LocalDate.of(year, 1, 1)
    val randomDate = dato.plusDays(Math.round(Math.random() * 365))
    val dateString = randomDate.format(DateTimeFormatter.ofPattern("ddMMyy"))
    while (true) {
        var fnr = dateString
        val randomNumber = Math.round(Math.random() * 500).toInt()
        if(randomNumber < 10) {
            fnr += "00"
        } else if(randomNumber < 100) {
            fnr += "0"
        }
        fnr += randomNumber
        fnr += beregnKontrollsiffer1(fnr)
        fnr += beregnKontrollsiffer2(fnr)
        if(fnr.length == 11) {
            return fnr
        }
    }
}

private fun beregnKontrollsiffer1(fnr: String) : Int {
    val kontrollSiffer1Multiplikatorer = intArrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2)
    return beregnKontrollsiffer(fnr, kontrollSiffer1Multiplikatorer)
}

private fun beregnKontrollsiffer2(fnr: String) : Int {
    val kontrollSiffer1Multiplikatorer = intArrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2)
    return beregnKontrollsiffer(fnr, kontrollSiffer1Multiplikatorer)
}

private fun beregnKontrollsiffer(fnr: String, multiplikatorTabell: IntArray) : Int {
    var sum = 0
    for(i in multiplikatorTabell.indices) {
        sum += multiplikatorTabell[i] * fnr[i].toString().toInt()
    }
    val rest = sum % 11
    if (rest == 0) return 0
    return 11 - rest
}


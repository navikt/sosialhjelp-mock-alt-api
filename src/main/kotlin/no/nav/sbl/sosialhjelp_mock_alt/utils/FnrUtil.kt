package no.nav.sbl.sosialhjelp_mock_alt.utils

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Kjoenn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.math.roundToLong

fun genererTilfeldigPersonnummer(dato: LocalDate = randomDate(), kjoenn: Kjoenn? = null): String {

    val dateString = dato.format(DateTimeFormatter.ofPattern("ddMMyy"))

    while (true) {
        var fnr = dateString
        var randomNumber: Int
        do {
            randomNumber = (Math.random() * 500).roundToInt()
        } while (!validerKjoenn(randomNumber, kjoenn))

        if (randomNumber < 10) {
            fnr += "00" + randomNumber
        } else if (randomNumber < 100) {
            fnr += "0" + randomNumber
        } else {
            fnr += randomNumber
        }
        fnr += beregnKontrollsiffer1(fnr)
        fnr += beregnKontrollsiffer2(fnr)
        if (fnr.length == 11) {
            return fnr
        }
    }
}

fun validerKjoenn(nummer: Int, kjoenn: Kjoenn?): Boolean {
    if (Kjoenn.MANN == kjoenn) {
        return nummer % 2 == 1
    }
    if (Kjoenn.KVINNE == kjoenn) {
        return nummer % 2 == 0
    }
    return true
}

private fun randomDate(): LocalDate {
    val year = (Math.random() * 100).roundToInt()
    val dato = LocalDate.of(year, 1, 1)
    val randomDate = dato.plusDays((Math.random() * 365).roundToLong())
    return randomDate
}

fun genererTilfeldigOrganisasjonsnummer(): String {
    var numberString = ""
    while (numberString.length < 8) {
        val number = (Math.random() * 10).roundToInt()
        numberString += number
    }
    return numberString + beregnOrgNummerKontrollsiffer(numberString)
}

private fun beregnOrgNummerKontrollsiffer(fnr: String): Int {
    val kontrollSiffer1Multiplikatorer = intArrayOf(3, 2, 7, 6, 5, 4, 3, 2)
    return beregnKontrollsiffer(fnr, kontrollSiffer1Multiplikatorer)
}

private fun beregnKontrollsiffer1(fnr: String): Int {
    val kontrollSiffer1Multiplikatorer = intArrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2)
    return beregnKontrollsiffer(fnr, kontrollSiffer1Multiplikatorer)
}

private fun beregnKontrollsiffer2(fnr: String): Int {
    val kontrollSiffer1Multiplikatorer = intArrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2)
    return beregnKontrollsiffer(fnr, kontrollSiffer1Multiplikatorer)
}

private fun beregnKontrollsiffer(fnr: String, multiplikatorTabell: IntArray): Int {
    var sum = 0
    for (i in multiplikatorTabell.indices) {
        sum += multiplikatorTabell[i] * fnr[i].toString().toInt()
    }
    val rest = sum % 11
    if (rest == 0) return 0
    return 11 - rest
}


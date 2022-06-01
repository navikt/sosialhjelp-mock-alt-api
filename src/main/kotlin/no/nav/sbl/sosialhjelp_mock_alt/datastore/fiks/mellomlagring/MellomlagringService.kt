package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.mellomlagring

import no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks.mellomlagring.MellomlagringDokumentInfo
import no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks.mellomlagring.MellomlagringDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MellomlagringService() {
    private val mellomlager: MellomlagringStorage = MellomlagringStorage()

    fun getAll(navEksternRefId: String): MellomlagringDto? {
        log.debug("Henter metadata om alle mellomlagrede vedlegg for soknad med navEksternRefId=$navEksternRefId")
        return mellomlager.getAll(soknadId = navEksternRefId)?.let { fileEntries ->
            MellomlagringDto(
                navEksternRefId = navEksternRefId,
                mellomlagringMetadataList = fileEntries.map {
                    MellomlagringDokumentInfo(
                        filnavn = it.filnavn,
                        filId = it.filId,
                        storrelse = it.bytes.size.toLong(),
                        mimetype = it.mimeType
                    )
                }
            )
        }
    }

    fun get(navEksternRefId: String, digisosDokumentId: String): ByteArray {
        log.debug("Henter fil for soknad med navEksternRefId=$navEksternRefId og dokument med digisosDokumentId=$digisosDokumentId")
        val fileEntry = mellomlager.find(soknadId = navEksternRefId, filId = digisosDokumentId)
        return fileEntry?.bytes ?: throw RuntimeException("Ingen mellomlagret vedlegg med digisosDokumentId=$digisosDokumentId funnet for soknad $navEksternRefId")
    }

    fun deleteAll(navEksternRefId: String) {
        log.debug("Sletter alle mellomlagrede vedlegg for soknad med navEksternRefId=$navEksternRefId")
        mellomlager.deleteAll(soknadId = navEksternRefId)
    }

    fun delete(navEksternRefId: String, digisosDokumentId: String) {
        log.debug("Sletter mellomlagrede vedlegg for soknad med navEksternRefId=$navEksternRefId og dokument med digisosDokumentId=$digisosDokumentId")
        mellomlager.delete(soknadId = navEksternRefId, filId = digisosDokumentId)
    }

    fun post(navEksternRefId: String, filnavn: String, bytes: ByteArray, mimeType: String) {
        val filId = UUID.randomUUID().toString()
        log.debug("Lagrer vedlegg med filnavn=$filnavn til mellomlager for soknad med navEksternRefId=$navEksternRefId. FilId=$filId")
        mellomlager.add(
            soknadId = navEksternRefId,
            filId = filId,
            fileName = filnavn,
            bytes = bytes,
            mimeType = mimeType
        )
    }

    companion object {
        private val log by logger()
    }
}

class MellomlagringStorage {
    private val maxSize = 200
    private val items: MutableMap<String, MutableList<FileEntry>> = mutableMapOf()

    fun add(soknadId: String, filId: String, fileName: String, bytes: ByteArray, mimeType: String) {
        while (items.size >= maxSize) {
            items.remove(items.keys.last())
        }
        if (items.containsKey(soknadId)) {
            items[soknadId]?.add(FileEntry(filId, fileName, bytes, mimeType))
        } else {
            items[soknadId] = mutableListOf(FileEntry(filId, fileName, bytes, mimeType))
        }
    }

    fun getAll(soknadId: String): MutableList<FileEntry>? {
        return items[soknadId]
    }

    fun find(soknadId: String, filId: String): FileEntry? {
        return items[soknadId]?.find { it.filId == filId }
    }

    fun deleteAll(soknadId: String) {
        items.remove(soknadId)
    }

    fun delete(soknadId: String, filId: String) {
        items[soknadId]?.removeIf { it.filId == filId }
    }
}

data class FileEntry(
    val filId: String,
    val filnavn: String,
    val bytes: ByteArray,
    val mimeType: String
)

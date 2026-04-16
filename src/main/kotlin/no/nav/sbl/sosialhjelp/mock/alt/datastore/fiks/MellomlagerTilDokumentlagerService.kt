package no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks

import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.dokumentlager.DokumentlagerService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.mellomlagring.MellomlagringService
import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import org.springframework.stereotype.Service

@Service
class MellomlagerTilDokumentlagerService(
    private val mellomlagringService: MellomlagringService,
    private val dokumentlagerService: DokumentlagerService,
) {
    /**
     * Moves all files in mellomlager for [navEksternRefId] to dokumentlager and returns their info.
     * Validates that all [forventedeFilnavn] are present in mellomlager before moving.
     * Returns an empty list immediately if [forventedeFilnavn] is empty.
     */
    fun flyttFilerFraMellomlager(
        navEksternRefId: String,
        forventedeFilnavn: List<String>,
    ): List<DokumentInfo> {
        if (forventedeFilnavn.isEmpty()) {
            logger.info("Ingen referanser til vedlegg i JsonVedleggSpec for referanse $navEksternRefId")
            return emptyList()
        }

        logger.info("Flytter ${forventedeFilnavn.size} vedlegg fra mellomlager til dokumentlager for referanse $navEksternRefId")

        val mellomlagrede =
            mellomlagringService.getAll(navEksternRefId)?.mellomlagringMetadataList
                ?: error("Finner ingen mellomlagrede filer for navEksternRefId $navEksternRefId")

        val mellomlagredeFilnavn = mellomlagrede.map { it.filnavn }
        require(forventedeFilnavn.all { mellomlagredeFilnavn.contains(it) }) {
            "Finnes filer i vedleggSpec uten tilhørende mellomlagret fil for navEksternRefId $navEksternRefId"
        }

        return mellomlagrede
            .map { doc ->
                val bytes = mellomlagringService.get(navEksternRefId, doc.filId)
                dokumentlagerService.lagreFil(doc.filId, doc.filnavn, bytes)
                DokumentInfo(
                    filnavn = doc.filnavn,
                    dokumentlagerDokumentId = doc.filId,
                    storrelse = bytes.size.toLong(),
                )
            }.also {
                logger.info("Sletter filer i mellomlager for referanse $navEksternRefId")
                mellomlagringService.deleteAll(navEksternRefId)
            }
    }

    companion object {
        private val logger by logger()
    }
}

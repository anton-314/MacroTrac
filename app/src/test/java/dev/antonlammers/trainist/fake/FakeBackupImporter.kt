package dev.antonlammers.trainist.fake

import dev.antonlammers.trainist.domain.backup.BackupImporter

/**
 * Records the URIs it was asked to import and either returns [result] or throws [failure], so the
 * ViewModel's success and error paths are both reachable.
 */
class FakeBackupImporter(
    var result: BackupImporter.Result = BackupImporter.Result(),
    var failure: Throwable? = null,
) : BackupImporter {

    val importedUris = mutableListOf<String>()

    /** Runs while the import is still in flight — lets a test observe the transient loading state. */
    var whileRunning: (suspend () -> Unit)? = null

    override suspend fun import(uri: String): BackupImporter.Result {
        importedUris += uri
        whileRunning?.invoke()
        failure?.let { throw it }
        return result
    }
}

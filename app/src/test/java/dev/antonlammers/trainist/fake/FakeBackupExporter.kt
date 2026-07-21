package dev.antonlammers.trainist.fake

import dev.antonlammers.trainist.domain.backup.BackupExporter

/**
 * Records how often an export ran and either returns [uri] or throws [failure], so the ViewModel's
 * success and error paths are both reachable.
 */
class FakeBackupExporter(
    private val uri: String = "content://dev.antonlammers.trainist.fileprovider/cache/backup.zip",
    var failure: Throwable? = null,
) : BackupExporter {

    var exportCount = 0
        private set

    /** Runs while the export is still in flight — lets a test observe the transient loading state. */
    var whileRunning: (suspend () -> Unit)? = null

    override suspend fun export(): String {
        exportCount++
        whileRunning?.invoke()
        failure?.let { throw it }
        return uri
    }
}

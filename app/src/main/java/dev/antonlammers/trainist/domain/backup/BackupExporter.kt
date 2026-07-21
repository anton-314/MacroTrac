package dev.antonlammers.trainist.domain.backup

/**
 * Writes a full backup and returns a handle the UI can share.
 *
 * The handle is the **string form of an Android `Uri`** rather than a `Uri` itself: that keeps this
 * interface — and therefore [dev.antonlammers.trainist.ui.data.DataViewModel] — free of Android
 * types, so the export flow is reachable by a plain JVM unit test. `Uri` round-trips losslessly
 * through `toString()`/`Uri.parse()`, and nothing above the UI layer ever inspects the value.
 */
interface BackupExporter {
    /** @return the shareable URI of the written backup, in string form. */
    suspend fun export(): String
}

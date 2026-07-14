package dev.antonlammers.trainist.data.repository

import androidx.room.withTransaction
import dev.antonlammers.trainist.data.local.AppDatabase
import javax.inject.Inject

/**
 * Runs a block inside a single database transaction. Abstracted behind an interface so repositories
 * that write a multi-row graph (templates, sessions) stay unit-testable with a pass-through fake,
 * while production gets real Room atomicity.
 */
interface TransactionRunner {
    suspend fun <R> transaction(block: suspend () -> R): R
}

class RoomTransactionRunner @Inject constructor(
    private val db: AppDatabase,
) : TransactionRunner {
    override suspend fun <R> transaction(block: suspend () -> R): R = db.withTransaction { block() }
}

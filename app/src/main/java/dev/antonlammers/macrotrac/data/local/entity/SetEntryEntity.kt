package dev.antonlammers.macrotrac.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A single logged set of a [SessionExerciseEntity]; cascade-deleted with its parent exercise. */
@Entity(
    tableName = "set_entries",
    foreignKeys = [
        ForeignKey(
            entity = SessionExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionExerciseId")],
)
data class SetEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionExerciseId: Long,
    val position: Int,
    val weightKg: Double,
    val reps: Int,
    val type: String,
    val completed: Boolean,
)

package dev.antonlammers.macrotrac.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Catalog + custom exercise. [stableId] carries a unique index so it can be upserted / referenced
 * by stable key. Muscle lists and instructions are stored newline-joined (a muscle name / an
 * instruction line never contains a newline) and split back in mapping.
 */
@Entity(tableName = "exercises", indices = [Index(value = ["stableId"], unique = true)])
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stableId: String,
    val name: String,
    val type: String,
    val isCustom: Boolean,
    val primaryMuscles: String,
    val secondaryMuscles: String,
    val equipment: String?,
    val mechanic: String?,
    val category: String?,
    val instructions: String,
    val restSeconds: Int?,
)

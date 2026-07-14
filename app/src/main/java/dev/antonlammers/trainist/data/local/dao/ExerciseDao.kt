package dev.antonlammers.trainist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.antonlammers.trainist.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun allExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE stableId = :stableId LIMIT 1")
    fun exerciseByStableId(stableId: String): Flow<ExerciseEntity?>

    /** Replace on stableId conflict (the unique index). Row ids may change; nothing references them. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(exercises: List<ExerciseEntity>)

    @Query("DELETE FROM exercises WHERE stableId = :stableId")
    suspend fun delete(stableId: String)
}

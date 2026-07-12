package dev.antonlammers.macrotrac.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.antonlammers.macrotrac.data.local.entity.SessionExerciseEntity
import dev.antonlammers.macrotrac.data.local.entity.SetEntryEntity
import dev.antonlammers.macrotrac.data.local.entity.WorkoutSessionEntity
import dev.antonlammers.macrotrac.data.local.relation.SessionWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC, startedAtMs DESC")
    fun allSessions(): Flow<List<SessionWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    fun sessionById(id: Long): Flow<SessionWithExercises?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE isActive = 1 ORDER BY startedAtMs DESC LIMIT 1")
    fun activeSession(): Flow<SessionWithExercises?>

    /** Replace on id (or insert when id == 0); a replace cascade-clears the old exercise/set graph. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert
    suspend fun insertSessionExercise(exercise: SessionExerciseEntity): Long

    @Insert
    suspend fun insertSets(sets: List<SetEntryEntity>)

    /** Cascades to set_entries via the FK. */
    @Query("DELETE FROM session_exercises WHERE sessionId = :sessionId")
    suspend fun deleteSessionExercises(sessionId: Long)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)
}

package dev.antonlammers.trainist.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import dev.antonlammers.trainist.data.local.entity.SessionExerciseEntity
import dev.antonlammers.trainist.data.local.entity.SetEntryEntity
import dev.antonlammers.trainist.data.local.entity.WorkoutSessionEntity

/** One session exercise together with its sets (sort both levels by position in mapping). */
data class SessionExerciseWithSets(
    @Embedded val sessionExercise: SessionExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionExerciseId")
    val sets: List<SetEntryEntity>,
)

/** A session with its full exercise → sets graph (two levels of nested [Relation]). */
data class SessionWithExercises(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(entity = SessionExerciseEntity::class, parentColumn = "id", entityColumn = "sessionId")
    val exercises: List<SessionExerciseWithSets>,
)

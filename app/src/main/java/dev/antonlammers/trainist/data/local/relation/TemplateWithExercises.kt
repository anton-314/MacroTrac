package dev.antonlammers.trainist.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import dev.antonlammers.trainist.data.local.entity.TemplateExerciseEntity
import dev.antonlammers.trainist.data.local.entity.WorkoutTemplateEntity

/** A template together with its (unordered on read — sort by position) exercise slots. */
data class TemplateWithExercises(
    @Embedded val template: WorkoutTemplateEntity,
    @Relation(parentColumn = "id", entityColumn = "templateId")
    val exercises: List<TemplateExerciseEntity>,
)

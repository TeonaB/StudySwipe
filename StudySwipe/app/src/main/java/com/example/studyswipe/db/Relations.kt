package com.example.studyswipe.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class UserWithSubjects(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = UserSubjectEntity::class,
            parentColumn = "userId",
            entityColumn = "subjectId"
        )
    )
    val subjects: List<SubjectEntity>
)

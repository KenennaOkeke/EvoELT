package org.kenenna.evoelt.jpa.repository

import org.kenenna.evoelt.jpa.entity.ProcessedSequenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProcessedSequenceRepository: JpaRepository<ProcessedSequenceEntity, UUID> {
    @Query("SELECT * FROM ee_processed_sequences pse " +
            "WHERE (pse.labels @> CAST(:labels AS jsonb) AND pse.labels <@ CAST(:labels AS jsonb)) " +
            "AND raw_sequence_id = :raw_sequence_id",
        nativeQuery = true)
    fun findByRawSequenceIdAndLabels(
        @Param("raw_sequence_id") rawSequenceId: UUID,
        @Param("labels") labels: String = "[]"
    ): ProcessedSequenceEntity?
}
package org.kenenna.evoelt.jpa.repository

import org.kenenna.evoelt.jpa.entity.RawSequenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RawSequenceRepository: JpaRepository<RawSequenceEntity, UUID> {
    @Query("SELECT * FROM ee_raw_sequences rse WHERE rse.labels @> CAST(:labels AS jsonb) AND rse.labels <@ CAST(:labels AS jsonb)", nativeQuery = true)
    fun findByLabels(@Param("labels") labels: String): RawSequenceEntity?
}
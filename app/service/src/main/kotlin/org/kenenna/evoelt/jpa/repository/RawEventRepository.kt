package org.kenenna.evoelt.jpa.repository

import org.kenenna.evoelt.jpa.entity.RawEventEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RawEventRepository: JpaRepository<RawEventEntity, UUID>, PagingAndSortingRepository<RawEventEntity, UUID> {
    @Query("select ree from RawEventEntity ree where ree.rawSequenceId = :rawSequenceId AND ree.rawSequenceOrderId <= :rawSequenceOrderId")
    fun findAllByRawSequenceIdAndRawSequenceOrderIsLessThanEqual(
        @Param("rawSequenceId") rawSequenceId: UUID,
        @Param("rawSequenceOrderId") rawSequenceOrderId: Long,
        pageable: Pageable?
    ): Page<RawEventEntity?>?

    fun countByRawSequenceId(rawSequenceId: UUID): Long
}
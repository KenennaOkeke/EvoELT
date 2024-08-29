package org.kenenna.evoelt.jpa.repository

import org.kenenna.evoelt.jpa.entity.ProcessedEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProcessedEventRepository: JpaRepository<ProcessedEventEntity, UUID>
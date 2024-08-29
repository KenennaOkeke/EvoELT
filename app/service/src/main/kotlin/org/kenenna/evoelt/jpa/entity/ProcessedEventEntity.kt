package org.kenenna.evoelt.jpa.entity

import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ee_processed_events")
class ProcessedEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val processedSequenceId: UUID,

    val rawEventId: UUID,

    val data: String? = null,

    val createdDt: Timestamp? = Timestamp.from(Instant.now()),
)
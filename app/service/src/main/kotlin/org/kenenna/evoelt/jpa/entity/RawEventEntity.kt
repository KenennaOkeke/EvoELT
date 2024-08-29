package org.kenenna.evoelt.jpa.entity

import jakarta.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ee_raw_events")
class RawEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val rawSequenceId: UUID,

    val rawSequenceOrderId: Long,

    val data: String = "",

    val createdDt: Timestamp? = Timestamp.from(Instant.now()),
)
package org.kenenna.evoelt.jpa.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnTransformer
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ee_processed_sequences")
class ProcessedSequenceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val rawSequenceId: UUID,

    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    val labels: String? = "[]",

    val createdDt: Timestamp? = Timestamp.from(Instant.now()),

    var updatedDt: Timestamp? = null,
)
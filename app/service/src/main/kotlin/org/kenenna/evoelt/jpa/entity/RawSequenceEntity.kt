package org.kenenna.evoelt.jpa.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnTransformer
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ee_raw_sequences")
class RawSequenceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    val labels: String? = "[]",

    val createdDt: Timestamp? = Timestamp.from(Instant.now()),

    var updatedDt: Timestamp? = null,
)
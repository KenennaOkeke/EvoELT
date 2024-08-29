package org.kenenna.evoelt.unit.db

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kenenna.evoelt.EvoEltSandboxConfig
import org.kenenna.evoelt.jpa.entity.RawEventEntity
import org.kenenna.evoelt.utils.SampleData
import org.kenenna.evoelt.jpa.entity.RawSequenceEntity
import org.kenenna.evoelt.jpa.repository.RawEventRepository
import org.kenenna.evoelt.jpa.repository.RawSequenceRepository
import org.kenenna.evoelt.utils.MessageBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ContextConfiguration
import java.util.*

@SpringBootTest
@ContextConfiguration(classes = [EvoEltSandboxConfig::class])
class RawEventRepositoryTests(
    @Autowired val rawSequenceRepository: RawSequenceRepository,
    @Autowired val rawEventRepository: RawEventRepository
) {
    @BeforeEach
    fun teardown() {
        rawEventRepository.deleteAll()
        rawSequenceRepository.deleteAll()
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun teardown(
            @Autowired rawEventRepository: RawEventRepository,
            @Autowired rawSequenceRepository: RawSequenceRepository
        ) {
            rawEventRepository.deleteAll()
            rawSequenceRepository.deleteAll()
        }
    }

    @Test
    fun `findAllByRawSequenceIdAndRawSequenceOrderIsLessThanEqual successfully stop limits with entries`() {
        val labels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        val rawSequence = rawSequenceRepository.save(RawSequenceEntity(labels = labels))
        rawEventRepository.save(RawEventEntity(rawSequenceId = rawSequence.id!!, rawSequenceOrderId = 1))
        rawEventRepository.save(RawEventEntity(rawSequenceId = rawSequence.id!!, rawSequenceOrderId = 2))
        rawEventRepository.save(RawEventEntity(rawSequenceId = rawSequence.id!!, rawSequenceOrderId = 3))
        val rawEvents = rawEventRepository.findAllByRawSequenceIdAndRawSequenceOrderIsLessThanEqual(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 2,
            pageable = Pageable.unpaged()
        )
        assertEquals(2, rawEvents!!.totalElements)
    }

    @Test
    fun `findAllByRawSequenceIdAndRawSequenceOrderIsLessThanEqual successfully stop limits without entries`() {
        val labels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        val rawSequence = rawSequenceRepository.save(RawSequenceEntity(labels = labels))
        val rawEvents = rawEventRepository.findAllByRawSequenceIdAndRawSequenceOrderIsLessThanEqual(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 2,
            pageable = Pageable.unpaged()
        )
        assertEquals(0, rawEvents!!.totalElements)
    }

    @Test
    fun `countByRawSequenceId successfully counts raw events`() {
        val labels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        val rawSequence = rawSequenceRepository.save(RawSequenceEntity(labels = labels))

        assertEquals(0, rawEventRepository.countByRawSequenceId(rawSequenceId = rawSequence.id!!))

        rawEventRepository.save(RawEventEntity(rawSequenceId = rawSequence.id!!, rawSequenceOrderId = 1))
        assertEquals(1, rawEventRepository.countByRawSequenceId(rawSequenceId = rawSequence.id!!))

        rawEventRepository.save(RawEventEntity(rawSequenceId = rawSequence.id!!, rawSequenceOrderId = 2))
        assertEquals(2, rawEventRepository.countByRawSequenceId(rawSequenceId = rawSequence.id!!))
    }

    @Test
    fun `countByRawSequenceId successfully counts zero raw events for an invalid Raw Sequence ID`() {
        assertEquals(0, rawEventRepository.countByRawSequenceId(rawSequenceId = UUID.randomUUID()))
    }
}
package org.kenenna.evoelt.unit.db

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kenenna.evoelt.EvoEltSandboxConfig
import org.kenenna.evoelt.utils.SampleData
import org.kenenna.evoelt.jpa.entity.RawSequenceEntity
import org.kenenna.evoelt.jpa.entity.ProcessedSequenceEntity
import org.kenenna.evoelt.jpa.repository.RawSequenceRepository
import org.kenenna.evoelt.jpa.repository.ProcessedSequenceRepository
import org.kenenna.evoelt.utils.MessageBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.util.*

@SpringBootTest
@ContextConfiguration(classes = [EvoEltSandboxConfig::class])
class ProcessedSequenceRepositoryTests(
    @Autowired val processedSequenceRepository: ProcessedSequenceRepository,
    @Autowired val rawSequenceRepository: RawSequenceRepository
) {
    @BeforeEach
    fun teardown() {
        processedSequenceRepository.deleteAll()
        rawSequenceRepository.deleteAll()
    }
    companion object {
        @JvmStatic
        @AfterAll
        fun teardown(
            @Autowired processedSequenceRepository: ProcessedSequenceRepository,
            @Autowired rawSequenceRepository: RawSequenceRepository
        ) {
            processedSequenceRepository.deleteAll()
            rawSequenceRepository.deleteAll()
        }
    }

    @Test
    fun `findByRawSequenceIdAndLabels successfully returns a sequence`() {
        val rawLabels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        val processedLabels = MessageBody(SampleData.getProcessedEventOneInSequenceOne()).getLabels()
        val uuid = rawSequenceRepository.save(RawSequenceEntity(labels = rawLabels)).id!!
        processedSequenceRepository.save(ProcessedSequenceEntity(rawSequenceId = uuid, labels = processedLabels))
        assertNotNull(processedSequenceRepository.findByRawSequenceIdAndLabels(rawSequenceId = uuid, labels = processedLabels))
    }

    @Test
    fun `findByRawSequenceIdAndLabels successfully returns a sequence without labels`() {
        val rawLabels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        val uuid = rawSequenceRepository.save(RawSequenceEntity(labels = rawLabels)).id!!
        processedSequenceRepository.save(ProcessedSequenceEntity(rawSequenceId = uuid))
        assertNotNull(processedSequenceRepository.findByRawSequenceIdAndLabels(rawSequenceId = uuid))
    }

    @Test
    fun `findByRawSequenceIdAndLabels fails to return a non-existent sequence`() {
        assertNull(processedSequenceRepository.findByRawSequenceIdAndLabels(rawSequenceId = UUID.randomUUID(), labels = "[]"))
    }

    @Test
    fun `findByRawSequenceIdAndLabels successfully returns sequence regardless of label order`() {
        val rawLabels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        val processedLabels = MessageBody(SampleData.getProcessedEventOneInSequenceOne()).getLabels()
        val uuid = rawSequenceRepository.save(RawSequenceEntity(labels = rawLabels)).id!!
        processedSequenceRepository.save(ProcessedSequenceEntity(rawSequenceId = uuid, labels = processedLabels))
        assertNotNull(processedSequenceRepository.findByRawSequenceIdAndLabels(rawSequenceId = uuid, labels = processedLabels))
        assertNotNull(processedSequenceRepository.findByRawSequenceIdAndLabels(rawSequenceId = uuid, labels = SampleData.reverseLabels(processedLabels)))
    }
}
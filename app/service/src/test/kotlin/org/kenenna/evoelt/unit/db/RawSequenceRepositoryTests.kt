package org.kenenna.evoelt.unit.db

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kenenna.evoelt.EvoEltSandboxConfig
import org.kenenna.evoelt.utils.SampleData
import org.kenenna.evoelt.jpa.entity.RawSequenceEntity
import org.kenenna.evoelt.jpa.repository.RawSequenceRepository
import org.kenenna.evoelt.utils.MessageBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [EvoEltSandboxConfig::class])
class RawSequenceRepositoryTests(
    @Autowired val rawSequenceRepository: RawSequenceRepository
) {
    @BeforeEach
    fun teardown() {
        rawSequenceRepository.deleteAll()
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun teardown(@Autowired rawSequenceRepository: RawSequenceRepository) {
            rawSequenceRepository.deleteAll()
        }
    }

    @Test
    fun `findByLabels successfully returns sequence`() {
        val labels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        rawSequenceRepository.save(RawSequenceEntity(labels = labels))
        assertNotNull(rawSequenceRepository.findByLabels(labels))
    }

    @Test
    fun `findByLabels fails to return a non-existent sequence`() {
        val labels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        assertNull(rawSequenceRepository.findByLabels(labels))
    }

    @Test
    fun `findByLabels successfully returns sequence regardless of label order`() {
        val labels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        rawSequenceRepository.save(RawSequenceEntity(labels = labels))
        assertNotNull(rawSequenceRepository.findByLabels(labels))
        assertNotNull(rawSequenceRepository.findByLabels(labels))
        assertNotNull(rawSequenceRepository.findByLabels(SampleData.reverseLabels(labels)))
    }
}
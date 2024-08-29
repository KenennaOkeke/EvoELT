package org.kenenna.evoelt.integration

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.kenenna.evoelt.EvoEltSandboxConfig
import org.kenenna.evoelt.utils.SampleData
import org.kenenna.evoelt.jpa.repository.RawSequenceRepository
import org.kenenna.evoelt.jpa.repository.RawEventRepository
import org.kenenna.evoelt.jpa.repository.ProcessedSequenceRepository
import org.kenenna.evoelt.jpa.repository.ProcessedEventRepository
import org.kenenna.evoelt.events.EventConsumer
import org.kenenna.evoelt.events.implementation.Sqs
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.util.*

@SpringBootTest
@ContextConfiguration(classes = [EvoEltSandboxConfig::class])
class EventConsumerTests(
    @Autowired val sqs: Sqs,
    @Autowired val sqsConsumerQueueUrl: String,
    @Autowired val sqsProducerQueueUrl: String,
    @Autowired val rawSequenceRepository: RawSequenceRepository,
    @Autowired val rawEventRepository: RawEventRepository,
    @Autowired val processedSequenceRepository: ProcessedSequenceRepository,
    @Autowired val processedEventRepository: ProcessedEventRepository,
    @Autowired val eventConsumer: EventConsumer
) {
    @BeforeEach
    fun teardown() {
        try {
            sqs.deleteQueue(sqsConsumerQueueUrl)
        } catch (_: Exception) { }
        try {
            sqs.deleteQueue(sqsProducerQueueUrl)
        } catch (_: Exception) { }
        processedEventRepository.deleteAll()
        processedSequenceRepository.deleteAll()
        rawEventRepository.deleteAll()
        rawSequenceRepository.deleteAll()
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun teardown(
            @Autowired rawSequenceRepository: RawSequenceRepository,
            @Autowired rawEventRepository: RawEventRepository,
            @Autowired processedSequenceRepository: ProcessedSequenceRepository,
            @Autowired processedEventRepository: ProcessedEventRepository,
        ) {
            processedEventRepository.deleteAll()
            processedSequenceRepository.deleteAll()
            rawEventRepository.deleteAll()
            rawSequenceRepository.deleteAll()
        }
    }

    @Test
    fun `receiveMessages successfully processes zero messages while queue exists`() {
        sqs.createQueue(sqsConsumerQueueUrl, 0)
        eventConsumer.receiveMessages(0)
        assertEquals(0, rawSequenceRepository.count())
    }

    @Test
    fun `receiveMessages successfully processes zero messages while queue exists and the wait time isn't zero`() {
        sqs.createQueue(sqsConsumerQueueUrl, 1)
        eventConsumer.receiveMessages(0)
        assertEquals(0, rawSequenceRepository.count())
    }

    @Test
    fun `receiveMessages successfully processes one raw event and can send the producer an ack`() {
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.createQueue(sqsProducerQueueUrl)
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventOneInSequenceOne())
        eventConsumer.receiveMessages()
        val rawEvent = rawEventRepository.findAll()[0]
        assertEquals("ABCD", rawEvent.data)
        assertEquals(1, rawEvent.rawSequenceOrderId)
        assertEquals(rawEvent.id.toString(), sqs.getMessages(sqsProducerQueueUrl)[0].body.getRawEventId())
    }

    @Test
    fun `receiveMessages successfully processes multiple raw event with the same label`() {
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.createQueue(sqsProducerQueueUrl)
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventOneInSequenceOne())
        eventConsumer.receiveMessages()
        assertNull(rawSequenceRepository.findAll()[0].updatedDt)
        assertEquals(1, rawEventRepository.findAll()[0].rawSequenceOrderId)

        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventTwoInSequenceOne())
        eventConsumer.receiveMessages()
        assertNotNull(rawSequenceRepository.findAll()[0].updatedDt)
        assertEquals(2, rawEventRepository.findAll()[1].rawSequenceOrderId)
    }

    @Test
    fun `receiveMessages successfully processes multiple raw event with different labels`() {
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.createQueue(sqsProducerQueueUrl)
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventOneInSequenceOne())
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventTwoInSequenceOne())
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventOneInSequenceOne())
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventOneInSequenceTwo())
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventTwoInSequenceTwo())
        eventConsumer.receiveMessages()
        assertEquals(2, rawSequenceRepository.findAll().size)
        assertEquals(5, rawEventRepository.findAll().size)
    }

    @Test
    fun `receiveMessages successfully processes one processed event`() {
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.createQueue(sqsProducerQueueUrl)
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventOneInSequenceOne())
        eventConsumer.receiveMessages()
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getProcessedEventOneInSequenceOne(rawEventRepository.findAll()[0].id!!))
        eventConsumer.receiveMessages()
        assertEquals("DCBA", processedEventRepository.findAll()[0].data)
    }

    @Test
    fun `receiveMessages fails to process a processed event with an invalid raw event id`() {
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getProcessedEventOneInSequenceOne(UUID.randomUUID()))
        assertThrows<NoSuchElementException> {
            eventConsumer.receiveMessages()
        }
    }

    @Test
    fun `receiveMessages successfully processes multiple processed events in one raw sequence`() {
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.createQueue(sqsProducerQueueUrl)
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventOneInSequenceOne())
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventTwoInSequenceOne())
        eventConsumer.receiveMessages()
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getProcessedEventOneInSequenceOne(rawEventRepository.findAll()[0].id!!))
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getProcessedEventOneInSequenceOne(rawEventRepository.findAll()[1].id!!))
        eventConsumer.receiveMessages()
        assertEquals(1, rawSequenceRepository.findAll().size)
        assertEquals(2, rawEventRepository.findAll().size)
        assertEquals(1, processedSequenceRepository.findAll().size)
        assertEquals(2, processedEventRepository.findAll().size)
    }

    @Test
    fun `receiveMessages successfully processes multiple processed events from multiple raw sequences`() {
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.createQueue(sqsProducerQueueUrl)
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventOneInSequenceOne())
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventTwoInSequenceOne())
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventOneInSequenceThree())
        eventConsumer.receiveMessages()
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getProcessedEventOneInSequenceOne(rawEventRepository.findAll()[0].id!!))
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getProcessedEventOneInSequenceOne(rawEventRepository.findAll()[1].id!!))
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getProcessedEventOneInSequenceThree(rawEventRepository.findAll()[2].id!!))
        eventConsumer.receiveMessages()
        assertEquals(2, rawSequenceRepository.findAll().size)
        assertEquals(3, rawEventRepository.findAll().size)
        assertEquals(2, processedSequenceRepository.findAll().size)
        assertEquals(3, processedEventRepository.findAll().size)
    }
}
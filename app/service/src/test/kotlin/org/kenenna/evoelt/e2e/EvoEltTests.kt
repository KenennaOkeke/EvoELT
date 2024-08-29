package org.kenenna.evoelt.e2e

import org.json.JSONArray
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kenenna.evoelt.EvoEltSandboxConfig
import org.kenenna.evoelt.jpa.repository.RawSequenceRepository
import org.kenenna.evoelt.jpa.repository.RawEventRepository
import org.kenenna.evoelt.jpa.repository.ProcessedSequenceRepository
import org.kenenna.evoelt.jpa.repository.ProcessedEventRepository
import org.kenenna.evoelt.utils.SampleData
import org.kenenna.evoelt.events.EventConsumer
import org.kenenna.evoelt.utils.MessageBody
import org.kenenna.evoelt.events.implementation.Sqs
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [EvoEltSandboxConfig::class])
@EnableAutoConfiguration
class EvoEltTests(
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
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.createQueue(sqsProducerQueueUrl)
        processedEventRepository.deleteAll()
        processedSequenceRepository.deleteAll()
        rawEventRepository.deleteAll()
        rawSequenceRepository.deleteAll()
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun teardown(
            @Autowired sqs: Sqs,
            @Autowired sqsConsumerQueueUrl: String,
            @Autowired sqsProducerQueueUrl: String,
            @Autowired rawSequenceRepository: RawSequenceRepository,
            @Autowired rawEventRepository: RawEventRepository,
            @Autowired processedSequenceRepository: ProcessedSequenceRepository,
            @Autowired processedEventRepository: ProcessedEventRepository,
        ) {
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
    }

    @Test
     fun systemTest() {
        // External Application sends raw event
        val rawEvent = SampleData.getRawEventOneInSequenceOne()
        sqs.sendMessage(sqsConsumerQueueUrl, rawEvent)

         // EvoELT Receives Message
        eventConsumer.receiveMessages()

        // Verify EvoElt stored raw event
        assertEquals(
            JSONArray(MessageBody(rawEvent).getLabels()).toString(),
            JSONArray(rawSequenceRepository.findAll()[0].labels).toString()
        )

        // Assert EvoELT's ack was sent to external application
        val ackMessage = sqs.getMessages(sqsProducerQueueUrl)
        assertEquals(
            JSONArray(rawSequenceRepository.findAll()[0].labels).toString(),
            JSONArray(ackMessage[0].body.getLabels()).toString()
        )
        assertEquals(rawEventRepository.findAll()[0].id.toString(), ackMessage[0].body.getRawEventId())

        //Processing/Transformation Application transforms event and sends processed event
        val processedEvent = SampleData.getProcessedEventOneInSequenceOne(
            rawEventRepository.findAll()[0].id!!
        )
        sqs.sendMessage(sqsConsumerQueueUrl, processedEvent)

        // EvoELT consumes and stores Processed Event
        eventConsumer.receiveMessages()

        //Assert EvoELT Processed event has been stored
        assertEquals(
            JSONArray(MessageBody(processedEvent).getLabels()).toString(),
            JSONArray(processedSequenceRepository.findAll()[0].labels).toString()
        )
     }
}
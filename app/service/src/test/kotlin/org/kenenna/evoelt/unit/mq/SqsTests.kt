package org.kenenna.evoelt.unit.mq

import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kenenna.evoelt.EvoEltSandboxConfig
import org.kenenna.evoelt.utils.SampleData
import org.kenenna.evoelt.events.implementation.Sqs
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException

@SpringBootTest
@ContextConfiguration(classes = [EvoEltSandboxConfig::class])
class SqsTests(
    @Autowired val sqs: Sqs,
    @Autowired val sqsConsumerQueueUrl: String,
    @Autowired val sqsProducerQueueUrl: String
) {
    @BeforeEach
    fun teardown() {
        try {
            sqs.deleteQueue(sqsConsumerQueueUrl)
        } catch (_: Exception) { }
        try {
            sqs.deleteQueue(sqsProducerQueueUrl)
        } catch (_: Exception) { }
    }

    @Test
    fun `getQueue successfully returns an existing consumer queue`() {
        sqs.createQueue(sqsConsumerQueueUrl)
        val queueUrl = sqs.getQueueUrl(sqsConsumerQueueUrl)
        assertEquals(true, queueUrl.endsWith(sqsConsumerQueueUrl))
    }

    @Test
    fun `getQueue fails to return a non-existing consumer queue`() {
        assertThrows<QueueDoesNotExistException> { sqs.getQueueUrl(sqsConsumerQueueUrl) }
    }

    @Test
    fun `getQueue successfully returns an existing producer queue`() {
        sqs.createQueue(sqsProducerQueueUrl)
        val queueUrl = sqs.getQueueUrl(sqsProducerQueueUrl)
        assertEquals(true, queueUrl.endsWith(queueUrl))
    }

    @Test
    fun `getQueue fails to return a non-existing producer queue`() {
        assertThrows<QueueDoesNotExistException> { sqs.getQueueUrl(sqsProducerQueueUrl) }
    }

    @Test
    fun `createQueue successfully creates a consumer queue`() {
        val createQueueResponse = sqs.createQueue(sqsConsumerQueueUrl)
        val actualQueueUrl = sqs.getQueueUrl(sqsConsumerQueueUrl)
        Assertions.assertTrue(createQueueResponse.queueUrl().endsWith(sqsConsumerQueueUrl))
        Assertions.assertTrue(actualQueueUrl.endsWith(sqsConsumerQueueUrl))
    }

    @Test
    fun `createQueue successfully creates a producer queue`() {
        val createQueueResponse = sqs.createQueue(sqsProducerQueueUrl)
        val actualQueueUrl = sqs.getQueueUrl(sqsProducerQueueUrl)
        Assertions.assertTrue(createQueueResponse.queueUrl().endsWith(sqsProducerQueueUrl))
        Assertions.assertTrue(actualQueueUrl.endsWith(sqsProducerQueueUrl))
    }

    @Test
    fun `deleteQueue successfully deletes an existing consumer queue`() {
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.deleteQueue(sqsConsumerQueueUrl)
        assertThrows<QueueDoesNotExistException> { sqs.getQueueUrl(sqsConsumerQueueUrl) }
    }

    @Test
    fun `deleteQueue successfully deletes an existing producer queue`() {
        sqs.createQueue(sqsProducerQueueUrl)
        sqs.deleteQueue(sqsProducerQueueUrl)
        assertThrows<QueueDoesNotExistException> { sqs.getQueueUrl(sqsProducerQueueUrl) }
    }

    @Test
    fun `deleteQueue fails to delete a non-existing consumer queue`() {
        assertThrows<QueueDoesNotExistException> { sqs.deleteQueue(sqsConsumerQueueUrl) }
    }

    @Test
    fun `deleteQueue fails to delete a non-existing producer queue`() {
        assertThrows<QueueDoesNotExistException> { sqs.deleteQueue(sqsProducerQueueUrl) }
    }

    @Test
    fun `sendMessage fails to send a message if the producer queue doesn't exist`() {
        val messagesToSend = SampleData.getRawEventOneInSequenceOne()
        assertThrows<QueueDoesNotExistException> { sqs.sendMessage(sqsProducerQueueUrl, messagesToSend) }
    }

    @Test
    fun `sendMessage successfully sends one message if the producer queue exists`() {
        val messageToSend = SampleData.getRawEventOneInSequenceOne()
        sqs.createQueue(sqsProducerQueueUrl)
        sqs.sendMessage(sqsProducerQueueUrl, messageToSend)
        val messages = sqs.getMessages(sqsProducerQueueUrl)
        assertEquals(1, messages.size)
        assertEquals(
            JSONObject(messageToSend).toString(),
            JSONObject(messages[0].body.toString()).toString()
        )
    }

    @Test
    fun `sendMessage successfully sends multiple messages if the producer queue exists`() {
        val messagesToSend = arrayOf(
            SampleData.getRawEventOneInSequenceOne(),
            SampleData.getRawEventTwoInSequenceOne()
        )
        sqs.createQueue(sqsProducerQueueUrl)
        sqs.sendMessage(sqsProducerQueueUrl, messagesToSend[0])
        sqs.sendMessage(sqsProducerQueueUrl, messagesToSend[1])
        val messages = sqs.getMessages(sqsProducerQueueUrl)
        assertEquals(2, messages.size)
        assertEquals(
            messagesToSend.map { JSONObject(it).toString() },
            messages.map { JSONObject(it.body.toString()).toString() }
        )
    }

    @Test
    fun `getMessages fails to get a message from the non-existent consumer queue`() {
        assertThrows<QueueDoesNotExistException> { sqs.getMessages(sqsConsumerQueueUrl) }
    }

    @Test
    fun `getMessages successfully gets zero messages from the consumer queue`() {
        sqs.createQueue(sqsConsumerQueueUrl)
        val messages = sqs.getMessages(sqsConsumerQueueUrl, 1)
        assertEquals(0, messages.size)
    }

    @Test
    fun `getMessages successfully gets one message from the consumer queue`() {
        val messageToSend = SampleData.getRawEventOneInSequenceOne()
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.sendMessage(sqsConsumerQueueUrl, SampleData.getRawEventOneInSequenceOne())
        val messages = sqs.getMessages(sqsConsumerQueueUrl, 1)
        assertEquals(1, messages.size)
        assertEquals(listOf(messageToSend).map { JSONObject(it).toString() }, messages.map { JSONObject(it.body.toString()).toString() })
    }

    @Test
    fun `getMessages successfully gets multiple messages from the consumer queue`() {
        val messagesToSend = arrayOf(
            SampleData.getRawEventOneInSequenceOne(),
            SampleData.getRawEventTwoInSequenceOne()
        )
        sqs.createQueue(sqsConsumerQueueUrl)
        sqs.sendMessage(sqsConsumerQueueUrl, messagesToSend[0])
        sqs.sendMessage(sqsConsumerQueueUrl, messagesToSend[1])
        val messages = sqs.getMessages(sqsConsumerQueueUrl, 1)
        assertEquals(2, messages.size)
        assertEquals(messagesToSend.map { JSONObject(it).toString() }, messages.map { JSONObject(it.body.toString()).toString() })
    }

    @Test
    fun `deleteMessage successfully deletes one out of two messages in the consumer queue`() {
        val messagesToSend = arrayOf(
            SampleData.getRawEventOneInSequenceOne(),
            SampleData.getRawEventTwoInSequenceOne()
        )
        sqs.createQueue(sqsConsumerQueueUrl, 0)
        sqs.sendMessage(sqsConsumerQueueUrl, messagesToSend[0])
        sqs.sendMessage(sqsConsumerQueueUrl, messagesToSend[1])
        val messages = sqs.getMessages(sqsConsumerQueueUrl, 1)
        sqs.deleteMessage(sqsConsumerQueueUrl, messages[0].id)
        val messagesAfterDelete = sqs.getMessages(sqsConsumerQueueUrl, 1)
        assertEquals(1, messagesAfterDelete.size)
    }

    @Test
    fun `deleteMessage successfully deletes two out of two messages in the consumer queue`() {
        val messagesToSend = arrayOf(
            SampleData.getRawEventOneInSequenceOne(),
            SampleData.getRawEventTwoInSequenceOne()
        )
        sqs.createQueue(sqsConsumerQueueUrl, 0)
        sqs.sendMessage(sqsConsumerQueueUrl, messagesToSend[0])
        sqs.sendMessage(sqsConsumerQueueUrl, messagesToSend[1])
        val messages = sqs.getMessages(sqsConsumerQueueUrl, 1)
        sqs.deleteMessage(sqsConsumerQueueUrl, messages[0].id)
        sqs.deleteMessage(sqsConsumerQueueUrl, messages[1].id)

        val messagesAfterDelete = sqs.getMessages(sqsConsumerQueueUrl, 1)
        assertEquals(0, messagesAfterDelete.size)
    }
}
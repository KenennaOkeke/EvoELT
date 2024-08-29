package org.kenenna.evoelt.events.implementation

import org.kenenna.evoelt.utils.MessageBody
import org.kenenna.evoelt.utils.ConsumedMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.*
import java.util.*

@Component
class Sqs(@Autowired private val sqsClient: SqsClient) {
    fun getQueueUrl(queueUrl: String): String {
        return sqsClient.getQueueUrl(
            GetQueueUrlRequest.builder().queueName(queueUrlToQueueName(queueUrl)).build()
        ).queueUrl()
    }

    fun createQueue(queueUrl: String, visibilityTimeOut: Int = 30): CreateQueueResponse {
        return sqsClient.createQueue(
            CreateQueueRequest.builder().queueName(queueUrlToQueueName(queueUrl)).attributes(mapOf(
                QueueAttributeName.FIFO_QUEUE to "true",
                QueueAttributeName.CONTENT_BASED_DEDUPLICATION to "false",
                QueueAttributeName.MESSAGE_RETENTION_PERIOD to (60 * 60 * 24 * 14).toString(),
                QueueAttributeName.VISIBILITY_TIMEOUT to visibilityTimeOut.toString()
        )).build())
    }

    fun deleteQueue(queueUrl: String): DeleteQueueResponse {
        return sqsClient.deleteQueue(DeleteQueueRequest.builder().queueUrl(queueUrl).build())
    }

    fun getMessages(queueUrl: String, waitTimeSeconds: Int = 10): List<ConsumedMessage> {
        return sqsClient.receiveMessage(
            ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(waitTimeSeconds)
                .build()
        ).messages().map { ConsumedMessage(
            id = it.receiptHandle(),
            idUnique = it.messageId(),
            body = MessageBody(it.body()),
        ) }
    }

    fun sendMessage(queueUrl: String, message: String, messageGroupId: UUID = UUID.randomUUID(), messageDeduplicationId: UUID = UUID.randomUUID()): SendMessageResponse {
        return sqsClient.sendMessage(
            SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .messageGroupId(messageGroupId.toString())
                .messageDeduplicationId(messageGroupId.toString())
                .build()
        )
    }

    fun deleteMessage(queueUrl: String, receiptHandle: String): DeleteMessageResponse {
        return sqsClient.deleteMessage(
            DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build()
        )
    }

    //TODO: Make obsolete and cleanup tests
    fun queueUrlToQueueName(queueUrl: String): String {
        return queueUrl.split("/").last()
    }
}
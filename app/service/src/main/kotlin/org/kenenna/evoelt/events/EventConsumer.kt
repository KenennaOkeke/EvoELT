package org.kenenna.evoelt.events

import org.kenenna.evoelt.EvoEltConfig
import org.kenenna.evoelt.jpa.entity.RawSequenceEntity
import org.kenenna.evoelt.jpa.entity.RawEventEntity
import org.kenenna.evoelt.jpa.entity.ProcessedSequenceEntity
import org.kenenna.evoelt.jpa.entity.ProcessedEventEntity
import org.kenenna.evoelt.jpa.repository.RawSequenceRepository
import org.kenenna.evoelt.jpa.repository.RawEventRepository
import org.kenenna.evoelt.jpa.repository.ProcessedSequenceRepository
import org.kenenna.evoelt.jpa.repository.ProcessedEventRepository
import org.kenenna.evoelt.events.implementation.Sqs
import org.kenenna.evoelt.utils.MessageBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant.now
import java.util.*

@Component
@EnableScheduling
class EventConsumer(
    @Autowired val sqs: Sqs,
    @Autowired val sqsConsumerQueueUrl: String,
    @Autowired val sqsProducerQueueUrl: String,
    @Autowired val rawSequenceRepository: RawSequenceRepository,
    @Autowired val rawEventRepository: RawEventRepository,
    @Autowired val processedSequenceRepository: ProcessedSequenceRepository,
    @Autowired val processedEventRepository: ProcessedEventRepository,
) {
    val log: Logger = LoggerFactory.getLogger(EventConsumer::class.java)

    @Scheduled(fixedRate = 1)
    fun scheduleReceiveMessages() {
        receiveMessages()
    }

    fun receiveMessages(waitTimeSeconds: Int = 10) {
        for (mqMessage in sqs.getMessages(sqsConsumerQueueUrl, waitTimeSeconds)) {
            val messageId = mqMessage.id
            log.info("Received SQS Message ID {}", messageId)

            val messageBody = mqMessage.body
            if (messageBody.getRawEventId().isEmpty()) {
                log.info("Saving Raw Event...")
                saveRawEvent(messageBody)
            } else {
                log.info("Saving Processed Event...")
                saveProcessedEvent(messageBody)
            }

            sqs.deleteMessage(sqsConsumerQueueUrl, messageId)
            log.info("Deleted SQS Message ID {}", messageId)
        }
    }

    private fun saveRawEvent(messageBody: MessageBody) {
        var rawSequence = rawSequenceRepository.findByLabels(messageBody.getLabels())
        if(rawSequence == null){
            rawSequence = RawSequenceEntity(
                labels = messageBody.getLabels()
            )
        } else {
            rawSequence.updatedDt = Timestamp.from(now())
        }

        val rawSequenceId = rawSequenceRepository.save(rawSequence).id

        var rawEvent = RawEventEntity(
            rawSequenceId = rawSequenceId!!,
            rawSequenceOrderId = rawEventRepository.countByRawSequenceId(rawSequenceId) + 1,
            data = messageBody.getData()
        )
        rawEvent = rawEventRepository.save(rawEvent)
        log.info("Saved Raw Event {}", rawEvent.id)

        val ackResponse = sqs.sendMessage(
            queueUrl = sqsProducerQueueUrl,
            message = JSONObject()
                .put("raw_event_id", rawEvent.id)
                .put("labels", rawSequence.labels)
                .toString(),

        )
        log.info("Sent raw SQS ACK {}", ackResponse.messageId())
    }

    private fun saveProcessedEvent(messageBody: MessageBody) {
        val rawSequenceId = rawEventRepository.findById(
            UUID.fromString(messageBody.getRawEventId())
        ).get().rawSequenceId

        var processedSequence = processedSequenceRepository.findByRawSequenceIdAndLabels(
            rawSequenceId,
            messageBody.getLabels()
        )
        if (processedSequence == null){
            processedSequence = ProcessedSequenceEntity(
                rawSequenceId = rawSequenceId,
                labels = messageBody.getLabels()
            )
        } else {
            processedSequence.updatedDt = Timestamp.from(now())
        }
        processedSequenceRepository.save(processedSequence)

        val processedEvent = processedEventRepository.save(ProcessedEventEntity(
            rawEventId = UUID.fromString(messageBody.getRawEventId()),
            data = messageBody.getData(),
            processedSequenceId = processedSequence.id!!
        ))
        log.info("Saved Processed Event {}", processedEvent)
    }
}
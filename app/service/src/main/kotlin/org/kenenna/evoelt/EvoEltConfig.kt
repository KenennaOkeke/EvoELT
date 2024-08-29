package org.kenenna.evoelt

import org.kenenna.evoelt.events.EventConsumer
import org.kenenna.evoelt.events.implementation.Sqs
import org.kenenna.evoelt.jpa.repository.ProcessedEventRepository
import org.kenenna.evoelt.jpa.repository.ProcessedSequenceRepository
import org.kenenna.evoelt.jpa.repository.RawEventRepository
import org.kenenna.evoelt.jpa.repository.RawSequenceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
class EvoEltConfig {
    @Bean
    fun sqsClient(@Value("\${mq.sqs.endpoint}") endpointOverride: String): SqsClient {
        val builder = SqsClient.builder()
        if(endpointOverride.isNotBlank()) {
            builder.endpointOverride(URI(endpointOverride))
        }
        return builder.build()
    }

    @Bean
    fun sqsProducerQueueUrl(@Value("\${mq.sqs.producer.queue.url}") queueUrl: String): String {
        return queueUrl
    }

    @Bean
    fun sqsConsumerQueueUrl(@Value("\${mq.sqs.consumer.queue.url}") queueUrl: String): String {
        return queueUrl
    }

    @Bean
    fun sqs(@Autowired sqsClient: SqsClient): Sqs {
        return Sqs(sqsClient)
    }

    @Bean
    fun eventConsumer(
        @Autowired sqs: Sqs,
        @Autowired sqsConsumerQueueUrl: String,
        @Autowired sqsProducerQueueUrl: String,
        @Autowired rawSequenceRepository: RawSequenceRepository,
        @Autowired rawEventRepository: RawEventRepository,
        @Autowired processedSequenceRepository: ProcessedSequenceRepository,
        @Autowired processedEventRepository: ProcessedEventRepository
        ): EventConsumer {
        return EventConsumer(
            sqs,
            sqsConsumerQueueUrl,
            sqsProducerQueueUrl,
            rawSequenceRepository,
            rawEventRepository,
            processedSequenceRepository,
            processedEventRepository
        )
    }
}
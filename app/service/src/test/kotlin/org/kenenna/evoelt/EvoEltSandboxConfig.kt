package org.kenenna.evoelt

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import software.amazon.awssdk.regions.Region.US_EAST_1
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

class EvoEltSandboxConfig: EvoEltConfig() {
    @Bean
    override fun sqsClient(@Value("\${mq.sqs.endpoint}") endpointOverride: String): SqsClient {
        val builder = SqsClient.builder().region(US_EAST_1)
        if(endpointOverride.isNotBlank()) {
            builder.endpointOverride(URI(endpointOverride))
        }
        return builder.build()
    }
}
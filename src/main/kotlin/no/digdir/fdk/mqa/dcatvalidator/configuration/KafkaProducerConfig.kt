package no.digdir.fdk.mqa.dcatvalidator.configuration

import no.fdk.mqa.MQAEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
open class KafkaProducerConfig {
    @Bean
    open fun kafkaTemplate(producerFactory: ProducerFactory<String, MQAEvent>): KafkaTemplate<String, MQAEvent> {
        return KafkaTemplate(producerFactory)
    }
}

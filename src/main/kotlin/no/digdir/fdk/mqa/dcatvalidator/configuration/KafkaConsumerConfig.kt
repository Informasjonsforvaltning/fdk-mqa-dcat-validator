package no.digdir.fdk.mqa.dcatvalidator.configuration

import no.fdk.mqa.DatasetEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@EnableKafka
@Configuration
open class KafkaConsumerConfig {

    @Bean
    open fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, DatasetEvent>): ConcurrentKafkaListenerContainerFactory<String, DatasetEvent> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, DatasetEvent> = ConcurrentKafkaListenerContainerFactory()
        factory.setConsumerFactory(consumerFactory)
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        return factory
    }
}

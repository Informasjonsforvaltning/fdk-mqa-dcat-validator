package no.digdir.fdk.mqa.dcatvalidator.configuration

import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.fdk.mqa.MQAEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
open class KafkaProducerConfig(
    @param:Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @param:Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String,
) {
    @Bean
    open fun producerFactory(): ProducerFactory<String, MQAEvent> {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        props["schema.registry.url"] = schemaRegistryUrl
        props["auto.register.schemas"] = false
        props["use.latest.version"] = true
        props["value.subject.name.strategy"] = "io.confluent.kafka.serializers.subject.RecordNameStrategy"
        props["key.subject.name.strategy"] = "io.confluent.kafka.serializers.subject.RecordNameStrategy"
        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    open fun kafkaTemplate(producerFactory: ProducerFactory<String, MQAEvent>): KafkaTemplate<String, MQAEvent> {
        return KafkaTemplate(producerFactory)
    }
}

package no.digdir.fdk.mqa.dcatvalidator.configuration

import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.fdk.mqa.MQAEvent
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
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
    @param:Value("\${spring.kafka.properties.security.protocol}") private val securityProtocol: String,
    @param:Value("\${spring.kafka.properties.sasl.mechanism}") private val saslMechanism: String,
    @param:Value("\${spring.kafka.properties.sasl.jaas.config}") private val saslJaasConfig: String,
    @param:Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String,
) {
    @Bean
    open fun producerFactory(): ProducerFactory<String, MQAEvent> {
        val props =
            mutableMapOf<String, Any>(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to securityProtocol,
                SaslConfigs.SASL_MECHANISM to saslMechanism,
                SaslConfigs.SASL_JAAS_CONFIG to saslJaasConfig,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            )
        props.putAll(KafkaAvroProperties.common(schemaRegistryUrl))
        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    open fun kafkaTemplate(producerFactory: ProducerFactory<String, MQAEvent>): KafkaTemplate<String, MQAEvent> =
        KafkaTemplate(producerFactory)
}

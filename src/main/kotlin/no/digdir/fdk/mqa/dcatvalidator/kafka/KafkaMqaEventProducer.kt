package no.digdir.fdk.mqa.dcatvalidator.kafka

import io.micrometer.core.instrument.Metrics
import no.fdk.mqa.MQAEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component


@Component
class KafkaMqaEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, MQAEvent>
) {
    fun sendMQAEvent(mqaEvent: MQAEvent) {
        kafkaTemplate.send("mqa-events", mqaEvent).handle { result, exception ->
            if (exception != null) {
                LOGGER.error("Error sending MQA event: " + exception.message)
                Metrics.counter("produced_messages", "status", "error").increment()
            } else {
                LOGGER.debug("MQA event sent - offset: " + result.recordMetadata.offset())
                Metrics.counter("produced_messages", "status", "success").increment()
            }
            null
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaMqaEventProducer::class.java)
    }
}

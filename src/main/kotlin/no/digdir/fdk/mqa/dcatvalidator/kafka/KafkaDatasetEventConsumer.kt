package no.digdir.fdk.mqa.dcatvalidator.kafka

import no.fdk.mqa.DatasetEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class KafkaDatasetEventConsumer(
    private val circuitBreaker: KafkaDatasetEventCircuitBreaker
) {
    @KafkaListener(
        topics = ["mqa-dataset-events"],
        groupId = "fdk-mqa-dcat-validator",
        concurrency = "4",
        containerFactory = "kafkaListenerContainerFactory",
        id = "mqa-dataset"
    )
    fun listen(record: ConsumerRecord<String, DatasetEvent>, ack: Acknowledgment) {
        try {
            circuitBreaker.process(record)
            ack.acknowledge()
        } catch (e: Exception) {
            ack.nack(Duration.ZERO)
        }
    }
}

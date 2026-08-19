package no.digdir.fdk.mqa.dcatvalidator.kafka

import no.fdk.mqa.DatasetEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class KafkaDatasetEventConsumer(private val datasetEventProcessor: DatasetEventProcessor) {
    @KafkaListener(
        topics = ["\${application.kafka.topics.dataset-events}"],
        groupId = "\${application.kafka.group-id}",
        concurrency = "4",
        containerFactory = "kafkaListenerContainerFactory",
        id = MQA_DATASET_LISTENER_ID,
    )
    fun listen(record: ConsumerRecord<String, DatasetEvent>, ack: Acknowledgment) {
        try {
            datasetEventProcessor.process(record)
            ack.acknowledge()
        } catch (e: Exception) {
            ack.nack(Duration.ZERO)
        }
    }

    companion object {
        const val MQA_DATASET_LISTENER_ID = "mqa-dataset"
    }
}

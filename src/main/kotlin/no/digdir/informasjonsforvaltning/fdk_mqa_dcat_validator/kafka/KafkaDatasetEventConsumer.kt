package no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.kafka

import io.micrometer.core.instrument.Metrics
import no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.service.DcatComplianceService
import no.fdk.mqa.DatasetEvent
import no.fdk.mqa.DatasetEventType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.system.measureTimeMillis


@Component
class KafkaDatasetEventConsumer {

    @Autowired
    private val dcatComplianceService: DcatComplianceService? = null

    @Autowired
    private val kafkaProducer: KafkaMqaEventProducer? = null

    @KafkaListener(
        topics = ["mqa-dataset-events"],
        groupId = "fdk-mqa-dcat-validator",
        concurrency = "4",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun listen(record: ConsumerRecord<String, DatasetEvent>, ack: Acknowledgment) {
        LOGGER.debug("Received message - offset: " + record.offset())

        val event = record.value()
        if(event?.type == DatasetEventType.DATASET_HARVESTED) {
            try {
                val elapsed = measureTimeMillis {
                    val mqaEvent = dcatComplianceService!!.validateDcatCompliance(event)
                    mqaEvent.let {
                        // Send an MQA event of type DCAT_COMPLIANCE_CHECKED
                        LOGGER.debug("Send MQAEvent with quality measurement - fdkId: " + event.fdkId)
                        kafkaProducer!!.sendMQAEvent(it!!)
                    }
                    LOGGER.debug("Sending acknowledgement - fdkId: " + event.fdkId)
                    ack.acknowledge()
                }
                Metrics.counter("processed_messages", "status", "success").increment()
                Metrics.timer("processing_time").record(Duration.ofMillis(elapsed))
            } catch (e: Exception) {
                LOGGER.error("Error processing message: " + e.message)
                Metrics.counter("processed_messages", "status", "error").increment()
            }
        } else {
            LOGGER.debug("Message type not supported, skipping message - offset: " + record.offset())
            Metrics.counter("processed_messages", "status", "skipped").increment()

            ack.acknowledge()
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaDatasetEventConsumer::class.java)
    }
}
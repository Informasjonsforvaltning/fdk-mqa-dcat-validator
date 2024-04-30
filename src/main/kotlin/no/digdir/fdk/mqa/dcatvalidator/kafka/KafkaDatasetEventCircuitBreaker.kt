package no.digdir.fdk.mqa.dcatvalidator.kafka

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.micrometer.core.instrument.Metrics
import no.digdir.fdk.mqa.dcatvalidator.service.DcatComplianceService
import no.fdk.mqa.DatasetEvent
import no.fdk.mqa.DatasetEventType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.system.measureTimeMillis


@Component
open class KafkaDatasetEventCircuitBreaker(
    private val dcatComplianceService: DcatComplianceService,
    private val kafkaProducer: KafkaMqaEventProducer
) {
    @CircuitBreaker(name = "mqa-dataset-cb")
    open fun process(record: ConsumerRecord<String, DatasetEvent>) {
        LOGGER.debug("Received message - offset: " + record.offset())

        val event = record.value()
        if(event?.type == DatasetEventType.DATASET_HARVESTED) {
            try {
                val elapsed = measureTimeMillis {
                    val mqaEvent = dcatComplianceService.validateDcatCompliance(event)
                    mqaEvent.let {
                        // Send an MQA event of type DCAT_COMPLIANCE_CHECKED
                        LOGGER.debug("Send MQAEvent with quality measurement - fdkId: " + event.fdkId)
                        kafkaProducer.sendMQAEvent(it!!)
                    }
                    LOGGER.debug("Sending acknowledgement - fdkId: " + event.fdkId)
                }
                Metrics.counter("processed_messages", "status", "success").increment()
                Metrics.timer("processing_time").record(Duration.ofMillis(elapsed))
            } catch (e: Exception) {
                LOGGER.error("Error processing message: " + e.message)
                Metrics.counter("processed_messages", "status", "error").increment()
                throw e
            }
        } else {
            LOGGER.debug("Message type not supported, skipping message - offset: " + record.offset())
            Metrics.counter("processed_messages", "status", "skipped").increment()
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaDatasetEventCircuitBreaker::class.java)
    }
}

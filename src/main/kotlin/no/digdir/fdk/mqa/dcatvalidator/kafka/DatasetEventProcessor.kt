package no.digdir.fdk.mqa.dcatvalidator.kafka

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.micrometer.core.instrument.Metrics
import no.digdir.fdk.mqa.dcatvalidator.service.DcatComplianceService
import no.fdk.mqa.DatasetEvent
import no.fdk.mqa.DatasetEventType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.system.measureTimeMillis

@Component
class DatasetEventProcessor(
    private val dcatComplianceService: DcatComplianceService,
    private val kafkaProducer: KafkaMqaEventProducer,
    @param:Qualifier("mqaDatasetCircuitBreaker")
    private val circuitBreaker: CircuitBreaker,
) {
    fun process(record: ConsumerRecord<String, DatasetEvent>) {
        circuitBreaker.executeRunnable {
            LOGGER.debug("Received message - offset: {}", record.offset())

            val event = record.value()
            if (event?.type != DatasetEventType.DATASET_HARVESTED) {
                LOGGER.debug("Message type not supported, skipping message - offset: {}", record.offset())
                Metrics.counter("processed_messages", "status", "skipped").increment()
                return@executeRunnable
            }

            try {
                val elapsed = measureTimeMillis {
                    val mqaEvent = dcatComplianceService.validateDcatCompliance(event)
                    if (mqaEvent != null) {
                        LOGGER.debug("Send MQAEvent with quality measurement - fdkId: {}", event.fdkId)
                        kafkaProducer.sendMQAEvent(mqaEvent)
                    }
                }
                recordSuccess(elapsed)
            } catch (e: Exception) {
                LOGGER.error("Error processing message: {}", e.message)
                Metrics.counter("processed_messages", "status", "error").increment()
                throw e
            }
        }
    }

    private fun recordSuccess(elapsedMillis: Long) {
        Metrics.counter("processed_messages", "status", "success").increment()
        Metrics.timer("processing_time").record(Duration.ofMillis(elapsedMillis))
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(DatasetEventProcessor::class.java)
    }
}

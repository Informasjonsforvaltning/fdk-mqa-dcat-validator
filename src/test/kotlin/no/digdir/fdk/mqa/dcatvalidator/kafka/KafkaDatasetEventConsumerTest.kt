package no.digdir.fdk.rdf.parse.eventpublisher.kafka

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.digdir.fdk.mqa.dcatvalidator.kafka.KafkaDatasetEventCircuitBreaker
import no.digdir.fdk.mqa.dcatvalidator.kafka.KafkaDatasetEventConsumer
import no.digdir.fdk.mqa.dcatvalidator.kafka.KafkaMqaEventProducer
import no.digdir.fdk.mqa.dcatvalidator.service.DcatComplianceService
import no.fdk.mqa.DatasetEvent
import no.fdk.mqa.DatasetEventType
import no.fdk.mqa.MQAEvent
import no.fdk.mqa.MQAEventType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

@ActiveProfiles("test")
class KafkaDatasetEventConsumerTest {
    private val dcatComplianceService: DcatComplianceService = mockk()
    private val kafkaTemplate: KafkaTemplate<String, MQAEvent> = mockk()
    private val ack: Acknowledgment = mockk()
    private val kafkaMqaEventProducer = KafkaMqaEventProducer(kafkaTemplate)
    private val circuitBreaker = KafkaDatasetEventCircuitBreaker(dcatComplianceService, kafkaMqaEventProducer)
    private val kafkaDatasetEventConsumer = KafkaDatasetEventConsumer(circuitBreaker)

    @Test
    fun `listen should produce a mqa event with valid dcat`() {
        val timestamp = System.currentTimeMillis()
        val validDcatMQAEvent = MQAEvent(
            MQAEventType.DCAT_COMPLIANCE_CHECKED,
            "fdk-id-valid",
            "assessment-graph-valid",
            timestamp
        )
        every { dcatComplianceService.validateDcatCompliance(any()) } returns validDcatMQAEvent
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent = DatasetEvent(DatasetEventType.DATASET_HARVESTED, "fdk-id-valid", "uri", timestamp)
        kafkaDatasetEventConsumer.listen(
            record = ConsumerRecord("dataset-events", 0, 0, "fdk-id-valid", datasetEvent),
            ack = ack
        )

        verify {
            kafkaTemplate.send(withArg {
                assertEquals("mqa-events", it)
            }, withArg {
                assertEquals(datasetEvent.fdkId, it.fdkId)
                assertEquals(MQAEventType.DCAT_COMPLIANCE_CHECKED, it.type)
                assertEquals("assessment-graph-valid", it.graph)
                assertEquals(datasetEvent.timestamp, it.timestamp)
            })
            ack.acknowledge()
        }
        confirmVerified(kafkaTemplate, ack)
     }

    @Test
    fun `listen should produce a mqa event with invalid dcat`() {
        val timestamp = System.currentTimeMillis()
        val invalidDcatMQAEvent = MQAEvent(
            MQAEventType.DCAT_COMPLIANCE_CHECKED,
            "fdk-id-invalid",
            "assessment-graph-invalid",
            timestamp
        )

        every { dcatComplianceService.validateDcatCompliance(any()) } returns invalidDcatMQAEvent
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent = DatasetEvent(DatasetEventType.DATASET_HARVESTED, "fdk-id-invalid", "uri", timestamp)
        kafkaDatasetEventConsumer.listen(
            record = ConsumerRecord("dataset-events", 0, 0, "fdk-id-invalid", datasetEvent),
            ack = ack
        )

        verify {
            kafkaTemplate.send(withArg {
                assertEquals("mqa-events", it)
            }, withArg {
                assertEquals(datasetEvent.fdkId, it.fdkId)
                assertEquals(MQAEventType.DCAT_COMPLIANCE_CHECKED, it.type)
                assertEquals("assessment-graph-invalid", it.graph)
                assertEquals(datasetEvent.timestamp, it.timestamp)
            })
            ack.acknowledge()
        }
        confirmVerified(kafkaTemplate, ack)
    }

    @Test
    fun `listen should not acknowledge when an exception occurs`() {
        every { dcatComplianceService.validateDcatCompliance(any()) } throws RuntimeException("Error validating DCAT compliance")
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent = DatasetEvent(DatasetEventType.DATASET_HARVESTED, "fdk-id-invalid", "uri", System.currentTimeMillis())
        kafkaDatasetEventConsumer.listen(
            record = ConsumerRecord("dataset-events", 0, 0, "fdk-id-invalid", datasetEvent),
            ack = ack
        )

        verify(exactly = 0) { kafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { ack.nack(Duration.ZERO) }
        verify(exactly = 0) { ack.acknowledge() }
        confirmVerified(kafkaTemplate, ack)
    }

}

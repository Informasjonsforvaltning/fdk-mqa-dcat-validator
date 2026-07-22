package no.digdir.fdk.mqa.dcatvalidator.kafka

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.digdir.fdk.mqa.dcatvalidator.configuration.ApplicationKafkaProperties
import no.digdir.fdk.mqa.dcatvalidator.service.DcatComplianceService
import no.fdk.mqa.DatasetEvent
import no.fdk.mqa.DatasetEventType
import no.fdk.mqa.MQAEvent
import no.fdk.mqa.MQAEventType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

class KafkaDatasetEventConsumerTest {
    private val dcatComplianceService: DcatComplianceService = mockk()
    private val kafkaTemplate: KafkaTemplate<String, MQAEvent> = mockk()
    private val ack: Acknowledgment = mockk()
    private val applicationKafkaProperties =
        ApplicationKafkaProperties(
            groupId = "fdk-mqa-dcat-validator",
            topics =
                ApplicationKafkaProperties.Topics(
                    datasetEvents = "mqa-dataset-events",
                    mqaEvents = "mqa-events",
                ),
        )
    private val kafkaMqaEventProducer = KafkaMqaEventProducer(kafkaTemplate, applicationKafkaProperties)
    private val datasetEventProcessor =
        DatasetEventProcessor(
            dcatComplianceService,
            kafkaMqaEventProducer,
            CircuitBreaker.ofDefaults("test-cb"),
        )
    private val kafkaDatasetEventConsumer = KafkaDatasetEventConsumer(datasetEventProcessor)

    @ParameterizedTest
    @ValueSource(strings = ["fdk-id-valid", "fdk-id-invalid"])
    fun `listen should produce a mqa event for harvested datasets`(fdkId: String) {
        val timestamp = System.currentTimeMillis()
        val assessmentGraph = "assessment-graph-$fdkId"
        val mqaEvent =
            MQAEvent(
                MQAEventType.DCAT_COMPLIANCE_CHECKED,
                fdkId,
                assessmentGraph,
                timestamp,
            )
        every { dcatComplianceService.validateDcatCompliance(any()) } returns mqaEvent
        every { kafkaTemplate.send(any(), any(), any()) } returns CompletableFuture()
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent = DatasetEvent(DatasetEventType.DATASET_HARVESTED, fdkId, "uri", timestamp)
        kafkaDatasetEventConsumer.listen(
            record = ConsumerRecord("dataset-events", 0, 0, fdkId, datasetEvent),
            ack = ack,
        )

        verify {
            kafkaTemplate.send(
                withArg {
                    assertEquals("mqa-events", it)
                },
                withArg {
                    assertEquals(datasetEvent.fdkId, it)
                },
                withArg {
                    assertEquals(datasetEvent.fdkId, it.fdkId)
                    assertEquals(MQAEventType.DCAT_COMPLIANCE_CHECKED, it.type)
                    assertEquals(assessmentGraph, it.graph)
                    assertEquals(datasetEvent.timestamp, it.timestamp)
                },
            )
            ack.acknowledge()
        }
        confirmVerified(kafkaTemplate, ack)
    }

    @Test
    fun `listen should not acknowledge when an exception occurs`() {
        every { dcatComplianceService.validateDcatCompliance(any()) } throws RuntimeException("Error validating DCAT compliance")
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent =
            DatasetEvent(
                DatasetEventType.DATASET_HARVESTED,
                "fdk-id-invalid",
                "uri",
                System.currentTimeMillis(),
            )
        kafkaDatasetEventConsumer.listen(
            record = ConsumerRecord("dataset-events", 0, 0, "fdk-id-invalid", datasetEvent),
            ack = ack,
        )

        verify(exactly = 0) { kafkaTemplate.send(any(), any(), any()) }
        verify(exactly = 1) { ack.nack(Duration.ZERO) }
        verify(exactly = 0) { ack.acknowledge() }
        confirmVerified(kafkaTemplate, ack)
    }

    @Test
    fun `listen should acknowledge and skip when event is null`() {
        every { ack.acknowledge() } returns Unit

        kafkaDatasetEventConsumer.listen(
            record = ConsumerRecord("dataset-events", 0, 0, "fdk-id", null),
            ack = ack,
        )

        verify(exactly = 0) { dcatComplianceService.validateDcatCompliance(any()) }
        verify(exactly = 0) { kafkaTemplate.send(any(), any(), any()) }
        verify(exactly = 1) { ack.acknowledge() }
        confirmVerified(dcatComplianceService, kafkaTemplate, ack)
    }

    @Test
    fun `listen should acknowledge without producing when validation returns null`() {
        every { dcatComplianceService.validateDcatCompliance(any()) } returns null
        every { ack.acknowledge() } returns Unit

        val datasetEvent =
            DatasetEvent(
                DatasetEventType.DATASET_HARVESTED,
                "fdk-id-skip",
                "uri",
                System.currentTimeMillis(),
            )
        kafkaDatasetEventConsumer.listen(
            record = ConsumerRecord("dataset-events", 0, 0, "fdk-id-skip", datasetEvent),
            ack = ack,
        )

        verify(exactly = 1) { dcatComplianceService.validateDcatCompliance(datasetEvent) }
        verify(exactly = 0) { kafkaTemplate.send(any(), any(), any()) }
        verify(exactly = 1) { ack.acknowledge() }
        confirmVerified(dcatComplianceService, kafkaTemplate, ack)
    }
}

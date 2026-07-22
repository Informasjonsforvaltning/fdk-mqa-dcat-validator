package no.digdir.fdk.mqa.dcatvalidator.configuration

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.mockk.mockk
import io.mockk.verify
import no.digdir.fdk.mqa.dcatvalidator.kafka.KafkaDatasetEventConsumer
import no.digdir.fdk.mqa.dcatvalidator.kafka.KafkaManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CircuitBreakerConsumerConfigurationTest {
    private val kafkaManager: KafkaManager = mockk(relaxUnitFun = true)
    private lateinit var circuitBreaker: CircuitBreaker

    @BeforeEach
    fun setUp() {
        val configuration = CircuitBreakerConsumerConfiguration(kafkaManager)
        val registry = configuration.circuitBreakerRegistry()
        circuitBreaker = registry.circuitBreaker(CircuitBreakerConsumerConfiguration.MQA_DATASET_CIRCUIT_BREAKER_ID)
    }

    @Test
    fun `opens circuit breaker and pauses kafka listener`() {
        circuitBreaker.transitionToOpenState()

        verify(exactly = 1) { kafkaManager.pause(KafkaDatasetEventConsumer.MQA_DATASET_LISTENER_ID) }
        verify(exactly = 0) { kafkaManager.resume(any()) }
    }

    @Test
    fun `moves to half open and resumes kafka listener`() {
        circuitBreaker.transitionToOpenState()
        circuitBreaker.transitionToHalfOpenState()

        verify(exactly = 1) { kafkaManager.pause(KafkaDatasetEventConsumer.MQA_DATASET_LISTENER_ID) }
        verify(exactly = 1) { kafkaManager.resume(KafkaDatasetEventConsumer.MQA_DATASET_LISTENER_ID) }
    }

    @Test
    fun `closes from half open and resumes kafka listener`() {
        circuitBreaker.transitionToOpenState()
        circuitBreaker.transitionToHalfOpenState()
        circuitBreaker.transitionToClosedState()

        verify(exactly = 1) { kafkaManager.pause(KafkaDatasetEventConsumer.MQA_DATASET_LISTENER_ID) }
        verify(exactly = 2) { kafkaManager.resume(KafkaDatasetEventConsumer.MQA_DATASET_LISTENER_ID) }
    }

    @Test
    fun `forced open pauses kafka listener`() {
        circuitBreaker.transitionToForcedOpenState()

        verify(exactly = 1) { kafkaManager.pause(KafkaDatasetEventConsumer.MQA_DATASET_LISTENER_ID) }
        verify(exactly = 0) { kafkaManager.resume(any()) }
    }
}

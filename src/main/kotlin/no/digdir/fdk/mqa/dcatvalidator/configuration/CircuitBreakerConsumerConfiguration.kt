package no.digdir.fdk.mqa.dcatvalidator.configuration

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent
import no.digdir.fdk.mqa.dcatvalidator.kafka.KafkaDatasetEventConsumer
import no.digdir.fdk.mqa.dcatvalidator.kafka.KafkaManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
open class CircuitBreakerConsumerConfiguration(
    private val kafkaManager: KafkaManager,
) {

    @Bean
    open fun circuitBreakerRegistry(): CircuitBreakerRegistry {
        val defaultConfig =
            CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .failureRateThreshold(50f)
                .permittedNumberOfCallsInHalfOpenState(3)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build()

        val registry = CircuitBreakerRegistry.of(defaultConfig)
        attachListener(registry)
        return registry
    }

    private fun attachListener(registry: CircuitBreakerRegistry) {
        registry.circuitBreaker(MQA_DATASET_CIRCUIT_BREAKER_ID)
            .eventPublisher
            .onStateTransition { event: CircuitBreakerOnStateTransitionEvent ->
                handleStateTransition(event)
            }
    }

    private fun handleStateTransition(
        event: CircuitBreakerOnStateTransitionEvent,
    ) {
        LOGGER.debug("Handling state transition in circuit breaker {}", event)
        when (event.stateTransition) {
            StateTransition.CLOSED_TO_OPEN,
            StateTransition.CLOSED_TO_FORCED_OPEN,
            StateTransition.HALF_OPEN_TO_OPEN,
            -> {
                LOGGER.warn("Circuit breaker opened, pausing Kafka listener: {}", KafkaDatasetEventConsumer.MQA_DATASET_LISTENER_ID)
                kafkaManager.pause(KafkaDatasetEventConsumer.MQA_DATASET_LISTENER_ID)
            }

            StateTransition.OPEN_TO_HALF_OPEN,
            StateTransition.HALF_OPEN_TO_CLOSED,
            StateTransition.FORCED_OPEN_TO_CLOSED,
            StateTransition.FORCED_OPEN_TO_HALF_OPEN,
            -> {
                LOGGER.info("Circuit breaker closed, resuming Kafka listener: {}", KafkaDatasetEventConsumer.MQA_DATASET_LISTENER_ID)
                kafkaManager.resume(KafkaDatasetEventConsumer.MQA_DATASET_LISTENER_ID)
            }

            else -> throw IllegalStateException("Unknown transition state: " + event.stateTransition)
        }
    }

    @Bean
    open fun mqaDatasetCircuitBreaker(registry: CircuitBreakerRegistry): CircuitBreaker =
        registry.circuitBreaker(MQA_DATASET_CIRCUIT_BREAKER_ID)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CircuitBreakerConsumerConfiguration::class.java)
        const val MQA_DATASET_CIRCUIT_BREAKER_ID = "mqa-dataset-cb"
    }
}

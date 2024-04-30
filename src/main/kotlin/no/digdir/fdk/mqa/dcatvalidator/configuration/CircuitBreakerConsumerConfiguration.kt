package no.digdir.fdk.mqa.dcatvalidator.configuration

import io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent
import no.digdir.fdk.mqa.dcatvalidator.kafka.KafkaManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

@Configuration
open class CircuitBreakerConsumerConfiguration(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    private val kafkaManager: KafkaManager
) {

    init {
        LOGGER.debug("Configuring circuit breaker event listener")
        circuitBreakerRegistry.circuitBreaker("mqa-dataset-cb").eventPublisher.onStateTransition { event: CircuitBreakerOnStateTransitionEvent ->
            when (event.stateTransition) {
                StateTransition.CLOSED_TO_OPEN,
                StateTransition.CLOSED_TO_FORCED_OPEN,
                StateTransition.HALF_OPEN_TO_OPEN -> kafkaManager.pause("mqa-dataset")

                StateTransition.OPEN_TO_HALF_OPEN,
                StateTransition.HALF_OPEN_TO_CLOSED,
                StateTransition.FORCED_OPEN_TO_CLOSED,
                StateTransition.FORCED_OPEN_TO_HALF_OPEN -> kafkaManager.resume("mqa-dataset")

                else -> throw IllegalStateException("Unknown transition state: " + event.stateTransition)
            }
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CircuitBreakerConsumerConfiguration::class.java)
    }
}

package no.digdir.fdk.mqa.dcatvalidator.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "application.kafka")
data class ApplicationKafkaProperties(
    val groupId: String,
    val topics: Topics,
) {
    data class Topics(
        val datasetEvents: String,
        val mqaEvents: String,
    )
}

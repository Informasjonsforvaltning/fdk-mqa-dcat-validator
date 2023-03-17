package no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean

@ConfigurationProperties("application")
data class ApplicationProperties(
    val prop: String
)

@Bean
fun objectMapper(): ObjectMapper =
    ObjectMapper().enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)

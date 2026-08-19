package no.digdir.fdk.mqa.dcatvalidator.configuration

internal object KafkaAvroProperties {
    fun common(schemaRegistryUrl: String): Map<String, Any> = mapOf(
        "schema.registry.url" to schemaRegistryUrl,
        "auto.register.schemas" to false,
        "use.latest.version" to true,
        "value.subject.name.strategy" to "io.confluent.kafka.serializers.subject.RecordNameStrategy",
        "key.subject.name.strategy" to "io.confluent.kafka.serializers.subject.RecordNameStrategy",
    )
}

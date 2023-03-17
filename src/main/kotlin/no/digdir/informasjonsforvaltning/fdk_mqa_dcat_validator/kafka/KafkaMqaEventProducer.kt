package no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.kafka

import no.fdk.mqa.MQAEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component


@Component
class KafkaMqaEventProducer {

    @Autowired
    private val kafkaTemplate: KafkaTemplate<String, MQAEvent>? = null

    fun sendMQAEvent(mqaEvent: MQAEvent) {
        kafkaTemplate!!.send("mqa-events", mqaEvent)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaMqaEventProducer::class.java)
    }
}
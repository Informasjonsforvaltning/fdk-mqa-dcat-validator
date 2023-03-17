package no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.kafka

import no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.service.DcatComplianceService
import no.fdk.mqa.DatasetEvent
import no.fdk.mqa.DatasetEventType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
class KafkaDatasetEventConsumer {

    @Autowired
    private val dcatComplianceService: DcatComplianceService? = null

    @Autowired
    private val kafkaProducer: KafkaMqaEventProducer? = null

    @KafkaListener(
        topics = ["mqa-dataset-events"],
        groupId = "fdk-mqa-dcat-validator",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun listen(record: ConsumerRecord<String, DatasetEvent>, ack: Acknowledgment) {
        LOGGER.debug("Received message - offset: " + record.offset())

        val event = record.value();
        if(event?.getType() == DatasetEventType.DATASET_HARVESTED) {
            try {
                val mqaEvent = dcatComplianceService!!.validateDcatCompliance(event)
                mqaEvent.let {
                    // Send an MQA event of type DCAT_COMPLIANCE_CHECKED
                    LOGGER.debug("Send MQAEvent with quality measurement - fdkId: " + event.getFdkId())
                    kafkaProducer!!.sendMQAEvent(it!!)
                }
                LOGGER.debug("Sending acknowledgement - fdkId: " + event.getFdkId())
                ack.acknowledge()
            } catch (e: Exception) {
                LOGGER.error("Error processing message: " + e.message)
            }
        } else {
            LOGGER.debug("Message type not supported, skipping message - offset: " + record.offset())
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaDatasetEventConsumer::class.java)
    }
}
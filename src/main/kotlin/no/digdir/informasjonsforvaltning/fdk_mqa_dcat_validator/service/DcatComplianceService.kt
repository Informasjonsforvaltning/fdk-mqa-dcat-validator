package no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.service

import no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.rdf.*
import no.fdk.mqa.DatasetEvent
import no.fdk.mqa.DatasetEventType
import no.fdk.mqa.MQAEvent
import no.fdk.mqa.MQAEventType
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.Lang
import org.apache.jena.shacl.ValidationReport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class DcatComplianceService {

    fun validateDcatCompliance(datasetEvent: DatasetEvent): MQAEvent? {
        if(datasetEvent.getType() != DatasetEventType.DATASET_HARVESTED) {
            throw Exception("Invalid dataset event type")
        }

        LOGGER.debug("Validate DCAT-AP compliance - fdkId: " + datasetEvent.getFdkId())

        val dataModel = loadModel(datasetEvent.getGraph().toString())

        val shapesModel = DcatComplianceService::class.java.getResource(DCAT_AP_NO_SHAPES)
            ?.let { loadModel(it.readText()) }
            ?: run {
                throw Exception("Unable to load shapes")
            }

        val validationReport: ValidationReport = validate(dataModel.graph, shapesModel.graph)

        if(LOGGER.isDebugEnabled) {
            if(validationReport.conforms()) {
                LOGGER.debug("Dataset is DCAT compliant - fdkId: " + datasetEvent.getFdkId())
            } else {
                LOGGER.debug("Dataset is not DCAT compliant - fdkId: " + datasetEvent.getFdkId())
                validationReport.entries.forEach(Consumer {
                    LOGGER.debug("Report - Value: ${it.value()?.toString()}")
                    LOGGER.debug("Report - Path: ${it.resultPath()?.toString()}")
                    LOGGER.debug("Report - Message: ${it.message()}")
                })
            }
        }

        // Create assessment model which we will be included in the MQA event
        val assessmentModel: Model = ModelFactory.createDefaultModel()
        val datasetResource: Resource? = dataModel.getDatasetResource()
        if(datasetResource == null) {
            LOGGER.warn("Model does not contain resource of type Dataset, skipping message - fdkId: ${datasetEvent.getFdkId()}")
        }

        return datasetResource?.let { dr ->
            // Extract the existing assessment for this dataset
            val assessmentResource: Resource? = dataModel.getAssessmentResource(dr)
            if(assessmentResource == null) {
                LOGGER.warn("Model does not contain resource of type Assessment, skipping message - fdkId: ${datasetEvent.getFdkId()}")
            }

            assessmentResource?.let { ar ->
                assessmentModel.addDatasetAssessment(ar, dr)
                assessmentModel.addComplianceQualityMeasurement(ar, dr, validationReport.conforms())

                // Output graph as Turtle
                val assessmentGraph = assessmentModel.writeToString(Lang.TURTLE)

                val mqaEvent = MQAEvent(
                    MQAEventType.DCAT_COMPLIANCE_CHECKED,
                    datasetEvent.getFdkId(),
                    assessmentGraph,
                    datasetEvent.getTimestamp())

                LOGGER.debug("$mqaEvent")
                mqaEvent
            }
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(DcatComplianceService::class.java)
        private const val DCAT_AP_NO_SHAPES: String = "/dcat-ap-no_shacl-shapes-2.0.0.ttl"
    }
}
package no.digdir.fdk.mqa.dcatvalidator.service

import no.digdir.fdk.mqa.dcatvalidator.rdf.addComplianceQualityMeasurement
import no.digdir.fdk.mqa.dcatvalidator.rdf.addDatasetAssessment
import no.digdir.fdk.mqa.dcatvalidator.rdf.getAssessmentResource
import no.digdir.fdk.mqa.dcatvalidator.rdf.getDatasetResource
import no.digdir.fdk.mqa.dcatvalidator.rdf.loadModel
import no.digdir.fdk.mqa.dcatvalidator.rdf.parseShapes
import no.digdir.fdk.mqa.dcatvalidator.rdf.validate
import no.digdir.fdk.mqa.dcatvalidator.rdf.writeToString
import no.fdk.mqa.DatasetEvent
import no.fdk.mqa.MQAEvent
import no.fdk.mqa.MQAEventType
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.shacl.Shapes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DcatComplianceService {
    private val shapes: Shapes by lazy {
        val shapesResource =
            javaClass.getResource(DCAT_AP_NO_SHAPES)
                ?: throw IllegalStateException("Unable to load shapes from $DCAT_AP_NO_SHAPES")
        parseShapes(loadModel(shapesResource.readText()).graph)
    }

    fun validateDcatCompliance(datasetEvent: DatasetEvent): MQAEvent? {
        LOGGER.debug("Validate DCAT-AP compliance - fdkId: {}", datasetEvent.fdkId)

        val dataModel = loadModel(datasetEvent.graph.toString())
        val validationReport = validate(dataModel.graph, shapes)

        if (LOGGER.isDebugEnabled) {
            if (validationReport.conforms()) {
                LOGGER.debug("Dataset is DCAT compliant - fdkId: {}", datasetEvent.fdkId)
            } else {
                LOGGER.debug("Dataset is not DCAT compliant - fdkId: {}", datasetEvent.fdkId)
                validationReport.entries.forEach { entry ->
                    LOGGER.debug("Report - Value: {}", entry.value()?.toString())
                    LOGGER.debug("Report - Path: {}", entry.resultPath()?.toString())
                    LOGGER.debug("Report - Message: {}", entry.message())
                }
            }
        }

        val datasetResource = dataModel.getDatasetResource()
        if (datasetResource == null) {
            LOGGER.warn(
                "Model does not contain resource of type Dataset, skipping message - fdkId: {}",
                datasetEvent.fdkId,
            )
            return null
        }

        val assessmentResource = dataModel.getAssessmentResource(datasetResource)
        if (assessmentResource == null) {
            LOGGER.warn(
                "Model does not contain resource of type Assessment, skipping message - fdkId: {}",
                datasetEvent.fdkId,
            )
            return null
        }

        val assessmentModel = ModelFactory.createDefaultModel()
        assessmentModel.addDatasetAssessment(assessmentResource, datasetResource)
        assessmentModel.addComplianceQualityMeasurement(
            assessmentResource,
            datasetResource,
            validationReport.conforms(),
        )

        val mqaEvent =
            MQAEvent(
                MQAEventType.DCAT_COMPLIANCE_CHECKED,
                datasetEvent.fdkId,
                assessmentModel.writeToString(Lang.TURTLE),
                datasetEvent.timestamp,
            )

        LOGGER.debug("{}", mqaEvent)
        return mqaEvent
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(DcatComplianceService::class.java)
        private const val DCAT_AP_NO_SHAPES: String = "/dcat-ap-no_shacl-shapes-2.0.0.ttl"
    }
}

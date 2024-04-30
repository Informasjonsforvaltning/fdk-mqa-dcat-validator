package no.digdir.fdk.mqa.dcatvalidator.service

import no.digdir.fdk.mqa.dcatvalidator.TestData
import no.digdir.fdk.mqa.dcatvalidator.rdf.DQV
import no.digdir.fdk.mqa.dcatvalidator.rdf.loadModel
import no.digdir.fdk.mqa.dcatvalidator.rdf.writeToString
import no.fdk.mqa.DatasetEvent
import no.fdk.mqa.DatasetEventType
import no.fdk.mqa.MQAEventType
import org.apache.jena.riot.Lang
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Tag("unit")
class DcatComplianceServiceTest {

    private val dcatComplianceService = DcatComplianceService()

    @Test
    fun complianceValidationReturnsNonCompliantMQAEventAndAssessment() {
        val datasetEventModel = DcatComplianceService::class.java.getResource(TestData.TEST_DATA_NON_COMPLIANT_DATASET_EVENT)
            ?.let { loadModel(it.readText()) }
            ?: run {
                throw Exception("Unable to load test data")
            }

        val expectedMqaEventModel = DcatComplianceService::class.java.getResource(TestData.TEST_DATA_NON_COMPLIANT_MQA_EVENT)
            ?.let { loadModel(it.readText()) }
            ?: run {
                throw Exception("Unable to load test data")
            }

        val datasetEvent = DatasetEvent(
            DatasetEventType.DATASET_HARVESTED,
            "1234",
            datasetEventModel.writeToString(Lang.TURTLE),
            System.currentTimeMillis()
        )

        val mqaEvent = dcatComplianceService.validateDcatCompliance(datasetEvent)!!
        val actualMqaEventModel = loadModel(mqaEvent.getGraph().toString())
        val qm = actualMqaEventModel.listSubjectsWithProperty(RDF.type, DQV.QualityMeasurement).next()
        val qmValue = actualMqaEventModel.listObjectsOfProperty(qm, DQV.value).next()

        assertFalse(qmValue.asLiteral().boolean)
        assertEquals(MQAEventType.DCAT_COMPLIANCE_CHECKED, mqaEvent.getType())
        assertEquals(datasetEvent.getFdkId(), mqaEvent.getFdkId())
        assertEquals(datasetEvent.getTimestamp(), mqaEvent.getTimestamp())
        assertTrue(expectedMqaEventModel.isIsomorphicWith(actualMqaEventModel))
    }

    @Test
    fun complianceValidationReturnsCompliantMQAEventAndAssessment() {
        val datasetEventModel = DcatComplianceService::class.java.getResource(TestData.TEST_DATA_COMPLIANT_DATASET_EVENT)
            ?.let { loadModel(it.readText()) }
            ?: run {
                throw Exception("Unable to load test data")
            }

        val expectedMqaEventModel = DcatComplianceService::class.java.getResource(TestData.TEST_DATA_COMPLIANT_MQA_EVENT)
            ?.let { loadModel(it.readText()) }
            ?: run {
                throw Exception("Unable to load test data")
            }

        val datasetEvent = DatasetEvent(
            DatasetEventType.DATASET_HARVESTED,
            "1234",
            datasetEventModel.writeToString(Lang.TURTLE),
            System.currentTimeMillis()
        )

        val mqaEvent = dcatComplianceService.validateDcatCompliance(datasetEvent)!!
        val actualMqaEventModel = loadModel(mqaEvent.getGraph().toString())
        val qm = actualMqaEventModel.listSubjectsWithProperty(RDF.type, DQV.QualityMeasurement).next()
        val qmValue = actualMqaEventModel.listObjectsOfProperty(qm, DQV.value).next()

        assertTrue(qmValue.asLiteral().boolean)
        assertEquals(MQAEventType.DCAT_COMPLIANCE_CHECKED, mqaEvent.getType())
        assertEquals(datasetEvent.getFdkId(), mqaEvent.getFdkId())
        assertEquals(datasetEvent.getTimestamp(), mqaEvent.getTimestamp())
        assertTrue(expectedMqaEventModel.isIsomorphicWith(actualMqaEventModel))
    }

    @Test
    fun complianceValidationReturnsNullWhenDatasetEventIsInvalid() {
        val datasetEventModel = DcatComplianceService::class.java.getResource(TestData.TEST_DATA_INVALID_DATASET_EVENT)
            ?.let { loadModel(it.readText()) }
            ?: run {
                throw Exception("Unable to load test data")
            }

        val datasetEvent = DatasetEvent(
            DatasetEventType.DATASET_HARVESTED,
            "1234",
            datasetEventModel.writeToString(Lang.TURTLE),
            System.currentTimeMillis()
        )

        val mqaEvent = dcatComplianceService.validateDcatCompliance(datasetEvent)

        assertEquals(null, mqaEvent)
    }

}

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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class DcatComplianceServiceTest {
    private val dcatComplianceService = DcatComplianceService()

    @ParameterizedTest
    @MethodSource("complianceCases")
    fun complianceValidationReturnsExpectedMQAEventAndAssessment(
        datasetEventPath: String,
        expectedMqaEventPath: String,
        expectedCompliant: Boolean,
    ) {
        val datasetEventModel = TestData.loadTestModel(datasetEventPath)
        val expectedMqaEventModel = TestData.loadTestModel(expectedMqaEventPath)

        val datasetEvent =
            DatasetEvent(
                DatasetEventType.DATASET_HARVESTED,
                "1234",
                datasetEventModel.writeToString(Lang.TURTLE),
                System.currentTimeMillis(),
            )

        val mqaEvent = dcatComplianceService.validateDcatCompliance(datasetEvent)!!
        val actualMqaEventModel = loadModel(mqaEvent.graph.toString())
        val qm = actualMqaEventModel.listSubjectsWithProperty(RDF.type, DQV.QualityMeasurement).next()
        val qmValue = actualMqaEventModel.listObjectsOfProperty(qm, DQV.value).next()

        assertEquals(expectedCompliant, qmValue.asLiteral().boolean)
        assertEquals(MQAEventType.DCAT_COMPLIANCE_CHECKED, mqaEvent.type)
        assertEquals(datasetEvent.fdkId, mqaEvent.fdkId)
        assertEquals(datasetEvent.timestamp, mqaEvent.timestamp)
        assertEquals(true, expectedMqaEventModel.isIsomorphicWith(actualMqaEventModel))
    }

    @Test
    fun complianceValidationReturnsNullWhenDatasetEventIsInvalid() {
        val datasetEventModel = TestData.loadTestModel(TestData.INVALID_DATASET_EVENT)

        val datasetEvent =
            DatasetEvent(
                DatasetEventType.DATASET_HARVESTED,
                "1234",
                datasetEventModel.writeToString(Lang.TURTLE),
                System.currentTimeMillis(),
            )

        assertNull(dcatComplianceService.validateDcatCompliance(datasetEvent))
    }

    companion object {
        @JvmStatic
        fun complianceCases() =
            listOf(
                Arguments.of(TestData.COMPLIANT_DATASET_EVENT, TestData.COMPLIANT_MQA_EVENT, true),
                Arguments.of(TestData.NON_COMPLIANT_DATASET_EVENT, TestData.NON_COMPLIANT_MQA_EVENT, false),
            )
    }
}

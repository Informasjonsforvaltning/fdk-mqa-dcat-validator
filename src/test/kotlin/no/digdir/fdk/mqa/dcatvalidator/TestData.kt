package no.digdir.fdk.mqa.dcatvalidator

import no.digdir.fdk.mqa.dcatvalidator.rdf.loadModel
import org.apache.jena.rdf.model.Model

object TestData {
    const val NON_COMPLIANT_DATASET_EVENT = "/non-compliant-dataset-event.ttl"
    const val NON_COMPLIANT_MQA_EVENT = "/non-compliant-mqa-event.ttl"
    const val COMPLIANT_DATASET_EVENT = "/compliant-dataset-event.ttl"
    const val COMPLIANT_MQA_EVENT = "/compliant-mqa-event.ttl"
    const val INVALID_DATASET_EVENT = "/invalid-dataset-event.ttl"

    fun loadTestModel(path: String): Model {
        val resource =
            javaClass.getResource(path)
                ?: error("Unable to load test data: $path")
        return loadModel(resource.readText())
    }
}

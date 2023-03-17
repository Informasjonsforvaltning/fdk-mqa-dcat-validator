package no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class DCATMQA {
    companion object {
        const val uri = "https://data.norge.no/vocabulary/dcatno-mqa#"

        val assessmentOf: Property = ResourceFactory.createProperty("${uri}assessmentOf")
        val hasAssessment: Property = ResourceFactory.createProperty("${uri}hasAssessment")
        val containsQualityMeasurement: Property = ResourceFactory.createProperty("${uri}containsQualityMeasurement")
        val dcatApCompliance: Resource = ResourceFactory.createResource("${uri}dcatApCompliance")
        val DatasetAssessment: Resource = ResourceFactory.createResource("${uri}DatasetAssessment")
    }
}
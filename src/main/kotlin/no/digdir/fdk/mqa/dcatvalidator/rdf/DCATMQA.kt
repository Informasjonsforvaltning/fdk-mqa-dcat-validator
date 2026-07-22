package no.digdir.fdk.mqa.dcatvalidator.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

object DCATMQA {
    const val URI = "https://data.norge.no/vocabulary/dcatno-mqa#"

    val assessmentOf: Property = ResourceFactory.createProperty("${URI}assessmentOf")
    val hasAssessment: Property = ResourceFactory.createProperty("${URI}hasAssessment")
    val containsQualityMeasurement: Property = ResourceFactory.createProperty("${URI}containsQualityMeasurement")
    val dcatApCompliance: Resource = ResourceFactory.createResource("${URI}dcatApCompliance")
    val DatasetAssessment: Resource = ResourceFactory.createResource("${URI}DatasetAssessment")
}

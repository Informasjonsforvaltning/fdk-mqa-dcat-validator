package no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class DQV {
    companion object {
        const val uri = "http://www.w3.org/ns/dqv#"

        val value: Property = ResourceFactory.createProperty("${uri}value")
        val computedOn: Property = ResourceFactory.createProperty("${uri}computedOn")
        val isMeasurementOf: Property = ResourceFactory.createProperty("${uri}isMeasurementOf")
        val QualityMeasurement: Resource = ResourceFactory.createResource("${uri}QualityMeasurement")
    }
}
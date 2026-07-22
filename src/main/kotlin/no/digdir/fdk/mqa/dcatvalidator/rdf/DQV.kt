package no.digdir.fdk.mqa.dcatvalidator.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

object DQV {
    const val URI = "http://www.w3.org/ns/dqv#"

    val value: Property = ResourceFactory.createProperty("${URI}value")
    val computedOn: Property = ResourceFactory.createProperty("${URI}computedOn")
    val isMeasurementOf: Property = ResourceFactory.createProperty("${URI}isMeasurementOf")
    val QualityMeasurement: Resource = ResourceFactory.createResource("${URI}QualityMeasurement")
}

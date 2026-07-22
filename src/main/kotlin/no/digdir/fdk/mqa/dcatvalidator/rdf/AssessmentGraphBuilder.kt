package no.digdir.fdk.mqa.dcatvalidator.rdf

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF

fun Model.getDatasetResource(): Resource? = listSubjectsWithProperty(RDF.type, DCAT.Dataset).nextOptional().orElse(null)

fun Model.getAssessmentResource(dataset: Resource): Resource? =
    listObjectsOfProperty(dataset, DCATMQA.hasAssessment).nextOptional().map { it.asResource() }.orElse(null)

fun Model.addDatasetAssessment(
    assessment: Resource,
    dataset: Resource,
) {
    add(assessment, RDF.type, DCATMQA.DatasetAssessment)
    add(assessment, DCATMQA.assessmentOf, dataset)
}

fun Model.addComplianceQualityMeasurement(
    assessment: Resource,
    dataset: Resource,
    compliant: Boolean,
) {
    val measurement: Resource = createResource()
    add(measurement, RDF.type, DQV.QualityMeasurement)
    add(measurement, DQV.isMeasurementOf, DCATMQA.dcatApCompliance)
    add(measurement, DQV.computedOn, dataset)
    add(measurement, DQV.value, createTypedLiteral(compliant))
    add(assessment, DCATMQA.containsQualityMeasurement, measurement)
}

package no.digdir.informasjonsforvaltning.fdk_mqa_dcat_validator.rdf

import org.apache.jena.graph.Graph
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.shacl.ShaclValidator
import org.apache.jena.shacl.Shapes
import org.apache.jena.shacl.ValidationReport
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets


fun validate(data: Graph, shapes: Graph): ValidationReport {
    val shapes: Shapes = Shapes.parse(shapes)
    return ShaclValidator.get().validate(shapes, data)
}

fun loadModel(graph: String): Model {
    val model = ModelFactory.createDefaultModel()
    RDFDataMgr.read(model, graph.byteInputStream(StandardCharsets.UTF_8), Lang.TURTLE)
    return model
}

fun Model.getDatasetResource(): Resource? =
    listSubjectsWithProperty(RDF.type, DCAT.Dataset).nextOptional().orElse(null)

fun Model.getAssessmentResource(dataset: Resource): Resource? =
    listObjectsOfProperty(dataset, DCATMQA.hasAssessment).nextOptional().map { it.asResource() }.orElse(null)

fun Model.addDatasetAssessment(assessment: Resource, dataset: Resource) {
    add(assessment, RDF.type, DCATMQA.DatasetAssessment)
    add(assessment, DCATMQA.assessmentOf, dataset)
}

fun Model.addComplianceQualityMeasurement(assessment: Resource, dataset: Resource, compliant: Boolean) {
    val measurement: Resource = createResource()
    add(measurement, RDF.type, DQV.QualityMeasurement)
    add(measurement, DQV.isMeasurementOf, DCATMQA.dcatApCompliance)
    add(measurement, DQV.computedOn, dataset)
    add(measurement, DQV.value, createTypedLiteral(compliant))
    add(assessment, DCATMQA.containsQualityMeasurement, measurement)
}

fun Model.writeToString(lang: Lang): String =
    ByteArrayOutputStream().use { out ->
        write(out, lang.name)
        out.flush()
        out.toString("UTF-8")
    }


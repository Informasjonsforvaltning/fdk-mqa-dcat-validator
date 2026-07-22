package no.digdir.fdk.mqa.dcatvalidator.rdf

import org.apache.jena.graph.Graph
import org.apache.jena.shacl.ShaclValidator
import org.apache.jena.shacl.Shapes
import org.apache.jena.shacl.ValidationReport

fun validate(data: Graph, shapes: Shapes): ValidationReport =
    ShaclValidator.get().validate(shapes, data)

fun parseShapes(shapesGraph: Graph): Shapes = Shapes.parse(shapesGraph)

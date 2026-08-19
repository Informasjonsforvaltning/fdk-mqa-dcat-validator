package no.digdir.fdk.mqa.dcatvalidator.rdf

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

fun loadModel(graph: String): Model {
    val model = ModelFactory.createDefaultModel()
    RDFDataMgr.read(model, graph.byteInputStream(StandardCharsets.UTF_8), Lang.TURTLE)
    return model
}

fun Model.writeToString(lang: Lang): String = ByteArrayOutputStream().use { out ->
    write(out, lang.name)
    out.flush()
    out.toString(StandardCharsets.UTF_8)
}

package io.konig.schemagen.jsonld;

import java.io.File;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.ContextManager;
import io.konig.core.GraphBuilder;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryContextManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.jsonld.SuffixContextNamer;

public class ShapeToJsonldContextTest {


	@Test
	public void test() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("institution", "http://www.konig.io/institution/");
		nsManager.add("shapes", "http://www.konig.io/shapes/v1/schema/");
		
		URI addressShapeId = uri("http://www.konig.io/shapes/v1/schema/Address");
		URI shapeId = uri("http://www.konig.io/shapes/v1/schema/Person");
		
		MemoryGraph graph = new MemoryGraph();
		GraphBuilder builder = new GraphBuilder(graph);
		builder.beginSubject(uri("http://www.konig.io/institution/Stanford"))
			.addProperty(RDF.TYPE, Schema.Organization)
		.endSubject()
		.beginSubject(uri("http://www.konig.io/institution/Princeton"))
			.addProperty(RDF.TYPE, Schema.Organization)
		.endSubject()
		.beginSubject(shapeId)
			.addProperty(RDF.TYPE, SH.Shape)
			.beginBNode(SH.property)
				.addProperty(SH.predicate, RDF.TYPE)
				.addProperty(SH.hasValue, uri("http://schema.org/Person"))
				.addProperty(SH.maxCount, 1)
				.addProperty(SH.minCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.predicate, Schema.givenName)
				.addProperty(SH.datatype, XMLSchema.STRING)
				.addProperty(SH.minCount, 1)
				.addProperty(SH.maxCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.predicate, Schema.address)
				.addProperty(SH.shape, addressShapeId)
				.addProperty(SH.minCount, 1)
				.addProperty(SH.maxCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.predicate, Schema.memberOf)
				.addProperty(SH.valueClass, Schema.Organization)
				.addProperty(SH.nodeKind, SH.IRI)
				.addProperty(SH.minCount, 0)
				.addProperty(SH.maxCount, 1)
			.endSubject()
		.endSubject();
		
		ContextManager contextManager = new MemoryContextManager();
		SuffixContextNamer contextNamer = new SuffixContextNamer("/context");
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(contextManager, shapeManager);
		loader.load(graph);
		
		
		File baseDir = new File("target/generated/contexts");

		ShapeToJsonldContext generator = new ShapeToJsonldContext(shapeManager, nsManager, contextNamer, graph);
		generator.generateAll(baseDir);
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}
}

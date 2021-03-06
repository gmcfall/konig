package io.konig.datacatalog;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class DataCatalogBuilderTest {
	
	private DataCatalogBuilder builder = new DataCatalogBuilder();

	private File exampleDir = new File("src/test/resources/DataCatalogBuilder/examples");
	private File outDir = new File("target/test/DataCatalogBuilder");
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();
	
	@Before
	public void setUp() {
		if (outDir.exists()) {
			IOUtil.recursiveDelete(outDir);
		}
		outDir.mkdirs();
	}
	
	@Test
	public void testHierarchicalNames() throws Exception {
		URI ontologyId = uri("http://example.com/ns/categories/");
		test("src/test/resources/DataCatalogBuilderTest/hierarchicalNames", ontologyId);
	}
	
	private void test(String sourcePath, URI ontologyId) throws Exception {
		load(sourcePath);
		DataCatalogBuildRequest request = new DataCatalogBuildRequest();
		request.setExampleDir(exampleDir);
		request.setOntologyId(ontologyId);
		request.setGraph(graph);
		request.setOutDir(outDir);
		request.setShapeManager(shapeManager);
		
		builder.build(request);
	}

	@Ignore
	public void test() throws Exception {
		
		load("src/test/resources/DataCatalogBuilderTest/rdf");
		URI ontologyId = uri("http://example.com/ns/core/");
		
		DataCatalogBuildRequest request = new DataCatalogBuildRequest();
		request.setExampleDir(exampleDir);
		request.setGraph(graph);
		request.setOntologyId(ontologyId);
		request.setOutDir(outDir);
		request.setShapeManager(shapeManager);
		
		builder.build(request);
		
	}


	private void load(String path) throws Exception {
		File file = new File(path);
		System.out.println(file.getAbsolutePath());
		RdfUtil.loadTurtle(file, graph, nsManager);
		
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	

}

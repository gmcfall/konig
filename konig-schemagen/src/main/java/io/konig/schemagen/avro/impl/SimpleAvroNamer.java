package io.konig.schemagen.avro.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.openrdf.model.URI;

import io.konig.core.Vertex;
import io.konig.core.util.StringUtil;
import io.konig.schemagen.avro.AvroNamer;
import io.konig.shacl.PropertyConstraint;

public class SimpleAvroNamer implements AvroNamer {
	
	private File idlDir;

	public SimpleAvroNamer(File idlDir) {
		this.idlDir = idlDir;
	}
	
	public SimpleAvroNamer() {
		
	}

	@Override
	public String toAvroNamespace(String uriNamespace) {
		
		
		StringBuilder builder = new StringBuilder();
		appendAvroNamespace(builder, uriNamespace);
		
		return builder.toString();
	}
	
	private void appendAvroNamespace(StringBuilder builder, String rdfNamespace) {
		int start = rdfNamespace.indexOf('/')+2;
		int end = rdfNamespace.indexOf('/', start+1);
		if (end < 0) {
			end = rdfNamespace.length();
		}
		
		String host = rdfNamespace.substring(start, end);
		List<String> hostParts = hostParts(host);
		appendDomain(builder, hostParts);
		appendPath(builder, rdfNamespace.substring(end));
		
	}

	private void appendPath(StringBuilder builder, String path) {
		
		StringTokenizer tokens = new StringTokenizer(path, "/#");
		while (tokens.hasMoreTokens()) {
			builder.append('.');
			builder.append(tokens.nextToken());
		}
		
	}

	private void appendDomain(StringBuilder builder, List<String> hostParts) {
		int size = hostParts.size();
		if (size > 0) {
			builder.append(hostParts.get(size-1));
		}
		if (size > 1) {
			builder.append('.');
			builder.append(hostParts.get(size-2));
		}
	}

	private List<String> hostParts(String host) {

		List<String> list = new ArrayList<>();
		StringTokenizer tokens = new StringTokenizer(host, ".");
		while (tokens.hasMoreTokens()) {
			list.add(tokens.nextToken());
		}
		return list;
	}

	@Override
	public String toAvroFullName(URI rdfName) {
		StringBuilder builder = new StringBuilder();
		appendAvroNamespace(builder, rdfName.getNamespace());
		builder.append('.');
		builder.append(rdfName.getLocalName());
		return builder.toString();
	}

	@Override
	public String toAvroSchemaURI(String shapeIRI) {
		String base = shapeIRI.replace('#', '/');
		StringBuilder builder = new StringBuilder();
		builder.append(base);
		builder.append('/');
		builder.append("avro");
		
		return builder.toString();
	}

	@Override
	public String enumName(String recordName, PropertyConstraint constraint) {
		StringBuilder builder = new StringBuilder();
		builder.append(StringUtil.capitalize(constraint.getPredicate().getLocalName()));
		builder.append("Enum");
		
		return builder.toString();
	}

	@Override
	public File idlFile(URI shapeIRI) {
		String fileName = toAvroFullName(shapeIRI) + ".avdl";
		return idlDir==null ? new File(fileName) : new File(idlDir, fileName);
	}

	@Override
	public String valueShapeName(String recordName, PropertyConstraint constraint) {
		String fieldName = constraint.getPredicate().getLocalName();
		StringBuilder builder = new StringBuilder();
		builder.append(recordName);
		builder.append('.');
		builder.append(fieldName);
		builder.append("Shape");
		return builder.toString();
	}

	

}

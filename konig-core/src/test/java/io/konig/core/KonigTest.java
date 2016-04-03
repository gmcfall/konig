package io.konig.core;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.impl.KonigLiteral;

public class KonigTest {
	private int bnodeCount = 1;
	
	protected URI uri(String value) {
		return new URIImpl(value);
	}
	
	protected BNode bnode() {
		return new BNodeImpl("bnode" + (bnodeCount++));
	}
	
	protected BNode bnode(String value) {
		return new BNodeImpl(value);
	}
	
	protected Literal literal(String value) {
		return new KonigLiteral(value);
	}
	
	protected ContextBuilder personContext() {
		ContextBuilder builder = new ContextBuilder("http://example.com/ctx/person");
		
		builder
			.namespace("schema", "http://schema.org/")
			.namespace("xsd", "http://www.w3.org/2001/XMLSchema#")
			.type("Address", "schema:PostalAddress")
			.type("Person", "schema:Person")
			.property("streetAddress", "schema:streetAddress", "xsd:string")
			.property("addressLocality", "schema:addressLocality", "xsd:string")
			.property("addressRegion", "schema:addressRegion", "xsd:string")
			.property("givenName", "schema:givenName", "xsd:string")
			.property("familyName", "schema:familyName", "xsd:string")
			.objectProperty("address", "schema:address")
			.objectProperty("parent", "schema:parent")
			.objectProperty("child", "schema:children");
		
		return builder;
	}

}

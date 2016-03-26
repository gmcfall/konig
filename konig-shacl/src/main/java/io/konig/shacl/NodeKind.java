package io.konig.shacl;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class NodeKind extends URIImpl {
	private static final long serialVersionUID = 1L;

	public static final NodeKind IRI = new NodeKind("http://www.w3.org/ns/shacl#IRI");
	public static final NodeKind BlankNode = new NodeKind("http://www.w3.org/ns/shacl#BlankNode");
	public static final NodeKind Literal = new NodeKind("http://www.w3.org/ns/shacl#Literal");

	private NodeKind(String uriString) {
		super(uriString);
	}
	
	public URI asURI() {
		return this;
	}

}
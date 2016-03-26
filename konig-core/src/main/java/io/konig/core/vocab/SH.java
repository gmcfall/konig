package io.konig.core.vocab;

/*
 * #%L
 * konig-core
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

public class SH {

	public static final URI IRI = new URIImpl("http://www.w3.org/ns/shacl#IRI");
	public static final URI BlankNode = new URIImpl("http://www.w3.org/ns/shacl#BlankNode");
	public static final URI Literal = new URIImpl("http://www.w3.org/ns/shacl#Literal");

	public static final URI predicate = new URIImpl("http://www.w3.org/ns/shacl#predicate");
	public static final URI allowedValues = new URIImpl("http://www.w3.org/ns/shacl#allowedValues");
	public static final URI and = new URIImpl("http://www.w3.org/ns/shacl#and");
	public static final URI constraint = new URIImpl("http://www.w3.org/ns/shacl#constraint");
	public static final URI datatype = new URIImpl("http://www.w3.org/ns/shacl#datatype");
	public static final URI directValueType = new URIImpl("http://www.w3.org/ns/shacl#directValueType");
	public static final URI hasValue = new URIImpl("http://www.w3.org/ns/shacl#hasValue");
	public static final URI minCount = new URIImpl("http://www.w3.org/ns/shacl#minCount");
	public static final URI maxCount = new URIImpl("http://www.w3.org/ns/shacl#maxCount");
	public static final URI minExclusive = new URIImpl("http://www.w3.org/ns/shacl#minExclusive");
	public static final URI maxExclusive = new URIImpl("http://www.w3.org/ns/shacl#maxExclusive");
	public static final URI minInclusive = new URIImpl("http://www.w3.org/ns/shacl#minInclusive");
	public static final URI maxInclusive = new URIImpl("http://www.w3.org/ns/shacl#maxInclusive");
	public static final URI nodeKind = new URIImpl("http://www.w3.org/ns/shacl#nodeKind");
	public static final URI not = new URIImpl("http://www.w3.org/ns/shacl#not");
	public static final URI or = new URIImpl("http://www.w3.org/ns/shacl#or");
	public static final URI pattern = new URIImpl("http://www.w3.org/ns/shacl#pattern");
	public static final URI valueClass = new URIImpl("http://www.w3.org/ns/shacl#valueClass");
	public static final URI valueShape = new URIImpl("http://www.w3.org/ns/shacl#valueShape");
	public static final URI qualifiedValueShape = new URIImpl("http://www.w3.org/ns/shacl#qualifiedValueShape");
	public static final URI qualifiedMinCount = new URIImpl("http://www.w3.org/ns/shacl#qualifiedMinCount");
	public static final URI qualifiedMaxCount = new URIImpl("http://www.w3.org/ns/shacl#qualifiedMaxCount");
	public static final URI EqualConstraint = new URIImpl("http://www.w3.org/ns/shacl#EqualConstraint");
	public static final URI NotEqualConstraint = new URIImpl("http://www.w3.org/ns/shacl#NotEqualConstraint");
	public static final URI LessThanConstraint = new URIImpl("http://www.w3.org/ns/shacl#LessThanConstraint");
	public static final URI LessThanOrEqualConstraint = new URIImpl("http://www.w3.org/ns/shacl#LessThanOrEqualConstraint");
	public static final URI NotConstraint = new URIImpl("http://www.w3.org/ns/shacl#NotConstraint");
	public static final URI AndConstraint = new URIImpl("http://www.w3.org/ns/shacl#AndConstraint");
	public static final URI OrConstraint = new URIImpl("http://www.w3.org/ns/shacl#OrConstraint");
	public static final URI ClosedShapeConstraint = new URIImpl("http://www.w3.org/ns/shacl#ClosedShapeConstraint");
	public static final URI scopeClass = new URIImpl("http://www.w3.org/ns/shacl#scopeClass");
	public static final URI property = new URIImpl("http://www.w3.org/ns/shacl#property");
	public static final URI minLength = new URIImpl("http://www.w3.org/ns/shacl#minLength");
	public static final URI maxLength = new URIImpl("http://www.w3.org/ns/shacl#maxLength");

}
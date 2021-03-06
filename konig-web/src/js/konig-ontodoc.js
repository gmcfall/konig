/*
 * #%L
 * konig-web
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
$(document).ready(function(){
	

var rdf = konig.rdf;
var owl = konig.owl;
var rdfs = konig.rdfs;
var vann = konig.vann;
var dcterms = konig.dcterms;
var dc = konig.dc;
var schema = konig.schema;
var sh = konig.sh;
var kol = konig.kol;
var xsd = konig.xsd;
var step = rdf.step;
var stringValue = rdf.stringValue;
var IRI = rdf.IRI;
var HistoryManager = konig.HistoryManager;
var BNode = rdf.BNode;
var __ = rdf.__;

/*****************************************************************************/
var SHACL = {
	Shape: new IRI("http://www.w3.org/ns/shacl#Shape")
};

var OWL = {
	Ontology: new IRI("http://www.w3.org/2002/07/owl#Ontology")	
};
/*****************************************************************************/
function PropertyOverview(propertyVertex) {
	this.propertyVertex = propertyVertex;
	this.init();
}

PropertyOverview.prototype.init = function() {
	var comment = this.propertyVertex.v().out(rdfs.comment, dcterms.description, dc.description).first();
	if (comment) {
		this.comment = comment.stringValue;
	}
	this.analyzeRange();
	this.analyzeDomain();
	this.analyzeInverse();
}

PropertyOverview.prototype.analyzeInverse = function() {

	this.inverseOf = this.propertyVertex.v().union(
		__.out(owl.inverseOf),
		__.inward(owl.inverseOf)
	).unique().toList();
	
	console.log(this.inverseOf);
}

PropertyOverview.prototype.analyzeRange = function() {
	var list = this.rangeList = [];
	var range = this.propertyVertex.v().out(rdfs.range).first();
	if (range) {
		// TODO: check if range is a union class
		list.push(range.id);
	} 
}

PropertyOverview.prototype.analyzeDomain = function() {
	var list = this.domainList = [];
	var domain = this.propertyVertex.v().out(rdfs.domain).first();
	if (domain) {
		// TODO: check if domain is a union class
		list.push(domain.id);
	}
}

/*****************************************************************************/
function PropertyManager(graph) {
	this.graph = graph;
	this.propertyMap = {};
}

PropertyManager.prototype.getPropertyOverview = function(propertyId) {
	var propertyVertex = this.graph.vertex(propertyId);
	
	var key = propertyVertex.id.stringValue;
	
	var overview = this.propertyMap[key];
	if (!overview) {
		// TODO: compute range and domain
		overview = new PropertyOverview(propertyVertex);
		this.propertyMap[key] = overview;
	}
	
	return overview;
}
/*****************************************************************************/
function PropertyInfo(predicate, expectedType, minCount, maxCount, description, nodeKind, valueShape) {
	this.predicate = predicate;
	this.propertyName = predicate.localName;
	this.expectedType = expectedType;
	this.minCount = minCount;
	this.maxCount = maxCount;
	this.description = description;
	this.nodeKind = nodeKind;
	this.valueShape = valueShape;
	this.or = false;
	
	if (expectedType.length) {
		expectedType[expectedType.length-1].last = true;
	}
}

PropertyInfo.prototype.toJsonSchema = function() {
	
	if (!this.valueShape && sh.IRI.equals(this.nodeKind)) {
		this.expectedType = [{
			stringValue: xsd.string.stringValue,
			localName: xsd.string.localName,
			last: true,
			modifier: "uri"
		}];
	} else if (
		this.expectedTypeContains(xsd.dateTime) ||
		this.expectedTypeContains(xsd.date)
	) {
		this.expectedType = [{
			stringValue: xsd.string.stringValue,
			localName: xsd.string.localName,
			last: true,
			modifier: "date-time"
		}];
	} else if (this.valueShape && this.expectedType.length==1) {
		
		// TODO: Handle the case of more than one expected type.
		
		var typeName = this.expectedType[0].localName;
		
		var jsonRendition = this.valueShape.v().out(kol.jsonSchemaRendition).first();
		
		if (jsonRendition) {
			this.expectedType = [{
				stringValue: jsonRendition.id.stringValue,
				localName: 'object',
				last: true,
				modifier: typeName
			}];
		} else {
			console.log("WARNING: failed to find jsonRendition of " + this.propertyName);
		}
	}
}

PropertyInfo.prototype.expectedTypeContains = function(value) {
	if (!(typeof(value)==="string")) {
		value = rdf.node(value).stringValue;
	}
	if (this.expectedType) {
		for (var i=0;i<this.expectedType.length; i++) {
			if (value === this.expectedType[i].stringValue) {
				return true;
			}
		}
	}
	return false;
}

PropertyInfo.prototype.clone = function() {
	var other = new PropertyInfo(this.predicate, this.expectedType, this.minCount,
			this.maxCount, this.description, this.nodeKind, this.valueShape);
	return other;
}

PropertyInfo.addType = function(list, typeVertex) {
	var type = typeVertex.id;
	
	if (type instanceof IRI) {

		var value = type.stringValue;
		for (var i=0; i<list.length; i++) {
			var item = list[i];
			if (item.stringValue === value) {
				return;
			}
		}
		list.push({
			stringValue: type.stringValue,
			localName: type.localName
		});
		
	} else {
		var unionVertex = typeVertex.v().out(owl.unionOf).first();
		if (unionVertex) {
			var union = unionVertex.toList();
			for (var i=0; i<union.length; i++) {
				PropertyInfo.addType(list, union[i]);
			}
		}
	}
}

/*****************************************************************************/
function PropertyBlock(fromClassVertex) {
	this.fromClassVertex = fromClassVertex;
	this.propertyList = [];
}

PropertyBlock.prototype.toJsonSchema = function() {
	for (var i=0; i<this.propertyList.length; i++) {
		this.propertyList[i].toJsonSchema();
	}
}

PropertyBlock.prototype.propertyInfoForPredicate = function(predicate) {
	var idValue = rdf.node(predicate).stringValue;
	for (var i=0; i<this.propertyList.length; i++) {
		var info = this.propertyList[i];
		if (info.predicate.stringValue === idValue) {
			return info;
		}
	}
	return null;
}

PropertyBlock.prototype.clone = function() {
	var other = new PropertyBlock(this.fromClassVertex);
	for (var i=0; i<this.propertyList.length; i++) {
		other.propertyList.push(this.propertyList[i].clone());
	}
	
	return other;
}
/*****************************************************************************/
function RenameClass(ontodoc, oldIRI, newIRI) {
	this.ontodoc = ontodoc;
	this.oldIRI = oldIRI;
	this.newIRI = newIRI;
}

RenameClass.prototype.redo = function() {
	this.ontodoc.renameClass(oldIRI, newIRI);
}
/*****************************************************************************/	
function HierarchyInfo(shapeInfo) {
	this.shapeInfo = shapeInfo;
	this.child = [];
	this.parent = [];
}


/*****************************************************************************/	
function OntologyInfo(manager, ontologyVertex) {
	this.manager = manager;
	this.vertex = ontologyVertex;
	this.label = manager.ontologyLabel(ontologyVertex);
	this.description = this.ontologyDescription();
	this.namespaceAddress = ontologyVertex.id.stringValue;
	this.namespacePrefix = this.ontologyPrefix();
	this.classList = [];
	this.propertyList = [];
}

OntologyInfo.prototype.ontologyPrefix = function() {
	var prefix = this.vertex.v().out(vann.preferredNamespacePrefix).first();
	if (prefix) {
		return prefix.stringValue;
	}
	
	// TODO: Refactor to support a list of contexts
	var inverse = this.manager.context.inverse();
	var term = inverse[this.vertex.id.stringValue];
	return term ? term : "";
}

OntologyInfo.prototype.ontologyDescription = function() {
	// TODO: handle multiple languages
	var description = this.vertex.v().out(schema.description, dcterms.description, rdfs.comment).first();
	return description ? description.stringValue : null;
}

/*****************************************************************************/
function OntologyManager(context, graph) {
	this.context = context;
	this.graph = graph;
	this.ontologyMap = {};
	this.ontologyList = [];
	this.init();
}

OntologyManager.prototype.init = function() {

	var ontoList = this.graph.V(owl.Ontology).inward(rdf.type).toList();
	for (var i=0; i<ontoList.length; i++) {
		var ontoVertex = ontoList[i];
		
		var info = new OntologyInfo(this, ontoVertex);
		this.ontologyMap[ontoVertex.id.stringValue] = info;
		this.ontologyList.push(info);
	}
	this.ontologyList.sort(function(a,b){
		return a.label.localeCompare(b.label);
	});
	this.analyzeProperties();
}

OntologyManager.prototype.analyzeProperties = function() {
	var map = {};
	
	var list = this.graph.V(
		owl.ObjectProperty, rdf.Property, owl.DatatypeProperty, owl.FunctionalProperty, owl.InverseFunctionalProperty, 
		owl.SymmetricProperty, owl.TransitiveProperty).inward(rdf.type).toList();
	
	list.sort(function(a,b){
		return a.id.localName.localeCompare(b.id.localName);
	});
	
	for (var i=0; i<list.length; i++) {
		var propertyId = list[i].id;
		if (!map[propertyId.stringValue]) {
			map[propertyId.stringValue] = propertyId;

			var ontologyInfo = this.getOntologyInfo(propertyId.namespace);
			ontologyInfo.propertyList.push(propertyId);

		}
	}
	
}

OntologyManager.prototype.getOntologyInfo = function(ontologyId) {
	var ontologyVertex = this.graph.vertex(ontologyId);
	var key = ontologyVertex.id.stringValue;
	var info = this.ontologyMap[key];
	if (!info) {
		info = new OntologyInfo(this, ontologyVertex);
		this.ontologyMap[key] = info;
	}
	return info;
}


OntologyManager.prototype.ontologyLabel = function(ontology) {
	var label = ontology.v().out(rdfs.label, dcterms.title, dc.title).first();
	if (label) {
		return label.stringValue;
	}
	
	return ontology.id.stringValue;
	
}

/*****************************************************************************/
function ClassManager(ontologyManager, graph) {
	this.ontologyManager = ontologyManager;
	this.graph = graph;
	this.classMap = {};
	this.classList = [];
	this.shapeMap = {};
	this.init();
}

ClassManager.prototype.init = function() {
	var classList = this.graph.V(rdfs.Class, owl.Class, rdfs.Datatype).inwardTransitiveClosure(rdfs.subClassOf).inward(rdf.type)
		.nodeKind(sh.IRI).toList();
	for (var i=0; i<classList.length; i++) {
		var owlClass = classList[i];
		var classInfo = this.getOrCreateClassInfo(owlClass);
	}
	

	this.classList.sort(function(a,b){
		return a.classVertex.id.localName.localeCompare(b.classVertex.id.localName);
	});
	
	this.assignUniqueNames();
}



ClassManager.prototype.assignUniqueNames = function() {
	var list = this.classList;
	var prevLocalName = null;
	for (var i=0; i<list.length; i++) {
		var classInfo = list[i];
		var localName = classInfo.classVertex.id.localName;
		classInfo.uniqueName = localName;
		if (
			(localName === prevLocalName) ||
			(
				(i<list.length-1) && 
				(list[i+1].classVertex.id.localName === localName)
			)
		) {
			var namespace = classInfo.classVertex.id.namespace;
			var ontoInfo = this.ontologyManager.getOntologyInfo(namespace);
			var prefix = ontoInfo.namespacePrefix;
			if (prefix) {
				classInfo.uniqueName = prefix + ":" + localName;
			}
		} 
		
		prevLocalName = localName;
		
	}
}

ClassManager.prototype.getOrCreateClassInfo = function(owlClass) {
	var classId = rdf.node(owlClass);
	var classInfo = this.classMap[classId.stringValue];
	if (classInfo == null) {
		classInfo = new ClassInfo(owlClass, this);
		this.classMap[classId.stringValue] = classInfo;
		this.classList.push(classInfo);
		// TODO: remove the next line to support lazy creation of shapeInfo
//		classInfo.getLogicalShape();
	}
	return classInfo;
}

ClassManager.prototype.computeShapeInfo = function(id) {
	var vertex = this.graph.vertex(id);
	
	if (vertex.instanceOf(owl.Class) || vertex.instanceOf(rdfs.Class)) {

		var owlClassIRI = vertex.id.stringValue;
		var classInfo = this.classMap[owlClassIRI];
		
		if (classInfo) {
		
			var shapeInfo = classInfo.getLogicalShape();
			
			shapeInfo.subClassList = classInfo.getSubClassList();
			
			if (!shapeInfo.comment) {
				shapeInfo.comment = classInfo.comment;
			}
			
			return shapeInfo;
		}
	} else if (vertex.instanceOf(rdfs.Datatype)) {
		var classInfo = this.getOrCreateClassInfo(vertex);
		var shapeInfo = new ShapeInfo(classInfo);
		return shapeInfo;
		
	} else {
		var schemaType = "";
		var rawShape = vertex.v().inward(kol.avroSchemaRendition).first();
		if (rawShape) {
			schemaType = "Avro";
		} else {
			rawShape = vertex.v().inward(kol.jsonSchemaRendition).first();
			if (rawShape) {
				schemaType = "JsonSchema";
			}
		}
		
		if (rawShape) {
			var shapeInfo = this.getOrCreateShapeInfo(rawShape);
			
			switch (schemaType) {
			case "Avro" : return shapeInfo.toAvroSchema(vertex.id.stringValue);
			case "JsonSchema" : return shapeInfo.toJsonSchema(vertex.id.stringValue);
			}
			
		}
	}
	return null;
}

ClassManager.prototype.getOrCreateShapeInfo = function(shape) {
	var shapeId = rdf.node(shape);
	var shapeInfo = this.shapeMap[shapeId.stringValue];
	if (!shapeInfo) {
		shape = this.graph.vertex(shape);
		
		var targetClass = shape.v().out(sh.targetClass).first();
		if (!targetClass) {
			console.log("targetClass not defined for shape: " + shapeId.stringValue);
			return null;
		}
		
		var classInfo = this.getOrCreateClassInfo(targetClass);
		
		shapeInfo = new ShapeInfo(classInfo, shape);
		this.shapeMap[shapeId.stringValue] = shapeInfo;
	}
	
	return shapeInfo;
}


ClassManager.prototype.logicalShapeName = function(owlClass) {
	var uri = rdf.node(owlClass);
	var prefixNode = this.graph.V(uri.namespace).out(vann.preferredNamespacePrefix).first(); 
	var prefix = prefixNode ? prefixNode.stringValue : "default";
	
	return "http://www.konig.io/shape/logical." + prefix + "." + uri.localName;
	
}

/*****************************************************************************/
function IndividualInfo(individualVertex) {
	this.individualVertex = individualVertex;
	this.label = individualVertex.propertyValue(rdfs.label);
	if (!this.label) {
		this.label = individualVertex.id.localName;
	}
	var comment = individualVertex.propertyValue(rdfs.comment);
	if (!comment) {
		this.comment = "";
	} else {
		this.comment = comment.stringValue;
	}
	
}

/*****************************************************************************/
function ClassInfo(owlClass, classManager) {
	var graph = classManager.graph;
	this.classVertex = graph.vertex(owlClass);
	this.classManager = classManager;
	this.analyzeComment();
	this.individualList = this.collectIndividuals();
	
}

ClassInfo.prototype.collectIndividuals = function() {
	var sink = [];
	if (this.classVertex.v().has(rdfs.subClassOf, schema.Enumeration).count()>0) {
		var list = this.classVertex.v().inward(rdf.type).toList();
		for (var i=0; i<list.length; i++) {
			var v = list[i];
			var individual = new IndividualInfo(v);
			sink.push(individual);
		}
	}
	return sink;
	
}

ClassInfo.prototype.getSubClassList = function() {
	if (!this.subClassList) {
		this.subClassList = this.classVertex.v().inward(rdfs.subClassOf).toList();
	}
	
	return this.subClassList;
}

ClassInfo.prototype.analyzeComment = function() {
	var comment = this.classVertex.v().out(rdfs.comment, dcterms.description, dc.description).first();
	if (comment) {
		this.comment = comment.stringValue;
	}
}

ClassInfo.prototype.getMediaTypeList = function() {
	if (!this.mediaTypeList) {
		var sink = this.mediaTypeList = [];
		var shapeList = this.classVertex.v().inward(sh.targetClass).toList();
		if (shapeList.length > 1) {

			this.mediaTypeList.push({
				id: this.classVertex.id.stringValue,
				mediaTypeName: ""
			});
			
			for (var i=0; i<shapeList.length; i++) {
				var shape = shapeList[i];
				var name = shape.v().out(kol.mediaTypeBaseName).first();
				if (!name) {
					// TODO: Generate media type name
					console.log("Failed to find mediaTypeBaseName for shape " + shape.id.stringValue);
				} else {
					var avro = shape.v().out(kol.avroSchemaRendition).first();
					var json = shape.v().out(kol.jsonSchemaRendition).first();
					
					if (avro) {
						sink.push({
							id: avro.id.stringValue,
							mediaTypeName : name.stringValue + "+avro"
						});
					};
					if (json) {
						sink.push({
							id: json.id.stringValue,
							mediaTypeName : name.stringValue + "+json"
						});
					}
				}
			}
		}
		
	}
	
	return this.mediaTypeList;
}

ClassInfo.prototype.getLogicalShape = function() {
	if (!this.logicalShape) {
		var logicalShape = this.classVertex.v().inward(sh.targetClass).hasType(kol.LogicalShape).first();
		if (!logicalShape) {

			// Get any old shape and treat it as the logical shape.
			// This is a temporary hack.
			// If there are other shapes we ought to merge the property constraints
			// into the OWL description
			
			// BEGIN HACK
//			logicalShape = this.classVertex.v().inward(sh.targetClass).first();
//			if (logicalShape) {
//				console.log("Found a pre-defined shape");
//				this.logicalShape = this.classManager.getOrCreateShapeInfo(logicalShape);
//				return this.logicalShape;
//			}
//			console.log("No pre-defined Shape was found for class " + this.classVertex.id.stringValue);
			// END HACK
			
			var graph = this.classManager.graph;

			var owlClass = this.classVertex;
			var shapeName = this.classManager.logicalShapeName(owlClass);
			logicalShape = graph.vertex(shapeName);
			graph.statement(logicalShape, sh.targetClass, owlClass);

			var isLiteral = owlClass.v().hasTransitive(rdfs.subClassOf, rdfs.Literal).first();
			if (!isLiteral) {
				this.addDirectProperties(owlClass, logicalShape);
				var classMap = {};
				this.addSuperProperties(owlClass, logicalShape, classMap);
				this.addShapeProperties(owlClass, logicalShape);
			}
		}
		this.logicalShape = this.classManager.getOrCreateShapeInfo(logicalShape);
	}
	return this.logicalShape;
}

ClassInfo.prototype.addShapeProperties = function(owlClass, logicalVertex) {
	var graph = this.classManager.graph;
	
	// Get the list of shapes that have owlClass as the targetClass
	var shapeList = owlClass.v().inward(sh.targetClass).toList();
	
	for (var i=0; i<shapeList.length; i++) {
		var shape = shapeList[i];
		var propertyList = shape.v().out(sh.property).toList();
		for (var j=0; j<propertyList.length; j++) {
			var property = propertyList[j];
			var predicate = property.propertyValue(sh.predicate);
			// Do we have an existing PropertyConstraint for the given predicate?
			var prior = logicalVertex.v().out(sh.property).has(sh.predicate, predicate).first();
			if (!prior) {
				// There is no existing PropertyConstraint for the given predicate.
				// Clone the one that we found, and add it to the logical shape.
				
				var clone = property.shallowClone();
				graph.statement(logicalVertex, sh.property, clone);
			} else {
				console.log("TODO: merge multiple property constraints");
			}
		}
	}
	
}


ClassInfo.prototype.addSuperProperties = function(owlClass, shape, classMap) {
	var manager = this.classManager;
	var graph = manager.graph;
	
	var superList = owlClass.v().out(rdfs.subClassOf).toList();
	if (superList.length > 0) {
		var andConstraint = graph.vertex();
		graph.statement(shape, sh.constraint, andConstraint);
	
		var listVertex = graph.vertex();
		graph.statement(andConstraint, sh.and, listVertex);

		var sink = listVertex.toList();
		
		for (var i=0; i<superList.length; i++) {
			var supertype = superList[i];
			if (!classMap[supertype.id.stringValue]) {
				classMap[supertype.id.stringValue] = supertype;
				
				var superInfo = manager.getOrCreateClassInfo(supertype);
				var logicalShape = superInfo.getLogicalShape();
				sink.push(logicalShape.rawShape);
			}
		}
	}
}

ClassInfo.prototype.addDirectProperties = function(owlClass, shape) {
	
	var graph = this.classManager.graph;
	var propertyList = owlClass.v().inward(rdfs.domain, schema.domainIncludes).unique().toList();
	
	
	for (var i=0; i<propertyList.length; i++) {
		this.addPropertyConstraint(shape, propertyList[i]);
	}
	
	
}

var appendUnique = function(list, element) {
	if (element) {
		var node = rdf.node(element);
		for (var i=0; i<list.length; i++) {
			var n = rdf.node(list[i]);
			if (n.stringValue === node.stringValue) {
				return;
			}
		}
		list.push(element);		
	}
}

ClassInfo.prototype.addPropertyConstraint = function(shape, property) {
	var predicate = rdf.node(property);
	var graph = this.classManager.graph;
	
	var result = shape.v().out(sh.property).has(sh.predicate, predicate).first();
	if (!result) {
		result = graph.vertex();
		graph.statement(shape, sh.property, result);
		graph.statement(result, sh.predicate, predicate);
		
		if (property.v().hasType(owl.FunctionalProperty).first()) {
			graph.statement(result, sh.maxCount, 1);
		}
		
		var description = Ontodoc.shaclDescription(property);
		if (description) {
			graph.statement(result, sh.description, description);
		}
		
		var propertyType = this.propertyType(property);
		var range = this.rangeValue(property);
		
			
		if (propertyType.equals(owl.DatatypeProperty) || (range.v && range.v().instanceOf(rdfs.Datatype))) {
			graph.statement(result, sh.datatype, range);
		} else {
			graph.statement(result, sh.valueClass, range);
		}
	}
		
		
	
}

ClassInfo.prototype.rangeValue = function(property) {
	var rangeIncludes = property.v().out(schema.rangeIncludes).toList();
	appendUnique(rangeIncludes, property.v().out(rdfs.range).first());
	
	if (rangeIncludes.length == 0) {
		return owl.Thing;
	}
	
	if (rangeIncludes.length == 1) {
		return rangeIncludes[0];
	}

	var graph = this.classManager.graph;
	
	var unionClass = graph.vertex();
	var listVertex = graph.vertex();
	graph.statement(unionClass, rdf.type, owl.Class);
	graph.statement(unionClass, owl.unionOf, listVertex);
	listVertex.elements = rangeIncludes;
	
	return unionClass;
}

ClassInfo.prototype.propertyType = function(property) {
	if (property.v().hasType(owl.ObjectProperty).first()) {
		return owl.ObjectProperty;
	}
	
	if (property.v().hasType(owl.DatatypeProperty).first()) {
		return owl.DatatypeProperty;
	}
	return rdf.Property;
}


/*****************************************************************************/	
function ShapeInfo(classInfo, rawShape) {
	this.classInfo = classInfo;
	this.rawShape = rawShape;
	if (rawShape) {
		this.init();
	}
}

ShapeInfo.prototype.toAvroSchema = function() {
	return this;
}

ShapeInfo.prototype.toJsonSchema = function(format) {
	if (this.directProperties) {
		this.directProperties.toJsonSchema();
	}
	for (var i=0; i<this.propertyBlock.length; i++) {
		if (this.propertyBlock[i] != this.directProperties) {
			this.propertyBlock[i].toJsonSchema();
		}
	}
	this.format = format;
	
	return this;
}

ShapeInfo.prototype.init = function() {

	this.propertyBlock = [];

	this.addDirectProperties();
	this.addSuperProperties(this.rawShape, true);
}

ShapeInfo.prototype.clone = function() {
	var other = new ShapeInfo(this.classInfo);
	other.rawShape = this.rawShape;
	other.propertyBlock = [];
	
	for (var i=0; i<this.propertyBlock.length; i++) {
		other.propertyBlock.push(this.propertyBlock[i].clone());
	}
	if (this.directProperties) {
		other.directProperties = this.directProperties.clone();
	}
	
	return other;
}

ShapeInfo.prototype.addSuperProperties = function(shape, isDirect) {
	if (!shape) return;
	var constraint = shape.v().out(sh.constraint).first();
	if (constraint) {
		var and = constraint.v().out(sh.and).first();
		if (and) {
			this.analyzeAnd(and, isDirect);
		} else {
			var or = contraint.v().out(sh.or).first();
			if (or) {
				this.analyzeOr(or, isDirect);
			} else {
				var not = constraint.v().out(sh.not).first();
				if (not) {
					this.analyzeNot(not, isDirect);
				} else {
					// TODO: analyze custom constraint
				}
			}
		}
	}
	
}

ShapeInfo.prototype.propertyBlockForClass = function(owlClassVertex) {
	var classId = rdf.node(owlClassVertex);
	var list = this.propertyBlock;
	for (var i=0; i<list.length; i++) {
		var block = list[i];
		if (block.fromClassVertex === owlClassVertex) {
			return block;
		}
	}
	
	var block = new PropertyBlock(owlClassVertex);
	list.push(block);
	
	return block;
	
}

ShapeInfo.prototype.analyzeAnd = function(and, isDirect) {
	var classManager = this.classInfo.classManager;
	var andList = and.toList();
	for (var i=0; i<andList.length; i++) {
		var shape = andList[i];
		
		if (shape.id instanceof IRI) {
			shapeInfo = classManager.getOrCreateShapeInfo(shape.id);
			this.copyPropertyBlocks(shapeInfo);
			
			
		} else {
			console.log("TODO: add local properties from and clause")
		}
	}
}

ShapeInfo.prototype.copyPropertyBlocks = function(shapeInfo) {
	var blockList = shapeInfo.propertyBlock;
	var directBlock = this.directProperties;
	for (var i=0; i<blockList.length; i++) {
		var block = blockList[i];
		var list = block.propertyList;
		if (list.length > 0) {
			var sinkBlock = this.propertyBlockForClass(block.fromClassVertex);
			var sink = sinkBlock.propertyList;
			
			for (var j=0; j<list.length; j++) {
				
				var propertyInfo = list[j];
				
				if (directBlock) {
					var prior = directBlock.propertyInfoForPredicate(propertyInfo.predicate);
					if (prior) {
						continue;
					}
				}
				sink.push(propertyInfo);
			}
		}
	}
}

ShapeInfo.prototype.analyzeOr = function(or, isDirect) {
	console.log("TODO: implement analyzeOr");
}

ShapeInfo.prototype.analyzeNot = function(not, isDirect) {
	console.log("TODO: implement analyzeNot");
}


ShapeInfo.prototype.addDirectProperties = function() {
	var classManager = this.classInfo.classManager;
	var owlClass = this.classInfo.classVertex;
	var block = new PropertyBlock(owlClass);
	var sink = block.propertyList;
	
	var source = this.rawShape.v().out(sh.property).toList();
	for (var i=0; i<source.length; i++) {
		var property = source[i];
		var description = Ontodoc.shaclDescription(property);
		var predicate = property.v().out(sh.predicate).first();
		var expectedType = [];
		var datatype = property.v().out(sh.datatype).first();
		var objectType = property.v().out(sh.objectType).first();
		var directValueType = property.v().out(sh.directValueType).first();
		var valueClass = property.v().out(sh.valueClass).first();
		var valueShape = property.v().out(sh.valueShape).first();
		var minCount = property.v().out(sh.minCount).first();
		var maxCount = property.v().out(sh.maxCount).first();
		var nodeKind = property.v().out(sh.nodeKind).first();
		var valueShape = property.v().out(sh.valueShape).first();
		if (datatype) {
			PropertyInfo.addType(expectedType, datatype);
		} else if (objectType) {
			PropertyInfo.addType(expectedType, objectType);
		} else if (directValueType) {
			PropertyInfo.addType(expectedType, directValueType);
		} else if (valueClass) {
			PropertyInfo.addType(expectedType, valueClass);
		} else if (valueShape) {
			var targetClass = valueShape.v().out(sh.targetClass).first();
			if (!targetClass) {
				// TODO: surface this warning in the user interface
				console.log("WARNING: Scope class not found for valueShape of  " + predicate.id.localName + " on " + valueShape.id.stringValue);
				targetClass = valueShape.graph.vertex(owl.Thing);
			}
			classManager.getOrCreateShapeInfo(targetClass);
			PropertyInfo.addType(expectedType, targetClass);
		}

		var propertyInfo = new PropertyInfo(
			predicate.id, expectedType, minCount, maxCount, description, nodeKind, valueShape
		);
		
		sink.push(propertyInfo);
	}
	if (sink.length > 0) {
		this.directProperties = block;
		
		this.propertyBlock.push(block);
	}
	
}

/*****************************************************************************/	
function Ontodoc(options) {
	this.ontologyGraph = options.ontologyGraph;
	this.actionHistory = new HistoryManager();
	this.editMode = typeof(options.edit)==="undefined" ? false : options.edit;
	this.multipleShapesPerClass = typeof(options.multipleShapesPerClass)==="undefined" ?
			false : options.multipleShapesPerClass;
	this.layout = $('body').layout(
		{
			applyDefaultStyles:true,
			north: {
				resizable: true,
				closable: false
			},
			west: {
				size: "250"
			}
		}
	);
	$('#navigation').layout({
		applyDefaultStyles: true,
		north: {
			size: "200"
		}
	});
	
	
	this.context = new konig.jsonld.Context(this.ontologyGraph['@context']);
	
	this.buildGraph();
	this.inferClasses();
	
	this.ontologyManager = new OntologyManager(this.context, this.graph);
	
	this.classManager = new ClassManager(this.ontologyManager, this.graph);
	this.propertyManager = new PropertyManager(this.graph);
	
	this.inferOntologies();
	this.renderOntologyList();
	this.renderClassList();

	this.classTemplate = $('#ontodoc-class-template').html();
	this.ontologyTemplate = $('#ontodoc-ontology-template').html();
	this.classEditTemplate = $('#ontodoc-class-edit-template').html();
	this.propertyTemplate = $('#ontodoc-property-template').html();
	
	var self = this;
	$(window).bind("hashchange", function(event){
		self.onHashChange();
	});
	$(window).trigger('hashchange');
	
	if (this.initEdit) {
		this.initEdit();
	}
	
}

/**
 * Given statements of the form:
 * <pre>
 *    ?x sh:targetClass ?y
 * </pre>
 * 
 * infer
 * <pre>
 *    ?y rdf:type owl:Class
 * </pre>
 */
Ontodoc.prototype.inferClasses = function() {
	var graph = this.graph;
	var shapeList = graph.V(sh.Shape).inward(rdf.type).toList();
	for (var i=0; i<shapeList.length; i++) {
		var shape = shapeList[i];
		var targetClass = shape.propertyValue(sh.targetClass);
		if (targetClass) {
			graph.statement(targetClass, rdf.type, owl.Class);
		}
	}
	
}


Ontodoc.shaclDescription = function(vertex) {
	// TODO: support multiple languages
	
	var description = vertex.v().out(sh.description).first();
	if (description == null) {
		description = vertex.v().out(rdfs.comment).first();
	}
	if (description == null) {
		var predicate = vertex.v().out(sh.predicate).first();
		if (predicate) {
			description = predicate.v().out(schema.description, dcterms.description, rdfs.comment).first();
		}
	}
	
	return description;
}

Ontodoc.prototype.analyzeSuperProperties = function() {

	var list = this.shapeInfoList();
	for (var i=0; i<list.length; i++) {
		var shapeInfo = list[i];
		var map = {};
		this.mapProperties(shapeInfo, map);
		this.addSuperProperties(shapeInfo, map);
	}
}

Ontodoc.prototype.mapProperties = function(shapeInfo, map) {
	var list = shapeInfo.propertyBlock;
	map[shapeInfo.owlClass.stringValue] = shapeInfo;
	if (list) {
		for (var i=0; i<list.length; i++) {
			var block = list[i];
			for (var j=0; j<block.propertyList.length; j++) {
				var propertyInfo = block.propertyList[j];
				map[propertyInfo.predicate.stringValue] = propertyInfo;
			}
		}
	}
}

Ontodoc.prototype.addSuperProperties = function(shapeInfo, map) {

}

Ontodoc.prototype.isMediaTypeDescriptor = function(vertex) {
	return 
		vertex.v().inward(kol.avroSchemaRendition).first() ||
		vertex.v().inward(kol.jsonSchemaRendition.first());
}

Ontodoc.prototype.onHashChange = function() {
	var location = window.location;
	var hash = location.hash;
	if (hash) {
		hash = hash.substring(1);
		var vertex = this.graph.vertex(hash, true);
		
		if (!vertex) {
			// TODO: fetch the resource via ajax call.
		} else {
			
			var shapeInfo = this.classManager.computeShapeInfo(vertex);
			
			if (shapeInfo) {
				this.renderShape(shapeInfo);
			} else if (vertex.instanceOf(owl.Ontology)) {
				this.renderOntology(vertex);
			} else if (vertex.instanceOf(rdf.Property)) {
				this.renderProperty(vertex);
			}
		}
	}
}

Ontodoc.prototype.getShapeInfo = function(owlClassIRI) {
	var key = stringValue(owlClassIRI);
	return this.classMap[key];
}

Ontodoc.prototype.buildHierarchy = function(owlClassIRI) {
	var path =
		this.graph.V(owlClassIRI).until(hasNot(rdfs.subClassOf)).repeat(outV(rdfs.subClassOf)).path();
}

Ontodoc.prototype.buildGraph = function() {

	this.graph = rdf.graph();
	var doc = this.ontologyGraph;
	var context = this.context;
	
	var expand = context.expand(doc);
	var flat = context.flatten(expand);
	this.graph.loadFlattened(flat['@graph']);
	
	var g = this.graph;
	
	g.statement(owl.ObjectProperty, rdfs.subClassOf, rdf.Property);
	g.statement(owl.DatatypeProperty, rdfs.subClassOf, rdf.Property);
}




Ontodoc.prototype.isLiteralClass = function(owlClass) {
	if (!owlClass) {
		return false;
	}
	var uri = rdf.node(owlClass);
	if (xsd.NAMESPACE === uri.namespace) {
		return true;
	}
	// TODO: Implement more robust test for Literal class
	return false;
}


Ontodoc.prototype.shapeInfoList = function() {
	var list = [];

	for (var key in this.classMap) {
		list.push(this.classMap[key]);
	}
	return list;
}

Ontodoc.prototype.analyzeProperties = function() {
	
	var list = this.shapeInfoList();
	
	while (list.length > 0) {
		this.extraClasses = [];
		for (var i=0; i<list.length; i++) {
			var info = list[i];
			this.buildProperties(info);
		}
		
		list = this.extraClasses;
	}
	
	delete this.extraClasses;
}

Ontodoc.prototype.buildProperties = function(shapeInfo) {
	
	// Get the list of shapes that reference the specified OWL class.
	var shapeList = this.graph.V(shapeInfo.owlClass).inward(sh.targetClass).toList();
	for (var i=0; i<shapeList.length; i++) {
		this.addPropertiesFromShape(shapeInfo, shapeList[i]);
	}
}

Ontodoc.prototype.addPropertiesFromShape = function(shapeInfo, shape) {
	var owlClass = shapeInfo.owlClass;
	if (!shapeInfo.localProperties) {
		shapeInfo.localProperties = new PropertyBlock(owlClass);
	}
	if (!shapeInfo.propertyBlock) {
		shapeInfo.propertyBlock = [];
	}
	var propertyBlock = shapeInfo.localProperties;
	shapeInfo.propertyBlock.push(propertyBlock);
	
	var propertyList = shape.v().out(sh.property).toList();
	for (var i=0; i<propertyList.length; i++) {
		
		var property = propertyList[i];
		var predicate = property.v().out(sh.predicate).first();
		
		if (predicate) {
			// TODO: check to see if a PropertyInfo already exists for the specified predicate.
			// Don't create a new one if it already exists.
			
			// TODO: Store the Shape specific details separately from the localProperties.
			
			var expectedType = [];
			var datatype = property.v().out(sh.datatype).first();
			var objectType = property.v().out(sh.objectType).first();
			var directValueType = property.v().out(sh.directValueType).first();
			var valueClass = property.v().out(sh.valueClass).first();
			var valueShape = property.v().out(sh.valueShape).first();
			var minCount = property.v().out(sh.minCount).first();
			var maxCount = property.v().out(sh.maxCount).first();
			var nodeKind = property.v().out(sh.nodeKind).first();
			var valueShape = property.v().out(sh.valueShape).first();
			// TODO: support description in multiple languages
			var description = property.v().out(sh.description).first();
			if (datatype) {
				PropertyInfo.addType(expectedType, datatype);
			} else if (objectType) {
				PropertyInfo.addType(expectedType, objectType);
			} else if (directValueType) {
				PropertyInfo.addType(expectedType, directValueType);
			} else if (valueClass) {
				PropertyInfo.addType(expectedType, valueClass);
			} else if (valueShape) {
				var targetClass = valueShape.v().out(sh.targetClass).first();
				if (!targetClass) {
					// TODO: surface this warning in the user interface
					console.log("WARNING: Scope class not found for valueShape of  " + predicate.id.localName + " on " + shape.id.stringValue);
					targetClass = this.graph.vertex(owl.Thing);
				}
				this.getOrCreateShapeInfo(targetClass);
				PropertyInfo.addType(expectedType, targetClass);
			}

			var propertyInfo = new PropertyInfo(
				predicate.id, expectedType, minCount, maxCount, description, nodeKind, valueShape
			);
			propertyBlock.propertyList.push(propertyInfo);
		}
		
	}
}


Ontodoc.prototype.renderProperty = function(propertyVertex) {
	var overview = this.propertyManager.getPropertyOverview(propertyVertex);
	
	var rendered = Mustache.render(this.propertyTemplate, overview);
	$(".ontodoc-main-content").empty().append(rendered);
}

Ontodoc.prototype.renderOntology = function(ontologyId) {
	
	var info = this.ontologyManager.getOntologyInfo(ontologyId);
	
	var rendered = Mustache.render(this.ontologyTemplate, info);
	$(".ontodoc-main-content").empty().append(rendered);
	
}

Ontodoc.prototype.renderShape = function(shapeInfo) {
	
	var classInfo = shapeInfo.classInfo;
	var owlClassIRI = classInfo.classVertex.id.stringValue;
	

	if (this.multipleShapesPerClass) {
		shapeInfo.mediaTypeList = shapeInfo.classInfo.getMediaTypeList();
	}

	shapeInfo.subClassList = classInfo.getSubClassList();
	
	if (!shapeInfo.comment) {
		shapeInfo.comment = classInfo.comment;
	}
	
	if (this.multipleShapesPerClass) {
		shapeInfo.mediaTypeList = classInfo.getMediaTypeList();
	}
	
	var rendered = this.editMode ?
			Mustache.render(this.classEditTemplate, shapeInfo) :
			Mustache.render(this.classTemplate, shapeInfo);
		
	$(".ontodoc-main-content").empty().append(rendered);
	this.renderClassBreadcrumbs(owlClassIRI);
	
	var self = this;
	$(".prop-ect a[resource]").each(function(index, element){
		var e = $(this);
		var className = e.attr("resource");
		e.click(self.clickClassName(className));
	});
	
	if (this.multipleShapesPerClass) {
		$("#konig-class-media-type").change(function(){
			var value = $(this).val();
			
			window.location.hash = value;
		});
	}
	
	if (this.editMode) {
		this.editClass(shapeInfo);
	}
	
	if (shapeInfo.format) {
		$("#konig-class-media-type").val(shapeInfo.format)
	}
	
	$(".ontodoc-main-content")[0].scrollTop = 0;
}

Ontodoc.prototype.renderClass = function(owlClassIRI) {
	
	
	var classInfo = this.classManager.classMap[owlClassIRI];
	
	
	
	if (classInfo) {
	
		var shapeInfo = classInfo.getLogicalShape();
		
		this.renderShape(shapeInfo);
	}
	
	
}

/**
 * Compute the sequence of ancestors in the type hierarchy for a given OWL Class
 * 
 * @param owlClassVertex
 * @returns {vertex[]} The list of vertices starting with the given owlClassVertex
 * and ending with the most distant ancestor at the top of the class hierarchy, or
 * null if the tree of ancestors is not linear.
 */
Ontodoc.prototype.subClassSequence = function(owlClassVertex) {
	
	var list = [];
	
	while (owlClassVertex != null) {
		list.push(owlClassVertex);
		var superList = owlClassVertex.v().out(rdfs.subClassOf).iri().toList();
		if (superList.length > 1) {
			return null;
		}
		owlClassVertex = superList.length==0 ? null : superList[0];
	}
	return list;
}


Ontodoc.prototype.renderClassBreadcrumbs = function(owlClassIRI) {
	var classManager = this.classManager;
	var breadcrumbs = $('.ontodoc-class .breadcrumbs');
	
	var owlClassVertex = this.graph.vertex(owlClassIRI);
	var pathList = this.subClassSequence(owlClassVertex);
	
	if (pathList) {
		for (var i=pathList.length-1; i>=0; i--) {
			var vertex = pathList[i];
			
			var info = classManager.getOrCreateClassInfo(vertex);
			var anchor = Mustache.render('<a href="#{{href}}" title="{{href}}">{{localName}}</a>', {
				localName: info.uniqueName,
				href: vertex.id.stringValue
			});
			
			if (i!=pathList.length-1) {
				 breadcrumbs.append("<span> &gt; </span>");
			}
			breadcrumbs.append(anchor);
		}
	} else {
		var canvas = document.createElement("canvas");
		breadcrumbs[0].appendChild(canvas);
		$(canvas).rdfspringy({
			vertex: owlClassVertex,
			property: rdfs.subClassOf
		});
	}
	
}

Ontodoc.prototype.inferOntologies = function() {
	var classMap = this.classManager.classMap;
	
	for (var key in classMap) {
		var info = classMap[key];
		var owlClass = info.classVertex;
		
		var namespace = this.graph.vertex(owlClass.id.namespace);
		
		if (namespace.v().hasType(owl.Ontology).toList().length==0) {
			namespace.v().addType(owl.Ontology).execute();
		}
		
	}
}

Ontodoc.prototype.renderOntologyList = function() {
	var container = $('#ontodoc-ontology-list');
	
	var infoList = this.ontologyManager.ontologyList;
	for (var i=0; i<infoList.length; i++) {

		var info = infoList[i];
		var anchor = Mustache.render(
			'<div class="ontodoc-index-entry"><a href="#{{{href}}}">{{label}}</a></div>', {
			href: info.vertex.id.stringValue,
			label: info.label
		});
		container.append(anchor);
	}
	
//	var ontoList = this.graph.V(owl.Ontology).inward(rdf.type).toList();
//	for (var i=0; i<ontoList.length; i++) {
//		var onto = ontoList[i];
//		var label = this.ontologyLabel(onto);
//		
//		var anchor = Mustache.render(
//			'<div class="ontodoc-index-entry"><a href="#{{{href}}}">{{label}}</a></div>', {
//			href: onto.id.stringValue,
//			label: label
//		});
//		
//		
//		container.append(anchor);
//		
//	}
}


Ontodoc.prototype.renderClassList = function() {
	
	var list = this.classManager.classList;
	
	var container = $("#ontodoc-class-list");
	for (var i=0; i<list.length; i++) {
		var classInfo = list[i];
		var classId = classInfo.classVertex.id;
		var ontologyInfo = this.ontologyManager.getOntologyInfo(classId.namespace);
		ontologyInfo.classList.push(classId);
		
		var anchor = Mustache.render(
				'<div class="ontodoc-index-entry"><a href="#{{{href}}}">{{label}}</a></div>', {
				href: classId.stringValue,
				label: classInfo.uniqueName
			});
		
		container.append(anchor);
		
	}
}

Ontodoc.prototype.clickClassName = function(owlClass) {
	var self = this;
	return function() {
		self.renderClass(owlClass);
	}
}

konig.Ontodoc = Ontodoc;
konig.buildOntodoc = function(options) {
	var ontologyService = options.ontologyService;
	
	ontologyService.getOntologyGraph(function(graphValue){
		options.ontologyGraph = graphValue;
		konig.ontodoc = new Ontodoc(options);
	});
}
konig.ShapeInfo = ShapeInfo;
konig.PropertyBlock = PropertyBlock;
	
	
});

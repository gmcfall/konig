package io.konig.schemagen.jsonschema;

import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.NamespaceManager;
import io.konig.schemagen.Generator;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class JsonSchemaGenerator extends Generator {
	
	private JsonSchemaNamer namer;
	private JsonSchemaTypeMapper typeMapper;
	
	public JsonSchemaGenerator(JsonSchemaNamer namer, NamespaceManager nsManager, JsonSchemaTypeMapper typeMapper) {
		super(nsManager);
		this.namer = namer;
		this.typeMapper = typeMapper;
	}
	

	public JsonSchemaNamer getNamer() {
		return namer;
	}

	/**
	 * Generate a JSON Schema for a given SHACL Shape.
	 * @param shape The Shape for which a JSON Schema is to be generated
	 * @return The generated JSON Schema
	 */
	public ObjectNode generateJsonSchema(Shape shape) {
		
		Worker worker = new Worker();
		return worker.generateJsonSchema(shape);
	}
	
	private class Worker {

		private ObjectMapper mapper = new ObjectMapper();

		public ObjectNode generateJsonSchema(Shape shape) {
			
			String schemaId = namer.schemaId(shape);
			
			
			ObjectNode json = mapper.createObjectNode();
			json.put("$schema", "http://json-schema.org/draft-04/schema#");
			json.put("id", schemaId);
			json.put("type", "object");
			
			putProperties(json, shape);
			
			return json;
		}

		private void putProperties(ObjectNode json, Shape shape) {
			
			List<PropertyConstraint> list = shape.getProperty();
			if (list != null && !list.isEmpty()) {
				ObjectNode properties = mapper.createObjectNode();
				json.put("properties", properties);
				for (PropertyConstraint constraint : list) {
					putProperty(properties, constraint);
				}
			}
			
			// TODO: list required fields.
			
		}

		private void putProperty(ObjectNode properties, PropertyConstraint property) {
			
			URI propertyId = property.getPredicate();
			
			String fieldName = propertyId.getLocalName();

			Integer maxCount = property.getMaxCount();
			
			ObjectNode field = mapper.createObjectNode();
			properties.put(fieldName, field);
			
			String doc = documentation(property);
			if (doc != null) {
				field.put("description", doc);
			}
			
			if (maxCount==null || maxCount>1) {
				field.put("type", "array");
				field.put("items", createType(fieldName, property, field));
			
			} else {
				field.put("type", createType(fieldName, property, field));
			}
			
			
		}
		
		private JsonNode createType(String fieldName, PropertyConstraint property, ObjectNode field) {
			
			JsonNode result = null;
			NodeKind nodeKind = property.getNodeKind();
			URI datatype = property.getDatatype();
			Resource valueShapeId = property.getValueShapeId();
			
			List<String> enumList = null;
			
			
			if (valueShapeId == null) {
				enumList = enumList(property);
			}
			String strictValue = strictValue(property);
			
			if (strictValue != null) {
				ObjectNode object = (ObjectNode) mapper.createObjectNode();
				object.put("type", "string");
				ArrayNode array = mapper.createArrayNode();
				object.put("enum", array);
				array.add(strictValue);
				result = object;
				
			} else if (enumList != null) {
				ObjectNode object = (ObjectNode) mapper.createObjectNode();
				object.put("type", "string");
				ArrayNode array = mapper.createArrayNode();
				object.put("enum", array);
				for (String value : enumList) {
					array.add(value);
				}
				result = object;
			} else if (nodeKind == NodeKind.IRI) {
				ObjectNode object = (ObjectNode) mapper.createObjectNode();
				object.put("type", "string");
				object.put("format", "uri");
				result = object;
				
			} else if (datatype != null) {
				JsonSchemaDatatype jsonType = typeMapper.type(property);
				String typeName = jsonType.getTypeName();
				String format = jsonType.getFormat();

				if (field != null && format==null) {
					result = mapper.getNodeFactory().textNode(typeName);
				} else {

					ObjectNode object = (ObjectNode) mapper.createObjectNode();
					object.put("type", typeName);
					if (format != null) {
						object.put("format", format);
					}
					result = object;
				}
				
			} else if (valueShapeId != null) {
				Shape valueShape = property.getValueShape();
				result = generateJsonSchema(valueShape);
			}
			
			
			return result;
		}

	}
	

}
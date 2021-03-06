package io.konig.core.path;

import java.util.HashSet;

/*
 * #%L
 * Konig Core
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


import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.SPARQLBuilder;
import io.konig.core.TraversalException;
import io.konig.core.Traverser;
import io.konig.core.Vertex;

/**
 * A step which explicitly adds one or more vertices to the traversal
 * @author Greg McFall
 *
 */
public class VertexStep implements Step {
	Resource[] resource;
	
	public VertexStep(Resource[] object) {
		this.resource = object;
	}
	
	@Override
	public boolean equals(Object other) {
		
		boolean result = false;
		if (other instanceof VertexStep) {
			VertexStep b = (VertexStep) other;
			if (resource.length == b.resource.length) {
				result = true;
				Set<Resource> set = new HashSet<>();
				for (Resource r : b.resource) {
					set.add(r);
				}
				
				for (Resource r : resource) {
					if (!set.contains(r)) {
						result = false; 
						break;
					}
				}
			}
		}
		return result;
	}


	@Override
	public void traverse(Traverser traverser) throws TraversalException {
		Graph graph = traverser.getGraph();
		Set<Value> value = traverser.getSource();
		for (Value v : value) {
			traverser.addResult(v);
		}
		for (Resource r : resource) {
			Vertex v = graph.vertex(r);
			traverser.addResult(v.getId());
		}

	}


	@Override
	public void visit(SPARQLBuilder builder) {
		if (resource.length==1) {
			builder.append(resource[0]);
		} else {
			builder.append('(');
			String delim = "";
			for (Resource r : resource) {
				builder.append(delim);
				builder.append(r);
				delim = "|";
			}
			builder.append(')');
		}
		
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (resource.length==1) {
			builder.append(resource[0]);
		} else {
			builder.append('(');
			String delim = "";
			for (Resource r : resource) {
				builder.append(delim);
				builder.append(r);
				delim = "|";
			}
			builder.append(')');
		}
		return builder.toString();
	}


	@Override
	public void append(StringBuilder builder, NamespaceManager nsManager) {
		throw new RuntimeException("Not implemented");
		
	}

}

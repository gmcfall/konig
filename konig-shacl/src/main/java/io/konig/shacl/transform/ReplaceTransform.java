package io.konig.shacl.transform;

/*
 * #%L
 * konig-shacl
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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ReplaceTransform {

	private Vertex source;
	private Vertex target;
	private Shape shape;
	
	public ReplaceTransform(Vertex source, Vertex target, Shape shape) {
		this.source = source;
		this.target = target;
		this.shape = shape;
	}
	
	public void execute() {
		Set<Edge> matched = new HashSet<Edge>();
		deleteStatements(matched);
		addStatements(matched);
	}

	private void addStatements(Set<Edge> matched) {

		Graph targetGraph = target.getGraph();
		List<Edge> newEdge = new ArrayList<Edge>();
		List<PropertyConstraint> propertyList = shape.getProperty();
		for (PropertyConstraint constraint : propertyList) {
			URI predicate = constraint.getPredicate();
			Set<Edge> sourceProperty = source.outProperty(predicate);
			for (Edge e : sourceProperty) {
				if (!matched.contains(e)) {
					newEdge.add(e);
				}
			}
		}
		for (Edge e : newEdge) {
			targetGraph.edge(e);
		}
		
	}

	private void deleteStatements(Set<Edge> matched) {
		
		List<Edge> zombie = new ArrayList<Edge>();
		List<PropertyConstraint> propertyList = shape.getProperty();
		for (PropertyConstraint constraint : propertyList) {
			URI predicate = constraint.getPredicate();
			Set<Edge> sourceProperty = source.outProperty(predicate);
			Set<Edge> targetProperty = target.outProperty(predicate);
			for (Edge targetEdge : targetProperty) {
				if (sourceProperty.contains(targetEdge)) {
					matched.add(targetEdge);
				} else {
					zombie.add(targetEdge);
				}
			}
		}

		Graph targetGraph = target.getGraph();
		for (Edge z : zombie) {
			targetGraph.remove(z);
		}
		
	}
	
	
	

}

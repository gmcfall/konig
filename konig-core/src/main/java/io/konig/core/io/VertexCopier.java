package io.konig.core.io;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;

public class VertexCopier {
	
	/**
	 * Copy a vertex to a specified graph by traversing all outgoing
	 * edges recursively.
	 * 
	 * @param v The vertex to be copied.
	 * @param g The graph into which the vertex is to be copied.
	 */
	public void deepCopy(Vertex v, Graph g) {
		Set<Resource> memory = new HashSet<>();
		Resource id = v.getId();
		memory.add(id);
		Vertex w = g.vertex(v.getId());
		copy(memory, v, w);
	}

	private void copy(Set<Resource> memory, Vertex v, Vertex w) {
		
		for (Entry<URI,Set<Edge>> out : v.outEdges()) {
			URI predicate = out.getKey();
			for (Edge edge : out.getValue()) {
				Value object = edge.getObject();
				w.addProperty(predicate, object);
				if (object instanceof Resource) {
					Resource objectId = (Resource) object;
					if (!memory.contains(objectId)) {
						memory.add(objectId);
						Vertex x = v.getGraph().vertex(objectId);
						Vertex y = w.getGraph().vertex(objectId);
						copy(memory, x, y);
					}
				}
			}
		}
		
	}

}

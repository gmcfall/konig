package io.konig.core.impl;

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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Edge;

public class EdgeMapImpl {

	private Map<URI, Set<Edge>> map = new LinkedHashMap<URI, Set<Edge>>();
	
	public void add(Edge edge) {
		Set<Edge> set = map.get(edge.getPredicate());
		if (set == null) {
			set = new LinkedHashSet<Edge>();
			map.put(edge.getPredicate(), set);
		}
		set.add(edge);
	}
	
	public Set<Edge> get(URI predicate) {
		return map.get(predicate);
	}
	
	public Set<Entry<URI, Set<Edge>>> entries() {
		return map.entrySet();
	}
}

package io.konig.core;

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


import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.Value;

import io.konig.core.path.Step;

public class Traverser {
	private Graph graph;
	private Set<Value> source;
	private Set<Value> resultSet;
	
	public Traverser(Graph graph) {
		this.graph = graph;
		resultSet = new LinkedHashSet<>();
	}
	
	public Traverser(Graph graph, Set<Value> source) {
		this.graph = graph;
		this.resultSet = source;
	}
	
	public void visit(Step step) {
		source = resultSet;
		resultSet = new HashSet<>();
		step.traverse(this);
	}
	
	public Graph getGraph() {
		return graph;
	}

	public Set<Value> getSource() {
		return source;
	}
	
	public Set<Value> getResultSet() {
		return resultSet;
	}
	
	public void addResult(Value result) {
		resultSet.add(result);
	}

}
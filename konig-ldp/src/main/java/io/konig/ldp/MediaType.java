package io.konig.ldp;

/*
 * #%L
 * Konig Linked Data Platform
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


public class MediaType {
	
	public static final MediaType TURTLE = new MediaType("text/turtle");

	private String fullName;
	private String type;
	private String subtype;
	
	public static MediaType instance(String fullName) {
		return new MediaType(fullName);
	}
	
	public MediaType(String fullName) {
		this.fullName = fullName;
		int slash = fullName.indexOf('/');
		type = fullName.substring(0, slash);
		subtype = fullName.substring(slash+1);
	}

	public String getFullName() {
		return fullName;
	}

	public String getType() {
		return type;
	}

	public String getSubtype() {
		return subtype;
	}

	public String toString() {
		return fullName;
	}
	
	public boolean matches(MediaType other) {
		
		return 
			fullName.equals(other.fullName) ||
			(
				("*".equals(type)    || "*".equals(other.type)    ||    type.equals(other.type)) &&
				("*".equals(subtype) || "*".equals(other.subtype) || subtype.equals(other.subtype))
			);
	}
}

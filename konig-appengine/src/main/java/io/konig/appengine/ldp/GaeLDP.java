package io.konig.appengine.ldp;

/*
 * #%L
 * konig-war
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


import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GaeLDP {

	
	public static final String MEMBERSHIP = "LdpMembership";
	public static final String RESOURCE = "LdpResource";
	
	public static final String SUBJECT = "subject";
	public static final String OBJECT = "object";
	
	public static final String TYPE = "ldpType";
	public static final String CONTENT_TYPE = "contentType";
	public static final String BODY = "body";

	public static Key resourceKey(String resourceIRI) {
		return KeyFactory.createKey(RESOURCE, resourceIRI);
	}
	
	public static String resourceId(Key resourceKey) {
		return resourceKey.getName();
	}
	
}

package io.konig.appengine;

/*
 * #%L
 * konig-appengine
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


import com.google.apphosting.api.ApiProxy;

public class GaeEnvironment {

	private static final String LOCALHOST = "localhost:8888";
	
	public static String getHostName() {
		ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
		String hostName = (String) env.getAttributes().get("com.google.appengine.runtime.default_version_hostname");
		if (hostName == null) {
			hostName = LOCALHOST;
		}
		return hostName;
	}

}

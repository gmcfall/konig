package io.konig.appengine;

import io.konig.core.ContextManager;
import io.konig.core.RewriteService;
import io.konig.core.impl.ProxyIriRewriteService;
import io.konig.core.io.ContextReader;

/*
 * #%L
 * konig-appengine
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


import io.konig.core.io.ResourceManager;
import io.konig.services.impl.BaseKonigConfig;

public class GaeKonigConfig extends BaseKonigConfig {
	
	private static GaeKonigConfig INSTANCE = new GaeKonigConfig();
	
	public static GaeKonigConfig getInstance() {
		return INSTANCE;
	}
	

	public ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new GaeResourceManager();
		}
		return resourceManager;
	}
	
	public ContextManager getContextManager() {
		if (contextManager == null) {
			ContextReader reader = new ContextReader();
			contextManager = new GaeContextManager(reader);
		}
		return contextManager;
	}



	protected RewriteService createRewriteService() {
		
		String hostName = GaeEnvironment.getHostName();
		StringBuilder builder = new StringBuilder();
		builder.append("http://");
		builder.append(hostName);
		builder.append('/');
		String localHost = builder.toString();
		
		return new ProxyIriRewriteService(GaeConstants.BASE_URI, GaeConstants.PROXY_URI, localHost);
	}
}
/*******************************************************************************
 * Copyright (c) 2020 Thales Global Services S.A.S.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package com.thalesgroup.mde.openmbee.connector.mms.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MMSProjectDescriptor extends MMSNamedDescriptor {
	public static final String FEATURE_PREFIX__EMF = "EMF_FEATURE__"; //$NON-NLS-1$
	public static final String FEATURE_PREFIX = "featurePrefix"; //$NON-NLS-1$
	public static final String CLIENT_SIDE_NAME = "clientSideName"; //$NON-NLS-1$
	public static final String PRIVATE_VISIBILITY = "private"; //$NON-NLS-1$
	public static final String INTERNAL_VISIBILITY = "internal"; //$NON-NLS-1$
	public String _refId;
	public String _elasticId;
	public Boolean _editable;
	public String _created;
	public String _modified;
	public String _projectId;
	public String categoryId;
	public String _creator;
	public String _modifier;
	public String _uri;
	public String _qualifiedName;
	public String _qualifiedId;
	public String orgId;
	public String featurePrefix;
	public String clientSideName;
	public List<String> _inRefIds = new ArrayList<>();

	public static class JsonMMS3TypeAdapter implements JsonDeserializer<MMSProjectDescriptor>, JsonSerializer<MMSProjectDescriptor> {

		private static final Gson gson = new GsonBuilder().create();

		@Override
		public MMSProjectDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return gson.fromJson(json, MMSProjectDescriptor.class);
		}

		@Override
		public JsonElement serialize(MMSProjectDescriptor desc, Type type, JsonSerializationContext context) {
			JsonObject serialized = gson.toJsonTree(desc, MMSProjectDescriptor.class).getAsJsonObject();
			serialized.addProperty("type", MMSConstants.MMS_TYPE_PROJECT); //$NON-NLS-1$
			return serialized;
		}
	}
	
	public static class JsonMMS4TypeAdapter implements JsonDeserializer<MMSProjectDescriptor>, JsonSerializer<MMSProjectDescriptor> {

		private static final Gson gson = new GsonBuilder().create();

		@Override
		public MMSProjectDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			MMSProjectDescriptor desc = gson.fromJson(json, MMSProjectDescriptor.class);
			desc.orgId = ((JsonObject) json).get("org").getAsString(); //$NON-NLS-1$
			JsonObject custom = ((JsonObject) json).get("custom").getAsJsonObject(); //$NON-NLS-1$
			desc.clientSideName = custom.has("clientSideName") ? custom.get("clientSideName").getAsString() : null; //$NON-NLS-1$ //$NON-NLS-2$
			desc.featurePrefix = custom.has("featurePrefix") ? custom.get("featurePrefix").getAsString() : null; //$NON-NLS-1$ //$NON-NLS-2$
			return desc;
		}

		@Override
		public JsonElement serialize(MMSProjectDescriptor desc, Type type, JsonSerializationContext context) {
			JsonObject serialized = gson.toJsonTree(desc, MMSProjectDescriptor.class).getAsJsonObject();
			serialized.remove("orgId"); //$NON-NLS-1$
			serialized.remove("clientSideName"); //$NON-NLS-1$
			serialized.remove("featurePrefix"); //$NON-NLS-1$
			serialized.remove("_inRefIds"); //$NON-NLS-1$
			serialized.addProperty("visibility", MMSProjectDescriptor.PRIVATE_VISIBILITY); //$NON-NLS-1$
			JsonObject custom = new JsonObject();
			custom.addProperty("clientSideName", desc.clientSideName); //$NON-NLS-1$
			custom.addProperty("featurePrefix", desc.featurePrefix); //$NON-NLS-1$
			serialized.add("custom", custom); //$NON-NLS-1$
			return serialized;
		}
	}
}

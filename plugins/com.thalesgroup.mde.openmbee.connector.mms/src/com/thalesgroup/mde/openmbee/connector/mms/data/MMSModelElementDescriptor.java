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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MMSModelElementDescriptor extends MMSIdentifiableDescriptor {
	transient public MMSVCSInfoDescriptior vcsiDescriptor;
	transient public MMSMSInfoDescriptor msiDescriptor;
	
	@Override
	public String toString() {
		return String.format("MMSModelElement:\r\n\t%s\r\n\t%s", //$NON-NLS-1$
				String.join("\r\n\t", Arrays.stream(MMSModelElementDescriptor.class.getFields()).map(f -> { //$NON-NLS-1$
					try {
						return String.format("%s: %s", f.getName(), f.get(this)); //$NON-NLS-1$
					} catch (IllegalArgumentException | IllegalAccessException e) {}
					return "Cannot get value for field "+f.getName(); //$NON-NLS-1$
				}).collect(Collectors.toList())),
				String.join("\r\n\t", msiDescriptor.attributes.entrySet().stream() //$NON-NLS-1$
										.map(e -> String.format("%s: %s", e.getKey(), objectToString(e.getValue()))) //$NON-NLS-1$
										.toArray(String[]::new)));
	}
	
	private String objectToString(Object o) {
		if(o == null) {
			return null;
		}
		if(o.getClass().isArray()) {
			Object[] a = (Object[]) o;
			return String.join(", ", Arrays.stream(a).map(ob -> objectToString(ob)).collect(Collectors.toList())); //$NON-NLS-1$
		}
		return o.toString();
	}
	
	public void setId(String id) {
		this.id = id;
		if(this.msiDescriptor != null) {
			this.msiDescriptor.id = id;
		}
		if(this.vcsiDescriptor != null) {
			this.vcsiDescriptor.id = id;
		}
	}
	
	/**
	 * MMS Version Control System Information Description 
	 */
	public static class MMSVCSInfoDescriptior extends MMSIdentifiableDescriptor {
		public String _created;
		public String _creator;
		public Boolean _editable;
		public String _elasticId;
		public List<String> _inRefIds = new ArrayList<>();
		public String _modified;
		public String _modifier;
		public String _projectId;
		public String _refId;
	}
	
	/**
	 * Modelling System specific Information from MMS
	 */
	public static class MMSMSInfoDescriptor extends MMSIdentifiableDescriptor {
		public String ownerId;
		public String type;
		
		public Map<String, Object> attributes = new HashMap<>();
		public String emfNsUri;
		
		public static abstract class AbstractInfoJsonTypeAdapter implements JsonDeserializer<MMSMSInfoDescriptor> {

			protected Gson gson;
			
			public AbstractInfoJsonTypeAdapter() {
				gson = new GsonBuilder().create();
			}
			
			@Override
			public MMSMSInfoDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				MMSMSInfoDescriptor msiDescriptor = new MMSMSInfoDescriptor();
				JsonObject jsonObject = json.getAsJsonObject();
				Set<String> processedFields = new HashSet<>();
				processedFields.addAll(setOfFieldNames(MMSVCSInfoDescriptior.class));
				processedFields.addAll(setOfFieldNames(MMSMSInfoDescriptor.class));
				
				try {
					JsonElement jsonElement = jsonObject.get("id"); //$NON-NLS-1$
					if(jsonElement!= null) msiDescriptor.id = jsonElement.getAsString();
				} catch(UnsupportedOperationException e) {}
				try {
					JsonElement jsonElement = jsonObject.get("type"); //$NON-NLS-1$
					if(jsonElement!= null) msiDescriptor.type = jsonElement.getAsString();
				} catch(UnsupportedOperationException e) {}
				try {
					JsonElement jsonElement = jsonObject.get("emfNsUri"); //$NON-NLS-1$
					if(jsonElement!= null) msiDescriptor.emfNsUri = jsonElement.getAsString();
				} catch(UnsupportedOperationException e) {}
				try {
					JsonElement jsonElement = jsonObject.get("ownerId"); //$NON-NLS-1$
					if(jsonElement!= null) msiDescriptor.ownerId = jsonElement.getAsString();
				} catch(UnsupportedOperationException e) {}
				
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					if(!processedFields.contains(entry.getKey())) {
						Object decodedObject = decode(entry.getValue());
						msiDescriptor.attributes.put(entry.getKey(), decodedObject);
					}
				}
				return msiDescriptor;
			}

			private Set<String> setOfFieldNames(Class<?> c) {
				return Arrays.stream(c.getFields()).map(f -> f.getName()).collect(Collectors.toSet());
			}
			
			protected final Object decode(JsonElement value) {
				if(value.isJsonArray()) {
					JsonArray array = value.getAsJsonArray();
					List<Object> list = new ArrayList<>();
					array.forEach(jo -> list.add(decode(jo)) );
					return list;
				}
				if(value.isJsonNull()) {
					return null;
				}
				if(value.isJsonPrimitive()) {
					return gson.fromJson(value, Object.class);
				}
				return decodeObject(value.getAsJsonObject());
			}

			protected abstract Object decodeObject(JsonObject value);
			
		}
		
		public static class ComplexTypeInfoJsonTypeAdapter extends AbstractInfoJsonTypeAdapter {
			
			public ComplexTypeInfoJsonTypeAdapter() {
				gson = new GsonBuilder().registerTypeAdapter(MMSMSInfoDescriptor.class, new SimpleTypeInfoJsonTypeAdapter()).create();
			}

			@Override
			protected Object decodeObject(JsonObject jo) {
				return gson.fromJson(jo, MMSMSInfoDescriptor.class);
			}
			
		}
		
		public static class SimpleTypeInfoJsonTypeAdapter extends AbstractInfoJsonTypeAdapter {
			
			@Override
			protected Object decodeObject(JsonObject jo) {
				return null;
			}
			
		}
	}

	public static class JsonMMS3TypeAdapter implements JsonDeserializer<MMSModelElementDescriptor>, JsonSerializer<MMSModelElementDescriptor> {

		private static final Gson gson = new GsonBuilder()
											.registerTypeAdapter(MMSMSInfoDescriptor.class, new MMSMSInfoDescriptor.ComplexTypeInfoJsonTypeAdapter())
											.create();

		@Override
		public MMSModelElementDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			MMSModelElementDescriptor desc = gson.fromJson(json, MMSModelElementDescriptor.class);
			desc.vcsiDescriptor = gson.fromJson(json, MMSVCSInfoDescriptior.class);
			desc.msiDescriptor = gson.fromJson(json, MMSMSInfoDescriptor.class);
			return desc;
		}

		@Override
		public JsonElement serialize(MMSModelElementDescriptor desc, Type type, JsonSerializationContext context) {
			JsonObject serialized = gson.toJsonTree(desc.vcsiDescriptor, MMSVCSInfoDescriptior.class).getAsJsonObject();
			
			serialized.addProperty("ownerId", desc.msiDescriptor.ownerId); //$NON-NLS-1$
			serialized.addProperty("type", desc.msiDescriptor.type); //$NON-NLS-1$
			serialized.addProperty("emfNsUri", desc.msiDescriptor.emfNsUri); //$NON-NLS-1$
			for (Entry<String, Object> attribute : desc.msiDescriptor.attributes.entrySet()) {
				if(attribute.getValue() != null && !"id".contentEquals(attribute.getKey())) { //$NON-NLS-1$
					serialized.add(attribute.getKey(), gson.toJsonTree(attribute.getValue()));
				}
			}
			return serialized;
		}
	}

	public static class JsonMMS4TypeAdapter implements JsonDeserializer<MMSModelElementDescriptor>, JsonSerializer<MMSModelElementDescriptor> {

		private static final Gson gson = new GsonBuilder()
											.registerTypeAdapter(MMSMSInfoDescriptor.class, new MMSMSInfoDescriptor.ComplexTypeInfoJsonTypeAdapter())
											.create();

		@Override
		public MMSModelElementDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return gson.fromJson(json, MMSModelElementDescriptor.class);
		}

		@Override
		public JsonElement serialize(MMSModelElementDescriptor desc, Type type, JsonSerializationContext context) {
			JsonObject serialized = gson.toJsonTree(desc.vcsiDescriptor, MMSVCSInfoDescriptior.class).getAsJsonObject();

			serialized.remove("_inRefIds"); //$NON-NLS-1$
			serialized.addProperty("id", desc.msiDescriptor.id); //$NON-NLS-1$
			String name = (String) desc.msiDescriptor.attributes.get("EMF_FEATURE__name"); //$NON-NLS-1$
			serialized.addProperty("name", name != null ? name : ""); //$NON-NLS-1$ //$NON-NLS-2$
			String description = (String) desc.msiDescriptor.attributes.get("EMF_FEATURE__description"); //$NON-NLS-1$
			serialized.addProperty("documentation", description != null ? description : ""); //$NON-NLS-1$ //$NON-NLS-2$
			serialized.addProperty("type", desc.msiDescriptor.type); //$NON-NLS-1$
			JsonObject msinfo = gson.toJsonTree(desc.msiDescriptor, MMSMSInfoDescriptor.class).getAsJsonObject();
			serialized.add("custom", msinfo); //$NON-NLS-1$
			serialized.addProperty("parent", desc.msiDescriptor.ownerId); //$NON-NLS-1$

			return serialized;
		}
	}
}

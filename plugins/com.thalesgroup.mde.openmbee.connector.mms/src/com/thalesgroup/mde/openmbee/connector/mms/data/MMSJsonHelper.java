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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MMSJsonHelper {

	public static List<MMSOrganizationDescriptor> readOrgsFromJson(String json) {
		List<MMSOrganizationDescriptor> orgs = new ArrayList<>();
		MMSRootDescriptor root = readRootFromJson(json);
		for (MMSOrganizationDescriptor element : root.orgs) {
			orgs.add(element);
		}
		return orgs;
	}
	
	public static MMSRootDescriptor readRootFromJson(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = getPreparedGsonBuilder().create();
			return gson.fromJson(reader, MMSRootDescriptor.class);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Cannot read the given json:%s%s", System.lineSeparator(), json), e); //$NON-NLS-1$
		}
	}
	
	public static GsonBuilder getPreparedGsonBuilder() {
		return new GsonBuilder().registerTypeHierarchyAdapter(Collection.class, new SkipEmptyCollectionsSerializer())
								.registerTypeAdapter(MMSModelElementDescriptor.class, new MMSModelElementDescriptor.JsonTypeAdapter());
	}
	
	public static class SkipEmptyCollectionsSerializer implements JsonSerializer<Collection<?>> {

		@Override
		public JsonElement serialize(Collection<?> src, Type typeOfSrc, JsonSerializationContext context) {
			if (src == null || src.isEmpty())
				return null;

			JsonArray array = new JsonArray();

			for (Object child : src) {
				JsonElement element = context.serialize(child);
				array.add(element);
			}

			return array;
		}
		
	}
}

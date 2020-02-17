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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MMSJsonHelper {

	public static String readTokenFromJson(String apiVersion, String json) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return MMS3JsonHelper.readTokenFromJson(json);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return MMS4JsonHelper.readTokenFromJson(json);
		}
		return ""; //$NON-NLS-1$
	}

	public static List<MMSOrganizationDescriptor> readOrganizationsFromJson(String apiVersion, String json) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return MMS3JsonHelper.readOrganizationsFromJson(json);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return MMS4JsonHelper.readOrganizationsFromJson(json);
		}
		return Collections.emptyList();
	}

	public static List<MMSProjectDescriptor> readProjectsFromJson(String apiVersion, String json) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return MMS3JsonHelper.readProjectsFromJson(json);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return MMS4JsonHelper.readProjectsFromJson(json);
		}
		return Collections.emptyList();
	}

	public static List<MMSRefDescriptor> readBranchesFromJson(String apiVersion, String json) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return MMS3JsonHelper.readBranchesFromJson(json);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return MMS4JsonHelper.readBranchesFromJson(json);
		}
		return Collections.emptyList();
	}

	public static List<MMSCommitDescriptor> readCommitsFromJson(String apiVersion, String json) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return MMS3JsonHelper.readCommitsFromJson(json);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return MMS4JsonHelper.readCommitsFromJson(json);
		}
		return Collections.emptyList();
	}

	public static List<MMSModelElementDescriptor> readElementsFromJson(String apiVersion, String json) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return MMS3JsonHelper.readElementsFromJson(json);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return MMS4JsonHelper.readElementsFromJson(json);
		}
		return Collections.emptyList();
	}

	public static MMSRootDescriptor readRootFromJson(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = getMMS3PreparedGsonBuilder().create();
			return gson.fromJson(reader, MMSRootDescriptor.class);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Cannot read the given json:%s%s", System.lineSeparator(), json), e); //$NON-NLS-1$
		}
	}

	public static GsonBuilder getMMS3PreparedGsonBuilder() {
		return new GsonBuilder().registerTypeHierarchyAdapter(Collection.class, new SkipEmptyCollectionsSerializer())
								.registerTypeAdapter(MMSModelElementDescriptor.class, new MMSModelElementDescriptor.JsonMMS3TypeAdapter())
								.registerTypeAdapter(MMSProjectDescriptor.class, new MMSProjectDescriptor.JsonMMS3TypeAdapter());
	}

	public static GsonBuilder getMMS4PreparedGsonBuilder() {
		return new GsonBuilder().registerTypeHierarchyAdapter(Collection.class, new SkipEmptyCollectionsSerializer())
								.registerTypeAdapter(MMSModelElementDescriptor.class, new MMSModelElementDescriptor.JsonMMS4TypeAdapter())
								.registerTypeAdapter(MMSProjectDescriptor.class, new MMSProjectDescriptor.JsonMMS4TypeAdapter());
	}
	
	private static class SkipEmptyCollectionsSerializer implements JsonSerializer<Collection<?>> {

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

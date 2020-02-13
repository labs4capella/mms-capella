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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

public class MMSJsonHelper {

	public static List<MMSOrganizationDescriptor> readOrganizationsFromJson(String json) {
		List<MMSOrganizationDescriptor> orgs = new ArrayList<>();
		try {
			MMSRootDescriptor root = readRootFromJson(json);
			for (MMSOrganizationDescriptor element : root.orgs) {
				orgs.add(element);
			}
		} catch (JsonSyntaxException ex) {
			// MMS4 data structure
			MMSOrganizationDescriptor[] orgsArray = readOrganizationsFromJsonArray(json);
			return Arrays.asList(orgsArray);
		}
		return orgs;
	}

	public static List<MMSProjectDescriptor> readProjectsFromJson(String json) {
		List<MMSProjectDescriptor> projects = new ArrayList<>();
		try {
			MMSRootDescriptor root = readRootFromJson(json);
			for (MMSProjectDescriptor element : root.projects) {
				projects.add(element);
			}
		} catch (JsonSyntaxException ex) {
			// MMS4 data structure
			MMSProjectDescriptor[] projectsArray = readProjectsFromJsonArray(json);
			return Arrays.asList(projectsArray);
		}
		return projects;
	}

	public static List<MMSRefDescriptor> readBranchesFromJson(String json) {
		List<MMSRefDescriptor> refs = new ArrayList<>();
		try {
			MMSRootDescriptor root = readRootFromJson(json);
			for (MMSRefDescriptor element : root.refs) {
				refs.add(element);
			}
		} catch (JsonSyntaxException ex) {
			// MMS4 data structure
			MMSRefDescriptor[] refsArray = readBranchesFromJsonArray(json);
			return Arrays.asList(refsArray);
		}
		return refs;
	}

	public static List<MMSCommitDescriptor> readCommitsFromJson(String json) {
		List<MMSCommitDescriptor> commits = new ArrayList<>();
		try {
			MMSRootDescriptor root = readRootFromJson(json);
			for (MMSCommitDescriptor element : root.commits) {
				commits.add(element);
			}
		} catch (JsonSyntaxException ex) {
			// MMS4 data structure
			MMSCommitDescriptor[] commitsArray = readCommitsFromJsonArray(json);
			return Arrays.asList(commitsArray);
		}
		return commits;
	}

	public static List<MMSModelElementDescriptor> readElementsFromJson(String json) {
		List<MMSModelElementDescriptor> elements = new ArrayList<>();
		try {
			MMSRootDescriptor root = readRootFromJson(json);
			for (MMSModelElementDescriptor element : root.elements) {
				elements.add(element);
			}
		} catch (JsonSyntaxException ex) {
			// MMS4 data structure
			MMSModelElementDescriptor[] elementsArray = readElementsFromJsonArray(json);
			return Arrays.asList(elementsArray);
		}
		return elements;
	}

	public static MMSRootDescriptor readRootFromJson(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = getPreparedGsonBuilder().create();
			return gson.fromJson(reader, MMSRootDescriptor.class);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Cannot read the given json:%s%s", System.lineSeparator(), json), e); //$NON-NLS-1$
		}
	}

	public static MMSOrganizationDescriptor[] readOrganizationsFromJsonArray(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = getPreparedGsonBuilder().create();
			return gson.fromJson(reader, MMSOrganizationDescriptor[].class);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Cannot read the given json:%s%s", System.lineSeparator(), json), e); //$NON-NLS-1$
		}
	}

	public static MMSProjectDescriptor[] readProjectsFromJsonArray(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = getPreparedGsonBuilder().create();
			return gson.fromJson(reader, MMSProjectDescriptor[].class);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Cannot read the given json:%s%s", System.lineSeparator(), json), e); //$NON-NLS-1$
		}
	}

	public static MMSRefDescriptor[] readBranchesFromJsonArray(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = getPreparedGsonBuilder().create();
			return gson.fromJson(reader, MMSRefDescriptor[].class);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Cannot read the given json:%s%s", System.lineSeparator(), json), e); //$NON-NLS-1$
		}
	}

	public static MMSCommitDescriptor[] readCommitsFromJsonArray(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = getPreparedGsonBuilder().create();
			return gson.fromJson(reader, MMSCommitDescriptor[].class);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Cannot read the given json:%s%s", System.lineSeparator(), json), e); //$NON-NLS-1$
		}
	}

	public static MMSModelElementDescriptor[] readElementsFromJsonArray(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = getPreparedGsonBuilder().create();
			return gson.fromJson(reader, MMSModelElementDescriptor[].class);
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

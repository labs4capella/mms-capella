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
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper.MMSConnectionException;

/**
 * JSon helpers for MMS4
 */
public class MMS4JsonHelper {

	private static final String INVALID_LOGIN = "Invalid login response:%s%s"; //$NON-NLS-1$
	private static final String INVALID_JSON_DATA = "Cannot read the given json:%s%s"; //$NON-NLS-1$

	/**
	 * Retrieves a token from a given json data
	 * @param json the json data to be read
	 * @return a token
	 */
	public static String readTokenFromJson(String json) {
		MMSRootDescriptor root = MMSJsonHelper.readRootFromJson(json);
		if (root.token != null && root.token.length()>0) {
			return root.token;
		} else {
			throw new MMSConnectionException(String.format(INVALID_LOGIN, System.lineSeparator(), json));
		}
	}

	/**
	 * Retrieves a list of organizations from a given json data
	 * @param json the json data to be read
	 * @return a list of organizations
	 */
	public static List<MMSOrganizationDescriptor> readOrganizationsFromJson(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = MMSJsonHelper.getMMS4PreparedGsonBuilder().create();
			return Arrays.asList(gson.fromJson(reader, MMSOrganizationDescriptor[].class));
		} catch (IOException e) {
			throw new RuntimeException(String.format(INVALID_JSON_DATA, System.lineSeparator(), json), e);
		}
	}

	/**
	 * Retrieves a list of projects from a given json data
	 * @param json the json data to be read
	 * @return a list of projects
	 */
	public static List<MMSProjectDescriptor> readProjectsFromJson(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = MMSJsonHelper.getMMS4PreparedGsonBuilder().create();
			return Arrays.asList(gson.fromJson(reader, MMSProjectDescriptor[].class));
		} catch (IOException e) {
			throw new RuntimeException(String.format(INVALID_JSON_DATA, System.lineSeparator(), json), e);
		}
	}

	/**
	 * Retrieves a list of branches from a given json data
	 * @param json the json data to be read
	 * @return a list of branches
	 */
	public static List<MMSRefDescriptor> readBranchesFromJson(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = MMSJsonHelper.getMMS4PreparedGsonBuilder().create();
			return Arrays.asList(gson.fromJson(reader, MMSRefDescriptor[].class));
		} catch (IOException e) {
			throw new RuntimeException(String.format(INVALID_JSON_DATA, System.lineSeparator(), json), e);
		}
	}

	/**
	 * Retrieves a list of commits from a given json data
	 * @param json the json data to be read
	 * @return a list of commits
	 */
	public static List<MMSCommitDescriptor> readCommitsFromJson(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = MMSJsonHelper.getMMS4PreparedGsonBuilder().create();
			return Arrays.asList(gson.fromJson(reader, MMSCommitDescriptor[].class));
		} catch (IOException e) {
			throw new RuntimeException(String.format(INVALID_JSON_DATA, System.lineSeparator(), json), e);
		}
	}

	/**
	 * Retrieves a list of elements from a given json data
	 * @param json the json data to be read
	 * @return a list of elements
	 */
	public static List<MMSModelElementDescriptor> readElementsFromJson(String json) {
		try(Reader reader = new StringReader(json)) {
			Gson gson = MMSJsonHelper.getMMS4PreparedGsonBuilder().create();
			return Arrays.asList(gson.fromJson(reader, MMSModelElementDescriptor[].class));
		} catch (IOException e) {
			throw new RuntimeException(String.format(INVALID_JSON_DATA, System.lineSeparator(), json), e);
		}
	}
}

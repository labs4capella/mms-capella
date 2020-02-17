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

import java.util.ArrayList;
import java.util.List;

import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper.MMSConnectionException;

/**
 * JSon helpers for MMS3
 */
public class MMS3JsonHelper {

	private static final String INVALID_LOGIN = "Invalid login response:%s%s"; //$NON-NLS-1$

	/**
	 * Retrieves a token from a given json data
	 * @param json the json data to be read
	 * @return a token
	 */
	public static String readTokenFromJson(String json) {
		MMSRootDescriptor root = MMSJsonHelper.readRootFromJson(json);
		if (root.data != null && root.data.ticket != null && root.data.ticket.length()>0) {
			return root.data.ticket;
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
		List<MMSOrganizationDescriptor> orgs = new ArrayList<>();

		MMSRootDescriptor root = MMSJsonHelper.readRootFromJson(json);
		for (MMSOrganizationDescriptor element : root.orgs) {
			orgs.add(element);
		}

		return orgs;
	}

	/**
	 * Retrieves a list of projects from a given json data
	 * @param json the json data to be read
	 * @return a list of projects
	 */
	public static List<MMSProjectDescriptor> readProjectsFromJson(String json) {
		List<MMSProjectDescriptor> projects = new ArrayList<>();

		MMSRootDescriptor root = MMSJsonHelper.readRootFromJson(json);
		for (MMSProjectDescriptor element : root.projects) {
			projects.add(element);
		}

		return projects;
	}

	/**
	 * Retrieves a list of branches from a given json data
	 * @param json the json data to be read
	 * @return a list of branches
	 */
	public static List<MMSRefDescriptor> readBranchesFromJson(String json) {
		List<MMSRefDescriptor> refs = new ArrayList<>();

		MMSRootDescriptor root = MMSJsonHelper.readRootFromJson(json);
		for (MMSRefDescriptor element : root.refs) {
			refs.add(element);
		}

		return refs;
	}

	/**
	 * Retrieves a list of commits from a given json data
	 * @param json the json data to be read
	 * @return a list of commits
	 */
	public static List<MMSCommitDescriptor> readCommitsFromJson(String json) {
		List<MMSCommitDescriptor> commits = new ArrayList<>();

		MMSRootDescriptor root = MMSJsonHelper.readRootFromJson(json);
		for (MMSCommitDescriptor element : root.commits) {
			commits.add(element);
		}

		return commits;
	}

	/**
	 * Retrieves a list of elements from a given json data
	 * @param json the json data to be read
	 * @return a list of elements
	 */
	public static List<MMSModelElementDescriptor> readElementsFromJson(String json) {
		List<MMSModelElementDescriptor> elements = new ArrayList<>();

		MMSRootDescriptor root = MMSJsonHelper.readRootFromJson(json);
		for (MMSModelElementDescriptor element : root.elements) {
			elements.add(element);
		}

		return elements;
	}
}

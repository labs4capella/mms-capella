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

import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.getMMS3PreparedGsonBuilder;
import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.getMMS4PreparedGsonBuilder;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.thalesgroup.mde.openmbee.connector.mms.utils.RestApiHelper;
import com.thalesgroup.mde.openmbee.connector.mms.utils.RestApiHelper.DeleteRequestWithBody;

/**
 * MMS3 & MMS4 API helpers
 */
public class MMSAPIHelper {

	private static final String URL_POSTFIX__ORGANIZATIONS = "orgs"; //$NON-NLS-1$
	private static final String URL_POSTFIX__ORGANIZATIONS_BY_ID = "orgs/%s"; //$NON-NLS-1$

	private static final String URL_POSTFIX__MMS3__PROJECTS = "orgs/%s/projects"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS3__PROJECTS_BY_ID = "projects/%s"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS3__BRANCHES = "projects/%s/refs"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS3__BRANCHES_BY_ID = "projects/%s/refs/%s"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS3__COMMITS = "projects/%s/refs/%s/commits"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS3__ELEMENTS = "projects/%s/refs/%s/elements"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS3__ELEMENTS_BY_COMMIT_ID = "projects/%s/refs/%s/elements?commitId=%s"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS3__ELEMENTS_BY_ID = "projects/%s/refs/%s/elements/%s"; //$NON-NLS-1$

	private static final String URL_POSTFIX__MMS4__PROJECTS = "orgs/%s/projects"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS4__PROJECTS_BY_ID = "orgs/%s/projects/%s"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS4__BRANCHES = "orgs/%s/projects/%s/branches"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS4__BRANCHES_BY_ID = "orgs/%s/projects/%s/branches/%s"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS4__COMMITS = "orgs/%s/projects/%s/branches/%s/commits"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS4__ELEMENTS = "orgs/%s/projects/%s/branches/%s/elements"; //$NON-NLS-1$
	private static final String URL_POSTFIX__MMS4__ELEMENTS_BY_ID = "orgs/%s/projects/%s/branches/%s/elements%s"; //$NON-NLS-1$

	/**
	 * Retrieves all the organizations
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request getOrganizations(RestApiHelper helper, String apiVersion) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__ORGANIZATIONS);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__ORGANIZATIONS);
		}
		return null;
	}

	/**
	 * Creates an organization with the given data
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organization organization related data
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request postOrganizations(RestApiHelper helper, String apiVersion, MMSOrganizationDescriptor organization) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			MMSRootDescriptor root = new MMSRootDescriptor();
			root.orgs.add(organization);
			String body = getMMS3PreparedGsonBuilder().create().toJson(root);
			return helper.preparePost(URL_POSTFIX__ORGANIZATIONS).bodyString(body, ContentType.APPLICATION_JSON);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			String body = getMMS3PreparedGsonBuilder().create().toJson(new MMSOrganizationDescriptor[] { organization });
			return helper.preparePost(URL_POSTFIX__ORGANIZATIONS).bodyString(body, ContentType.APPLICATION_JSON);
		}
		return null;
	}

	/**
	 * Deletes an organization having a given identifier
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static DeleteRequestWithBody deleteOrganizations(RestApiHelper helper, String apiVersion, String organizationId) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareDelete(URL_POSTFIX__ORGANIZATIONS_BY_ID, organizationId);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return helper.prepareDelete(URL_POSTFIX__ORGANIZATIONS_BY_ID, organizationId);
		}
		return null;
	}

	/**
	 * Retrieves all the projects related to a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request getProjects(RestApiHelper helper, String apiVersion, String organizationId) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS3__PROJECTS, organizationId);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS4__PROJECTS, organizationId);
		}
		return null;
	}

	/**
	 * Creates a project with the given data, related to a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param project project related data
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request postProjects(RestApiHelper helper, String apiVersion, String organizationId, MMSProjectDescriptor project) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			MMSRootDescriptor root = new MMSRootDescriptor();
			root.projects.add(project);
			String body = getMMS3PreparedGsonBuilder().create().toJson(root);
			return helper.preparePost(URL_POSTFIX__MMS3__PROJECTS, organizationId).bodyString(body, ContentType.APPLICATION_JSON);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			String body = getMMS4PreparedGsonBuilder().create().toJson(new MMSProjectDescriptor[] { project });
			return helper.preparePost(URL_POSTFIX__MMS4__PROJECTS, organizationId).bodyString(body, ContentType.APPLICATION_JSON);
		}
		return null;
	}

	/**
	 * Deletes a project having a given identifier, related to a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static DeleteRequestWithBody deleteProjects(RestApiHelper helper, String apiVersion, String organizationId, String projectId) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareDelete(URL_POSTFIX__MMS3__PROJECTS_BY_ID, projectId);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return helper.prepareDelete(URL_POSTFIX__MMS4__PROJECTS_BY_ID, organizationId, projectId);
		}
		return null;
	}

	/**
	 * Retrieves all the branches related to a given project, within a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request getBranches(RestApiHelper helper, String apiVersion, String organizationId, String projectId) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS3__BRANCHES, projectId);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS4__BRANCHES, organizationId, projectId);
		}
		return null;
	}

	/**
	 * Retrieves the branch having a given identifier, related to a given project, within a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @param branchId branch identifier
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request getBranch(RestApiHelper helper, String apiVersion, String organizationId, String projectId, String branchId) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS3__BRANCHES_BY_ID, projectId, branchId);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS4__BRANCHES_BY_ID, organizationId, projectId, branchId);
		}
		return null;
	}

	/**
	 * Creates a branch with the given data, related to a given project, within a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @param branch branch related data
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request postBranches(RestApiHelper helper, String apiVersion, String organizationId, String projectId, MMSRefDescriptor branch) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			MMSRootDescriptor root = new MMSRootDescriptor();
			root.refs.add(branch);
			String body = getMMS3PreparedGsonBuilder().create().toJson(root);
			return helper.preparePost(URL_POSTFIX__MMS3__BRANCHES, projectId).bodyString(body, ContentType.APPLICATION_JSON);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			String body = getMMS3PreparedGsonBuilder().create().toJson(new MMSRefDescriptor[] { branch });
			return helper.preparePost(URL_POSTFIX__MMS4__BRANCHES, projectId).bodyString(body, ContentType.APPLICATION_JSON);
		}
		return null;
	}

	/**
	 * Deletes a branch having a given identifier, related to a given project, within a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @param branchId branch identifier
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static DeleteRequestWithBody deleteBranches(RestApiHelper helper, String apiVersion, String organizationId, String projectId, String branchId) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareDelete(URL_POSTFIX__MMS3__BRANCHES_BY_ID, projectId, branchId);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return helper.prepareDelete(URL_POSTFIX__MMS4__BRANCHES_BY_ID, organizationId, projectId, branchId);
		}
		return null;
	}

	/**
	 * Retrieves all the commits related to a given branch, within a given project and a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @param branchId branch identifier
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request getCommits(RestApiHelper helper, String apiVersion, String organizationId, String projectId, String branchId) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS3__COMMITS, projectId, branchId);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS4__COMMITS, organizationId, projectId, branchId);
		}
		return null;
	}

	/**
	 * Retrieves all the elements related to a given branch, within a given project and a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @param branchId branch identifier
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request getElements(RestApiHelper helper, String apiVersion, String organizationId, String projectId, String branchId) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS3__ELEMENTS, projectId, branchId);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS4__ELEMENTS, organizationId, projectId, branchId);
		}
		return null;
	}

	/**
	 * Retrieves all the elements related to a given branch, within a given project and a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @param branchId branch identifier
	 * @param commitId commit identifier
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request getElements(RestApiHelper helper, String apiVersion, String organizationId, String projectId, String branchId, String commitId) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS3__ELEMENTS_BY_COMMIT_ID, projectId, branchId, commitId);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			// FIXME not implemented yet
		}
		return null;
	}

	/**
	 * Retrieves the element having a given identifier, related to a given branch (and an optional commit), within a given project and a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @param branchId branch identifier
	 * @param elementId element identifier
	 * @param commitId commit identifier (optional, can be null)
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request getElement(RestApiHelper helper, String apiVersion, String organizationId, String projectId, String branchId, String elementId) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS3__ELEMENTS_BY_ID, projectId, branchId, elementId);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return helper.prepareGet(URL_POSTFIX__MMS4__ELEMENTS_BY_ID, organizationId, projectId, branchId, elementId);
		}
		return null;
	}

	/**
	 * Creates a set of elements with the given data, related to a given branch, within a given project and a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @param branchId branch identifier
	 * @param commitComment the commit description
	 * @param modelElements elements related data
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static Request postElements(RestApiHelper helper, String apiVersion, String organizationId, String projectId, String branchId, String commitComment, List<MMSModelElementDescriptor> modelElements) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			MMSRootDescriptor root = new MMSRootDescriptor();
			root.elements = modelElements;
			root.comment = commitComment;
			String body = getMMS3PreparedGsonBuilder().create().toJson(root);
			return helper.preparePost(URL_POSTFIX__MMS3__ELEMENTS, projectId, branchId).bodyString(body, ContentType.APPLICATION_JSON);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			String body = getMMS4PreparedGsonBuilder().create().toJson(modelElements.toArray());
			return helper.preparePost(URL_POSTFIX__MMS4__ELEMENTS, organizationId, projectId, branchId).bodyString(body, ContentType.APPLICATION_JSON);
		}
		return null;
	}

	/**
	 * Deletes a set of elements having the given identifiers, related to a given branch, within a given project and a given organization
	 * @param helper REST API helper
	 * @param apiVersion MMS API version
	 * @param organizationId organization identifier
	 * @param projectId project identifier
	 * @param branchId branch identifier
	 * @param commitComment the commit description
	 * @param modelElements elements related data
	 * @return the built HTTP request, or null if the API version is not handled
	 */
	public static DeleteRequestWithBody deleteElements(RestApiHelper helper, String apiVersion, String organizationId, String projectId, String branchId, String commitComment, List<MMSModelElementDescriptor> modelElements) {
		if (MMSServerDescriptor.API_VERSION_3.equals(apiVersion)) {
			MMSRootDescriptor root = new MMSRootDescriptor();
			root.elements = modelElements;
			root.comment = commitComment;
			String body = getMMS3PreparedGsonBuilder().create().toJson(root);
			return helper.prepareDelete(URL_POSTFIX__MMS3__ELEMENTS, projectId, branchId).bodyString(body, ContentType.APPLICATION_JSON);
		} else if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			List<String> ids = modelElements.stream().map(elt -> elt.id).collect(Collectors.toList());
			String body = getMMS4PreparedGsonBuilder().create().toJson(ids.toArray());
			return helper.prepareDelete(URL_POSTFIX__MMS4__ELEMENTS, organizationId, projectId, branchId).bodyString(body, ContentType.APPLICATION_JSON);
		}
		return null;
	}
}

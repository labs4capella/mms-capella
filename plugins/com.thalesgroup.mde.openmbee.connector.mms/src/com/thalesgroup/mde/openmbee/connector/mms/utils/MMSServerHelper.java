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
package com.thalesgroup.mde.openmbee.connector.mms.utils;

import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.readBranchesFromJson;
import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.readCommitsFromJson;
import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.readElementsFromJson;
import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.readOrganizationsFromJson;
import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.readProjectsFromJson;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.thalesgroup.mde.openmbee.connector.mms.MmsConnectorPlugin;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSAPIHelper;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSCommitDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSModelElementDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSOrganizationDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.utils.RestApiHelper.DeleteRequestWithBody;

public class MMSServerHelper {
	private static final String MSG__INVALID_BASIC_AUTH_DATA = "The given string isn't a valid basic authentication data: %s"; //$NON-NLS-1$
	private static final String PREFIX__BASIC_AUTH_DATA = "Basic "; //$NON-NLS-1$
	private static final String ESCAPING_PATTERN = "[^a-zA-Z0-9\\-_]"; //$NON-NLS-1$
	private static final int MAX_RETRY_NUMBER = 5;

	public static final String MMS_REF__DEFAULT = "master"; //$NON-NLS-1$

	private String baseUrl;
	private String apiVersion;
	private RestApiHelper restHelper;

	public MMSServerHelper(String baseUrl, String apiVersion, String autData) {
		this.baseUrl = baseUrl;
		this.apiVersion = apiVersion;
		this.restHelper = new RestApiHelper(baseUrl, null, autData,
			MMSServerDescriptor.API_VERSION_4.equals(apiVersion) ? "api/" : ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setAutData(String autData) {
		restHelper.setAutData(autData);
	}
	
	public String login(String username, String password) throws MMSConnectionException {
		return login(baseUrl, apiVersion, username, password);
	}
	
	public static String login(String url, String apiVersion, String username, String password) throws MMSConnectionException {
		try {
			if(!url.endsWith("api/login")) { //$NON-NLS-1$
				if(!url.endsWith("/")) { //$NON-NLS-1$
					url = url + "/"; //$NON-NLS-1$
				}
				url = url + "api/login"; //$NON-NLS-1$
			}
			Request request = Request.Post(url)
										.bodyString(
												String.format("{\"username\": \"%s\", \"password\": \"%s\"}",  //$NON-NLS-1$
														username, password), 
												ContentType.APPLICATION_JSON);
			String loginTicketJson = tryToExecuteAndGetContentAsString(request);
			return MMSJsonHelper.readTokenFromJson(apiVersion, loginTicketJson);
		} catch (IOException e) {
			throw new MMSConnectionException("Unable to send login request to "+url, e); //$NON-NLS-1$
		}
	}

	/**
	 * Decode the given string.
	 * 
	 * @param authData
	 * @return the 1st element is the user name, the 2nd is the password
	 * @throws IllegalArgumentException if the given authData is not a valid Basic authentication string
	 */
	public static String[] decodeBasicAuthData(String authData) throws IllegalArgumentException {
		if(authData == null || !authData.startsWith(PREFIX__BASIC_AUTH_DATA)) {
			throw new IllegalArgumentException(String.format(MSG__INVALID_BASIC_AUTH_DATA, authData));
		}
		String[] userPasswordPair = 
				new String(
						Base64.getDecoder().decode(
								authData.substring(PREFIX__BASIC_AUTH_DATA.length()).getBytes())
						).split(":"); //$NON-NLS-1$
		if(userPasswordPair.length != 2) {
			throw new IllegalArgumentException(String.format(MSG__INVALID_BASIC_AUTH_DATA, authData));
		}
		return userPasswordPair;
	}

	public static String encodeBasicAuthData(String username, String password) {
		return PREFIX__BASIC_AUTH_DATA+Base64.getEncoder().encodeToString((username+":"+password).getBytes()); //$NON-NLS-1$
	}

	/*********************************************************************************************
	 ******************************* Organization handler features *******************************
	 *********************************************************************************************/
	
	/**
	 * Queries the organizations from the server.
	 * 
	 * @return the list of the organizations which are available for the user
	 */
	public List<MMSOrganizationDescriptor> getOrgs() throws MMSConnectionException {
		try {
			Request request = MMSAPIHelper.getOrganizations(restHelper, apiVersion);
			String json = tryToExecuteAndGetContentAsString(request);
			return readOrganizationsFromJson(apiVersion, json);
		} catch (IOException e) {
			throw new MMSConnectionException("Cannot query organizations from "+baseUrl, e); //$NON-NLS-1$
		}
	}

	/**
	 * Creates an organization at the server. The id also used as the name of the organization.
	 * 
	 * @param orgId the id of the future project
	 * @return the descriptor of the newly created project
	 */
	public MMSOrganizationDescriptor createOrg(String orgId) throws MMSConnectionException {
		return createOrg(orgId, orgId);
	}
	
	/**
	 * Creates an organization at the server.
	 * 
	 * @param orgId the id of the future project
	 * @param orgName the name of the future project
	 * @return the descriptor of the newly created project
	 */
	public MMSOrganizationDescriptor createOrg(String orgId, String orgName) throws MMSConnectionException {
		try {
			MMSOrganizationDescriptor org = new MMSOrganizationDescriptor();
			org.id = orgId;
			org.name = orgName;
			Request request = MMSAPIHelper.postOrganizations(restHelper, apiVersion, org);
			String response = tryToExecuteAndGetContentAsString(request);
			List<MMSOrganizationDescriptor> createdOrg = readOrganizationsFromJson(apiVersion, response);
			return createdOrg.get(0);
		} catch (IOException e) {
			throw new MMSConnectionException("Cannot create org with the id "+orgId, e); //$NON-NLS-1$
		}
	}

	/**
	 * Tries to query the organization for the specified id and if it doesn't exist
	 * creates it. If project creation is necessary the id is also used as name.
	 * 
	 * @param orgId
	 * @return the project for the specified id
	 */
	public MMSOrganizationDescriptor getOrCreateOrg(String orgId) throws MMSConnectionException {
		List<MMSOrganizationDescriptor> orgList = getOrgs();
		Optional<MMSOrganizationDescriptor> organizationWithSpecifiedId = 
				orgList.stream().filter(o -> orgId.contentEquals(o.id)).findFirst();
		return organizationWithSpecifiedId.orElseGet(() -> createOrg(orgId, orgId));
	}

	/**
	 * Removes the specified organization (and every projects from it).
	 * 
	 * @param orgId the id of the removable organization
	 * @return the response of the organization deletion call
	 */
	public boolean removeOrg(MMSOrganizationDescriptor orgId) throws MMSConnectionException {
		return removeOrg(orgId.id);
	}
	
	/**
	 * Removes the specified organization (and every projects from it).
	 * 
	 * @param orgId the id of the removable organization
	 * @return the status of the organization deletion call (200 if everything is OK)
	 */
	public boolean removeOrg(String orgId) throws MMSConnectionException {
		try {
			Request request = MMSAPIHelper.getProjects(restHelper, apiVersion, orgId);
			String containedProjectsJson = tryToExecuteAndGetContentAsString(request);
			List<MMSProjectDescriptor> projects = readProjectsFromJson(apiVersion, containedProjectsJson);
			projects.forEach(p -> { if(!removeProject(p)) MmsConnectorPlugin.getDefault().getLogger().error("Project cannot be deleted: "+p.id);}); //$NON-NLS-1$
			int statusCode = MMSAPIHelper.deleteOrganizations(restHelper, apiVersion, orgId).execute().returnResponse().getStatusLine().getStatusCode();
			return 200 == statusCode;
		} catch (IOException e) {
			throw new MMSConnectionException("Cannot remove organisation "+orgId, e); //$NON-NLS-1$
		}
	}

	/*********************************************************************************************
	 *********************************  Project handler features *********************************
	 *********************************************************************************************/

	/**
	 * Queries projects of the specified organization from the server.
	 * 
	 * @param organizationId the id of the organization
	 * @return the list of projects
	 */
	public List<MMSProjectDescriptor> getProjects(String organizationId) throws MMSConnectionException {
		try {
			Request request = MMSAPIHelper.getProjects(restHelper, apiVersion, organizationId);
			String json = tryToExecuteAndGetContentAsString(request);
			return readProjectsFromJson(apiVersion, json);
		} catch (IOException e) {
			throw new MMSConnectionException(String.format("Cannot query projects of '%s' organization from %s", organizationId, baseUrl), e); //$NON-NLS-1$
		}
	}

	public MMSProjectDescriptor createProject(String orgId, String projectId) throws MMSConnectionException {
		return createProject(orgId, projectId, projectId);
	}

	public MMSProjectDescriptor createProject(String orgId, String projectId, String projectName) throws MMSConnectionException {
		return createProject(orgId, projectId, projectName, projectName);
	}

	/**
	 * @param projectId if its null then a unique id will be generated based on the given name and organization
	 * @return the project descriptor (its id may differs from the given one because escaped characters are used for creation)
	 * @throws MMSConnectionException
	 */
	public MMSProjectDescriptor createProject(String orgId, String projectId, String projectServerName, String projectEclipseName) throws MMSConnectionException {
		try {
			MMSProjectDescriptor project = new MMSProjectDescriptor();
			project.clientSideName = projectEclipseName;
			project.featurePrefix = MMSProjectDescriptor.FEATURE_PREFIX__EMF;
			project.name = projectServerName;
			project.orgId = orgId;
			if (projectId == null || projectId.isEmpty()) {
				project.id = generateUniqueProjectId(orgId, projectServerName);
			} else {
				project.id = escape(projectId);
			}
			Request request = MMSAPIHelper.postProjects(restHelper, apiVersion, orgId, project);
			String resp = tryToExecuteAndGetContentAsString(request);
			List<MMSProjectDescriptor> respRoot = readProjectsFromJson(apiVersion, resp);
			return respRoot.get(0);
		} catch (IOException e) {
			throw new MMSConnectionException(String.format("Cannot create project in the '%s' organization with the id '%s'", orgId, projectId), e); //$NON-NLS-1$
		}
	}

	public String generateUniqueProjectId(String orgId, String projectName) {
		if (MMSServerDescriptor.API_VERSION_4.equals(apiVersion)) {
			return UUID.randomUUID().toString();
		}

		String projectIdBase = generateBaseProjectId(orgId, projectName);
		Set<String> projectIdsOnServer = getProjects(orgId).stream().map(p -> p.id.toLowerCase()).collect(Collectors.toSet());
		boolean idCollision = projectIdsOnServer.contains(projectIdBase.toLowerCase());
		long i = -1;
		while(idCollision) {
			i++;
			idCollision = projectIdsOnServer.contains(String.format("%s%d", projectIdBase.toLowerCase(), i)); //$NON-NLS-1$
		}
		if(i >= 0) projectIdBase = String.format("%s%d", projectIdBase, i); //$NON-NLS-1$
		return projectIdBase;
	}

	public String generateBaseProjectId(String orgId, String projectName) {
		return "PROJECT-"+escape(String.format("%s-%s", orgId, projectName)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static String escape(String projectId) {
		return projectId.replaceAll(ESCAPING_PATTERN, ""); //$NON-NLS-1$
	}

	public MMSProjectDescriptor getOrCreateProject(String orgId, String id) throws MMSConnectionException {
		return getProjects(orgId).stream().filter(p -> id.equalsIgnoreCase(p.id)).findFirst().orElseGet(() -> createProject(orgId, id, id));
	}

	public boolean removeProject(MMSProjectDescriptor p) {
		try {
			int statusCode = MMSAPIHelper.deleteProjects(restHelper, apiVersion, p.orgId, p.id).execute().returnResponse().getStatusLine().getStatusCode();
			return 200 == statusCode;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*********************************************************************************************
	 ********************************** Branch handler features **********************************
	 *********************************************************************************************/
	public List<MMSRefDescriptor> getBranches(String organizationId, String projectId) throws MMSConnectionException {
		try {
			Request request = MMSAPIHelper.getBranches(restHelper, apiVersion, organizationId, projectId);
			String json = tryToExecuteAndGetContentAsString(request);
			return readBranchesFromJson(apiVersion, json);
		} catch (IOException e) {
			throw new MMSConnectionException(String.format("Cannot query branches of '%s' project from %s", projectId, baseUrl), e); //$NON-NLS-1$
		}
	}

	public MMSRefDescriptor getBranch(String organizationId, String projectId, String branchId) throws MMSConnectionException {
		try {
			Request request = MMSAPIHelper.getBranch(restHelper, apiVersion, organizationId, projectId, branchId);
			String json = tryToExecuteAndGetContentAsString(request);
			return readBranchesFromJson(apiVersion, json).get(0);
		} catch (IOException e) {
			throw new MMSConnectionException(String.format("Cannot query branches of '%s' project from %s", projectId, baseUrl), e); //$NON-NLS-1$
		}
	}

	public MMSRefDescriptor createBranch(MMSProjectDescriptor owner, String branchId) throws MMSConnectionException {
		return createBranch(owner.orgId, owner.id, branchId, branchId, MMS_REF__DEFAULT);
	}

	public MMSRefDescriptor createBranch(String organizationId, String projectId, String branchId) throws MMSConnectionException {
		return createBranch(organizationId, projectId, branchId, branchId, MMS_REF__DEFAULT);
	}

	public MMSRefDescriptor createBranch(String organizationId, String projectId, String branchId, String branchName) throws MMSConnectionException {
		return createBranch(organizationId, projectId, branchId, branchName, MMS_REF__DEFAULT);
	}

	public MMSRefDescriptor createBranch(String organizationId, String projectId, String branchId, String branchName, String parentRefId) throws MMSConnectionException {
		try {
			MMSRefDescriptor branch = new MMSRefDescriptor();
			branch.id = branchId;
			branch.name = branchName;
			branch.type = MMSRefDescriptor.TYPE__BRANCH;
			branch.parentRefId = parentRefId;
			Request request = MMSAPIHelper.postBranches(restHelper, apiVersion, organizationId, projectId, branch);
			String resp = tryToExecuteAndGetContentAsString(request);
			List<MMSRefDescriptor> refs = readBranchesFromJson(apiVersion, resp);
			return (refs != null && refs.size() > 0) ? refs.get(0) : null;
		} catch (IOException e) {
			throw new MMSConnectionException(String.format("Cannot create branch '%s' for the project '%s'", branchId, projectId), e); //$NON-NLS-1$
		}
	}
	
	public MMSRefDescriptor getOrCreateBranch(String organizationId, String projectId, String branchId) {
		MMSRefDescriptor branch;
		branch = getBranches(organizationId, projectId).stream()
									.filter(r -> branchId.contentEquals(branchId))
									.findFirst().orElseGet(() -> createBranch(projectId, branchId, branchId));
		return branch;
	}
	
	public List<MMSCommitDescriptor> getCommits(String organizationId, String projectId, String branchId) throws MMSConnectionException {
		try {
			Request request = MMSAPIHelper.getCommits(restHelper, apiVersion, organizationId, projectId, branchId);
			String json = tryToExecuteAndGetContentAsString(request);
			return readCommitsFromJson(apiVersion, json);
		} catch (IOException e) {
			throw new MMSConnectionException(String.format("Cannot query commits from the '%s' barnch of '%s' project from %s", branchId, projectId, baseUrl), e); //$NON-NLS-1$
		}
	}

	public boolean removeBranch(MMSRefDescriptor ref) {
		try {
			int statusCode = MMSAPIHelper.deleteBranches(restHelper, apiVersion, null, ref._projectId, ref.id).execute().returnResponse().getStatusLine().getStatusCode();
			return 200 == statusCode;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*********************************************************************************************
	 ******************************  Model element handler features ******************************
	 *********************************************************************************************/
	public List<MMSModelElementDescriptor> getModelElement(String projectId, String branchId, String elementId) throws MMSConnectionException {
		try {
			Request request = MMSAPIHelper.getElement(restHelper, apiVersion, null, projectId, branchId, elementId);
			String json = tryToExecuteAndGetContentAsString(request);
			return readElementsFromJson(apiVersion, json);
		} catch (IOException e) {
			throw new MMSConnectionException(String.format("Cannot query branches of '%s' project from %s", projectId, baseUrl), e); //$NON-NLS-1$
		}
	}

	public List<MMSModelElementDescriptor> getModelElements(String organizationId, String projectId, String branchId) throws MMSConnectionException {
		try {
			Request request = MMSAPIHelper.getElements(restHelper, apiVersion, organizationId, projectId, branchId);
			String json = tryToExecuteAndGetContentAsString(request);
			return readElementsFromJson(apiVersion, json);
		} catch (IOException e) {
			throw new MMSConnectionException(String.format("Cannot query branches of '%s' project from %s", projectId, baseUrl), e); //$NON-NLS-1$
		}
	}

	public MMSModelElementDescriptor prepareModelElement(String projectId, String branchId, 
														String id, String nsUri, String type,
														String ownerId, Map<String, Object> modelingAttributes) {
		MMSModelElementDescriptor med = new MMSModelElementDescriptor();
		med.vcsiDescriptor = new MMSModelElementDescriptor.MMSVCSInfoDescriptior();
		med.msiDescriptor = new MMSModelElementDescriptor.MMSMSInfoDescriptor();
		med.setId(id);
		med.msiDescriptor.type = type;
		med.msiDescriptor.emfNsUri = nsUri;
		med.msiDescriptor.ownerId = ownerId;
		for (Entry<String, Object> attribute : modelingAttributes.entrySet()) {
			med.msiDescriptor.attributes.put(attribute.getKey(), attribute.getValue());
		}
		return med;
	}
	
	public List<MMSModelElementDescriptor> createModelElements(String organizationId, String projectId, String branchId, List<MMSModelElementDescriptor> meds) 
			throws MMSConnectionException {
		return createModelElements(organizationId, projectId, branchId, "", meds); //$NON-NLS-1$
	}

	public List<MMSModelElementDescriptor> createModelElements(String organizationId, String projectId, String branchId, String commitComment, List<MMSModelElementDescriptor> modelElements) throws MMSConnectionException {
		try {
			Request request = MMSAPIHelper.postElements(restHelper, apiVersion, organizationId, projectId, branchId, commitComment, modelElements);
			String json = tryToExecuteAndGetContentAsString(request);
			return readElementsFromJson(apiVersion, json);
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Element creation failed at the branch '%s' of project '%s' on the server '%s'", branchId, projectId, baseUrl), e); //$NON-NLS-1$
		}
	}
	
	public boolean removeModelElements(String organizationId, String projectId, String branchId, String commitComment, List<MMSModelElementDescriptor> modelElements) throws MMSConnectionException {
		try {
			DeleteRequestWithBody request = MMSAPIHelper.deleteElements(restHelper, apiVersion, organizationId, projectId, branchId, commitComment, modelElements);
			String json = tryToExecuteAndGetContentAsString(request);
			List<MMSModelElementDescriptor> returnedElements = readElementsFromJson(apiVersion, json);
			return returnedElements.size() == modelElements.size();
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Element creation failed at the branch '%s' of project '%s' on the server '%s'", branchId, projectId, baseUrl), e); //$NON-NLS-1$
		}
	}

	private static String tryToExecuteAndGetContentAsString(Request req) throws ClientProtocolException, IOException {
		String response = null;
		int retryNumber = 0;
		do {
			try {
			response = req.execute().returnContent().asString();
			} catch (HttpResponseException respEx) {
				// Try it again if it's just an internal server error
				// Sometimes MMS works better for the 2nd try :)
				if(HttpStatus.SC_INTERNAL_SERVER_ERROR == respEx.getStatusCode() && retryNumber <= MAX_RETRY_NUMBER) {
					MmsConnectorPlugin.getDefault().getLogger().warn(
							String.format("Request: %s%sRe-try time: %d", //$NON-NLS-1$
											req.toString(), 
											System.lineSeparator(), 
											retryNumber));
					retryNumber++;
				} else {
					throw respEx;
				}
			}
		} while (response == null);
		return response;
	}
	
	private static String tryToExecuteAndGetContentAsString(DeleteRequestWithBody req) throws ClientProtocolException, IOException {
		String response = null;
		int retryNumber = 0;
		do {
			try {
				response = req.execute().returnContent().asString();
			} catch (HttpResponseException respEx) {
				// Try it again if it's just an internal server error
				// Sometimes MMS works better for the 2nd try :)
				if(HttpStatus.SC_INTERNAL_SERVER_ERROR == respEx.getStatusCode() && retryNumber <= MAX_RETRY_NUMBER) {
					MmsConnectorPlugin.getDefault().getLogger().warn(
							String.format("Request: %s%sRe-try time: %d", //$NON-NLS-1$
											req.toString(), 
											System.lineSeparator(), 
											retryNumber));
					retryNumber++;
				} else {
					throw respEx;
				}
			}
		} while (response == null);
		return response;
	}
	
	public static class MMSConnectionException extends RuntimeException {
		private static final long serialVersionUID = -3481265917495763803L;

		public MMSConnectionException(String message) {
			super(message);
		}
		
		public MMSConnectionException(Throwable e) {
			super(e);
		}
		
		public MMSConnectionException(String message, Throwable e) {
			super(message, e);
		}
	}
}

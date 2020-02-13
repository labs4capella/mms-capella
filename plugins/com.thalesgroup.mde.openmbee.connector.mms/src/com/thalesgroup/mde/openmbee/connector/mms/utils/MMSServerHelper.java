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

import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.getPreparedGsonBuilder;
import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.readBranchesFromJson;
import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.readCommitsFromJson;
import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.readOrganizationsFromJson;
import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.readProjectsFromJson;
import static com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper.readRootFromJson;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.thalesgroup.mde.openmbee.connector.mms.MmsConnectorPlugin;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSCommitDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSModelElementDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSOrganizationDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRootDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.utils.RestApiHelper.DeleteRequestWithBody;

public class MMSServerHelper {
	private static final String MSG__INVALID_BASIC_AUTH_DATA = "The given string isn't a valid basic authentication data: %s"; //$NON-NLS-1$
	private static final String PREFIX__BASIC_AUTH_DATA = "Basic "; //$NON-NLS-1$

	private static final String ESCAPING_PATTERN = "[^a-zA-Z0-9\\-_]"; //$NON-NLS-1$
	
	public static final String URL_POSTFIX__PROJECTS = "projects"; //$NON-NLS-1$
	public static final String URL_POSTFIX__PROJECTS_OF_ORGANIZATION = "orgs/%s/projects"; //$NON-NLS-1$
	public static final String URL_POSTFIX__ORGANIZATIONS = "orgs"; //$NON-NLS-1$
	public static final String MMS_REF__DEFAULT = "master"; //$NON-NLS-1$

	private static final int MAX_RETRY_NUMBER = 5;
	
	private String baseUrl;
	private RestApiHelper restHelper;

	public MMSServerHelper(String baseUrl, String autData) {
		this.baseUrl = baseUrl;
		restHelper = new RestApiHelper(baseUrl, null, autData, isMMS4API());
	}

	public boolean isMMS4API() {
		// MMS4 services API
		// FIXME an explicit information shall replace this hazardous URL interpretation
		return this.baseUrl.contains("alfresco") ? false : true; //$NON-NLS-1$
	}

	public void setAutData(String autData) {
		restHelper.setAutData(autData);
	}
	
	public String login(String username, String password) throws MMSConnectionException {
		return login(baseUrl, username, password);
	}
	
	public static String login(String url, String username, String password) throws MMSConnectionException {
		String loginTicketJson;
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
			loginTicketJson = tryToExecuteAndGetContentAsString(request);
		} catch (IOException e) {
			throw new MMSConnectionException("Unable to send login request to "+url, e); //$NON-NLS-1$
		}
		MMSRootDescriptor root = readRootFromJson(loginTicketJson);
		if (root.data != null && root.data.ticket != null && root.data.ticket.length()>0) {
			return root.data.ticket;
		} else if (root.token != null && root.token.length()>0) {
			return root.token;
		} else {
			throw new MMSConnectionException("Invalid login response:\r\n"+loginTicketJson); //$NON-NLS-1$
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
			Request request = restHelper.prepareGet(URL_POSTFIX__ORGANIZATIONS);
			String orgsJson = tryToExecuteAndGetContentAsString(request);
			return readOrganizationsFromJson(orgsJson);
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
		MMSOrganizationDescriptor org = new MMSOrganizationDescriptor();
		org.id = orgId;
		org.name = orgName;
		MMSRootDescriptor root = new MMSRootDescriptor();
		root.orgs.add(org);
		
		String json = getPreparedGsonBuilder().create().toJson(root);
		try {
			Request request = restHelper.preparePost(URL_POSTFIX__ORGANIZATIONS)
										.bodyString(json, ContentType.APPLICATION_JSON);
			String response = tryToExecuteAndGetContentAsString(request);
			List<MMSOrganizationDescriptor> createdOrg = readOrganizationsFromJson(response);
			return createdOrg.get(0);
		} catch (IOException e) {
			throw new MMSConnectionException("Cannot create org with the id "+orgId, e); //$NON-NLS-1$
		}
	}

	/**
	 * Tries to wuery the organization for the specified id and if it doesn't exist
	 * creates it. If project creation is necessary the id is also used as name.
	 * 
	 * @param orgId
	 * @return the project for the specified id
	 */
	public MMSOrganizationDescriptor getOrCreateOrg(String orgId) throws MMSConnectionException {
		MMSOrganizationDescriptor org;
		List<MMSOrganizationDescriptor> orgList = getOrgs();
		Optional<MMSOrganizationDescriptor> organizationWithSpecifiedId = 
				orgList.stream().filter(o -> orgId.contentEquals(o.id)).findFirst();
		org = organizationWithSpecifiedId.orElseGet(() -> createOrg(orgId, orgId));
		return org;
	}

	/**
	 * Removes the specified organisation (and every projects from it).
	 * 
	 * @param orgId the id of the removable organisation
	 * @return the response of the organisation deletion call
	 */
	public boolean removeOrg(MMSOrganizationDescriptor orgId) throws MMSConnectionException {
		return removeOrg(orgId.id);
	}
	
	/**
	 * Removes the specified organisation (and every projects from it).
	 * 
	 * @param orgId the id of the removable organisation
	 * @return the status of the organisation deletion call (200 if everything is OK)
	 */
	public boolean removeOrg(String orgId) throws MMSConnectionException {
		try {
			Request request = restHelper.prepareGet(URL_POSTFIX__PROJECTS_OF_ORGANIZATION, orgId);
			String containedProjectsJson = tryToExecuteAndGetContentAsString(request);
			MMSRootDescriptor root = readRootFromJson(containedProjectsJson);
			root.projects.forEach(p -> { if(!removeProject(p)) MmsConnectorPlugin.getDefault().getLogger().error("Project cannot be deleted: "+p.id);}); //$NON-NLS-1$
			int statusCode = restHelper.prepareDelete("orgs/%s", orgId).execute().returnResponse().getStatusLine().getStatusCode(); //$NON-NLS-1$
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
	 * @param orgId the id of the organization
	 * @return the list of projects
	 */
	public List<MMSProjectDescriptor> getProjects(String orgId) throws MMSConnectionException {
		try {
			Request request = restHelper.prepareGet(URL_POSTFIX__PROJECTS_OF_ORGANIZATION, orgId);
			String projectsJson = tryToExecuteAndGetContentAsString(request);
			return readProjectsFromJson(projectsJson);
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Cannot query projects of '%s' organization from %s", orgId, baseUrl), e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Queries projects of the the server.
	 * 
	 * @return the list of projects
	 */
	public List<MMSProjectDescriptor> getProjects() throws MMSConnectionException {
		try {
			Request request = restHelper.prepareGet(URL_POSTFIX__PROJECTS);
			String projectsJson = tryToExecuteAndGetContentAsString(request);
			return readProjectsFromJson(projectsJson);
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Cannot query projects from %s", baseUrl), e); //$NON-NLS-1$
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
		MMSProjectDescriptor project = new MMSProjectDescriptor();
		project.name = projectServerName;
		project.featurePrefix = MMSProjectDescriptor.FEATURE_PREFIX__EMF;
		if(projectId == null || projectId.isEmpty()) {
			project.id = generateUniqueProjectId(orgId, projectServerName);
		} else {
			project.id = escape(projectId);
		}
		project.clientSideName = projectEclipseName;
		project.orgId = orgId;
		MMSRootDescriptor root = new MMSRootDescriptor();
		root.projects.add(project);
		
		String json = getPreparedGsonBuilder().create().toJson(root);
		try {
			Request request = restHelper.preparePost(URL_POSTFIX__PROJECTS_OF_ORGANIZATION, orgId)
										.bodyString(json, ContentType.APPLICATION_JSON);
			String resp = tryToExecuteAndGetContentAsString(request);
			MMSRootDescriptor respRoot = readRootFromJson(resp);
			return respRoot.projects.get(0);
		} catch (IOException e) {
			throw new MMSConnectionException(String.format("Cannot create project in the '%s' organization with the id '%s'", orgId, projectId), e); //$NON-NLS-1$
		}
	}

	public String generateUniqueProjectId(String orgId, String projectName) {
		String projectIdBase = generateBaseProjectId(orgId, projectName);
		Set<String> projectIdsOnServer = getProjects().stream().map(p -> p.id.toLowerCase()).collect(Collectors.toSet());
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
		MMSProjectDescriptor project;
		project = getProjects(orgId).stream()
									.filter(p -> id.equalsIgnoreCase(p.id))
									.findFirst().orElseGet(() -> createProject(orgId, id, id));
		return project;
	}

	public boolean removeProject(MMSProjectDescriptor p) {
		try {
			int statusCode = restHelper.prepareDelete("projects/%s", p.id).execute().returnResponse().getStatusLine().getStatusCode(); //$NON-NLS-1$
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
			Request request;
			if (isMMS4API()) {
				request = restHelper.prepareGet("orgs/%s/projects/%s/branches", organizationId, projectId); //$NON-NLS-1$
			} else {
				request = restHelper.prepareGet("projects/%s/refs", projectId); //$NON-NLS-1$
			}
			String projectsJson = tryToExecuteAndGetContentAsString(request);
			return readBranchesFromJson(projectsJson);
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Cannot query branches of '%s' project from %s", projectId, baseUrl), e); //$NON-NLS-1$
		}
	}
	
	public MMSRefDescriptor getBranch(String organizationId, String projectId, String branchId) throws MMSConnectionException {
		try {
			Request request;
			if (isMMS4API()) {
				request = restHelper.prepareGet("orgs/%s/projects/%s/branches/%s", organizationId, projectId, branchId); //$NON-NLS-1$
			} else {
				request = restHelper.prepareGet("projects/%s/refs/%s", projectId, branchId); //$NON-NLS-1$
			}
			String projectsJson = tryToExecuteAndGetContentAsString(request);
			return readBranchesFromJson(projectsJson).get(0);
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Cannot query branches of '%s' project from %s", projectId, baseUrl), e); //$NON-NLS-1$
		}
	}
	
	public MMSRefDescriptor createBranch(MMSProjectDescriptor owner, String branchId) throws MMSConnectionException {
		return createBranch(owner.id, branchId, branchId, MMS_REF__DEFAULT);
	}
	
	public MMSRefDescriptor createBranch(String projectId, String branchId) throws MMSConnectionException {
		return createBranch(projectId, branchId, branchId, MMS_REF__DEFAULT);
	}
	
	public MMSRefDescriptor createBranch(String projectId, String branchId, String branchName) throws MMSConnectionException {
		return createBranch(projectId, branchId, branchName, MMS_REF__DEFAULT);
	}
	
	public MMSRefDescriptor createBranch(String projectId, String branchId, String branchName, String parentRefId) throws MMSConnectionException {
		MMSRefDescriptor branch = new MMSRefDescriptor();
		branch.id = branchId;
		branch.name = branchName;
		branch.type = MMSRefDescriptor.TYPE__BRANCH;
		branch.parentRefId = parentRefId;
		MMSRootDescriptor root = new MMSRootDescriptor();
		root.refs.add(branch);
		
		String json = getPreparedGsonBuilder().create().toJson(root);
		try {
			Request request = restHelper.preparePost("projects/%s/refs", projectId) //$NON-NLS-1$
										.bodyString(json, ContentType.APPLICATION_JSON);
			String resp = tryToExecuteAndGetContentAsString(request);
			MMSRootDescriptor respRoot = readRootFromJson(resp);
			return (respRoot.refs != null && respRoot.refs.size() > 0) ? respRoot.refs.get(0) : null;
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Cannot create branch '%s' for the project '%s'", branchId, projectId), e); //$NON-NLS-1$
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
			//Request request = restHelper.prepareGet("projects/%s/refs/%s/commits", projectId, branchId); //$NON-NLS-1$
			Request request;
			if (isMMS4API()) {
				request = restHelper.prepareGet("orgs/%s/projects/%s/branches/%s/commits", organizationId, projectId, branchId); //$NON-NLS-1$
			} else {
				request = restHelper.prepareGet("projects/%s/refs/%s/commits", projectId, branchId); //$NON-NLS-1$
			}
			String commitsJson = tryToExecuteAndGetContentAsString(request);
			return readCommitsFromJson(commitsJson);
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Cannot query commits from the '%s' barnch of '%s' project from %s", branchId, projectId, baseUrl), e); //$NON-NLS-1$
		}
	}

	public boolean removeBranch(MMSRefDescriptor p) {
		try {
			int statusCode = restHelper.prepareDelete("projects/%s/%s", p._projectId, p.id).execute().returnResponse().getStatusLine().getStatusCode(); //$NON-NLS-1$
			return 200 == statusCode;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	
	/*********************************************************************************************
	 ******************************  Model element handler features ******************************
	 *********************************************************************************************/

	public List<MMSModelElementDescriptor> getModelElement(String projectId, String branchId, String elementId, String commitId) throws MMSConnectionException {
		try {
			Request preparedGet;
			if(commitId == null) {
				preparedGet = restHelper.prepareGet("projects/%s/refs/%s/elements/%s", projectId, branchId, elementId); //$NON-NLS-1$
			} else {
				preparedGet = restHelper.prepareGet("projects/%s/refs/%s/elements/%s?commitId=%s", projectId, branchId, elementId, commitId); //$NON-NLS-1$
			}
			String projectsJson = tryToExecuteAndGetContentAsString(preparedGet);
			return readRootFromJson(projectsJson).elements;
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Cannot query branches of '%s' project from %s", projectId, baseUrl), e); //$NON-NLS-1$
		}
	}
	
	public List<MMSModelElementDescriptor> getModelElements(String projectId, String branchId) throws MMSConnectionException {
		try {
			Request request = restHelper.prepareGet("projects/%s/refs/%s/elements", projectId, branchId); //$NON-NLS-1$
			String projectsJson = tryToExecuteAndGetContentAsString(request);
			return readRootFromJson(projectsJson).elements;
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Cannot query branches of '%s' project from %s", projectId, baseUrl), e); //$NON-NLS-1$
		}
	}
	
	public List<MMSModelElementDescriptor> getModelElements(String projectId, String branchId, String commitId) throws MMSConnectionException {
		if(commitId==null) {
			return getModelElements(projectId, branchId);
		}
		try {
			Request request = restHelper.prepareGet("projects/%s/refs/%s/elements?commitId=%s", projectId, branchId, commitId); //$NON-NLS-1$
			String projectsJson = tryToExecuteAndGetContentAsString(request);
			return readRootFromJson(projectsJson).elements;
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Cannot query branches of '%s' project from %s", projectId, baseUrl), e); //$NON-NLS-1$
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
	
	public List<MMSModelElementDescriptor> createModelElements(String projectId, String branchId, List<MMSModelElementDescriptor> meds) 
			throws MMSConnectionException {
		return createModelElements(projectId, branchId, "", meds); //$NON-NLS-1$
	}
	
	public List<MMSModelElementDescriptor> createModelElements(String projectId, String branchId, String commitComment, List<MMSModelElementDescriptor> meds) 
																throws MMSConnectionException {
		MMSRootDescriptor root = new MMSRootDescriptor();
		root.elements = meds;
		root.comment = commitComment;
		
		String json = getPreparedGsonBuilder().create().toJson(root);
		try {
			Request request = restHelper.preparePost("projects/%s/refs/%s/elements", projectId, branchId) //$NON-NLS-1$
										.bodyString(json, ContentType.APPLICATION_JSON);
			String resp = tryToExecuteAndGetContentAsString(request);
			MMSRootDescriptor returnedRoot = readRootFromJson(resp);
			return returnedRoot.elements;
		} catch (IOException e) {
			throw new MMSConnectionException(
					String.format("Element creation failed at the branch '%s' of project '%s' on the server '%s'", branchId, projectId, baseUrl), e); //$NON-NLS-1$
		}
	}
	
	public boolean removeModelElements(String projectId, String branchId, String commitComment, List<MMSModelElementDescriptor> meds) 
			throws MMSConnectionException {
		MMSRootDescriptor root = new MMSRootDescriptor();
		root.elements = meds;
		root.comment = commitComment;
		
		String json = getPreparedGsonBuilder().create().toJson(root);
		try {
			DeleteRequestWithBody request = 
					restHelper.prepareDelete("projects/%s/refs/%s/elements", projectId, branchId) //$NON-NLS-1$
									.bodyString(json, ContentType.APPLICATION_JSON);
			String resp = tryToExecuteAndGetContentAsString(request);
			MMSRootDescriptor returnedRoot = readRootFromJson(resp);
			return returnedRoot.elements.size() == meds.size();
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

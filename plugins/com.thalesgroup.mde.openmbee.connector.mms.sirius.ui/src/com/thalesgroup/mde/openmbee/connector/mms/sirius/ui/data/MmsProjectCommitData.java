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
package com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.data;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;

public class MmsProjectCommitData extends MmsConnectionData {
	public final String commitComment;
	public final String projectServerName;
	public final String projectId;
	public final String projectFeaturePrefix;
	public final String refId;
	public final boolean newProject;
	
	public MmsProjectCommitData(MmsConnectionData connectionData, String commitComment, String projectServerName, String projectId, boolean newProject) {
		this(connectionData.serverUrl, connectionData.autData, connectionData.orgId, commitComment, newProject, projectServerName, projectId, null, null);
	}
	
	public MmsProjectCommitData(String serverUrl, String autData, String orgId, String commitComment, boolean newProject, String projectServerName, String projectId, String projectFeaturePrefix, String refId) {
		super(serverUrl, autData, orgId);
		this.commitComment = commitComment;
		this.projectServerName = projectServerName;
		this.projectId = projectId;
		this.newProject = newProject;
		this.projectFeaturePrefix = projectFeaturePrefix;
		this.refId = refId;
	}
	
	public MmsProjectCommitData(MmsConnectionData connectionData, String commitComment, boolean newProject, MMSProjectDescriptor project, MMSRefDescriptor ref) {
		this(connectionData.serverUrl, 
				connectionData.autData, 
				connectionData.orgId, 
				commitComment, 
				newProject, 
				project == null ? null : project.name, 
				project == null ? null : project.id, 
				project == null ? null : project.featurePrefix, 
				ref == null ? null : ref.id);
	}
	
}
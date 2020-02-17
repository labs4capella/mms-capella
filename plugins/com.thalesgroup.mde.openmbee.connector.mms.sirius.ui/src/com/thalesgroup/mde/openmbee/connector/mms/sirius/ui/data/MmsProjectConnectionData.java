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

public class MmsProjectConnectionData extends MmsConnectionData {
	public final String projectId;
	public final String refId;
	
	public MmsProjectConnectionData(MmsConnectionData connectionData, String projectId, String refId) {
		this(connectionData.serverUrl, connectionData.apiVersion, connectionData.autData, connectionData.orgId, projectId, refId);
	}
	
	public MmsProjectConnectionData(String serverUrl, String apiVersion, String autData, String orgId, String projectId, String refId) {
		super(serverUrl, apiVersion, autData, orgId);
		this.projectId = projectId;
		this.refId = refId;
	}
	
}
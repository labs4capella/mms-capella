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

public class MmsProjectImportData extends MmsProjectConnectionData {
	public final String commitId;

	public MmsProjectImportData(MmsConnectionData connectionData, String projectId, String refId, String commitId) {
		super(connectionData.serverUrl, connectionData.apiVersion, connectionData.autData, connectionData.orgId, projectId, refId);
		this.commitId = commitId;
	}

}

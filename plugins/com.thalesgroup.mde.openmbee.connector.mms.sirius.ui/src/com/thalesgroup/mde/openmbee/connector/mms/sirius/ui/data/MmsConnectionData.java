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

public class MmsConnectionData {
	public final String serverUrl;
	public final String autData;
	public final String orgId;
	
	public MmsConnectionData(String serverUrl, String autData, String orgId) {
		this.serverUrl = serverUrl;
		this.autData = autData;
		this.orgId = orgId;
	}
}
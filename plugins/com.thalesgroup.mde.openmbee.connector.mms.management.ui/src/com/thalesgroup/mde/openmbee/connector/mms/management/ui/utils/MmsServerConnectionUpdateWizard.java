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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui.utils;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.MmsServerManagementUiPlugin;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeParent;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper;

public class MmsServerConnectionUpdateWizard extends MmsServerConnectionWizard {
	private final MMSServerDescriptor server;
	private boolean connectionDataModified = false;

	public MmsServerConnectionUpdateWizard(MMSTreeParent viewerDataRoot, MMSServerDescriptor server) {
		super(viewerDataRoot);
		this.server = server;
	}
	
	@Override
	public void addPages() {
		super.addPages();
		try {
			String[] userPasswordPair = MMSServerHelper.decodeBasicAuthData(server.autData);
			connectionPage.setDefaultUser(userPasswordPair[0]);
			connectionPage.setDefaultPassword(userPasswordPair[1]);
		} catch (IllegalArgumentException e) {}
		connectionPage.setDefaultName(server.name);
		connectionPage.setDefaultUrl(server.url);
		connectionPage.setDefaultAPIVersion(server.apiVersion);
	}
	
	@Override
	public boolean performFinish() {
		MMSServerDescriptor connectionData = connectionPage.getConnectionData();
		boolean success = connectionData != null;
		if(success) {
			connectionDataModified = !server.url.contentEquals(connectionData.url) ||
									server.apiVersion == null ||
									!server.apiVersion.contentEquals(connectionData.apiVersion) ||
									!server.autData.contentEquals(connectionData.autData);
			success =  connectionDataModified || !server.name.contentEquals(connectionData.name);
			if(success) {
				MmsServerManagementUiPlugin plugin = MmsServerManagementUiPlugin.getInstance();
				plugin.addStoredMmsUrl(connectionPage.getUrl());
				plugin.addStoredMmsUser(connectionPage.getUser());
				
				// replace the old data with the new
				plugin.removeStoredMMSServerDescriptor(server);
				server.id = connectionData.id;
				server.url = connectionData.url;
				server.apiVersion = connectionData.apiVersion;
				server.autData = connectionData.autData;
				server.name = connectionData.name;
				plugin.addStoredMMSServerDescriptor(server);
			} else {
				connectionPage.setErrorMessage("The given data are the same as the original."); //$NON-NLS-1$
			}
		}
		return success;
	}
	
	public boolean isConnectionDataModified() {
		return connectionDataModified;
	}

}

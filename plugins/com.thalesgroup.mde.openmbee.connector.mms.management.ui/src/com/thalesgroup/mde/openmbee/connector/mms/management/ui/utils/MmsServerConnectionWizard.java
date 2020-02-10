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

import org.eclipse.jface.wizard.Wizard;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.MmsServerManagementUiPlugin;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeParent;

public class MmsServerConnectionWizard extends Wizard {
	protected MmsServerConnectionPage connectionPage;
	protected final MMSTreeParent viewerDataRoot;

	public MmsServerConnectionWizard(MMSTreeParent viewerDataRoot) {
		this.viewerDataRoot = viewerDataRoot;
	}

	@Override
	public void addPages() {
		connectionPage = new MmsServerConnectionPage();
		addPage(connectionPage);
	}

	@Override
	public boolean performFinish() {
		MMSServerDescriptor connectionData = connectionPage.getConnectionData();
		boolean success = connectionData != null;
		if(success) {
			success = success && viewerDataRoot.addChild(connectionData);
			if(success) {
				MmsServerManagementUiPlugin plugin = MmsServerManagementUiPlugin.getInstance();
				plugin.addStoredMmsUrl(connectionPage.getUrl());
				plugin.addStoredMmsUser(connectionPage.getUser());
				plugin.addStoredMMSServerDescriptor(connectionData);
			} else {
				connectionPage.setErrorMessage("The connection to the server already exists for this user."); //$NON-NLS-1$
			}
		}
		return success;
	}
}
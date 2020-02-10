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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui.views.actions;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.wizard.WizardDialog;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSIdentifiableDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.MmsServerManagementUiPlugin;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeParent;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.utils.MmsServerConnectionUpdateWizard;

public class SelectionBasedMmsUpdateAction extends SelectionBasedAction {

	public SelectionBasedMmsUpdateAction(StructuredViewer viewer) {
		super(viewer);
		this.setText("Edit"); //$NON-NLS-1$
		this.setImageDescriptor(MmsServerManagementUiPlugin.getInstance().getImageRegistry().getDescriptor(MmsServerManagementUiPlugin.IMG__EDIT));
	}
	
	@Override
	public void run() {
		Object o = getSelectedElements().get(0);
		if(o instanceof MMSTreeParent) {
			MMSTreeParent mmso = (MMSTreeParent)o;
			MMSIdentifiableDescriptor mms = mmso.getMmsObject();
			if(mms instanceof MMSServerDescriptor) {
				MMSServerDescriptor server = (MMSServerDescriptor)mms;
				MmsServerConnectionUpdateWizard wizard = new MmsServerConnectionUpdateWizard(mmso.getParent(), server);
				WizardDialog wd = new WizardDialog(viewer.getControl().getShell(), wizard);
				wd.setTitle(wizard.getWindowTitle());
				if(WizardDialog.OK == wd.open()) {
					// if the connection data has been modified subtree re-load need to be forced
					if(wizard.isConnectionDataModified()) {
						mmso.setChildren(null);
					}
					viewer.refresh(mmso);
				}
			}
		}
	}

	@Override
	public void updateEnablement() {
		setEnabled(viewer.getStructuredSelection().size() == 1 &&
				getSelectedElements().stream()
					.allMatch(s -> s instanceof MMSTreeParent && 
							((MMSTreeParent)s).getMmsObject() instanceof MMSServerDescriptor));
		
	}

}

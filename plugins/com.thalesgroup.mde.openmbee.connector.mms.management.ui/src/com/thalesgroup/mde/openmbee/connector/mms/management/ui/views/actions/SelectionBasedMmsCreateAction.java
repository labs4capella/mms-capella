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

import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSIdentifiableDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSOrganizationDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeObject;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeParent;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.utils.MmsChildCreationWizard;

public class SelectionBasedMmsCreateAction extends SelectionBasedAction {

	public SelectionBasedMmsCreateAction(StructuredViewer viewer) {
		super(viewer);
		this.setText("Create"); //$NON-NLS-1$
		this.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
	}
	
	@Override
	public void run() {
		List<?> selectedElements = getSelectedElements();
		if(selectedElements.size() == 1) {
			Object o = selectedElements.get(0);
			if(o instanceof MMSTreeParent) {
				MMSTreeParent mmso = (MMSTreeParent) o;
				MmsChildCreationWizard wizard;
				if(mmso.getMmsObject() instanceof MMSRefDescriptor) {
					wizard = new MmsChildCreationWizard(mmso.getParent(), (MMSRefDescriptor) mmso.getMmsObject());
					mmso = mmso.getParent();
				} else {
					wizard = new MmsChildCreationWizard(mmso);
				}
				WizardDialog wd = new WizardDialog(viewer.getControl().getShell(), wizard);
				wd.setTitle(wizard.getWindowTitle());
				if(WizardDialog.OK == wd.open()) {
					viewer.refresh(mmso);
				}
			}
		}
	}

	@Override
	public void updateEnablement() {
		List<?> selectedElements = getSelectedElements();
		if(selectedElements.size() == 1) {
			Object o = selectedElements.get(0);
			if(o instanceof MMSTreeObject) {
				MMSIdentifiableDescriptor mmsd = ((MMSTreeObject)o).getMmsObject();
				if(mmsd instanceof MMSServerDescriptor) {
					setText("Create organization"); //$NON-NLS-1$
					setEnabled(true);
					return;
				}
				if(mmsd instanceof MMSOrganizationDescriptor) {
					setText("Create project"); //$NON-NLS-1$
					setEnabled(true);
					return;
				}
				if(mmsd instanceof MMSProjectDescriptor) {
					setText("Create ref"); //$NON-NLS-1$
					setEnabled(true);
					return;
				}
				if(mmsd instanceof MMSRefDescriptor) {
					setText("Create ref based on this one"); //$NON-NLS-1$
					setEnabled(true);
					return;
				}
			}
		}
		setText("Create"); //$NON-NLS-1$
		setEnabled(false);
	}

}

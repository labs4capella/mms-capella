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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.PlatformUI;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSCommitDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSIdentifiableDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSOrganizationDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeObject;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeParent;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.ExecutionResult;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.ImportRunnable;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.data.MmsConnectionData;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.data.MmsProjectImportData;

public class SelectionBasedMmsCheckoutAction extends SelectionBasedAction {

	public SelectionBasedMmsCheckoutAction(StructuredViewer viewer) {
		super(viewer);
		this.setText("Checkout"); //$NON-NLS-1$
		// Use the import action image: org.eclipse.ui.internal.IWorkbenchGraphicConstants.IMG_ETOOL_IMPORT_WIZ
		this.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_ETOOL_IMPORT_WIZ")); //$NON-NLS-1$
	}
	
	@Override
	public void run() {
		// Start the import wizard with prepared data
		Object o = getSelectedElements().get(0);
		if(o instanceof MMSTreeObject) {
			MMSTreeObject mmso = (MMSTreeObject)o;
			MMSIdentifiableDescriptor mms = mmso.getMmsObject();
			if(mms instanceof MMSRefDescriptor || mms instanceof MMSCommitDescriptor) {
				MMSCommitDescriptor commit;
				MMSTreeParent treeRef;
				if(mms instanceof MMSRefDescriptor) {
					commit = null;
					treeRef = (MMSTreeParent) mmso;
				} else {
					commit = (MMSCommitDescriptor) mms;
					treeRef = mmso.getParent();
				}
				MMSServerDescriptor server = 	(MMSServerDescriptor)		treeRef.getParent().getParent().getParent().getMmsObject();
				MMSOrganizationDescriptor org =	(MMSOrganizationDescriptor)	treeRef.getParent().getParent().getMmsObject();
				MMSProjectDescriptor project =	(MMSProjectDescriptor)		treeRef.getParent().getMmsObject();
				MMSRefDescriptor ref =			(MMSRefDescriptor)			treeRef.getMmsObject();
				
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(viewer.getControl().getShell());
				try {
					dialog.run(true, true, new ImportRunnable(
							new AtomicReference<ExecutionResult>(), 
							new MmsProjectImportData(
									new MmsConnectionData(server.url, server.autData, org.id), 
									project.id, ref.id, 
									commit == null ? null : commit.id), 
							project.clientSideName, m -> {
								dialog.getShell().getDisplay().asyncExec(new Runnable() {
									
									@Override
									public void run() {
										MessageDialog.openError(dialog.getShell(), "Import Error", m); //$NON-NLS-1$
									}
								});
							}));
				} catch (InvocationTargetException | InterruptedException e) {}
			}
		}
	}

	@Override
	public void updateEnablement() {
		setEnabled(viewer.getStructuredSelection().size() == 1 &&
				getSelectedElements().stream()
					.allMatch(s -> s instanceof MMSTreeObject && 
									(
										((MMSTreeObject)s).getMmsObject() instanceof MMSRefDescriptor || 
										((MMSTreeObject)s).getMmsObject() instanceof MMSCommitDescriptor
									)
								)
					);
	}

}

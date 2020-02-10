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
package com.thalesgroup.mde.openmbee.connector.mms.sirius.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IWorkbench;

import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.data.MmsProjectImportData;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.pages.MmsProjectVersionSelectionWizardPage;

public class CapellaMmsImportWizard extends ResultPageOwnerWizard {

	private MmsProjectVersionSelectionWizardPage projectVersionSelection;

	public CapellaMmsImportWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		projectVersionSelection = new MmsProjectVersionSelectionWizardPage();
		addPage(projectVersionSelection);
	}

	@Override
	public String getWindowTitle() {
		return "Import Capella Model from MMS"; //$NON-NLS-1$
	}
	
	protected WizardPage getPageBeforeResult() {
		return projectVersionSelection;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {}
	
	protected ExecutionResult executeActionAndUpdateUI() {
		AtomicReference<ExecutionResult> success = new AtomicReference<>();
		MmsProjectImportData connectionData = null;
		String projectName = null;
		
		try {
			connectionData =projectVersionSelection.getConnectionData();
			projectName = projectVersionSelection.getProjectName();
		} catch(Exception e) {}
		
		try {
			getContainer().run(true, true, new ImportRunnable(success, connectionData, projectName, m -> {
				getShell().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						projectVersionSelection.setErrorMessage(m);
					}
				});
			}));
		} catch (InterruptedException e) {
			getShell().getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					projectVersionSelection.setErrorMessage(e.getMessage());
				}
			});
		} catch (InvocationTargetException e) {
			MmsSiriusConnectorUiPlugin.getDefault().getLogger().error(e);
		}
		if(success.get() != null && success.get().result != ExecutionResult.Result.ERROR) {
			MmsSiriusConnectorUiPlugin.getDefault().addStoredMmsUrl(connectionData.serverUrl);
			MmsSiriusConnectorUiPlugin.getDefault().addStoredMmsUser(projectVersionSelection.getUser());
			MmsSiriusConnectorUiPlugin.getDefault().savePreferences();
		}
		
		return success.get();
	}
}

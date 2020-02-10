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

import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IExportWizard;

public abstract class ResultPageOwnerWizard extends Wizard implements IExportWizard {
	
	protected abstract WizardPage getPageBeforeResult();

	@Override
	public boolean performFinish() {
		ExecutionResult executionResult = executeActionAndUpdateUI();
		return executionResult != null && executionResult.result != ExecutionResult.Result.ERROR;
	}

	protected abstract ExecutionResult executeActionAndUpdateUI();
	
	protected void initializePageChangingListener(org.eclipse.jface.wizard.IWizardContainer newWizardContainer, IPageChangingListener pcListener) {
		IWizardContainer oldWizardContainer = getContainer();
		
		if(oldWizardContainer instanceof WizardDialog) {
			WizardDialog changeProvider = (WizardDialog) oldWizardContainer;
			changeProvider.removePageChangingListener(pcListener);
		}
		
		if(newWizardContainer instanceof WizardDialog) {
			WizardDialog changeProvider = (WizardDialog) newWizardContainer;
			changeProvider.addPageChangingListener(pcListener);
		}
	}

}

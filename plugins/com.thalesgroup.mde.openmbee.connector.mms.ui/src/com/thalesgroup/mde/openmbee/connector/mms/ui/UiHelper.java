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
package com.thalesgroup.mde.openmbee.connector.mms.ui;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class UiHelper {
	private static final UiHelper _INSTANCE = new UiHelper();

	public static UiHelper getInstance() {
		return _INSTANCE;
	}
	
	private UiHelper() {}
	
	public Text createTextRow(WizardPage containingPage, Composite container, String label, int fillableNumberOfColumns, List<Control> disablableControls) {
		return createTextRow(containingPage, container, label, fillableNumberOfColumns, 0, disablableControls);
	}
	public Text createTextRow(WizardPage containingPage, Composite container, String label, int fillableNumberOfColumns, int extraStyle, List<Control> disablableControls) {
		return createTextRow(containingPage, container, label, fillableNumberOfColumns, extraStyle, true, disablableControls);
	}
	
	public Text createTextRow(WizardPage containingPage, Composite container, String label, int fillableNumberOfColumns, int extraStyle, boolean withResetListener, List<Control> disablableControls) {
		createLabel(container, label);
		int style = SWT.BORDER | extraStyle;
		if((extraStyle & SWT.MULTI) == 0) {
			style = style | SWT.SINGLE;
		}
		Text txt = new Text(container, style);
		if(withResetListener) {
			txt.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					if(containingPage.isPageComplete()) {
						disablableControls.forEach(c -> {if(c!=null) {c.setEnabled(false);}});
						containingPage.setPageComplete(false);
						containingPage.getWizard().getContainer().updateButtons();
					}
				}
			});
		}
		applyLayoutToFillSpace(txt, fillableNumberOfColumns);
		return txt;
	}

	protected void createLabel(Composite container, String label) {
		Label lbl = new Label(container, SWT.NONE);
		lbl.setText(label);
		GridData gdLbl = new GridData(SWT.LEFT, SWT.TOP, false, false);
		lbl.setLayoutData(gdLbl);
	}

	protected void applyLayoutToFillSpace(Control control, int fillableNumberOfColumns) {
		GridData gdTxt = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdTxt.horizontalSpan = fillableNumberOfColumns < 2 ? 1 : (fillableNumberOfColumns - 1);
		control.setLayoutData(gdTxt);
	}
}

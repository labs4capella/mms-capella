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
package com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.pages;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSIdentifiableDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSOrganizationDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.data.MmsConnectionData;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.utils.CollectionContentProvider;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.utils.NamedDescriptorLabelProvider;
import com.thalesgroup.mde.openmbee.connector.mms.ui.MmsServerConnectionDataComposite;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper;

public class ConnectMmsServerWizardPage extends WizardPage {

	private static final String TITLE__CONNECTION_WARNING = "Connection Warning"; //$NON-NLS-1$
	private static final String MESSAGE__NO_ORG_WARNING = "No organization can be found on the server."+System.lineSeparator()+"New organizations can be created in the MMS Server Management view."; //$NON-NLS-1$ //$NON-NLS-2$

	protected static class CriterionBasedFinishingStatusUpdater extends SelectionAdapter {
		private final String criterionId;
		private final ConnectMmsServerWizardPage finishablePage;
		
		public CriterionBasedFinishingStatusUpdater(ConnectMmsServerWizardPage page, String criterionId) {
			this.criterionId = criterionId;
			this.finishablePage = page;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			finishablePage.updatePageCompletness(criterionId, (((Combo)e.getSource()).getSelectionIndex() >= 0));
		}
	}

	private static final String DESCRIPTION_DEFAULT = "Fill the connection data of the MMS server and choose the organization under which the project shall be uploaded."; //$NON-NLS-1$
	protected static final String CRITERION_COMBO = "ComboCriterion"; //$NON-NLS-1$
	protected static final String TITLE = "MMS Server Connection Properties"; //$NON-NLS-1$
	protected static final String LABEL_TESTCONNECT = "Load organizations"; //$NON-NLS-1$
	protected static final String LABEL_ORG = "Organization:"; //$NON-NLS-1$
	protected static final String TEMPLATE_BASICAUTDATA = "Basic %s"; //$NON-NLS-1$
	protected static final String TEMPLATE_USERPASSWD = "%s:%s"; //$NON-NLS-1$
	protected static final int LAYOUT_COLUMNNUM = 3;
	protected ComboViewer orgSelector;
	protected SelectionAdapter finishingComboListener;
	protected MmsServerConnectionDataComposite container;
	protected MMSServerHelper serverHelper;
	private Map<String, Boolean> completnessCriteria = new HashMap<>();

	public ConnectMmsServerWizardPage() {
		super(TITLE);
		setTitle(TITLE);
		setDescription(DESCRIPTION_DEFAULT);
	}
	
	protected String getDefaultDescrition() {
		return DESCRIPTION_DEFAULT;
	}

	@Override
	public void createControl(Composite parent) {
		container = new MmsServerConnectionDataComposite(this, parent, SWT.NONE, LAYOUT_COLUMNNUM, LABEL_TESTCONNECT, new ArrayList<>(), a -> {
			loadOrganizationsIntoComboViewer();
		});
		orgSelector = createComboBoxRow(container, LABEL_ORG, container.numberOfColumns);
		
		setControl(container);
		finishingComboListener = new CriterionBasedFinishingStatusUpdater(this, CRITERION_COMBO);
		orgSelector.getCombo().addSelectionListener(finishingComboListener);
		updatePageCompletness(CRITERION_COMBO, false);
	}
	
	protected void updatePageCompletness(String completnessKey, boolean completnessValue) {
		if(completnessKey != null) {
			completnessCriteria.put(completnessKey, completnessValue);
		}
		setPageComplete(completnessCriteria.values().stream().noneMatch(c -> !c));
		getWizard().getContainer().updateButtons();
	}

	protected void loadOrganizationsIntoComboViewer() {
		serverHelper = new MMSServerHelper(container.getUrl(), getConnectionData().autData);
		List<MMSOrganizationDescriptor> orgs = serverHelper.getOrgs();
		if(orgs != null && orgs.size()>0) {
			orgSelector.setInput(orgs);
		} else {
			MessageDialog.openWarning(getShell(), TITLE__CONNECTION_WARNING, MESSAGE__NO_ORG_WARNING);
		}
		orgSelector.getCombo().setEnabled(true);
		setDescription(getDefaultDescrition());
	}

	protected ComboViewer createComboBoxRow(Composite container, String label, int fillableNumberOfColumns) {
		return createComboBoxRow(container, label, null, fillableNumberOfColumns);
	}
	
	protected ComboViewer createComboBoxRow(Composite container, String label, GridData gdLabel, int fillableNumberOfColumns) {
		createLabel(container, label, gdLabel);
		ComboViewer combo = new ComboViewer(container, SWT.READ_ONLY);
		combo.setContentProvider(new CollectionContentProvider());
		combo.setLabelProvider(new NamedDescriptorLabelProvider());
		combo.getCombo().setEnabled(false);
		applyLayoutToFillSpace(combo.getCombo(), fillableNumberOfColumns);
		return combo;
	}
	
	protected Text createTextRow(Composite container, String label, int fillableNumberOfColumns) {
		return createTextRow(container, label, fillableNumberOfColumns, 0);
	}
	
	protected Text createTextRow(Composite container, String label, int fillableNumberOfColumns, int extraStyle) {
		return createTextRow(container, label, fillableNumberOfColumns, extraStyle, true);
	}
	
	protected Text createTextRow(Composite container, String label, int fillableNumberOfColumns, int extraStyle, boolean withResetListener) {
		return createTextRow(container, label, null, fillableNumberOfColumns, extraStyle, withResetListener);
	}
	
	protected Text createTextRow(Composite container, String label, GridData gdLabel, int fillableNumberOfColumns, int extraStyle, boolean withResetListener) {
		createLabel(container, label, gdLabel);
		int style = SWT.BORDER | extraStyle;
		if((extraStyle & SWT.MULTI) == 0) {
			style = style | SWT.SINGLE;
		}
		Text txt = new Text(container, style);
		if(withResetListener) {
			txt.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					if(isPageComplete()) {
						disablableControls().forEach(c -> {if(c!=null) {c.setEnabled(false);}});
						setPageComplete(false);
						getWizard().getContainer().updateButtons();
					}
				}
			});
		}
		applyLayoutToFillSpace(txt, fillableNumberOfColumns);
		return txt;
	}
	
	protected List<Control> disablableControls() {
		List<Control> controls = new ArrayList<>();
		addControlIfViewerNotNull(controls, orgSelector);
		return controls;
	}

	protected void addControlIfViewerNotNull(List<Control> controls, Viewer cv) {
		if(cv != null) {
			controls.add(cv.getControl());
		}
	}

	protected void applyLayoutToFillSpace(Control control, int fillableNumberOfColumns) {
		GridData gdTxt = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdTxt.horizontalSpan = fillableNumberOfColumns < 2 ? 1 : (fillableNumberOfColumns - 1);
		control.setLayoutData(gdTxt);
	}

	protected void createLabel(Composite container, String label, GridData gd) {
		Label lbl = new Label(container, SWT.NONE);
		lbl.setText(label);
		GridData gdLbl = gd != null ? gd : new GridData(SWT.LEFT, SWT.TOP, false, false);
		lbl.setLayoutData(gdLbl);
	}
	
	public MmsConnectionData getConnectionData() {
		String userpasswd = String.format(TEMPLATE_USERPASSWD, container.getUser(), container.getPassword());
		String autData = String.format(TEMPLATE_BASICAUTDATA, Base64.getEncoder().encodeToString(userpasswd.getBytes()));
		String orgId = getSelectedDescriptorId(orgSelector);
		return new MmsConnectionData(container.getUrl(), autData, orgId);
	}
	
	public String getUser() {
		return container.getUser();
	}

	protected String getSelectedDescriptorId(ComboViewer selector) {
		Object selectedDescriptor = getSelectedElement(selector);
		String id = null;
		if(selectedDescriptor != null && selectedDescriptor instanceof MMSIdentifiableDescriptor) {
			id = ((MMSIdentifiableDescriptor)selectedDescriptor).id;
		}
		return id;
	}

	protected Object getSelectedElement(ComboViewer comboViewer) {
		return comboViewer.getElementAt(comboViewer.getCombo().getSelectionIndex());
	}

}

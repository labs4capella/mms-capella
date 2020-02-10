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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSOrganizationDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.data.MmsProjectCommitData;
import com.thalesgroup.mde.openmbee.connector.mms.ui.TextVerifyListener;

public class MmsProjectCommitWizardPage extends ConnectMmsServerWizardPage {
	protected static final String CRITERION_REF = "RefSelectionCriterion"; //$NON-NLS-1$
	private static final String LABEL_PROJECT = "MMS Project"; //$NON-NLS-1$
	private static final String LABEL_PROJECTNAME = "Name on MMS"; //$NON-NLS-1$
	private static final String LABEL_PROJECTID = "ID on MMS"; //$NON-NLS-1$
	private static final String LABEL_REF = "Ref (branch)"; //$NON-NLS-1$
	private static final String LABEL_COMMITCOMMENT = "Commit Comment"; //$NON-NLS-1$
	private static final String LABEL_PROJECTTYPE_NEW = "New"; //$NON-NLS-1$
	private static final String LABEL_PROJECTTYPE_UPDATE = "Update"; //$NON-NLS-1$
	private static final String MESSAGE_ID_DIFFERS = "The generated ID for the server project differs from the default generated one because of ID collisions on the server."; //$NON-NLS-1$
	protected Text projectName;
	protected Text projectId;
	protected Text commitComment;
	private ComboViewer projectSelector;
	private ComboViewer refSelector;
	private AtomicBoolean isNewProject = new AtomicBoolean(true);

	public MmsProjectCommitWizardPage() {
		super();
	}
	
	public void setProjectName(String projectName) {
		this.projectName.setText(projectName);
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		orgSelector.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Update new project specific data
				updateNewProjectData();
				
				// Update existing project specific data
				updateProjectUpdateData();
			}
		});
		

		GridData gdFill = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gdFill.horizontalSpan = container.numberOfColumns;
		
		Button btnNewProject = new Button(container, SWT.RADIO);
		btnNewProject.setLayoutData(gdFill);
		btnNewProject.setText(LABEL_PROJECTTYPE_NEW);
		btnNewProject.setSelection(true);
		GridData gdHorizontallyIndentedLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gdHorizontallyIndentedLabel.horizontalIndent = 20;
		projectName = createTextRow(container, LABEL_PROJECTNAME, gdHorizontallyIndentedLabel, container.numberOfColumns, SWT.SINGLE, false);
		projectName.setEnabled(false);
		projectId = createTextRow(container, LABEL_PROJECTID, gdHorizontallyIndentedLabel, container.numberOfColumns, SWT.SINGLE, false);
		projectId.setEnabled(false);
		projectId.setEditable(false);

		Button btnUpdateProject = new Button(container, SWT.RADIO);
		btnUpdateProject.setLayoutData(gdFill);
		btnUpdateProject.setText(LABEL_PROJECTTYPE_UPDATE);
		projectSelector = createComboBoxRow(container, LABEL_PROJECT, gdHorizontallyIndentedLabel, container.numberOfColumns);
		projectSelector.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object selectedElement = getSelectedElement(projectSelector);
				if(selectedElement instanceof MMSProjectDescriptor) {
					MMSProjectDescriptor projectDescriptor = (MMSProjectDescriptor)selectedElement;
					// handle old projects without client-side name
					if(projectDescriptor.clientSideName == null) {
						projectDescriptor.clientSideName = projectDescriptor.name;
					}
					List<MMSRefDescriptor> refs = 
							serverHelper.getBranches(projectDescriptor.id);
					refSelector.setInput(refs);
					refSelector.getCombo().setEnabled(true);
				}
			}
		});
		refSelector = createComboBoxRow(container, LABEL_REF, gdHorizontallyIndentedLabel, container.numberOfColumns);
		refSelector.getCombo().addSelectionListener(new CriterionBasedFinishingStatusUpdater(this, CRITERION_REF));
		
		commitComment = createTextRow(container, LABEL_COMMITCOMMENT, container.numberOfColumns, SWT.MULTI, false);
		((GridData)commitComment.getLayoutData()).grabExcessVerticalSpace = true;
		
		btnNewProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isNewProjectEnabled = btnNewProject.getSelection();
				isNewProject.set(isNewProjectEnabled);
				updateNewProjectData();
				updateProjectUpdateData();
				if(!isNewProjectEnabled) {
					setMessage(null, IMessageProvider.WARNING);
				}
			}
		});
		
		projectName.addVerifyListener(new TextVerifyListener() {

			@Override
			public void verifyText(VerifyEvent e) {
				setGeneratedId(getNewTextValue(e));
			}
		});
		updatePageCompletness(CRITERION_REF, isNewProject.get());
	}

	private void updateNewProjectData() {
		Object selectedElement = getSelectedElement(orgSelector);
		boolean areNewProjectSpecificFieldsEnabled = selectedElement instanceof MMSOrganizationDescriptor && isNewProject.get();
		if(areNewProjectSpecificFieldsEnabled) {
			setGeneratedId(projectName.getText());
		}
		projectName.setEnabled(areNewProjectSpecificFieldsEnabled);
		projectId.setEnabled(areNewProjectSpecificFieldsEnabled);
		updatePageCompletness(CRITERION_REF, areNewProjectSpecificFieldsEnabled);
	}

	private void updateProjectUpdateData() {
		Object selectedElement = getSelectedElement(orgSelector);
		boolean areUpdateProjectSpecificFieldsEnabled = selectedElement instanceof MMSOrganizationDescriptor && !isNewProject.get();
		if(areUpdateProjectSpecificFieldsEnabled) {
			List<MMSProjectDescriptor> projects = serverHelper.getProjects(
					((MMSOrganizationDescriptor)selectedElement).id);
			projectSelector.setInput(projects);
			refSelector.getCombo().setEnabled(false);
		}
		projectSelector.getCombo().setEnabled(areUpdateProjectSpecificFieldsEnabled);
	}

	private void setGeneratedId(String projectName) {
		String selectedOrgId = getSelectedDescriptorId(orgSelector);
		String generatedProjectId = "";
		if(projectName != null && !projectName.isEmpty() && selectedOrgId != null && !selectedOrgId.isEmpty()) {
			if(serverHelper == null) {
				generatedProjectId = projectName;
			} else {
				generatedProjectId = serverHelper.generateUniqueProjectId(selectedOrgId, projectName);
			}
		}
		if(projectId != null) {
			projectId.setText(generatedProjectId);
		}

		String defaultProjectId;
		if(serverHelper == null || projectName == null || selectedOrgId == null || projectName.isEmpty() || selectedOrgId.isEmpty()) {
			defaultProjectId = ""; //$NON-NLS-1$
		} else {
			defaultProjectId = serverHelper.generateBaseProjectId(selectedOrgId, projectName);
		}
		if(!generatedProjectId.contentEquals(defaultProjectId) && !defaultProjectId.isEmpty()) {
			setMessage(MESSAGE_ID_DIFFERS, IMessageProvider.WARNING);
		} else if(getMessage() != null && MESSAGE_ID_DIFFERS.contentEquals(getMessage())) {
			setMessage(null, IMessageProvider.WARNING);
		}
	}
	
	@Override
	protected void loadOrganizationsIntoComboViewer() {
		super.loadOrganizationsIntoComboViewer();
	}
	
	@Override
	protected List<Control> disablableControls() {
		List<Control> controls = super.disablableControls();
		controls.add(projectName);
		return controls;
	}
	
	@Override
	public MmsProjectCommitData getConnectionData() {
		try {
			if(!orgSelector.getCombo().isEnabled()) {
				return new MmsProjectCommitData(super.getConnectionData(), null, isNewProject.get(), null, null);
			} else if(isNewProject.get()) {
				return new MmsProjectCommitData(super.getConnectionData(), commitComment.getText(), projectName.getText(), projectId.getText(), isNewProject.get());
			} else {
				Object project = getSelectedElement(projectSelector);
				Object ref = getSelectedElement(refSelector);
				if(project instanceof MMSProjectDescriptor && ref instanceof MMSRefDescriptor) {
					return new MmsProjectCommitData(super.getConnectionData(), commitComment.getText(), isNewProject.get(), (MMSProjectDescriptor)project, (MMSRefDescriptor)ref);
				}
			}
		} catch (Exception e) {}
		return null;
	}

}

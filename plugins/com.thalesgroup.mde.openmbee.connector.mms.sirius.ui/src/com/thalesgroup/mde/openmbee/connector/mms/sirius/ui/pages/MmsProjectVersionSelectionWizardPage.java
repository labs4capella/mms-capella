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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSCommitDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSOrganizationDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.data.MmsProjectImportData;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.utils.CommitDescriptorLabelProvider;
import com.thalesgroup.mde.openmbee.connector.mms.ui.TextVerifyListener;

public class MmsProjectVersionSelectionWizardPage extends ConnectMmsServerWizardPage {
	protected static final String CRITERION_NAME = "ProjectNameCriterion"; //$NON-NLS-1$
	private static final String DESCRIPTION_DEFAULT = "Fill the connection data then select the project to import."; //$NON-NLS-1$
	private static final String TITLE = "MMS Project Selection"; //$NON-NLS-1$
	private static final String LABEL_PROJECT = "Project:"; //$NON-NLS-1$
	private static final String LABEL_REF = "Ref (branch):"; //$NON-NLS-1$
	private static final String LABEL_COMMIT = "Commit:"; //$NON-NLS-1$
	private static final String LABEL_PROJECTNAME = "Local Project Name:"; //$NON-NLS-1$
	private static final String MESSAGE_PROJECT_EXISTS = "Project already exists in workspace."; //$NON-NLS-1$
	private ComboViewer projectSelector;
	private ComboViewer refSelector;
	private ComboViewer commitSelector;
	private Text projectName;

	public MmsProjectVersionSelectionWizardPage() {
		super();
		setTitle(TITLE);
		setDescription(DESCRIPTION_DEFAULT);
	}
	
	@Override
	protected String getDefaultDescrition() {
		return DESCRIPTION_DEFAULT;
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		orgSelector.getCombo().removeSelectionListener(finishingComboListener);
		orgSelector.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object selectedElement = getSelectedElement(orgSelector);
				if(selectedElement instanceof MMSOrganizationDescriptor) {
					List<MMSProjectDescriptor> projects = 
							serverHelper.getProjects(((MMSOrganizationDescriptor)selectedElement).id);
					projectSelector.setInput(projects);
					projectSelector.getCombo().setEnabled(true);
					refSelector.getCombo().setEnabled(false);
					commitSelector.getCombo().setEnabled(false);
				}
			}
		});
		
		projectSelector = createComboBoxRow(container, LABEL_PROJECT, LAYOUT_COLUMNNUM);
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
							serverHelper.getBranches(projectDescriptor.orgId, projectDescriptor.id);
					refSelector.setInput(refs);
					refSelector.getCombo().setEnabled(true);
					commitSelector.getCombo().setEnabled(false);
					String newProjectName;
					String message;
					if(alreadyExistsInWorkspace(projectDescriptor.clientSideName)) {
						newProjectName = projectDescriptor.clientSideName; //generateUniqueName(projectDescriptor.clientSideName);
						message = null; //"Project name collision detected and the recommended name is automatically set to a unique one.";
					} else {
						newProjectName = projectDescriptor.clientSideName;
						message = null;
					}
					projectName.setText(newProjectName);
					if(getMessageType() < IMessageProvider.ERROR || message != null)
						setMessage(message, IMessageProvider.WARNING);
					projectName.setEnabled(true);
				}
			}
		});
		
		
		refSelector = createComboBoxRow(container, LABEL_REF, LAYOUT_COLUMNNUM);
		refSelector.getCombo().addSelectionListener(finishingComboListener);
		refSelector.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object selectedProject = getSelectedElement(projectSelector);
				Object selectedRef = getSelectedElement(refSelector);
				if(selectedProject instanceof MMSProjectDescriptor && selectedRef instanceof MMSRefDescriptor) {
					List<MMSCommitDescriptor> commits = serverHelper.getCommits(
																		((MMSProjectDescriptor)selectedProject).orgId, 
																		((MMSProjectDescriptor)selectedProject).id, 
																		((MMSRefDescriptor)selectedRef).id);
					commitSelector.setInput(commits);
					commitSelector.setSelection(new StructuredSelection(commits.stream().sorted((a,b) -> b._created.compareTo(a._created)).findFirst().get()));
					commitSelector.getCombo().setEnabled(true);
				}
			}
		});
		commitSelector = createComboBoxRow(container, LABEL_COMMIT, LAYOUT_COLUMNNUM);
		commitSelector.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if(e1 instanceof MMSCommitDescriptor && e2 instanceof MMSCommitDescriptor) {
					// change the ordering to descending
					return super.compare(viewer, ((MMSCommitDescriptor)e1)._created, ((MMSCommitDescriptor)e2)._created)*(-1);
				} else {
					return super.compare(viewer, e1, e2);
				}
			}
		});
		commitSelector.setLabelProvider(new CommitDescriptorLabelProvider());
		commitSelector.getCombo().addSelectionListener(finishingComboListener);
		projectName = createTextRow(container, LABEL_PROJECTNAME, LAYOUT_COLUMNNUM, SWT.SINGLE, false);
		projectName.setEnabled(false);
		projectName.setEditable(false);
		projectName.addVerifyListener(new TextVerifyListener() {
			
			@Override
			public void verifyText(VerifyEvent e) {
				String message = alreadyExistsInWorkspace(getNewTextValue(e)) ? MESSAGE_PROJECT_EXISTS : null;
				setMessage(message, IMessageProvider.ERROR);
				updatePageCompletness(CRITERION_NAME, message == null);
			}
		});
		updatePageCompletness(CRITERION_NAME, false);
	}
	
	protected String generateUniqueName(String baseName) {
		Stream<IProject> projects = Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()).stream();
		long i = 0;
		String[] generatedName = new String[1];
		do {
			generatedName[0] = String.format("%s%d", baseName, i); //$NON-NLS-1$
			i++;
		} while(projects.anyMatch(p -> p.getName().contentEquals(generatedName[0])));
		return generatedName[0];
	}

	protected boolean alreadyExistsInWorkspace(String newProjectName) {
		return (newProjectName==null || newProjectName.isEmpty()) ? false : ResourcesPlugin.getWorkspace().getRoot().getProject(newProjectName).exists();
	}

	@Override
	public MmsProjectImportData getConnectionData() {
		String projectId = getSelectedDescriptorId(projectSelector);
		String refId = getSelectedDescriptorId(refSelector);
		String commitId = getSelectedDescriptorId(commitSelector);
		return new MmsProjectImportData(super.getConnectionData(), projectId, refId, commitId);
	}
	
	@Override
	protected List<Control> disablableControls() {
		List<Control> controls = super.disablableControls();
		addControlIfViewerNotNull(controls, projectSelector);
		addControlIfViewerNotNull(controls, refSelector);
		controls.add(projectName);
		return controls;
	}

	protected void addControlIfViewerNotNull(List<Control> controls, Viewer cv) {
		if(cv != null) {
			controls.add(cv.getControl());
		}
	}

	public String getProjectName() {
		return projectName.getText();
	}
}

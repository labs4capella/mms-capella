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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.utils.CollectionContentProvider;

public class ProjectSelectorWizardPage extends WizardPage {

	private static final String NATURE_ID__CAPELLA = "org.polarsys.capella.project.nature"; //$NON-NLS-1$
	private static final String DESCRIPTION_DEFAULT = "Select the project which would be exported to MMS."; //$NON-NLS-1$
	private static final String TITLE = "Project Selection"; //$NON-NLS-1$
	private TreeViewer projectSelector;
	private IProject defaultProject;

	public ProjectSelectorWizardPage() {
		super(TITLE);
		setTitle(TITLE);
		setDescription(DESCRIPTION_DEFAULT);
	}
	
	public void setSelectedProject(IProject project) {
		defaultProject = project;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		projectSelector = new TreeViewer(container, SWT.SINGLE);
		projectSelector.setContentProvider(new CollectionContentProvider());
		projectSelector.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		List<IProject> input = Arrays.stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
										.filter(p -> {
											try {
												return p.hasNature(NATURE_ID__CAPELLA);
											} catch (CoreException e) {
												return false;
											}
										}).collect(Collectors.toList());
		projectSelector.setInput(input);
		projectSelector.getTree().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(((TreeItem)e.item).getData() instanceof IProject);
				getWizard().getContainer().updateButtons();
			}
		});
		setControl(container);
		setPageComplete(false);
		if(defaultProject != null && projectSelector.getInput() instanceof Collection &&
				((Collection<?>)projectSelector.getInput()).contains(defaultProject)) {
			projectSelector.setSelection(new StructuredSelection(defaultProject));
			setPageComplete(true);
		}
		getWizard().getContainer().updateButtons();
	}
	
	public IProject getSelectedProject() {
		try {
			return isPageComplete() ? (IProject)projectSelector.getStructuredSelection().getFirstElement() : null;
		} catch(Exception e) {
			return null;
		}
	}
}

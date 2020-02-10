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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui.utils;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSIdentifiableDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSNamedDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSOrganizationDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeParent;
import com.thalesgroup.mde.openmbee.connector.mms.ui.UiHelper;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper;

public class MmsChildCreationWizard extends Wizard {
	private static final String MSG_WIZARD_PAGE_REF = "%s%sThe parent of the new reference will be the '%s'"; //$NON-NLS-1$
	private static final String MSG_WIZARD_PAGE = "Create a new %s under '%s' with the specified id and name."; //$NON-NLS-1$
	private static final String TITLE = "%s Creation"; //$NON-NLS-1$
	private static final String VALIDATION_PATTERN_NAME = "[\\s]*"; //$NON-NLS-1$
	private static final String VALIDATION_PATTERN_ID = "[a-z][-a-z0-9]*"; //$NON-NLS-1$
	private static final String MSG_TXT_NAME = "If it is unset, the id will be used as name"; //$NON-NLS-1$
	private static final String MSG_ID_INVALID = "Invalid characters were found in the ID. Only alphanumeric characters and hyphens are allowed and it needs to be started by a letter."; //$NON-NLS-1$
	private static final String MSG_ID_INUSE = "The ID is already used on the server"; //$NON-NLS-1$
	private final MMSServerHelper serverHelper;
	private final MMSIdentifiableDescriptor parent;
	private String parentRefId = MMSServerHelper.MMS_REF__DEFAULT;
	private final MMSTreeParent treeParent;
	private MmsChildType childType;
	private static final UiHelper _HELPER = UiHelper.getInstance();
	private AtomicReference<MMSNamedDescriptor> preparedChild = new AtomicReference<>();

	public MmsChildCreationWizard(MMSTreeParent parent, MMSRefDescriptor parentRefDescriptor) {
		this(parent);
		if(parentRefDescriptor != null) {
			this.childType = MmsChildType.Ref;
			this.parentRefId = parentRefDescriptor.id;
		}
	}
	
	public MmsChildCreationWizard(MMSTreeParent parent) {
		this.serverHelper = parent.getServerHelper();
		this.treeParent = parent;
		this.parent = parent.getMmsObject();
		if(this.parent instanceof MMSServerDescriptor) {
			this.childType = MmsChildType.Organization;
		} else if(this.parent instanceof MMSOrganizationDescriptor) {
			this.childType = MmsChildType.Project;
		} else if(this.parent instanceof MMSProjectDescriptor) {
			this.childType = MmsChildType.Ref;
		} else {
			throw new IllegalArgumentException("Cannot create child for the parent: "+this.parent); //$NON-NLS-1$
		}
		this.setWindowTitle(String.format(TITLE, childType.name()));
	}
	
	@Override
	public void addPages() {
		addPage(new WizardPage(String.format(TITLE, childType.name())) {
			
			private Text name;
			private Text id;
			
			@Override
			public void createControl(Composite parent) {
				String baseMessage = String.format(MSG_WIZARD_PAGE, childType.name(), MmsChildCreationWizard.this.parent.id);
				if (childType == MmsChildType.Ref) {
					baseMessage = String.format(MSG_WIZARD_PAGE_REF, baseMessage, System.lineSeparator(), parentRefId);
				}
				setMessage(baseMessage);
				Composite container = new Composite(parent, SWT.NONE);
				GridLayout layout = new GridLayout();
				container.setLayout(layout);
				layout.numColumns = 2;
				
				id = _HELPER.createTextRow(this, container, String.format("%s id", childType.name()), 3, SWT.NONE, true, Collections.emptyList()); //$NON-NLS-1$
				name = _HELPER.createTextRow(this, container, String.format("%s name", childType.name()), 3, SWT.NONE, true, Collections.emptyList()); //$NON-NLS-1$
				name.setMessage(MSG_TXT_NAME);
				
				// Fill the space before the button
				Label lblFillBeforeButton = new Label(container, SWT.NONE);
				GridData gdFill = new GridData(SWT.FILL, SWT.CENTER, true, false);
				gdFill.horizontalSpan = 2;
				lblFillBeforeButton.setLayoutData(gdFill);
				Button btnValidate = new Button(container, SWT.PUSH);
				btnValidate.setText("Validate"); //$NON-NLS-1$
				btnValidate.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						if(!id.getText().matches(VALIDATION_PATTERN_ID)) {
							setErrorMessage(MSG_ID_INVALID);
							setPageComplete(false);
							return;
						}
						boolean valid = false;
						MMSNamedDescriptor nd = null;
						switch(childType) {
						case Organization:
							valid = serverHelper.getOrgs().stream()
													.noneMatch(o -> o.id.contentEquals(id.getText()));
							if(valid) {
								nd = new MMSOrganizationDescriptor();
							}
							break;
						case Project:
							valid = serverHelper.getProjects().stream()
													.noneMatch(p -> p.id.contentEquals(id.getText()));
							if(valid) {
								nd = new MMSProjectDescriptor();
							}
							break;
						case Ref:
							valid = serverHelper.getBranches(MmsChildCreationWizard.this.parent.id).stream()
													.noneMatch(b -> b.id.contentEquals(id.getText()));
							if(valid) {
								nd = new MMSRefDescriptor();
							}
							break;
						}
						
						if(valid) {
							nd.id = id.getText();
							nd.name = name.getText().matches(VALIDATION_PATTERN_NAME) ? id.getText() : name.getText();
							preparedChild.set(nd);
							setErrorMessage(null);
						} else {
							setErrorMessage(MSG_ID_INUSE);
						}
						setPageComplete(valid);
					}
				});
				
				setControl(container);
			}
		});
	}

	@Override
	public boolean performFinish() {
		MMSIdentifiableDescriptor created = null;
		switch (childType) {
		case Organization:
			created = serverHelper.createOrg(preparedChild.get().id, preparedChild.get().name);
			break;
		case Project:
			created = serverHelper.createProject(parent.id, preparedChild.get().id, preparedChild.get().name);
			break;
		case Ref:
			created = serverHelper.createBranch(parent.id, preparedChild.get().id, preparedChild.get().name, parentRefId);
			break;
		}
		if(created != null) {
			return treeParent.addChild(created);
		} else {
			return false;
		}
	}
	
	protected static enum MmsChildType {
		Organization, Project, Ref
	}

}

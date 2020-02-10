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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper.MMSConnectionException;

public class MmsServerConnectionDataComposite extends Composite {
	private static final String MSG__LOG_UNEXPECTED_ERROR = "Unexpected error while trying to connect to the MMS server at '%s' with user '%s'"; //$NON-NLS-1$
	private static final String MSG__UNEXPECTED_ERROR = "Unexpected error while trying to connect to the server. See Error Log view for details."; //$NON-NLS-1$
	private static final String MSG__WRONG_SERVER_URL = "Wrong server URL"; //$NON-NLS-1$
	private static final String MSG__CANNOT_CONNECT_MMS = "Cannot connect to the server with the given data"; //$NON-NLS-1$
	private static final String TITLE__CONNECTION_ERROR = "Connection Error"; //$NON-NLS-1$
	private static final String MSG__UNSUCCESSFUL_LOGIN = "Login to the server %s was not successful."; //$NON-NLS-1$
	protected static final int LAYOUT_MINCOLUMNNUM = 3;
	protected static final String LABEL_URL = "MMS server URL:"; //$NON-NLS-1$
	protected static final String LABEL_USER = "Username:"; //$NON-NLS-1$
	protected static final String LABEL_PASS = "Password:"; //$NON-NLS-1$
	
	private UiHelper helper = UiHelper.getInstance();
	private Text txtUrl;
	private Text txtUser;
	private Text txtPass;
	private String serverUrl;
	private String username;
	private String password;
	private MMSServerDescriptor serverConnectionData;
	public final int numberOfColumns;

	public MmsServerConnectionDataComposite(WizardPage containingPage, Composite parent, int style, int columnsInGrid, String lblConnectionTesterButton, List<Control> disablableControls, Consumer<MMSServerDescriptor> actionToExecuteAfterSuccessfulConnectionTest) {
		super(parent, style);
		numberOfColumns = columnsInGrid < LAYOUT_MINCOLUMNNUM ? LAYOUT_MINCOLUMNNUM : columnsInGrid;
		GridLayout layout = new GridLayout();
		this.setLayout(layout);
		layout.numColumns = numberOfColumns;
		
		txtUrl = helper.createTextRow(containingPage, this, LABEL_URL, numberOfColumns, disablableControls);
		txtUrl.setText(MmsUiPlugin.getDefault().getStoredMmsUrls()[0]);
		new ContentProposalAdapter(txtUrl, new TextContentAdapter(), 
				new VariableContentProposalProvider() {
					@Override
					protected Collection<String> getPossibleValues() {
						return Arrays.asList(MmsUiPlugin.getDefault().getStoredMmsUrls());
					}
				}, null, null).setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		txtUser = helper.createTextRow(containingPage, this, LABEL_USER, numberOfColumns, disablableControls);
		txtUser.setText(MmsUiPlugin.getDefault().getStoredMmsUsers()[0]);
		new ContentProposalAdapter(txtUser, new TextContentAdapter(), 
				new VariableContentProposalProvider() {
			@Override
			protected Collection<String> getPossibleValues() {
				return Arrays.asList(MmsUiPlugin.getDefault().getStoredMmsUsers());
			}
		}, null, null).setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		txtPass = helper.createTextRow(containingPage, this, LABEL_PASS, numberOfColumns, SWT.PASSWORD, disablableControls);
		
		// Fill the space before the button
		Label lblFillBeforeButton = new Label(this, SWT.NONE);
		GridData gdFill = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gdFill.horizontalSpan = numberOfColumns - 1;
		lblFillBeforeButton.setLayoutData(gdFill);
		Button btnTestConnection = new Button(this, SWT.PUSH);
		btnTestConnection.setText(lblConnectionTesterButton);
		btnTestConnection.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					boolean isLoginSuccessful = checkConnection();
					if(isLoginSuccessful) {
						serverConnectionData = new MMSServerDescriptor(serverUrl, MMSServerHelper.encodeBasicAuthData(username, password));
						actionToExecuteAfterSuccessfulConnectionTest.accept(serverConnectionData);
					} else {
						throw new MMSConnectionException(String.format(MSG__UNSUCCESSFUL_LOGIN, serverUrl));
					}
				} catch (MMSConnectionException ex) {
					MessageDialog.openError(getShell(), TITLE__CONNECTION_ERROR, MSG__CANNOT_CONNECT_MMS);
				} catch (IllegalArgumentException ex) {
					MessageDialog.openError(getShell(), TITLE__CONNECTION_ERROR, MSG__WRONG_SERVER_URL);
					MmsUiPlugin.getDefault().getLogger().error(MSG__WRONG_SERVER_URL+": "+serverUrl, ex); //$NON-NLS-1$
				} catch (Exception ex) {
					MessageDialog.openError(getShell(), TITLE__CONNECTION_ERROR, MSG__UNEXPECTED_ERROR);
					MmsUiPlugin.getDefault().getLogger().error(
							String.format(MSG__LOG_UNEXPECTED_ERROR,
									serverUrl,
									username
							), ex);
				}
			}
		});
	}

	protected boolean checkConnection() {
		serverUrl = txtUrl.getText();
		username = txtUser.getText();
		password = txtPass.getText();
		String ticket = MMSServerHelper.login(serverUrl, username, password);
		return ticket != null && !ticket.isEmpty();
	}
	
	public String getUrl() {
		return serverUrl;
	}
	
	public String getUser() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setUrl(String url) {
		txtUrl.setText(url);
	}
	
	public void setUser(String user) {
		txtUser.setText(user);
	}
	
	public void setPassword(String password) {
		txtPass.setText(password);
	}

	public MMSServerDescriptor getConnectionData() {
		return serverConnectionData;
	}
}

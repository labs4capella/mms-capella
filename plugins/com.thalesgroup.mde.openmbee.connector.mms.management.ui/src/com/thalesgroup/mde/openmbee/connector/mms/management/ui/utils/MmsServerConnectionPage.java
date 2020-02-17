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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.ui.MmsServerConnectionDataComposite;
import com.thalesgroup.mde.openmbee.connector.mms.ui.UiHelper;

public class MmsServerConnectionPage extends WizardPage {
	private static final String MESSAGE__SERVER_NAME = "If it is unset the url of the server will be used as name"; //$NON-NLS-1$
	private static final String LABEL__TEST_BUTTON = "Test connection"; //$NON-NLS-1$
	private static final String LABEL__SERVER_NAME = "Server name (optional):"; //$NON-NLS-1$
	private static final String PAGE_TITLE = "MMS Server Data"; //$NON-NLS-1$
	private MmsServerConnectionDataComposite connection;
	private Text serverName;
	private String defaultName = null;
	private String defaultUrl = null;
	private String defaultAPIVersion = null;
	private String defaultUser = null;
	private String defaultPassword = null;

	public MmsServerConnectionPage() {
		super(PAGE_TITLE);
		setTitle(PAGE_TITLE);
	}

	@Override
	public void createControl(Composite parent) {
		ArrayList<Control> disablableControls = new ArrayList<>();
		connection = new MmsServerConnectionDataComposite(this, parent, SWT.NONE, 3, LABEL__TEST_BUTTON, disablableControls, a -> {
			serverName.setEnabled(true);
			setPageComplete(true);
		});
		GridData gdFill = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdFill.horizontalSpan = connection.numberOfColumns-1;
		connection.setLayoutData(gdFill);
		serverName = UiHelper.getInstance().createTextRow(this, connection, LABEL__SERVER_NAME, connection.numberOfColumns, null);
		serverName.setMessage(MESSAGE__SERVER_NAME);
		serverName.setEnabled(false);
		Arrays.asList(serverName.getListeners(SWT.Modify)).forEach(l -> serverName.removeListener(SWT.Modify, l));
		disablableControls.add(serverName);
		
		// set default values
		if(defaultName != null) {
			serverName.setText(defaultName);
		}
		if(defaultUrl != null) {
			connection.setUrl(defaultUrl);
		}
		if(defaultAPIVersion != null) {
			connection.setAPIVersion(defaultAPIVersion);
		}
		if(defaultUser != null) {
			connection.setUser(defaultUser);
		}
		if(defaultPassword != null) {
			connection.setPassword(defaultPassword);
		}
		
		setControl(connection);
	}
	
	public MMSServerDescriptor getConnectionData() {
		MMSServerDescriptor connectionData = connection.getConnectionData();
		if(!serverName.getText().isEmpty()) { 
			connectionData.name = serverName.getText();
		}
		return connectionData;
	}

	public String getUrl() {
		return connection.getUrl();
	}

	public String getAPIVersion() {
		return connection.getAPIVersion();
	}
	
	public String getUser() {
		return connection.getUser();
	}
	
	public String getPassword() {
		return connection.getPassword();
	}

	public void setDefaultUrl(String url) {
		defaultUrl = url;
	}

	public void setDefaultAPIVersion(String apiVersion) {
		defaultAPIVersion = apiVersion;
	}
	
	public void setDefaultUser(String user) {
		defaultUser = user;
	}
	
	public void setDefaultPassword(String password) {
		defaultPassword = password;
	}
	
	public void setDefaultName(String name) {
		defaultName = name;
	}
}
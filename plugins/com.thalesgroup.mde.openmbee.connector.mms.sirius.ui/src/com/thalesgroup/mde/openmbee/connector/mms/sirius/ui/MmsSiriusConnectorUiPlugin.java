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

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.thalesgroup.mde.openmbee.connector.mms.ui.MmsUiPlugin;
import com.thalesgroup.mde.openmbee.connector.mms.utils.LogHelper;

public class MmsSiriusConnectorUiPlugin extends Plugin {

	/**
	 * The bundle Id.
	 */
	public static final String PLUGIN_ID = "com.thalesgroup.mde.openmbee.connector.mms.sirius.ui"; //$NON-NLS-1$
	/**
	 * 
	 */
	private static MmsSiriusConnectorUiPlugin plugin;
	private LogHelper logHelper;

	/**
	 * 
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		logHelper = new LogHelper(getLog());
	}
	
	public String[] getStoredMmsUrls() {
		return MmsUiPlugin.getDefault().getStoredMmsUrls();
	}
	
	public void addStoredMmsUrl(String newUrl) {
		MmsUiPlugin.getDefault().addStoredMmsUrl(newUrl);
	}
	
	public String[] getStoredMmsUsers() {
		return MmsUiPlugin.getDefault().getStoredMmsUsers();
	}
	
	public void addStoredMmsUser(String newUser) {
		MmsUiPlugin.getDefault().addStoredMmsUser(newUser);
	}
	
	void savePreferences() {
	}

	/**
	 * 
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		savePreferences();
		super.stop(context);
		plugin = null;
		logHelper = null;
	}

	/**
	 * 
	 */
	public static MmsSiriusConnectorUiPlugin getDefault() {
		return plugin;
	}
	
	public LogHelper getLogger() {
		return logHelper;
	}

}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import com.thalesgroup.mde.openmbee.connector.mms.utils.LogHelper;

public class MmsUiPlugin extends Plugin {
	/**
	 * The bundle Id.
	 */
	public static final String PLUGIN_ID = "com.thalesgroup.mde.openmbee.connector.mms.ui"; //$NON-NLS-1$
	private static final String PREFERENCES_MMS_URL = PLUGIN_ID+".mmsUrl"; //$NON-NLS-1$
	private static final String PREFERENCES_MMS_USER = PLUGIN_ID+".mmsUser"; //$NON-NLS-1$
	private static final String DEFAULT_URL = "/alfresco/service"; //$NON-NLS-1$
	private static final String DEFAULT_USER = ""; //$NON-NLS-1$
	private static final String PREF_VALUE_SEPARATOR = ";"; //$NON-NLS-1$
	
	private static MmsUiPlugin plugin;
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
	
	public void savePreferences() {
		try {
			InstanceScope.INSTANCE.getNode(PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
			logHelper.error(e);
		}
	}
	
	public String[] getStoredMmsUrls() {
		return InstanceScope.INSTANCE.getNode(PLUGIN_ID).get(PREFERENCES_MMS_URL, DEFAULT_URL).split(PREF_VALUE_SEPARATOR);
	}
	
	public void addStoredMmsUrl(String newUrl) {
		if(newUrl != null && !newUrl.isEmpty() && !newUrl.contains(PREF_VALUE_SEPARATOR)) {
			String currentValuesArray = InstanceScope.INSTANCE.getNode(PLUGIN_ID).get(PREFERENCES_MMS_URL, ""); //$NON-NLS-1$
			List<String> urls = new ArrayList<>(Arrays.asList(currentValuesArray.split(PREF_VALUE_SEPARATOR)));
			if(urls.stream().noneMatch(u -> newUrl.contentEquals(u))) {
				urls.add(0, newUrl);
			}
			InstanceScope.INSTANCE.getNode(PLUGIN_ID).put(PREFERENCES_MMS_URL, String.join(PREF_VALUE_SEPARATOR, urls));
		}
	}
	
	public String[] getStoredMmsUsers() {
		return InstanceScope.INSTANCE.getNode(PLUGIN_ID).get(PREFERENCES_MMS_USER, DEFAULT_USER).split(PREF_VALUE_SEPARATOR);
	}
	
	public void addStoredMmsUser(String newUser) {
		if(newUser != null && !newUser.isEmpty() && !newUser.contains(PREF_VALUE_SEPARATOR)) {
			String currentValuesArray = InstanceScope.INSTANCE.getNode(PLUGIN_ID).get(PREFERENCES_MMS_USER, ""); //$NON-NLS-1$
			List<String> users = new ArrayList<>(Arrays.asList(currentValuesArray.split(PREF_VALUE_SEPARATOR)));
			if(users.stream().noneMatch(u -> newUser.contentEquals(u))) {
				users.add(0, newUser);
			}
			InstanceScope.INSTANCE.getNode(PLUGIN_ID).put(PREFERENCES_MMS_USER, String.join(PREF_VALUE_SEPARATOR, users));
		}
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
	public static MmsUiPlugin getDefault() {
		return plugin;
	}
	
	public LogHelper getLogger() {
		return logHelper;
	}

}

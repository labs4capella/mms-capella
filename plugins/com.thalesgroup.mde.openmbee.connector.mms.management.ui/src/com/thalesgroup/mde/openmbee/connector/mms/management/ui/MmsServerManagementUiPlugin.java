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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSJsonHelper;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.ui.MmsUiPlugin;
import com.thalesgroup.mde.openmbee.connector.mms.utils.LogHelper;

public class MmsServerManagementUiPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "com.thalesgroup.mde.openmbee.connector.mms.management.ui"; //$NON-NLS-1$
	public static final String IMG__ADD_SERVER = "add_participant.png"; //$NON-NLS-1$
	public static final String IMG__EDIT = "edit.gif"; //$NON-NLS-1$
	public static final String IMG__REFRESH = "refresh.gif"; //$NON-NLS-1$
	public static final String IMG__MMS_PROJECT = "project.gif"; //$NON-NLS-1$
	public static final String IMG__MMS_REF = "branch.png"; //$NON-NLS-1$
	public static final String IMG__MMS_SERVER = "mms_server.png"; //$NON-NLS-1$
	private static final String PREFERENCES_MMS_SERVERDATA = PLUGIN_ID+".mmsServerData"; //$NON-NLS-1$
	private static MmsServerManagementUiPlugin _INSTANCE;
	private LogHelper logHelper;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		_INSTANCE = this;
		logHelper = new LogHelper(getLog());
	}
	
	public static MmsServerManagementUiPlugin getInstance() {
		return _INSTANCE;
	}
	
	public MMSServerDescriptor[] getStoredMMSServerDescriptors() {
		try(StringReader stringReader = new StringReader(getStoredServerDataString())) {
			return MMSJsonHelper.getMMS3PreparedGsonBuilder().create().fromJson(stringReader, MMSServerDescriptor[].class);
		} catch (Exception e) {
			logHelper.error(e);
		}
		return new MMSServerDescriptor[0];
	}
	
	public void addStoredMMSServerDescriptor(MMSServerDescriptor serverDescriptor) {
		try(StringReader stringReader = new StringReader(getStoredServerDataString())) {
			MMSServerDescriptor[] fromJson = MMSJsonHelper.getMMS3PreparedGsonBuilder().create().fromJson(stringReader, MMSServerDescriptor[].class);
			Set<MMSServerDescriptor> existingOnes = new HashSet<>();
			if(fromJson != null && fromJson.length > 0) {
				existingOnes.addAll(Arrays.asList(fromJson));
			}
			if(!existingOnes.contains(serverDescriptor)) {
				if(existingOnes.add(serverDescriptor)) {
					SecurePreferencesFactory.getDefault().node(PLUGIN_ID).put(PREFERENCES_MMS_SERVERDATA, MMSJsonHelper.getMMS3PreparedGsonBuilder().create().toJson(existingOnes), true);
				}
			}
		} catch (StorageException e) {
			logHelper.error(e);
		}
	}
	
	public void removeStoredMMSServerDescriptor(MMSServerDescriptor serverDescriptor) {
		try(StringReader stringReader = new StringReader(getStoredServerDataString())) {
			MMSServerDescriptor[] fromJson = MMSJsonHelper.getMMS3PreparedGsonBuilder().create().fromJson(stringReader, MMSServerDescriptor[].class);
			Set<MMSServerDescriptor> existingOnes = new HashSet<>();
			if(fromJson != null && fromJson.length > 0) {
				existingOnes.addAll(Arrays.asList(fromJson));
			}
			Optional<MMSServerDescriptor> removable = existingOnes.stream().filter(s -> serverDescriptor.id.contentEquals(s.id)).findFirst();
			if(removable.isPresent() && existingOnes.remove(removable.get())) {
				SecurePreferencesFactory.getDefault().node(PLUGIN_ID).put(PREFERENCES_MMS_SERVERDATA, MMSJsonHelper.getMMS3PreparedGsonBuilder().create().toJson(existingOnes), true);
			}
		} catch (StorageException e) {
			logHelper.error(e);
		}
	}

	private String getStoredServerDataString() {
		String storedServerDataString = null;
		try {
			storedServerDataString = SecurePreferencesFactory.getDefault().node(PLUGIN_ID).get(PREFERENCES_MMS_SERVERDATA, "[]"); //$NON-NLS-1$
		} catch (StorageException e1) {}
		if(storedServerDataString == null) {
			storedServerDataString = "[]"; //$NON-NLS-1$
		}
		return storedServerDataString;
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
	
	public LogHelper getLogger() {
		return logHelper;
	}
	
	public void savePreferences() {
		try {
			SecurePreferencesFactory.getDefault().node(PLUGIN_ID).flush();
		} catch (IOException e) {
			logHelper.error(e);
		}
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		savePreferences();
		_INSTANCE = null;
		logHelper = null;
		super.stop(context);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(IMG__ADD_SERVER,	imageDescriptorFromPlugin(PLUGIN_ID, "icons/add_participant.png")); //$NON-NLS-1$
		reg.put(IMG__EDIT,			imageDescriptorFromPlugin(PLUGIN_ID, "icons/edit.gif")); //$NON-NLS-1$
		reg.put(IMG__REFRESH,		imageDescriptorFromPlugin(PLUGIN_ID, "icons/refresh.gif")); //$NON-NLS-1$
		reg.put(IMG__MMS_PROJECT,	imageDescriptorFromPlugin(PLUGIN_ID, "icons/project.gif")); //$NON-NLS-1$
		reg.put(IMG__MMS_REF,		imageDescriptorFromPlugin(PLUGIN_ID, "icons/branch.png")); //$NON-NLS-1$
		reg.put(IMG__MMS_SERVER,	imageDescriptorFromPlugin(PLUGIN_ID, "icons/mms_server.png")); //$NON-NLS-1$
	}
}

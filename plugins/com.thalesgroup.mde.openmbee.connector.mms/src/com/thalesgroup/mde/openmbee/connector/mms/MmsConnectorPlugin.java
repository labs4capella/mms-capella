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
package com.thalesgroup.mde.openmbee.connector.mms;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.thalesgroup.mde.openmbee.connector.mms.utils.LogHelper;

public class MmsConnectorPlugin extends Plugin {

	/**
	 * The bundle Id.
	 */
	public static final String PLUGIN_ID = "com.thalesgroup.mde.openmbee.connector.mms"; //$NON-NLS-1$

	/**
	 * 
	 */
	private static MmsConnectorPlugin plugin;
	private LogHelper logHelper;

	/**
	 * 
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
		super.start(context);
		logHelper = new LogHelper(getLog());
	}

	/**
	 * 
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		logHelper = null;
	}

	/**
	 * 
	 */
	public static MmsConnectorPlugin getDefault() {
		return plugin;
	}

	/**
	 * 
	 */
	public LogHelper getLogger() {
		return logHelper;
	}

}

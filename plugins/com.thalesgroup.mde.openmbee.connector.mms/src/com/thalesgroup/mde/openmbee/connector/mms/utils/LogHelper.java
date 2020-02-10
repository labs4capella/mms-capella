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
package com.thalesgroup.mde.openmbee.connector.mms.utils;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class LogHelper {
	private final ILog logger;
	private final String bundleName;

	public LogHelper(ILog logger) {
		this.logger = logger;
		this.bundleName = logger.getBundle().getSymbolicName();
	}

	public void info(String message) {
		logger.log(new Status(IStatus.INFO, bundleName, message));
	}

	public void warn(String message) {
		logger.log(new Status(IStatus.WARNING, bundleName, message));
	}

	public void warn(String message, Exception exception) {
		logger.log(new Status(IStatus.WARNING, bundleName, message, exception));
	}

	public void error(String message) {
		logger.log(new Status(IStatus.ERROR, bundleName, message));
	}

	public void error(Exception exception) {
		logger.log(new Status(IStatus.ERROR, bundleName, exception.getMessage(), exception));
	}
	public void error(String message, Exception exception) {
		logger.log(new Status(IStatus.ERROR, bundleName, message, exception));
	}

}

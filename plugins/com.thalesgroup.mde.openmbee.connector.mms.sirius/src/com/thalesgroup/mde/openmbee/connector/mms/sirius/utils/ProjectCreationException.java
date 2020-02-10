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
package com.thalesgroup.mde.openmbee.connector.mms.sirius.utils;

public class ProjectCreationException extends RuntimeException {
	
	public ProjectCreationException(String message) {
		super(message);
	}

	public ProjectCreationException(Throwable e) {
		super(e);
	}

	public ProjectCreationException(String message, Throwable e) {
		super(message, e);
	}

	private static final long serialVersionUID = 477229237623111924L;
	
}
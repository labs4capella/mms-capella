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

public class ProjectAlreadyExistsException extends ProjectCreationException {
	
	private static final long serialVersionUID = 7004610046126588548L;

	public ProjectAlreadyExistsException(String message) {
		super(message);
	}
	
	public ProjectAlreadyExistsException(Throwable e) {
		super(e);
	}
	
	public ProjectAlreadyExistsException(String message, Throwable e) {
		super(message, e);
	}
	
}
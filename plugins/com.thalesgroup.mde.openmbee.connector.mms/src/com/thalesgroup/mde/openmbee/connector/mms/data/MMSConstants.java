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
package com.thalesgroup.mde.openmbee.connector.mms.data;

import com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FilesystemPackage;

public class MMSConstants {
	public static final String MMS_TYPE_PROJECT = "Project"; //$NON-NLS-1$
	public static final String MMS_TYPE_FOLDER = FilesystemPackage.eINSTANCE.getFolder().getName();
	public static final String MMS_TYPE_FILE = FilesystemPackage.eINSTANCE.getFile().getName();
}

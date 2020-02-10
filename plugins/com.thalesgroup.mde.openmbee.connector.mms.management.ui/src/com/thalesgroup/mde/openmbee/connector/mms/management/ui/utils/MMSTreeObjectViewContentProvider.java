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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Shell;

import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeObject;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeParent;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper.MMSConnectionException;

public class MMSTreeObjectViewContentProvider implements ITreeContentProvider {
	private static final String TITLE__CONNECTION_ERROR = "Connection Error"; //$NON-NLS-1$
	private static final String MESSAGE__CONNECTION_ERROR = "Cannot connect to the server with the given data. Maybe the server is not available or some of the connection data is incorrect."; //$NON-NLS-1$
	private Shell shellForDialog;

	public MMSTreeObjectViewContentProvider(Shell shellForDialog) {
		this.shellForDialog = shellForDialog;
	}

	@Override
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}
	
	@Override
	public Object getParent(Object child) {
		if (child instanceof MMSTreeObject) {
			return ((MMSTreeObject)child).getParent();
		}
		return null;
	}
	
	@Override
	public Object [] getChildren(Object parent) {
		if (parent instanceof MMSTreeParent) {
			try {
				return ((MMSTreeParent)parent).getChildren();
			} catch (MMSConnectionException e) {
				MessageDialog.openError(this.shellForDialog, TITLE__CONNECTION_ERROR, MESSAGE__CONNECTION_ERROR);
			}
		}
		return new Object[0];
	}
	
	@Override
	public boolean hasChildren(Object parent) {
		if (parent instanceof MMSTreeParent)
			return ((MMSTreeParent)parent).hasChildren();
		return false;
	}
}
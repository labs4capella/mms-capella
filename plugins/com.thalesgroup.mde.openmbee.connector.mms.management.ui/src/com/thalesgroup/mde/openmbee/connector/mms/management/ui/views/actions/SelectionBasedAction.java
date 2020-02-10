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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui.views.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;

public abstract class SelectionBasedAction extends Action {
	protected final StructuredViewer viewer;
	
	public SelectionBasedAction(StructuredViewer viewer) {
		this.viewer = viewer;
	}
	
	protected List<?> getSelectedElements() {
		return viewer.getStructuredSelection().toList();
	}
	
	public abstract void updateEnablement();

}

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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.thalesgroup.mde.openmbee.connector.mms.management.ui.views.actions.SelectionBasedAction;

public class SelectionBasedActionUpdater implements ISelectionChangedListener {
	private final List<SelectionBasedAction> updatableActions;
	
	public SelectionBasedActionUpdater(SelectionBasedAction... updatableActions) {
		List<SelectionBasedAction> actions = new ArrayList<>();
		try {
			actions = new ArrayList<>(Arrays.asList(updatableActions));
		} catch (NullPointerException e) {
		}
		this.updatableActions = actions;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updatableActions.forEach(a -> a.updateEnablement());
	}
}
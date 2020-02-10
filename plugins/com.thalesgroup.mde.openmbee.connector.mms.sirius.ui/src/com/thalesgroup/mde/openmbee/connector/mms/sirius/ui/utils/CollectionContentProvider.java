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
package com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.utils;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class CollectionContentProvider implements ITreeContentProvider {
	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof Collection) {
			return ((Collection<?>)inputElement).toArray();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) { return false; }

	@Override
	public Object getParent(Object element) { return null; }

	@Override
	public Object[] getChildren(Object parentElement) { return null; }
}
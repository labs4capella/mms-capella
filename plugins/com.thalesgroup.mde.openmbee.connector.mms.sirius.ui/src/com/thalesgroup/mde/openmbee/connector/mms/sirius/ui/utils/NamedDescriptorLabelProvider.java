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

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSNamedDescriptor;

public class NamedDescriptorLabelProvider extends IdentifiableDescriptorLabelProvider {
	@Override
	public String getText(Object element) {
		if(element instanceof MMSNamedDescriptor) {
			MMSNamedDescriptor desc = (MMSNamedDescriptor) element;
			return String.format("%s [%s]", desc.name, desc.id); //$NON-NLS-1$
		}
		return super.getText(element);
	}
}
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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui.data;

import org.eclipse.core.runtime.IAdaptable;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSIdentifiableDescriptor;

public class MMSTreeObject implements IAdaptable {
	protected MMSTreeParent parent;
	protected MMSIdentifiableDescriptor mmsObject;
	
	public MMSTreeObject(MMSIdentifiableDescriptor mmsObject) {
		this.mmsObject = mmsObject;
	}
	
	public void setParent(MMSTreeParent parent) {
		this.parent = parent;
	}
	
	public MMSTreeParent getParent() {
		return parent;
	}
	
	public MMSIdentifiableDescriptor getMmsObject() {
		return mmsObject;
	}
	
	@Override
	public <T> T getAdapter(Class<T> key) {
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof MMSTreeObject ? mmsObject.id.contentEquals(((MMSTreeObject)obj).mmsObject.id) : false;
	}
}
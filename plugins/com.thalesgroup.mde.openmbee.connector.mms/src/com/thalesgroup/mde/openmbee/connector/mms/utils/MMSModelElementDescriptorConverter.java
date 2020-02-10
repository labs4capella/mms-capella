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

import java.util.List;
import java.util.Map;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSModelElementDescriptor;

public interface MMSModelElementDescriptorConverter<T0, T1> {
	public Map<MMSModelElementDescriptor, T0> fromMMS(List<MMSModelElementDescriptor> meds, T1 optionalT0Store);
	public Map<T0, MMSModelElementDescriptor> toMMS(List<T0> tObjects, String featurePrefix);
}

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

import java.util.List;

public class MMSCompartmentDescriptor {
	public String commitId;
	public String refId;
	public String projectId;
	public String orgId;
	
	public MMSCompartmentDescriptor(List<String> segments) {
		commitId = segments.get(8);
		refId = segments.get(6);
		projectId = segments.get(4);
		orgId = segments.get(2);
	}
	
	public static MMSCompartmentDescriptor fromCompartmentURIOrNull(String compartmentURI) {
		return null;
	}
}

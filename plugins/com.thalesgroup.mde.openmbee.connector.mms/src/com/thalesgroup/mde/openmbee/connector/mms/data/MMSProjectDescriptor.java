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

import java.util.ArrayList;
import java.util.List;

public class MMSProjectDescriptor extends MMSNamedDescriptor {
	public static final String FEATURE_PREFIX__EMF = "EMF_FEATURE__"; //$NON-NLS-1$
	public static final String FEATURE_PREFIX = "featurePrefix"; //$NON-NLS-1$
	public static final String CLIENT_SIDE_NAME = "clientSideName"; //$NON-NLS-1$
	public String _refId;
	public String _elasticId;
	public Boolean _editable;
	public String type = MMSConstants.MMS_TYPE_PROJECT;
	public String _created;
	public String _modified;
	public String _projectId;
	public String categoryId;
	public String _creator;
	public String twcId;
	public String _modifier;
	public String _uri;
	public String _qualifiedName;
	public String _qualifiedId;
	public String orgId; // MMS3.x
	public String org; // MMS4
	public String featurePrefix;
	public String clientSideName;
	public List<String> _inRefIds = new ArrayList<>();
}

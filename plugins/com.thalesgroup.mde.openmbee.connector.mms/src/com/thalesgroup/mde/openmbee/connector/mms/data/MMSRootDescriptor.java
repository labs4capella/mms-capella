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

public class MMSRootDescriptor {
	public List<MMSOrganizationDescriptor> orgs = new ArrayList<>();
	public List<MMSProjectDescriptor> projects = new ArrayList<>();
	public List<MMSRefDescriptor> refs = new ArrayList<>();
	public MMSDataDescriptor data;
	public String token; // MMS4
	public List<MMSModelElementDescriptor> elements = new ArrayList<>();
	/**
	 * In case of committing elements
	 */
	public String comment;
	/**
	 * In case of committing elements
	 */
	public String source;
	/**
	 * In case of querying commits
	 */
	public List<MMSCommitDescriptor> commits = new ArrayList<>();
}
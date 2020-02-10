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

public class MMSRefDescriptor extends MMSNamedDescriptor {
	public String description;
	public String parentCommitId;
	public String parentRefId;
	public String status;
	public String twcId;
    public String type;
    
    public static final String TYPE__BRANCH = "Branch"; //$NON-NLS-1$
    
    /**
     * Automatic attribute, do not set it manually.
     */
    public String _elasticId;
    /**
     * Automatic attribute, do not set it manually.
     */
    public String _projectId;
    /**
     * Automatic attribute, do not set it manually.
     */
    public String _modifier;
    /**
     * Automatic attribute, do not set it manually.
     */
    public String _modified;
    /**
     * Automatic attribute, do not set it manually.
     */
    public String _creator;
    /**
     * Automatic attribute, do not set it manually.
     */
    public String _created;
    /**
     * Automatic attribute, do not set it manually.
     */
    public String _commitId;

}

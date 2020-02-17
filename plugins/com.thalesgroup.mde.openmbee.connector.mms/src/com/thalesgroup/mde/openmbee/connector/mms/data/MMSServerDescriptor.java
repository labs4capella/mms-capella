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

public class MMSServerDescriptor extends MMSNamedDescriptor {

	public static final String API_VERSION_3 = "MMS 3.x"; //$NON-NLS-1$
	public static final String API_VERSION_4 = "MMS 4"; //$NON-NLS-1$

	public MMSServerDescriptor(String url, String version, String authData) {
		this(url, url, version, authData);
	}
	
	public MMSServerDescriptor(String name, String url, String version, String autData) {
		this.id = url+autData;
		this.url = url;
		this.name = name;
		this.apiVersion = version;
		this.autData = autData;
	}
	
	public String url;
	public String apiVersion;
	public String autData;
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof MMSServerDescriptor ? this.id.contentEquals(((MMSServerDescriptor)obj).id) : false;
	}
}

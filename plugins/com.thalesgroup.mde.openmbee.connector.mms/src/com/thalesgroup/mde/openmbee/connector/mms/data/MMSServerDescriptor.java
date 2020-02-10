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
	
	public MMSServerDescriptor(String url, String authData) {
		this(url, url, authData);
	}
	
	public MMSServerDescriptor(String name, String url, String autData) {
		this.id = url+autData;
		this.url = url;
		this.name = name;
		this.autData = autData;
	}
	
	public String url;
	public String autData;
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof MMSServerDescriptor ? this.id.contentEquals(((MMSServerDescriptor)obj).id) : false;
	}
}

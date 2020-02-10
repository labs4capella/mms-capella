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
package com.thalesgroup.mde.openmbee.connector.mms.ui;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

public abstract class TextVerifyListener implements VerifyListener {

	protected final String getNewTextValue(VerifyEvent e) {
		String oldTextValue = ((Text)e.widget).getText();
		String newTextValue = oldTextValue.substring(0, e.start) + e.text + oldTextValue.substring(e.end);
		return newTextValue;
	}
}
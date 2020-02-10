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

import java.util.Collection;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

public abstract class VariableContentProposalProvider implements IContentProposalProvider {
	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		String searched = contents.substring(0, position);
		Collection<String> props = getPossibleValues();
		return props.stream().filter(prop -> prop.startsWith(searched))
								.map(str -> new ContentProposal(str))
								.toArray(ContentProposal[]::new);
	}

	protected abstract Collection<String> getPossibleValues();
}
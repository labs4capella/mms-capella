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
package com.thalesgroup.mde.openmbee.connector.mms.sirius.ui;

public class ExecutionResult {
	public static enum Result { OK, WARNING, ERROR }
	public final ExecutionResult.Result result;
	private String wizardMessage;
	private String logMessage;
	private long executionTime = -1;
	private Exception problemCause;
	
	private ExecutionResult(ExecutionResult.Result result, String logMessage, String wizardMessage, Exception problemCause) {
		this.result = result;
		this.logMessage = logMessage;
		this.wizardMessage = wizardMessage;
		this.problemCause = problemCause;
	}
	
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	
	public long getExecutionTime() {
		return executionTime;
	}
	
	public String getWizardMessage() {
		return wizardMessage;
	}
	
	public String getLogMessage() {
		return logMessage;
	}
	
	public Exception getProblemCause() {
		return problemCause;
	}
	
	public static ExecutionResult ok() {
		return new ExecutionResult(Result.OK, "", "", null); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static ExecutionResult ok(String logMessage) {
		return new ExecutionResult(Result.OK, logMessage, "", null); //$NON-NLS-1$
	}
	
	public static ExecutionResult error() {
		return new ExecutionResult(Result.ERROR, "", "", null); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static ExecutionResult error(String logMessage) {
		return new ExecutionResult(Result.ERROR, logMessage, "", null); //$NON-NLS-1$
	}
	
	public static ExecutionResult error(String logMessage, String wizardMessage, Exception problemCause) {
		return new ExecutionResult(Result.ERROR, logMessage, wizardMessage, problemCause);
	}
	
	public static ExecutionResult warning() {
		return new ExecutionResult(Result.WARNING, "", "", null); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static ExecutionResult warning(String logMessage) {
		return new ExecutionResult(Result.WARNING, logMessage, "", null); //$NON-NLS-1$
	}
}
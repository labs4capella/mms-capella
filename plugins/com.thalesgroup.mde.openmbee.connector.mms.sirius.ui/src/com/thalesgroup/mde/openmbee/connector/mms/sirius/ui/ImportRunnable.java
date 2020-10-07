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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.thalesgroup.mde.openmbee.connector.mms.sirius.SiriusProjectConnector;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.data.MmsProjectImportData;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.utils.ProjectAlreadyExistsException;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.utils.ProjectCreationException;

public class ImportRunnable implements IRunnableWithProgress {
	private static final String MSG__MISSING_DATA = "Missing connection data."; //$NON-NLS-1$
	private static final String MSG__UNEXPECTED_ERROR_DETAILED = "Unexpected exception while trying to import project '%s' from MMS '%s' under organization '%s'"; //$NON-NLS-1$
	private static final String MSG__UNEXPECTED_ERROR = "Unexpected error. See Error Log view for details."; //$NON-NLS-1$
	private static final String MSG__PROJECT_CANNOT_BE_CREATED_DETAILED = "The project '%s' cannot be imported from MMS '%s' under from organization '%s'"; //$NON-NLS-1$
	private static final String MSG__PROJECT_CANNOT_BE_CREATED = "The local project cannot be created. See Error Log view for details."; //$NON-NLS-1$
	private static final String MSG__PROJECT_ALREADY_EXISTS = "The project already exists in the workspace."; //$NON-NLS-1$
	private static final String MSG__UNSUCCESSFUL_IMPORT = "Cannot import the project."; //$NON-NLS-1$
	private static final String MSG__SUCCESSFUL_IMPORT = "Imported project '%s' from '%s' under the organization '%s' in %d ms"; //$NON-NLS-1$
	private final AtomicReference<ExecutionResult> success;
	private final MmsProjectImportData connectionData;
	private final String projectName;
	private final Consumer<String> errorDisplay;

	public ImportRunnable(AtomicReference<ExecutionResult> success,
							MmsProjectImportData connectionData, 
							String projectName,
							Consumer<String> errorDisplay) {
		this.success = success;
		this.connectionData = connectionData;
		this.projectName = projectName;
		this.errorDisplay = errorDisplay;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		ExecutionResult result = executeImport(connectionData, projectName, monitor);
		success.set(result);
		if(result.result == ExecutionResult.Result.ERROR && result.getWizardMessage() != null) {
			errorDisplay.accept(result.getWizardMessage());
		}
		switch(result.result) {
			case OK: {
				MmsSiriusConnectorUiPlugin.getDefault().getLogger().info(result.getLogMessage());
				break;
			}
			case WARNING: {
				MmsSiriusConnectorUiPlugin.getDefault().getLogger().warn(result.getLogMessage());
				break;
			}
			case ERROR: {
				if(result.getProblemCause() == null)
					MmsSiriusConnectorUiPlugin.getDefault().getLogger().error(result.getLogMessage());
				else
					MmsSiriusConnectorUiPlugin.getDefault().getLogger().error(result.getLogMessage(), result.getProblemCause());
				break;
			}
		}
		monitor.done();
	}
	
	private ExecutionResult executeImport(MmsProjectImportData connectionData, String projectName, IProgressMonitor monitor) throws InterruptedException {
		boolean success = false;
		String logMessage = null;
		String wizardMessage = null;
		Exception problemCause = null;
		long startTime = System.currentTimeMillis();
		long endTime = -1;
		
		if(connectionData != null) {
			try {
				SiriusProjectConnector projectConnector = new SiriusProjectConnector();
				monitor.beginTask("Import from MMS", projectConnector.getNumberOfSubTaskOfFromMms()); //$NON-NLS-1$
				success = projectConnector.fromMms(connectionData.serverUrl,
													connectionData.apiVersion,
													connectionData.autData,
													connectionData.orgId,
													connectionData.projectId, 
													connectionData.refId,
													connectionData.commitId,
													projectName,
													monitor);
				endTime = System.currentTimeMillis();
				if(success) {
					logMessage = String.format(MSG__SUCCESSFUL_IMPORT, 
											connectionData.projectId, 
											connectionData.serverUrl, 
											connectionData.orgId,
											endTime - startTime);
				} else {
					wizardMessage = MSG__UNSUCCESSFUL_IMPORT;
				}
			} catch(ProjectAlreadyExistsException e) {
				wizardMessage = MSG__PROJECT_ALREADY_EXISTS;
			} catch(ProjectCreationException e) {
				wizardMessage = MSG__PROJECT_CANNOT_BE_CREATED;
				logMessage = String.format(MSG__PROJECT_CANNOT_BE_CREATED_DETAILED,
											connectionData.projectId,
											connectionData.serverUrl,
											connectionData.orgId);
			} catch(InterruptedException e) {
				throw e;
			} catch (Exception e) {
				wizardMessage = MSG__UNEXPECTED_ERROR;
				logMessage = String.format(MSG__UNEXPECTED_ERROR_DETAILED,
											connectionData.projectId,
											connectionData.serverUrl,
											connectionData.orgId);
				problemCause = e;
			}
		} else {
			logMessage = MSG__MISSING_DATA;
			wizardMessage = logMessage;
		}
		
		if(endTime < startTime) {
			endTime = System.currentTimeMillis();
		}
		
		ExecutionResult res = success ? ExecutionResult.ok(logMessage) : ExecutionResult.error(logMessage, wizardMessage, problemCause);
		res.setExecutionTime(endTime-startTime);
		
		return res;
	}
}
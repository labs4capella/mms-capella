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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.ui.IWorkbench;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.SiriusProjectConnector;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.data.MmsProjectCommitData;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.pages.MmsProjectCommitWizardPage;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.ui.pages.ProjectSelectorWizardPage;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper;

public class CapellaMmsExportWizard extends ResultPageOwnerWizard {
	private ProjectSelectorWizardPage projectSelection;
	private MmsProjectCommitWizardPage mmsConnection;
	private IProject selectedProject;
	private final IPageChangingListener projectPather = new IPageChangingListener() {
		
		@Override
		public void handlePageChanging(PageChangingEvent event) {
			if(event.getCurrentPage() == projectSelection && event.getTargetPage() == mmsConnection) {
				mmsConnection.setProjectName(projectSelection.getSelectedProject().getName());
			}
		}
	};

	public CapellaMmsExportWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages() {
		projectSelection = new ProjectSelectorWizardPage();
		mmsConnection = new MmsProjectCommitWizardPage();
		addPage(projectSelection);
		addPage(mmsConnection);

		if(selectedProject != null) {
			projectSelection.setSelectedProject(selectedProject);
		}
		
	}

	@Override
	public String getWindowTitle() {
		return "Export Capella Model to MMS"; //$NON-NLS-1$
	}
	
	protected WizardPage getPageBeforeResult() {
		return mmsConnection;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if(selection instanceof StructuredSelection) {
			if(((StructuredSelection)selection).size() == 1) {
				Object selected = ((StructuredSelection)selection).getFirstElement();
				if(selected instanceof IResource) {
					selectedProject = ((IResource) selected).getProject();
				}
			}
		}
	}
	
	@Override
	public void setContainer(IWizardContainer newWizardContainer) {
		initializePageChangingListener(newWizardContainer, projectPather);
		super.setContainer(newWizardContainer);
	}

	protected ExecutionResult executeActionAndUpdateUI() {
		AtomicReference<ExecutionResult> success = new AtomicReference<>();
		AtomicReference<MmsProjectCommitData> connectionData = new AtomicReference<>();
		AtomicReference<IProject> selectedProject = new AtomicReference<>();
		
		try {
			connectionData.set(mmsConnection.getConnectionData());
			selectedProject.set(projectSelection.getSelectedProject());
		} catch (Exception e) {}
		
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					ExecutionResult result = executeExport(selectedProject.get(), connectionData.get(), monitor);
					success.set(result);
					if(result.result == ExecutionResult.Result.ERROR && result.getWizardMessage() != null) {
						getShell().getDisplay().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								mmsConnection.setErrorMessage(result.getWizardMessage());
							}
						});
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
			});
		} catch (InterruptedException e) {
			getShell().getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					mmsConnection.setErrorMessage(e.getMessage());
				}
			});
		} catch (InvocationTargetException e) {
			MmsSiriusConnectorUiPlugin.getDefault().getLogger().error(e);
		}
		if(success.get() != null && success.get().result != ExecutionResult.Result.ERROR) {
			MmsSiriusConnectorUiPlugin.getDefault().addStoredMmsUrl(connectionData.get().serverUrl);
			MmsSiriusConnectorUiPlugin.getDefault().addStoredMmsUser(mmsConnection.getUser());
			MmsSiriusConnectorUiPlugin.getDefault().savePreferences();
		}
		
		return success.get();
	}

	private ExecutionResult executeExport(IProject selectedProject, MmsProjectCommitData connectionData, IProgressMonitor monitor) throws InterruptedException {
		boolean success = false;
		String logMessage = null;
		String wizardMessage = null;
		Exception problemCause = null;
		long startTime = System.currentTimeMillis();
		long endTime = -1;
		
		if(connectionData != null && selectedProject != null) {
			try {
				MMSServerHelper serverHelper = new MMSServerHelper(connectionData.serverUrl, connectionData.apiVersion, connectionData.autData);
				SiriusProjectConnector projectConnector = new SiriusProjectConnector();
				monitor.beginTask("Export to MMS", 2+projectConnector.getNumberOfSubTaskOfToMms()); //$NON-NLS-1$
				
				String organizationId;
				String projectId;
				String projectFeaturePrefix;
				String refId;
				monitor.subTask("Prepare project on server"); //$NON-NLS-1$
				// prepare project
				if(connectionData.newProject) {
					// create if necessary
					MMSProjectDescriptor project = serverHelper.createProject(connectionData.orgId, 
														connectionData.projectId,
														connectionData.projectServerName, 
														selectedProject.getName());
					organizationId = project.orgId;
					projectId = project.id;
					projectFeaturePrefix = project.featurePrefix;
					MMSRefDescriptor ref = serverHelper.getOrCreateBranch(project.orgId, project.id, MMSServerHelper.MMS_REF__DEFAULT);
					refId = ref.id;
				} else {
					// otherwise use the caught data
					organizationId = connectionData.orgId;
					projectId = connectionData.projectId;
					projectFeaturePrefix = connectionData.projectFeaturePrefix;
					refId = connectionData.refId;
				}
				monitor.worked(1);
				
				// collect convertible resources
				if(monitor.isCanceled()) {
					throw new InterruptedException(SiriusProjectConnector.MESSAGE_CANCELLATION_EXPORT);
				}
				monitor.subTask("Collect resources"); //$NON-NLS-1$
				List<Resource> siriusModelFiles = getSiriusModelFilesOfProject(selectedProject);
				monitor.worked(1);
				
				// convert project and upload it to the prepared mms project
				if(monitor.isCanceled()) {
					throw new InterruptedException(SiriusProjectConnector.MESSAGE_CANCELLATION_EXPORT);
				}
				if(siriusModelFiles.size()>1) {
					success = projectConnector.toMms(connectionData.serverUrl,
														connectionData.apiVersion,
														connectionData.autData,
														organizationId,
														projectId, 
														refId != null ? refId : MMSServerHelper.MMS_REF__DEFAULT,
														siriusModelFiles,
														connectionData.commitComment,
														projectFeaturePrefix,
														monitor);
					endTime = System.currentTimeMillis();
					if(success) {
						logMessage = String.format("Exported project '%s' to '%s' under the organization '%s' with the id '%s' in %d ms.", //$NON-NLS-1$
								selectedProject.getName(), 
								connectionData.serverUrl, 
								connectionData.orgId,
								projectId,
								endTime - startTime);
					} else {
						wizardMessage = "Cannot export the project"; //$NON-NLS-1$
					}
				} else {
					wizardMessage = "No model files can be found. Check that the model has been opened."; //$NON-NLS-1$
				}
				
			} catch(InterruptedException e) {
				throw e;
			} catch (Exception e) {
				problemCause = e;
				wizardMessage = "Unexpected error while trying to export project. See Error Log view for details."; //$NON-NLS-1$
				logMessage = String.format("Unexpected exception while trying to export project '%s' to MMS '%s' under organization '%s'", //$NON-NLS-1$
						selectedProject.getName(),
						connectionData.serverUrl,
						connectionData.orgId
				);
			}
		} else {
			logMessage = String.format("Missing data: %s%s%s", //$NON-NLS-1$
					(selectedProject == null ? "project" : ""), //$NON-NLS-1$ //$NON-NLS-2$
					((selectedProject == null && connectionData == null) ? " and " : ""), //$NON-NLS-1$ //$NON-NLS-2$
					(connectionData == null ? "connection data" : "")); //$NON-NLS-1$ //$NON-NLS-2$
			wizardMessage = logMessage;
		}
		
		if(endTime < startTime) {
			endTime = System.currentTimeMillis();
		}
		
		ExecutionResult res = success ? ExecutionResult.ok(logMessage) : ExecutionResult.error(logMessage, wizardMessage, problemCause);
		res.setExecutionTime(endTime-startTime);
		
		return res;
	}
	
	private List<Resource> getSiriusModelFilesOfProject(IProject selectedProject) {
		List<Resource> resources = new ArrayList<>();
		try {
			Optional<IResource> siriusFile = Arrays.stream(selectedProject.members())
													.filter(r -> "aird".contentEquals(r.getFileExtension())) //$NON-NLS-1$
													.findFirst();
			
			if(siriusFile.isPresent()) {
				SessionManager sm = SessionManager.INSTANCE;
				URI sessionResourceURI = URI.createPlatformResourceURI(String.format("/%s/%s", //$NON-NLS-1$
																				selectedProject.getName(), 
																				siriusFile.get().getName()), 
																		true);
				Session session = sm.getExistingSession(sessionResourceURI);
				if(session != null) {
					resources.addAll(session.getSemanticResources());
					resources.add(session.getSessionResource());
				}
			}
		} catch (CoreException e) {
			MmsSiriusConnectorUiPlugin.getDefault().getLogger().error(e);
			resources.clear();
		}
		return resources;
	}

}

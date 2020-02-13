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
package com.thalesgroup.mde.openmbee.connector.mms.sirius;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FilesystemPackage;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSConstants;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSModelElementDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.utils.ProjectCreationException;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper;

public class SiriusProjectConnector {
	private static final String MESSAGE__PROJECT_CANNOT_BE_DELETED = "Project '%s' cannot be deleted"; //$NON-NLS-1$
	
	private static final String MESSAGE__DELETE_COMMIT = "%s"+System.lineSeparator()+"Deletion part"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String MESSAGE__UPDATE_COMMIT = "%s"+System.lineSeparator()+"Addition/Update part"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String MESSAGE__UNSUCCESSFUL_DELETE = "Deletion part of the commit cannot be executed."; //$NON-NLS-1$
	/**
	 * Message to show project corruption. It needs three parameters: project, branch and server identifier.
	 */
	private static final String MESSAGE__PROJECT_CORRUPTION = "No elements can be found for the project '%s' at the branch '%s'. Maybe it has been corrupted on the server '%s'."; //$NON-NLS-1$
	private static final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
	private static final String FILTER_TEMPLATE__HOLDING_BIN  = "holding_bin_%s"; //$NON-NLS-1$
	private static final String FILTER_TEMPLATE__VIEW_INSTANCES_BIN  = "view_instances_bin_%s"; //$NON-NLS-1$
	public static final String MESSAGE_CANCELLATION_EXPORT = "MMS export cancelled. Probably some elements have already been created on the server."; //$NON-NLS-1$
	public static final String MESSAGE_CANCELLATION_IMPORT = "MMS import cancelled."; //$NON-NLS-1$
	
	public boolean toMms(String baseUrl, String autData, String organizationId, String projectId, String branchId, Collection<Resource> resources, String featurePrefix) throws InterruptedException {
		return toMms(baseUrl, autData, organizationId, projectId, branchId, resources, "", featurePrefix); //$NON-NLS-1$
	}
	
	public boolean toMms(String baseUrl, String autData, String organizationId, String projectId, String branchId, Collection<Resource> resources, String commitComment, String featurePrefix) throws InterruptedException {
		return toMms(baseUrl, autData, organizationId, projectId, branchId, resources, commitComment, featurePrefix, new NullProgressMonitor());
	}
	
	public boolean toMms(String baseUrl, String autData, String organizationId, String projectId, String branchId, Collection<Resource> resources, String commitComment, String featurePrefix, IProgressMonitor monitor) throws InterruptedException {
		try {
			// Convert structure
			startNewSubTaskIfNotCancelled(monitor, "Convert project structure", MESSAGE_CANCELLATION_EXPORT); //$NON-NLS-1$
			ProjectStructureConverter structureConverter = new ProjectStructureConverter();
			Map<Resource, IFile> files = resources.stream().collect(Collectors.toMap(r -> r, r -> getFile(r)));
			Map<IResource, MMSModelElementDescriptor> convertedFiles = 
					structureConverter.toMMS(baseUrl, autData, projectId, branchId, featurePrefix, files.values());
			monitor.worked(1);
			
			// Convert model elements
			startNewSubTaskIfNotCancelled(monitor, "Convert model elements", MESSAGE_CANCELLATION_EXPORT); //$NON-NLS-1$
			EmfMmsModelElementDescriptorConverter modelConverter = new EmfMmsModelElementDescriptorConverter();
			List<EObject> convertibles = new ArrayList<>();
			resources.stream().forEach(r -> r.getAllContents().forEachRemaining(eo -> convertibles.add(eo)));
			Map<EObject, MMSModelElementDescriptor> convertedModelElements = modelConverter.toMMS(convertibles, featurePrefix);
			monitor.worked(1);
			
			// Connect the representations of root model elements to the representations of structure elements
			for (Resource res : resources) {
				String ownerId = convertedFiles.get(files.get(res)).id;
				for(EObject root : res.getContents()) {
					convertedModelElements.get(root).msiDescriptor.ownerId = ownerId; 
				}
			}
			monitor.worked(1);
			
			
			// Upload
			startNewSubTaskIfNotCancelled(monitor, "Upload model to MMS (cannot be cancelled)", MESSAGE_CANCELLATION_EXPORT); //$NON-NLS-1$
			MMSServerHelper serverHelper = new MMSServerHelper(baseUrl, autData);
			List<MMSModelElementDescriptor> structureWithModelElements = Stream.concat(convertedFiles.values().stream(), 
					convertedModelElements.values().stream())
					.collect(Collectors.toList());
			Set<String> idsOfStructureWithModelElements = structureWithModelElements.stream().map(med -> med.id).collect(Collectors.toSet());
			List<String> filteredElements = Arrays.asList(projectId, 
														String.format(FILTER_TEMPLATE__HOLDING_BIN, projectId), 
														String.format(FILTER_TEMPLATE__VIEW_INSTANCES_BIN, projectId));
			// Calculate the removable elements
			List<MMSModelElementDescriptor> removables = serverHelper.getModelElements(organizationId, projectId, branchId).stream()
																	.filter(med -> {
																		// filter out the base elements
																		if(filteredElements.contains(med.id)) {
																			return false;
																		}
																		// If something is not available in the current elements it needs to be removed
																		return !idsOfStructureWithModelElements.contains(med.id);
																	}).collect(Collectors.toList());
			if(removables.size() > 0) {
				// Delete the locally removed elements from the server
				if(!serverHelper.removeModelElements(organizationId, projectId, branchId, String.format(MESSAGE__DELETE_COMMIT, commitComment), removables)) {
					throw new InterruptedException(MESSAGE__UNSUCCESSFUL_DELETE);
				}
			}
			monitor.worked(1);
			
			List<MMSModelElementDescriptor> createdOnServer = 
					serverHelper.createModelElements(projectId, 
													branchId, 
													removables.size() > 0 ? String.format(MESSAGE__UPDATE_COMMIT, commitComment) : commitComment, 
													structureWithModelElements);
			monitor.worked(1);
			
			return createdOnServer.size() > 0 || structureWithModelElements.size() == 0;
		} catch(MMSServerHelper.MMSConnectionException e) {
			MmsSiriusConnectorPlugin.getDefault().getLogger().error(e);
		}
		
		return false;
	}
	
	public int getNumberOfSubTaskOfToMms() {
		return 5;
	}

	private void startNewSubTaskIfNotCancelled(IProgressMonitor monitor, String subTaskName, String interruptionMessage) throws InterruptedException {
		if(monitor.isCanceled()) {
			throw new InterruptedException(interruptionMessage);
		}
		monitor.subTask(subTaskName);
	}
	
	private IFile getFile(Resource res) {
		return wsRoot.getFile(new Path(res.getURI().toPlatformString(true)));
	}
	
	public boolean fromMms(String baseUrl, String autData, String projectId, String branchId) throws InterruptedException {
		return fromMms(baseUrl, autData, projectId, branchId, null, null, new NullProgressMonitor());
	}
	
	public boolean fromMms(String baseUrl, String autData, String projectId, String branchId, String commitId, String projectName, IProgressMonitor monitor) throws InterruptedException {
		boolean success = false;
		List<MMSModelElementDescriptor> elements = null;
		MMSServerHelper serverHelper = new MMSServerHelper(baseUrl, autData);
		try {
			startNewSubTaskIfNotCancelled(monitor, "Download data from MMS", MESSAGE_CANCELLATION_IMPORT); //$NON-NLS-1$
			String holdingBinFilter = String.format(FILTER_TEMPLATE__HOLDING_BIN, projectId);
			String viewInstancesBinFilter = String.format(FILTER_TEMPLATE__VIEW_INSTANCES_BIN, projectId);
			elements = serverHelper.getModelElements(projectId, branchId, commitId);
			elements = elements.stream().filter(e -> !holdingBinFilter.contentEquals(e.id) && !viewInstancesBinFilter.contentEquals(e.id) ).collect(Collectors.toList());
			monitor.worked(1);
		} catch (MMSServerHelper.MMSConnectionException e) {
			MmsSiriusConnectorPlugin.getDefault().getLogger().error(e);
		}
		if(elements != null) {
			startNewSubTaskIfNotCancelled(monitor, "Search project structure descriptors", MESSAGE_CANCELLATION_IMPORT); //$NON-NLS-1$
			MMSModelElementDescriptor projectDescriptor = elements.stream().filter(e -> projectId.contentEquals(e.id)).findFirst().orElse(null);
			if(projectDescriptor != null) {
				elements.remove(projectDescriptor);
			} else {
				try {
					projectDescriptor = serverHelper.getModelElement(projectId, branchId, projectId, null).get(0);
				} catch (IndexOutOfBoundsException e) {
					throw new InterruptedException("Project descriptor cannot be found on the server."); //$NON-NLS-1$
				}
			}
			String featurePrefix = findFirstFeatureValue(projectDescriptor, MMSProjectDescriptor.FEATURE_PREFIX).orElse("").toString(); //$NON-NLS-1$
			String projectNameInWorkspace = projectName != null ? projectName :
													findFirstFeatureValue(projectDescriptor, MMSProjectDescriptor.CLIENT_SIDE_NAME)
														.orElse(findFirstFeatureValue(projectDescriptor, "name") //$NON-NLS-1$
																.orElse(projectDescriptor.msiDescriptor.id)).toString();
			
			List<MMSModelElementDescriptor> structureDescriptors = elements.stream()
							.filter(e -> e.msiDescriptor.emfNsUri != null && FilesystemPackage.eNS_URI.contentEquals(e.msiDescriptor.emfNsUri))
							.sorted((a,b) -> a.id.contentEquals(b.msiDescriptor.ownerId) ? -1 : (b.id.contentEquals(a.msiDescriptor.ownerId) ? 1 : 0) )
							.collect(Collectors.toList());
			monitor.worked(1);
			
			startNewSubTaskIfNotCancelled(monitor, "Create local project structure", MESSAGE_CANCELLATION_IMPORT); //$NON-NLS-1$
			ProjectStructureConverter structureConverter = new ProjectStructureConverter();
			Map<String, Resource> resourcesForIds = new HashMap<>();
			ResourceSetImpl rs = new ResourceSetImpl();
			Map<MMSModelElementDescriptor, IResource> iresources = structureConverter.fromMMS(structureDescriptors, rs, projectNameInWorkspace, resourcesForIds, featurePrefix);
			if(iresources.size() < 1) {
				throw new ProjectCreationException(String.format(MESSAGE__PROJECT_CORRUPTION, projectId, branchId, baseUrl));
			}
			IProject newProject = iresources.values().stream().findFirst().orElse(null).getProject();
			monitor.worked(1);
			
			try {
				startNewSubTaskIfNotCancelled(monitor, "Create model data", MESSAGE_CANCELLATION_IMPORT); //$NON-NLS-1$
				List<MMSModelElementDescriptor> modelElementDescriptors = elements.stream().filter(e -> 
									!MMSConstants.MMS_TYPE_FILE.contentEquals(e.msiDescriptor.type) && 
									!MMSConstants.MMS_TYPE_FOLDER.contentEquals(e.msiDescriptor.type))
								.sorted((a,b) -> a.id.contentEquals(b.msiDescriptor.ownerId) ? -1 : (b.id.contentEquals(a.msiDescriptor.ownerId) ? 1 : 0) )
								.collect(Collectors.toList());
				EmfMmsModelElementDescriptorConverter modelConverter = new EmfMmsModelElementDescriptorConverter();
				Map<MMSModelElementDescriptor, EObject> eos = modelConverter.fromMMS(modelElementDescriptors, rs, resourcesForIds, featurePrefix);
				
				success = modelElementDescriptors.size() < 3 || eos.size() > 0;
				monitor.worked(1);
				
				// cleanup the unnecessary new files and folders if the import wasn't successful
				if(!success) {
					removeProject(newProject);
				}
			} catch(Exception e) {
				removeProject(newProject);
				throw e;
			}
		}
		
		return success;
	}
	
	public int getNumberOfSubTaskOfFromMms() {
		return 4;
	}

	private Optional<Object> findFirstFeatureValue(MMSModelElementDescriptor modelElementDescriptor, final String featureKey) {
		return modelElementDescriptor.msiDescriptor.attributes.entrySet().stream()
																.filter(e -> featureKey.contentEquals(e.getKey()))
																.map(e -> e.getValue()).findFirst();
	}

	private void removeProject(IProject project) {
		try {
			project.delete(true, true, new NullProgressMonitor());
		} catch (CoreException e) {
			MmsSiriusConnectorPlugin.getDefault().getLogger().error(String.format(MESSAGE__PROJECT_CANNOT_BE_DELETED, project.getName()), e);
		}
	}
}

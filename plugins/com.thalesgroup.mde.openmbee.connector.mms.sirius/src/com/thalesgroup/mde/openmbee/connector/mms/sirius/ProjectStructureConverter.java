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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FilesystemPackage;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSConstants;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSModelElementDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.utils.ProjectAlreadyExistsException;
import com.thalesgroup.mde.openmbee.connector.mms.sirius.utils.ProjectCreationException;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper;

public class ProjectStructureConverter {
	public static final String FILE_CONTENT = FilesystemPackage.eINSTANCE.getFile_Content().getName();
	public static final String FILE_PATH = FilesystemPackage.eINSTANCE.getFileSystemElement_Path().getName();
	public static final String FILE_NAME = FilesystemPackage.eINSTANCE.getFileSystemElement_Name().getName();
	private static final IWorkspace ws = ResourcesPlugin.getWorkspace();

	public Map<MMSModelElementDescriptor, IResource> fromMMS(Collection<MMSModelElementDescriptor> meds, ResourceSet rs, String projectName, String featurePrefix) {
		return fromMMS(meds, rs, projectName, new HashMap<>(), featurePrefix);
	}
	
	public Map<MMSModelElementDescriptor, IResource> fromMMS(Collection<MMSModelElementDescriptor> meds, ResourceSet rs, String projectName, Map<String, Resource> resourcesForIds, String featurePrefix) {
		Map<MMSModelElementDescriptor, IResource> convertedFiles = new HashMap<>();
		if(rs == null) rs = new ResourceSetImpl();
		if(resourcesForIds == null) resourcesForIds = new HashMap<>();
		List<MMSModelElementDescriptor> resourceDescriptors = 
				meds.stream().filter(med -> 
							med.msiDescriptor.emfNsUri != null && FilesystemPackage.eNS_URI.contentEquals(med.msiDescriptor.emfNsUri)
						).collect(Collectors.toList());
		List<MMSModelElementDescriptor> modelFileDescriptors = new ArrayList<>();
		if(resourceDescriptors.size()>0) {
			String fileNameFeatureId = featurePrefix+FILE_NAME;
			String filePathFeatureId = featurePrefix+FILE_PATH;
			String fileContentFeatureId = featurePrefix+FILE_CONTENT;
			Map<MMSModelElementDescriptor, String> filesToResolve = new HashMap<>();
			if(projectName == null || projectName.isEmpty()) {
				String p = resourceDescriptors.get(0).msiDescriptor.attributes.get(filePathFeatureId).toString();
				projectName = p.startsWith("/") ? p.substring(1) : p; //$NON-NLS-1$
				projectName = projectName.contains("/") ? projectName.substring(0, projectName.indexOf("/")) : projectName; //$NON-NLS-1$ //$NON-NLS-2$
			}
			IWorkspaceRoot wsroot = ws.getRoot();
			if(wsroot.getProject(projectName).exists()) {
				throw new ProjectAlreadyExistsException(String.format("Project '%s' already exists.", projectName)); //$NON-NLS-1$
			}
			String projectFolderPath= wsroot.getLocation().toOSString()+File.separator+projectName;
			File projectFolder = new File(projectFolderPath);
			try {
				Files.createDirectory(projectFolder.toPath());
			} catch (IOException | SecurityException e) {
				new ProjectCreationException(String.format("The project '%s' cannot be created", projectName), e); //$NON-NLS-1$
			}
			projectFolderPath = projectFolderPath+File.separator;
			for (MMSModelElementDescriptor resD : resourceDescriptors) {
				String name = resD.msiDescriptor.attributes.get(fileNameFeatureId).toString();
				Object content = resD.msiDescriptor.attributes.get(fileContentFeatureId);
				if(content != null) {
					String path = projectFolderPath+name;
					File file = new File(path);
					if(content instanceof String) {
						// create text file
						try(FileWriter fw = new FileWriter(file)) {
							file.createNewFile();
							fw.write((String) content);
						} catch (IOException e) {
							throw new ProjectCreationException(String.format("The '%s' file cannot be created for the '%s' project", name, projectName), e); //$NON-NLS-1$
						}
					} else if(content instanceof byte[]) {
						// create binary file
						try(FileOutputStream os = new FileOutputStream(file)) {
							file.createNewFile();
							os.write((byte[]) content);
						} catch (IOException e) {
							throw new ProjectCreationException(String.format("The '%s' file cannot be created for the '%s' project", name, projectName), e); //$NON-NLS-1$
						}
					}
					filesToResolve.put(resD, path);
				} else {
					modelFileDescriptors.add(resD);
				}
			}
			IProject project = wsroot.getProject(projectName);
			NullProgressMonitor npm = new NullProgressMonitor();
			try {
				if(!project.exists()) {
					project.create(npm);
				}
				if(!project.isOpen()) {
					project.open(npm);
				}
			} catch (CoreException e) {
				throw new ProjectCreationException(String.format("Project '%s' cannot be opened", projectName), e); //$NON-NLS-1$
			}
			filesToResolve.forEach((med, path) -> {
				IFile file = wsroot.getFileForLocation(Path.fromOSString(path));
				if(file != null) {
					convertedFiles.put(med, file);
				}
			});
			for (MMSModelElementDescriptor mfD : modelFileDescriptors) {
				String originalPath = mfD.msiDescriptor.attributes.get(filePathFeatureId).toString();
				String path = String.format("/%s%s", projectName, originalPath.substring( //$NON-NLS-1$
																		originalPath.indexOf('/', originalPath.startsWith("/") ? 1 : 0))); //$NON-NLS-1$
				URI uri = URI.createPlatformResourceURI(String.format("%s", path), true); //$NON-NLS-1$
				resourcesForIds.put(mfD.id, 
						rs.createResource(uri));
				convertedFiles.put(mfD, wsroot.getFile(new Path(uri.toPlatformString(true))));
			}
			rs.getResources().forEach(res -> {
				try {
					res.save(null);
				} catch (IOException e) {
					throw new ProjectCreationException(e);
				}
			});
		}
		return convertedFiles;
	}
	
	public Map<IResource, MMSModelElementDescriptor> toMMS(String baseUrl, String autData, String projectId, String branchId, String featurePrefix, Collection<IFile> files) {
		String fileNameFeatureId = featurePrefix+FILE_NAME;
		String filePathFeatureId = featurePrefix+FILE_PATH;
		String fileContentFeatureId = featurePrefix+FILE_CONTENT;
		String fileSystemNsUri = FilesystemPackage.eNS_URI;
		Stack<IResource> convertibles = new Stack<>();
		Map<IResource, MMSModelElementDescriptor> convertedResources = new HashMap<>();
		Map<String, MMSModelElementDescriptor> convertedResourcesForIDs = new HashMap<>();
		MMSServerHelper mmsHelper = new MMSServerHelper(baseUrl, autData);
		convertibles.addAll(files);
		while(!convertibles.empty()) {
			IResource current = convertibles.pop();
			String type = null;
			switch(current.getType()) {
				case IResource.PROJECT:
				case IResource.ROOT:	break;
				case IResource.FOLDER:	type = MMSConstants.MMS_TYPE_FOLDER; break;
				case IResource.FILE:	type = MMSConstants.MMS_TYPE_FILE; break;
			}
			if(type != null) {
				String ownerId = null;
				IContainer container = current.getParent();
				if(container != null && container.getType() != IResource.ROOT) {
					if(container.getType() == IResource.PROJECT) {
						ownerId = projectId;
						// save the .project file if it exists
						IFile projectDescriptorFile = ((IProject) container).getFile(".project"); //$NON-NLS-1$
						if(projectDescriptorFile.exists()) {
							String pdfPath = projectDescriptorFile.getFullPath().toString();
							String id = UUID.nameUUIDFromBytes(pdfPath.getBytes()).toString();
							String pdfContent = null;
							try(InputStream is = projectDescriptorFile.getContents()) {
								pdfContent = IOUtils.toString(is, StandardCharsets.UTF_8.name());
							} catch (IOException | CoreException e) {
								MmsSiriusConnectorPlugin.getDefault().getLogger().error(e);
							}
							MMSModelElementDescriptor pdfConverted = 
									mmsHelper.prepareModelElement(projectId, branchId, 
											id, null, MMSConstants.MMS_TYPE_FILE, ownerId,
											Stream.of(new Object[][] {
												{fileNameFeatureId, projectDescriptorFile.getName()},
												{filePathFeatureId, pdfPath},
												{fileContentFeatureId, pdfContent}
											}).collect(Collectors.toMap(d -> (String)d[0], d -> d[1])));
							convertedResources.put(projectDescriptorFile, pdfConverted);
							convertedResourcesForIDs.put(id, pdfConverted);
						}
					} else {
						ownerId = UUID.nameUUIDFromBytes(container.getFullPath().toString().getBytes()).toString();
					}
				}
				if(ownerId != null) {
					String id = UUID.nameUUIDFromBytes(current.getFullPath().toString().getBytes()).toString();
					MMSModelElementDescriptor converted = 
							mmsHelper.prepareModelElement(projectId, branchId, 
									id, null, type, ownerId,
									Stream.of(new Object[][] {
										{fileNameFeatureId, current.getName()},
										{filePathFeatureId, current.getFullPath().toString()}
									}).collect(Collectors.toMap(d -> (String)d[0], d -> d[1])));
					convertedResources.put(current, converted);
					convertedResourcesForIDs.put(id, converted);
					convertibles.add(container);
				}
			}
		}
		// add every resource to the resourceIds attribute of its container if its container if not the project itself
		for(MMSModelElementDescriptor current : convertedResources.values()) {
			current.msiDescriptor.emfNsUri = fileSystemNsUri;
			if(!projectId.contentEquals(current.msiDescriptor.ownerId)) {
				MMSModelElementDescriptor owner = convertedResourcesForIDs.get(current.msiDescriptor.ownerId);
				Object resourceIds = owner.msiDescriptor.attributes.get("resourceIds"); //$NON-NLS-1$
				if(resourceIds == null) {
					resourceIds = new ArrayList<String>();
					owner.msiDescriptor.attributes.put("resourceIds", resourceIds); //$NON-NLS-1$
				}
				((ArrayList<String>)resourceIds).add(current.id);
			}
		}
		return convertedResources;
	}
}

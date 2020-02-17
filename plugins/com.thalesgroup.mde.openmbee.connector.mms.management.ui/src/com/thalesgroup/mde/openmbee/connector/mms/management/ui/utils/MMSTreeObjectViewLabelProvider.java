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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui.utils;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSCommitDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSIdentifiableDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSNamedDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.MmsServerManagementUiPlugin;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeObject;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeParent;

public class MMSTreeObjectViewLabelProvider extends LabelProvider {
	private static final String LBL_SERVER_DESCRIPTOR1 = "[%s] %s"; //$NON-NLS-1$
	private static final String LBL_SERVER_DESCRIPTOR2 = "[%s] %s [%s]"; //$NON-NLS-1$

		private static final String LBL_NAMED_DESCRIPTOR = "%s [%s]"; //$NON-NLS-1$
		private static final String LBL_COMMIT_DESCRIPTOR = "%s [%s - %s]"; //$NON-NLS-1$
		private IWorkbench workbench;
		
		public MMSTreeObjectViewLabelProvider(IWorkbench workbench) {
			this.workbench = workbench;
		}

		public String getText(Object obj) {
			if(obj instanceof MMSTreeObject) {
				MMSIdentifiableDescriptor mmsObject = ((MMSTreeObject)obj).getMmsObject();
				if(mmsObject instanceof MMSNamedDescriptor && ((MMSNamedDescriptor)mmsObject).name != null) {
					if(mmsObject.id == null) { 
						return ((MMSNamedDescriptor)mmsObject).name;
					} else if(mmsObject instanceof MMSServerDescriptor) {
						MMSServerDescriptor mmsServer = (MMSServerDescriptor)mmsObject;
						return mmsServer.url.contentEquals(mmsServer.name) ? 
								String.format(LBL_SERVER_DESCRIPTOR1, mmsServer.apiVersion, mmsServer.name) : 
								String.format(LBL_SERVER_DESCRIPTOR2, mmsServer.apiVersion, mmsServer.name, mmsServer.url);
					} else {
						return String.format(LBL_NAMED_DESCRIPTOR, ((MMSNamedDescriptor)mmsObject).name, mmsObject.id);
					}
				}
				if(mmsObject.id != null) {
					if(mmsObject instanceof MMSCommitDescriptor) {
						return String.format(LBL_COMMIT_DESCRIPTOR, mmsObject.id, ((MMSCommitDescriptor)mmsObject)._creator, ((MMSCommitDescriptor)mmsObject)._created);
					}
					return mmsObject.id;
				}
				return mmsObject.toString();
			}
			return obj.toString();
		}
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof MMSTreeParent) {
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
				if(((MMSTreeParent)obj).getMmsObject() instanceof MMSServerDescriptor) {
					return MmsServerManagementUiPlugin.getInstance().getImageRegistry().get(MmsServerManagementUiPlugin.IMG__MMS_SERVER);
				}
				else if(((MMSTreeParent)obj).getMmsObject() instanceof MMSRefDescriptor) {
					return MmsServerManagementUiPlugin.getInstance().getImageRegistry().get(MmsServerManagementUiPlugin.IMG__MMS_REF);
				}
				else if(((MMSTreeParent)obj).getMmsObject() instanceof MMSProjectDescriptor) {
					return MmsServerManagementUiPlugin.getInstance().getImageRegistry().get(MmsServerManagementUiPlugin.IMG__MMS_PROJECT);
				}
			}
			return workbench.getSharedImages().getImage(imageKey);
		}
	}
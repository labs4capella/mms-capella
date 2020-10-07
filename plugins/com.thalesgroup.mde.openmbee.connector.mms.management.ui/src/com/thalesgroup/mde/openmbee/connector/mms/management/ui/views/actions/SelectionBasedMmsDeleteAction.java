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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui.views.actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSIdentifiableDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSOrganizationDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.MmsServerManagementUiPlugin;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeObject;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeParent;

public class SelectionBasedMmsDeleteAction extends SelectionBasedAction {
	
	private static final String MSG__ORGANIZATION_DELETION = "Are you sure that you want to delete the organization? Every project in the organization will be deleted too."; //$NON-NLS-1$
	private static final String TTL__ORGANIZATION_DELETION = "Organization Deletion Warning"; //$NON-NLS-1$

	private static class RemovePriorityComparator implements Comparator<MMSTreeObject> {
		@Override
		public int compare(MMSTreeObject o1, MMSTreeObject o2) {
			int comparison = priority(o1.getMmsObject())-priority(o2.getMmsObject());
			return comparison > 0 ? 1 : (comparison < 0 ? -1 : 0);
		}

		private int priority(MMSIdentifiableDescriptor desc) {
			if(desc instanceof MMSRefDescriptor) {
				return 3;
			} else if(desc instanceof MMSProjectDescriptor) {
				return 2;
			} else if(desc instanceof MMSOrganizationDescriptor) {
				return 1;
			}
			return 0;
		}
	}

	
	public SelectionBasedMmsDeleteAction(StructuredViewer viewer) {
		super(viewer);
		this.setText("Delete"); //$NON-NLS-1$
		this.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
	}

	@Override
	public void run() {
		AtomicBoolean needToRefreshViewer = new AtomicBoolean(false);
		MmsServerManagementUiPlugin plugin = MmsServerManagementUiPlugin.getInstance();
		List<?> selectedElements = getSelectedElements();
		List<MMSTreeParent> removables = new ArrayList<>();
		
		// Remove sub elements. there are two case:
		// 1) we don't need to remove it separately because it will be deleted with the parent
		// 2) we need to remove it together with all its siblings
		for (Object o : selectedElements) {
			if (o instanceof MMSTreeParent) {
				MMSTreeParent mto = (MMSTreeParent)o;
				List<MMSTreeParent> mtps = new ArrayList<>();
				for(MMSTreeParent current = mto.getParent(); current != null; current = current.getParent()) {
					if(!(current.getMmsObject() instanceof MMSServerDescriptor)) {
						mtps.add(current);
					}
				}
				if(selectedElements.stream().noneMatch(se -> mtps.contains(se))) {
					removables.add(mto);
				}
			}
		}
		
		// Execute the specific remove actions
		removables.stream().sorted(new RemovePriorityComparator()).forEach(rem -> {
			needToRefreshViewer.set(remove(plugin, rem) || needToRefreshViewer.get());
		});
		
		// Refresh the viewer if any removal has been executed
		if(needToRefreshViewer.get()) {
			viewer.refresh();
		}
	}
	
	private boolean remove(MmsServerManagementUiPlugin plugin, MMSTreeParent rem) {
		MMSIdentifiableDescriptor mmso = rem.getMmsObject();
		boolean retVal = false;
		if(mmso instanceof MMSRefDescriptor) {
			retVal = rem.getServerHelper().removeBranch(((MMSProjectDescriptor) rem.getParent().getMmsObject()).id, (MMSRefDescriptor) mmso);
		}
		if(mmso instanceof MMSProjectDescriptor) {
			retVal = rem.getServerHelper().removeProject((MMSProjectDescriptor) mmso);
		}
		if(mmso instanceof MMSOrganizationDescriptor) {
			MessageDialog warning = new MessageDialog(viewer.getControl().getShell(), 
														TTL__ORGANIZATION_DELETION, 
														null, 
														MSG__ORGANIZATION_DELETION, 
														MessageDialog.WARNING , 
														0,
														"Yes", "No"); //$NON-NLS-1$ //$NON-NLS-2$
			int isOk = warning.open();
			if(MessageDialog.OK == isOk) {
				retVal = rem.getServerHelper().removeOrg((MMSOrganizationDescriptor)mmso);
			}
		}
		if(mmso instanceof MMSServerDescriptor) {
			plugin.removeStoredMMSServerDescriptor((MMSServerDescriptor) mmso);
			plugin.savePreferences();
			rem.getParent().removeChild(mmso);
			retVal = true;
		}
		if(retVal) {
			rem.getParent().removeChild(mmso);
		}
		return retVal;
	}
	
	@Override
	public void updateEnablement() {
		setEnabled(viewer.getStructuredSelection().size()>0 && 
				getSelectedElements().stream()
				.allMatch(s -> {
					return s instanceof MMSTreeParent;
				}));
	}
}
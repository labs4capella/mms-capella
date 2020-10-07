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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSIdentifiableDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSOrganizationDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSProjectDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSRefDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSServerHelper;

public class MMSTreeParent extends MMSTreeObject {
	protected List<MMSTreeObject> children;
	protected MMSServerHelper helper;
	protected boolean hasChildren = true;
	
	public MMSTreeParent(MMSIdentifiableDescriptor mmsObject) {
		super(mmsObject);
		if(mmsObject instanceof MMSServerDescriptor) {
			MMSServerDescriptor server = (MMSServerDescriptor)mmsObject;
			helper = new MMSServerHelper(server.url, server.apiVersion, server.autData);
		}
	}
	
	public void setChildren(List<MMSTreeObject> children) {
		this.children = children == null ? new ArrayList<>() : children;
		this.children.forEach(c -> {
			c.parent = this;
			if(c instanceof MMSTreeParent && ((MMSTreeParent)c).helper == null && helper != null) {
				((MMSTreeParent)c).helper = this.helper;
			}
		});
	}
	
	public boolean addChild(MMSIdentifiableDescriptor child) {
		MMSTreeObject wrapper = wrap(child);
		return addChild(wrapper);
	}

	private MMSTreeObject wrap(MMSIdentifiableDescriptor mmsObject) {
		MMSTreeObject wrapper;
		if(mmsObject instanceof MMSServerDescriptor || 
				mmsObject instanceof MMSOrganizationDescriptor || 
				mmsObject instanceof MMSProjectDescriptor || 
				mmsObject instanceof MMSRefDescriptor) {
			wrapper = new MMSTreeParent(mmsObject);
		} else {
			wrapper = new MMSTreeObject(mmsObject);
		}
		return wrapper;
	}
	
	public boolean addChild(MMSTreeObject child) {
		if(children == null) {
			children = new ArrayList<>();
		}
		child.parent = this;
		if(child instanceof MMSTreeParent && ((MMSTreeParent)child).helper == null && helper != null) {
			((MMSTreeParent)child).helper = helper;
		}
		return children.contains(child) ? false : children.add(child);
	}
	
	public MMSTreeObject[] getChildren() {
		if(children == null || children.size() == 0) {
			if(mmsObject instanceof MMSServerDescriptor) {
				setChildren(wrapAll(helper.getOrgs()));
			} else if(mmsObject instanceof MMSOrganizationDescriptor) {
				setChildren(wrapAll(helper.getProjects(mmsObject.id)));
			} else if(mmsObject instanceof MMSProjectDescriptor) {
				setChildren(wrapAll(helper.getBranches(((MMSProjectDescriptor) mmsObject).orgId, mmsObject.id)));
			} else if(mmsObject instanceof MMSRefDescriptor) {
				setChildren(wrapAll(helper.getCommits(((MMSProjectDescriptor) parent.mmsObject).orgId, parent.mmsObject.id, mmsObject.id)));
			}
		}
		if(children == null || children.size() == 0) hasChildren = false;
		return children == null ? new MMSTreeObject[0] : children.toArray(new MMSTreeObject[children.size()]);
	}
	
	private List<MMSTreeObject> wrapAll(Collection<? extends MMSIdentifiableDescriptor> all) {
		return all.stream().map(o -> wrap(o)).collect(Collectors.toCollection(ArrayList::new));
	}

	public boolean hasChildren() {
		return this.hasChildren;//getChildren().length>0;
	}

	public boolean removeChild(MMSIdentifiableDescriptor removable) {
		Optional<MMSTreeObject> removableChild = children.stream().filter(c -> c.mmsObject.equals(removable)).findFirst();
		return removableChild.isPresent() && children.remove(removableChild.get());
	}

	public MMSServerHelper getServerHelper() {
		return helper;
	}
}
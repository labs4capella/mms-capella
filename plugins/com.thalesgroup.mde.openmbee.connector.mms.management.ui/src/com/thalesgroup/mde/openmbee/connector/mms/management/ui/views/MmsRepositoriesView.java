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
package com.thalesgroup.mde.openmbee.connector.mms.management.ui.views;

import javax.inject.Inject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.part.ViewPart;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSServerDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.MmsServerManagementUiPlugin;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSRepositoryViewRoot;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.data.MMSTreeParent;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.utils.MMSTreeObjectViewContentProvider;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.utils.MMSTreeObjectViewLabelProvider;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.utils.MmsServerConnectionWizard;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.views.actions.SelectionBasedAction;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.views.actions.SelectionBasedMmsCheckoutAction;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.views.actions.SelectionBasedMmsCreateAction;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.views.actions.SelectionBasedMmsDeleteAction;
import com.thalesgroup.mde.openmbee.connector.mms.management.ui.views.actions.SelectionBasedMmsUpdateAction;


public class MmsRepositoriesView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.thalesgroup.mde.openmbee.connector.mms.management.ui.views.MmsRepositoriesView"; //$NON-NLS-1$

	@Inject IWorkbench workbench;
	
	private TreeViewer viewer;
	private MMSTreeParent viewerDataRoot;
	private Action connectMmsServer;
	private Action refreshView;
	private SelectionBasedAction delete;
	private SelectionBasedAction create;
	private SelectionBasedAction update;
	private SelectionBasedAction checkout;
	 
	public MmsRepositoriesView() {
		retrieveViewerData();
	}

	protected void retrieveViewerData() {
		MMSRepositoryViewRoot mmsRoot = new MMSRepositoryViewRoot();
		mmsRoot.id = "root";
		viewerDataRoot = new MMSTreeParent(mmsRoot);
		MMSServerDescriptor[] storedMMSServerDescriptors = MmsServerManagementUiPlugin.getInstance().getStoredMMSServerDescriptors();
		if(storedMMSServerDescriptors != null) {
			for (MMSServerDescriptor stored : storedMMSServerDescriptors) {
				viewerDataRoot.addChild(stored);
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		viewer.setContentProvider(new MMSTreeObjectViewContentProvider(getSite().getShell()));
		viewer.setInput(viewerDataRoot);
		viewer.setLabelProvider(new MMSTreeObjectViewLabelProvider(workbench));

		// Create the help context id for the viewer's control
		workbench.getHelpSystem().setHelp(viewer.getControl(), "com.thalesgroup.mde.openmbee.connector.mms.management.ui.viewer"); //$NON-NLS-1$
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		
		// Add listener which updates the selection based actions
		viewer.addSelectionChangedListener(new SelectionBasedActionUpdater(delete, create, update, checkout));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MmsRepositoriesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(delete);
		manager.add(create);
		manager.add(update);
		manager.add(checkout);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshView);
		manager.add(new Separator());
		manager.add(connectMmsServer);
	}

	private void makeActions() {
		connectMmsServer = new Action() {
			public void run() {
				Wizard connectionWizard = new MmsServerConnectionWizard(viewerDataRoot);
				connectionWizard.setWindowTitle("MMS Server Connection"); //$NON-NLS-1$
				WizardDialog wd = new WizardDialog(getSite().getShell(), connectionWizard);
				wd.setTitle(connectionWizard.getWindowTitle());
				if(WizardDialog.OK == wd.open()) {
					viewer.refresh(viewerDataRoot);
				}
			}
		};
		connectMmsServer.setText("Connect"); //$NON-NLS-1$
		connectMmsServer.setToolTipText("Connect to MMS Server"); //$NON-NLS-1$
		connectMmsServer.setImageDescriptor(
				MmsServerManagementUiPlugin.getInstance().getImageRegistry().getDescriptor(
						MmsServerManagementUiPlugin.IMG__ADD_SERVER));

		refreshView = new Action() {
			public void run() {
				retrieveViewerData();
				if (null != viewer) {
					viewer.refresh(viewerDataRoot);
				}
			}
		};
		refreshView.setText("Refresh"); //$NON-NLS-1$
		refreshView.setToolTipText("Refresh view"); //$NON-NLS-1$
		refreshView.setImageDescriptor(
				MmsServerManagementUiPlugin.getInstance().getImageRegistry().getDescriptor(
						MmsServerManagementUiPlugin.IMG__REFRESH));
		
		
		delete = new SelectionBasedMmsDeleteAction(viewer);
		create = new SelectionBasedMmsCreateAction(viewer);
		update = new SelectionBasedMmsUpdateAction(viewer);
		checkout = new SelectionBasedMmsCheckoutAction(viewer);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	public MMSTreeParent getViewerDataRoot() {
		return viewerDataRoot;
	}
}

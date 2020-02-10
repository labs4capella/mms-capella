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
package com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>File System Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getName <em>Name</em>}</li>
 *   <li>{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getPath <em>Path</em>}</li>
 *   <li>{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getOwner <em>Owner</em>}</li>
 * </ul>
 *
 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FilesystemPackage#getFileSystemElement()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface FileSystemElement extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FilesystemPackage#getFileSystemElement_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Path</em>' attribute.
	 * @see #setPath(String)
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FilesystemPackage#getFileSystemElement_Path()
	 * @model
	 * @generated
	 */
	String getPath();

	/**
	 * Sets the value of the '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getPath <em>Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Path</em>' attribute.
	 * @see #getPath()
	 * @generated
	 */
	void setPath(String value);

	/**
	 * Returns the value of the '<em><b>Owner</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Owner</em>' reference.
	 * @see #setOwner(Folder)
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FilesystemPackage#getFileSystemElement_Owner()
	 * @model
	 * @generated
	 */
	Folder getOwner();

	/**
	 * Sets the value of the '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getOwner <em>Owner</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Owner</em>' reference.
	 * @see #getOwner()
	 * @generated
	 */
	void setOwner(Folder value);

} // FileSystemElement

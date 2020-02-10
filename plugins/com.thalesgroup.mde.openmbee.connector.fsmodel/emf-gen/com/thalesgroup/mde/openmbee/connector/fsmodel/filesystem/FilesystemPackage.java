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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FilesystemFactory
 * @model kind="package"
 * @generated
 */
public interface FilesystemPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "filesystem";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.thalesgroup.com/mde/openmbee/connector";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "com.thalesgroup.mde.openmbee.connector.fsmodel";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	FilesystemPackage eINSTANCE = com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FilesystemPackageImpl.init();

	/**
	 * The meta object id for the '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement <em>File System Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FilesystemPackageImpl#getFileSystemElement()
	 * @generated
	 */
	int FILE_SYSTEM_ELEMENT = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_SYSTEM_ELEMENT__NAME = 0;

	/**
	 * The feature id for the '<em><b>Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_SYSTEM_ELEMENT__PATH = 1;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_SYSTEM_ELEMENT__OWNER = 2;

	/**
	 * The number of structural features of the '<em>File System Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_SYSTEM_ELEMENT_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>File System Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_SYSTEM_ELEMENT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FileImpl <em>File</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FileImpl
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FilesystemPackageImpl#getFile()
	 * @generated
	 */
	int FILE = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE__NAME = FILE_SYSTEM_ELEMENT__NAME;

	/**
	 * The feature id for the '<em><b>Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE__PATH = FILE_SYSTEM_ELEMENT__PATH;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE__OWNER = FILE_SYSTEM_ELEMENT__OWNER;

	/**
	 * The feature id for the '<em><b>Content</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE__CONTENT = FILE_SYSTEM_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>File</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_FEATURE_COUNT = FILE_SYSTEM_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>File</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_OPERATION_COUNT = FILE_SYSTEM_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FolderImpl <em>Folder</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FolderImpl
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FilesystemPackageImpl#getFolder()
	 * @generated
	 */
	int FOLDER = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FOLDER__NAME = FILE_SYSTEM_ELEMENT__NAME;

	/**
	 * The feature id for the '<em><b>Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FOLDER__PATH = FILE_SYSTEM_ELEMENT__PATH;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FOLDER__OWNER = FILE_SYSTEM_ELEMENT__OWNER;

	/**
	 * The number of structural features of the '<em>Folder</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FOLDER_FEATURE_COUNT = FILE_SYSTEM_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Folder</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FOLDER_OPERATION_COUNT = FILE_SYSTEM_ELEMENT_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement <em>File System Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>File System Element</em>'.
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement
	 * @generated
	 */
	EClass getFileSystemElement();

	/**
	 * Returns the meta object for the attribute '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getName()
	 * @see #getFileSystemElement()
	 * @generated
	 */
	EAttribute getFileSystemElement_Name();

	/**
	 * Returns the meta object for the attribute '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getPath <em>Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Path</em>'.
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getPath()
	 * @see #getFileSystemElement()
	 * @generated
	 */
	EAttribute getFileSystemElement_Path();

	/**
	 * Returns the meta object for the reference '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getOwner <em>Owner</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Owner</em>'.
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement#getOwner()
	 * @see #getFileSystemElement()
	 * @generated
	 */
	EReference getFileSystemElement_Owner();

	/**
	 * Returns the meta object for class '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.File <em>File</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>File</em>'.
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.File
	 * @generated
	 */
	EClass getFile();

	/**
	 * Returns the meta object for the attribute '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.File#getContent <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Content</em>'.
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.File#getContent()
	 * @see #getFile()
	 * @generated
	 */
	EAttribute getFile_Content();

	/**
	 * Returns the meta object for class '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.Folder <em>Folder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Folder</em>'.
	 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.Folder
	 * @generated
	 */
	EClass getFolder();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	FilesystemFactory getFilesystemFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement <em>File System Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.FileSystemElement
		 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FilesystemPackageImpl#getFileSystemElement()
		 * @generated
		 */
		EClass FILE_SYSTEM_ELEMENT = eINSTANCE.getFileSystemElement();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FILE_SYSTEM_ELEMENT__NAME = eINSTANCE.getFileSystemElement_Name();

		/**
		 * The meta object literal for the '<em><b>Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FILE_SYSTEM_ELEMENT__PATH = eINSTANCE.getFileSystemElement_Path();

		/**
		 * The meta object literal for the '<em><b>Owner</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FILE_SYSTEM_ELEMENT__OWNER = eINSTANCE.getFileSystemElement_Owner();

		/**
		 * The meta object literal for the '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FileImpl <em>File</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FileImpl
		 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FilesystemPackageImpl#getFile()
		 * @generated
		 */
		EClass FILE = eINSTANCE.getFile();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FILE__CONTENT = eINSTANCE.getFile_Content();

		/**
		 * The meta object literal for the '{@link com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FolderImpl <em>Folder</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FolderImpl
		 * @see com.thalesgroup.mde.openmbee.connector.fsmodel.filesystem.impl.FilesystemPackageImpl#getFolder()
		 * @generated
		 */
		EClass FOLDER = eINSTANCE.getFolder();

	}

} //FilesystemPackage

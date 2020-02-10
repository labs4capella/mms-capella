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

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.sirius.business.api.resource.ResourceDescriptor;
import org.eclipse.sirius.viewpoint.DAnalysis;
import org.eclipse.sirius.viewpoint.RGBValues;
import org.eclipse.sirius.viewpoint.ViewpointFactory;
import org.eclipse.sirius.viewpoint.ViewpointPackage;

import com.thalesgroup.mde.openmbee.connector.mms.data.MMSModelElementDescriptor;
import com.thalesgroup.mde.openmbee.connector.mms.utils.MMSModelElementDescriptorConverter;

public class EmfMmsModelElementDescriptorConverter implements MMSModelElementDescriptorConverter<EObject, ResourceSet> {
	public static final String ID_PREFIX_LOCAL = "LOCAL_ID"; //$NON-NLS-1$
	public static final String ID_PREFIX_URIFRAGMENT = "URI_FRAGMENT"; //$NON-NLS-1$
	public static final String ID_PREFIX_XMI = "XMI_ID"; //$NON-NLS-1$
	public static final String ID_SEPARATOR = "__"; //$NON-NLS-1$
	public static final String TYPE_SEPARATOR = "#"; //$NON-NLS-1$

	@Override
	public Map<MMSModelElementDescriptor, EObject> fromMMS(List<MMSModelElementDescriptor> meds, ResourceSet rs) {
		return fromMMS(meds, rs, new HashMap<>(), ""); //$NON-NLS-1$
	}

	public Map<MMSModelElementDescriptor, EObject> fromMMS(List<MMSModelElementDescriptor> meds, ResourceSetImpl rs, String featurePrefix) {
		return fromMMS(meds, rs, new HashMap<>(), featurePrefix);
	}
	
	@SuppressWarnings("unchecked")
	public Map<MMSModelElementDescriptor, EObject> fromMMS(List<MMSModelElementDescriptor> meds, ResourceSet rs, Map<String, Resource> resourcesForIds, String featurePrefix) {
		Map<MMSModelElementDescriptor, EObject> eos = new HashMap<>();
		Map<String, EObject> eosForIDs = new HashMap<>();
		EPackage.Registry registry = EPackage.Registry.INSTANCE;
		if(resourcesForIds == null) resourcesForIds = new HashMap<>();
		if(featurePrefix == null) featurePrefix = ""; //$NON-NLS-1$
		int featurePrefixLength = featurePrefix.length();
		// prepare EObjects
		for (MMSModelElementDescriptor med : meds) {
			EPackage ePackage = registry.getEPackage(med.msiDescriptor.emfNsUri);
			EClassifier eClassifier = ePackage.getEClassifier(med.msiDescriptor.type);
			if(eClassifier != null && eClassifier instanceof EClass) {
				EObject eo = EcoreUtil.create((EClass)eClassifier);
				eos.put(med, eo);
				eosForIDs.put(med.id, eo);
			}
		}
		// fill the attributes
		for (Entry<MMSModelElementDescriptor, EObject> eoForMed : eos.entrySet()) {
			EObject eo = eoForMed.getValue();
			EClass eClass = eo.eClass();
			for(Entry<String, Object> medAttribute : eoForMed.getKey().msiDescriptor.attributes.entrySet()) {
				String featureName = medAttribute.getKey();
				if(featureName.startsWith(featurePrefix)) {
					featureName = featureName.substring(featurePrefixLength);
				}
				EStructuralFeature esf = eClass.getEStructuralFeature(featureName);
				if(esf==null && featureName.length()>2) {
					if(featureName.endsWith("Id")) {
						esf = eClass.getEStructuralFeature(featureName.substring(0, featureName.length()-2));
					} else if(featureName.endsWith("Ids")) {
						esf = eClass.getEStructuralFeature(featureName.substring(0, featureName.length()-3));
					}
				}
				if(esf == null) {
					if(!featureName.startsWith("_")) MmsSiriusConnectorPlugin.getDefault().getLogger().warn(featureName);
				} else if(esf.isChangeable() && !esf.isDerived() && !esf.isTransient() && !isOppositeOfContainmentReference(esf)) {
					if(esf.isMany()) {
						Object values = medAttribute.getValue();
						if(values instanceof List) {
							List<Object> elements = (List<Object>) values;
							Map<Object, Object> valuesToElements = new HashMap<>();
							EList<Object> eList = (EList<Object>)eo.eGet(esf);
							for (Object o : elements) {
								Object value;
								if(esf instanceof EAttribute) {
									value = getFeatureValueFromString(esf, o);
								} else {
									value = decodeEObject(o, eosForIDs, rs, resourcesForIds);
								}
								if(value != null) {
									valuesToElements.put(value, o);
									if(!eList.contains(value)) {
										eList.add(value);
									}
								} else {
									MmsSiriusConnectorPlugin.getDefault().getLogger().error("This converted to null: "+o); //$NON-NLS-1$
								}
							}
							ECollections.sort(eList, (a,b) -> {
								int ai = elements.indexOf(valuesToElements.get(a));
								int bi = elements.indexOf(valuesToElements.get(b));
								return Integer.compare(ai, bi);
							});
						} else {
							MmsSiriusConnectorPlugin.getDefault().getLogger().error("This value is not a list: "+values.toString()); //$NON-NLS-1$
						}
					} else {
						Object value;
						if(esf instanceof EAttribute) {
							value = getFeatureValueFromString(esf, medAttribute.getValue());
							eo.eSet(esf, value);
						} else if(!eo.eIsSet(esf)) {
							value = decodeEObject(medAttribute.getValue(), eosForIDs, rs, resourcesForIds);
							eo.eSet(esf, value);
						}
					}
				}
			}
		}
		for (Entry<MMSModelElementDescriptor, EObject> eoForMed : eos.entrySet()) {
			EObject eo = eoForMed.getValue();
			if(eo.eContainer() == null && eo.eResource() == null) {
				Resource res = resourcesForIds.get(eoForMed.getKey().msiDescriptor.ownerId);
				if(res != null) {
					if(eo instanceof DAnalysis) {
						res.getContents().add(0, eo);
					} else {
						res.getContents().add(eo);
					}
				}
			}
		}
		for(Resource res : resourcesForIds.values()) {
			try {
				res.save(null);
			} catch (IOException e) {
				throw new RuntimeException(String.format("Cannot save resource '%s'", res.getURI()), e); //$NON-NLS-1$
			}
		}
		return eos;
	}

	/**
	 * @param esf
	 * @return {@code true} if the given feature is an EReference 
	 *         which has an opposite reference and 
	 *         this opposite is a containment reference
	 */
	protected boolean isOppositeOfContainmentReference(EStructuralFeature esf) {
		return esf instanceof EReference && 
				((EReference)esf).getEOpposite() != null && 
				((EReference)esf).getEOpposite().isContainment();
	}

	@SuppressWarnings("unchecked")
	private EObject decodeEObject(Object idObject, Map<String, EObject> eosForIDs, ResourceSet rs, Map<String, Resource> resourcesForIds) {
		if(idObject instanceof String) {
			String id = (String)idObject;
			EObject eo = eosForIDs.get(id);
			if(eo == null) {
				String[] idParts = id.split(ID_SEPARATOR);
				if(idParts.length == 3) {
					if(ID_PREFIX_XMI.contentEquals(idParts[0]) || ID_PREFIX_URIFRAGMENT.contentEquals(idParts[0])) {
						Resource res = rs.getResource(URI.createURI(idParts[1]), true);
						// Documentation of XMLResource#getEObjectToIDMap recommends to use Resource#getEObject instead of itself. See:
						// https://download.eclipse.org/modeling/emf/emf/javadoc/2.10.0/org/eclipse/emf/ecore/xmi/XMLResource.html#getEObjectToIDMap()
						// So it can be used in both cases.
						eo = res.getEObject(idParts[2]);
						if(eo != null) {
							eosForIDs.put(id, eo);
						}
					} else if(ID_PREFIX_LOCAL.contentEquals(idParts[0])) {
						String containerId = id.substring(ID_PREFIX_LOCAL.length(), id.lastIndexOf(ID_SEPARATOR));
						EObject econtainer = decodeEObject(containerId, eosForIDs, rs, resourcesForIds);
						if(econtainer != null) {
							EStructuralFeature containingFeature = 
									econtainer.eClass().getEStructuralFeature(idParts[idParts.length-2]);
							Object eGet = econtainer.eGet(containingFeature);
							if(containingFeature.isMany()) {
								eo = ((List<EObject>)eGet).get(Integer.parseInt(idParts[idParts.length-1]));
							} else {
								eo = (EObject) eGet;
							}
						}
					}
				}
			}
			return eo;
		}
		return null;
	}

	private Object getFeatureValueFromString(EStructuralFeature ea, Object value) {
		if (ea.getEType() instanceof EDataType) {
			try {
				EClassifier eType = ea.getEType();
				EFactory factory = eType.getEPackage().getEFactoryInstance();
				return factory.createFromString((EDataType) eType, (String) value);
			} catch(IllegalArgumentException e) {
				MmsSiriusConnectorPlugin.getDefault().getLogger().error(e);
			}
		}
		return value;
	}

	@Override
	public Map<EObject, MMSModelElementDescriptor> toMMS(List<EObject> eobjects, String featurePrefix) {
		Map<EObject, MMSModelElementDescriptor> meds = new HashMap<>();
		Set<Resource> scopedResources = new HashSet<>();
		// collect scoped resources
		for (EObject eobject : eobjects) {
			if(eobject != null) {
				scopedResources.add(eobject.eResource());
			}
		}
		// prepare meds
		for (EObject eobject : eobjects) {
			if(eobject != null) {
				MMSModelElementDescriptor med = new MMSModelElementDescriptor();
				med.vcsiDescriptor = new MMSModelElementDescriptor.MMSVCSInfoDescriptior();
				med.msiDescriptor = new MMSModelElementDescriptor.MMSMSInfoDescriptor();
				
				String id = getId(eobject, meds, scopedResources);
				med.setId(id);
				
				meds.put(eobject, med);
			}
		}
		
		// save feature values and necessary metadata
		EReference gmfViewElementFeature = NotationPackage.eINSTANCE.getView_Element();
		for (Entry<EObject, MMSModelElementDescriptor> entry : meds.entrySet()) {
			EObject eobject = entry.getKey();
			MMSModelElementDescriptor med = entry.getValue();
			EClass eClass = eobject.eClass();
			med.msiDescriptor.ownerId = getId(eobject.eContainer(), meds, scopedResources);
			med.msiDescriptor.type = eobject.eClass().getName();
			med.msiDescriptor.emfNsUri = eobject.eClass().getEPackage().getNsURI();
			
			for(EStructuralFeature esf : eClass.getEAllStructuralFeatures()) {
				String esfId = null;
				Object value = null;
				if(esf instanceof EReference) {
					if(esf != gmfViewElementFeature || eobject.eIsSet(esf)) {
						EReference eref = (EReference) esf;
						esfId = String.format("%s%s", eref.getName(), eref.isMany() ? "Ids" : "Id"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if(eref.isMany()) {
							Object values = eobject.eGet(eref);
							if(values != null && values instanceof List) {
								List<?> valueList = (List<?>) values;
								if(valueList.size() > 0) {
									value = valueList.stream().map(eo -> getId((EObject)eo, meds, scopedResources)).collect(Collectors.toList());
								}
							}
						} else {
							value = getId((EObject)eobject.eGet(eref), meds, scopedResources);
						}
					}
				} else {
					EAttribute eattr = (EAttribute) esf;
					esfId = eattr.getName();
					if(eattr.isMany()) {
						Object values = eobject.eGet(eattr);
						if(values != null && values instanceof List) {
							List<?> valueList = (List<?>) values;
							if(valueList.size() > 0) {
								value = valueList.stream().map(v -> convert(eattr, v)).collect(Collectors.toList());
							}
						}
					} else {
						Object v = eobject.eGet(eattr);
						value = convert(eattr, v);
					}
				}
				if(esfId != null && value != null) {
//					if("type".contentEquals(esfId) || "id".contentEquals(esfId)) {
//						System.out.println(String.format("Cannot add '%s' attribute with the value '%s' to the EObject '%s'", esfId, value, med.id));
//					} else {
						med.msiDescriptor.attributes.put(featurePrefix+esfId, value);
//					}
				}
			}
		}
		return meds;
	}
	
	private Object convert(EAttribute eattr, Object v) {
		if(v == null) return null;
		try {
			if (eattr.getEType() instanceof EDataType) {
				EDataType eType = eattr.getEAttributeType();
				EFactory factory = eType.getEPackage().getEFactoryInstance();
				try {
					return factory.convertToString(eType, v);
				} catch (IllegalArgumentException ex) {
					// some attributes have the 'EMap' type, which causes an exception
					return null;
				}
			}
			return v;
		} catch (RuntimeException rex) {
			if(rex.getCause() instanceof NotSerializableException) {
				if (v instanceof RGBValues) {
					return ViewpointFactory.eINSTANCE.convertToString(ViewpointPackage.eINSTANCE.getRGBValues(), v);
				} else if (v instanceof ResourceDescriptor) {
					return ViewpointFactory.eINSTANCE.convertToString(ViewpointPackage.eINSTANCE.getResourceDescriptor(), v);
				} else if (v instanceof EEnumLiteral) {
						return v.toString();
				}
			}
			throw rex;
		}
	}
	
	private String getId(EObject eobject, Map<EObject, MMSModelElementDescriptor> meds, Collection<Resource> scopedResources) {
		String id = null;
		if(eobject != null) {
			if(meds != null) {
				MMSModelElementDescriptor med = meds.get(eobject);
				if(med != null) {
					id = med.id;
				}
			}
			if(id == null) {
				Resource eResource = eobject.eResource();
				// if its in the scoped resources then we don't need its uri
				if(scopedResources != null && !scopedResources.contains(eResource)) {
					
					id = getExternalId(eobject, false, meds);
				} else {
					id = EcoreUtil.getID(eobject);
					if (id == null || id.length() == 0) {
						id = UUID.randomUUID().toString();
					}
				}
			}
		}
		return id;
	}
	public String getExternalId(EObject eobject, boolean calculateQualifiedNameIfNecessary, Map<EObject, MMSModelElementDescriptor> meds) {
		String id = null;
		if(eobject != null) {
			
			Resource eResource = eobject.eResource();
			if(id == null) {
				id = EcoreUtil.getID(eobject);
				String prefix = null;
				String resourceId = "";
				if(eResource == null) {
					resourceId = "NO_RESOURCE"+ID_SEPARATOR; //$NON-NLS-1$
				} else {
					resourceId = eResource.getURI().toString() + ID_SEPARATOR;
				}
				if (eResource instanceof XMLResource) {
					prefix = ID_PREFIX_XMI;
					id = ((XMLResource) eResource).getID(eobject);
				}
				if (id == null || id.length() == 0) {
					prefix = ID_PREFIX_URIFRAGMENT;
					id = EcoreUtil.getURI(eobject).fragment();
				}
				if(id != null && id.length()>0) {
					id = String.format("%s%s%s%s", prefix, ID_SEPARATOR, resourceId, EcoreUtil.getURI(eobject).fragment(), id); //$NON-NLS-1$
				}
				
				if ((id == null || id.length() == 0) && calculateQualifiedNameIfNecessary) {
					EAttribute idFeature = eobject.eClass().getEAllAttributes().stream()
														.filter(eattr -> "name".contentEquals(eattr.getName().toLowerCase()))
														.findFirst().orElse(null);
					if(eobject.eContainer() != null) {
						id = String.format("%s%s%s", getExternalId(eobject.eContainer(), calculateQualifiedNameIfNecessary, meds), //$NON-NLS-1$
													ID_SEPARATOR,
													getFeatureValueAsString(eobject, idFeature));
					} else {
						id = getFeatureValueAsString(eobject, idFeature);
					}
				}
			}
			
			if(id != null) {}
			else if(eobject.eContainer() != null) {
				id = String.join(ID_SEPARATOR,
						ID_PREFIX_LOCAL,
						getExternalId(eobject.eContainer(), calculateQualifiedNameIfNecessary, meds), 
						generateLocalId(eobject));
			} else if(eResource != null) {
				id = String.format("%s%s%d", //$NON-NLS-1$
						eResource.getURI(), 
						ID_SEPARATOR, 
						eResource.getContents().indexOf(eobject));
			} else {
				id = "NO_CONTAINER"+UUID.randomUUID(); //$NON-NLS-1$
				MmsSiriusConnectorPlugin.getDefault().getLogger().error(String.format("Unknown id for %s (the generated is %s)", eobject, id)); //$NON-NLS-1$
			}
		}
		return id;
	}

	private String getFeatureValueAsString(EObject eobject, EAttribute idFeature) {
		String stringValue = null;
		if(idFeature != null) {
			Object value = eobject.eGet(idFeature);
			stringValue = value == null ? null : value.toString();
		}
		return stringValue;
	}

	private String generateLocalId(EObject eobject) {
		String localId = null;
		EStructuralFeature ecf = eobject.eContainingFeature();
		if(ecf != null) {
			if(ecf.isMany() && eobject.eContainer() != null) {
				localId = String.format("%s%s%s", ecf.getName(), ID_SEPARATOR, ((List<?>)eobject.eContainer().eGet(ecf)).indexOf(eobject)); //$NON-NLS-1$
			} else {
				localId = ecf.getName();
			}
		}
		return localId;
	}

}

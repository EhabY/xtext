/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.common.types.util;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.eclipse.xtext.common.types.JvmArrayType;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.access.ClasspathTypeProviderFactory;
import org.eclipse.xtext.common.types.access.impl.ClasspathTypeProvider;
import org.eclipse.xtext.common.types.util.TypeArgumentContext.Provider;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class TypeArgumentContextTest extends TestCase {

	private ClasspathTypeProvider typeProvider;
	private JvmTypeReferences typeRefs;
	private Provider typeArgCtxProvider;
	private ResourceSetImpl resourceSet;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		resourceSet = new ResourceSetImpl();
		Resource resource = new XMLResourceImpl(URI.createURI("http://synthetic.resource"));
		resourceSet.getResources().add(resource);
		typeArgCtxProvider = new TypeArgumentContext.Provider();
		final ClasspathTypeProviderFactory typeProviderFactory = new ClasspathTypeProviderFactory(getClass().getClassLoader());
		typeProvider = typeProviderFactory.createTypeProvider(resourceSet);
		assertNotNull(typeProvider);
		typeArgCtxProvider.setTypeProviderFactory(typeProviderFactory);
		typeRefs = new JvmTypeReferences(TypesFactory.eINSTANCE, typeProvider);
	}
	
	@Override
	protected void tearDown() throws Exception {
		typeProvider = null;
		typeRefs = null;
		resourceSet = null;
		typeArgCtxProvider = null;
		super.tearDown();
	}
	
	public void testSimple() throws Exception {
		JvmTypeReference reference = typeRefs.typeReference("java.util.List").wildCardExtends("java.lang.CharSequence").create();
		TypeArgumentContext typeArgumentContext = typeArgCtxProvider.get(reference);
		JvmTypeReference argument = typeArgumentContext.getBoundArgument(((JvmGenericType)reference.getType()).getTypeParameters().get(0));
		assertTrue(EcoreUtil.equals(((JvmParameterizedTypeReference)reference).getArguments().get(0), argument));
	}
	
	public void testPrimitive() throws Exception {
		JvmTypeReference primitiveRef = typeRefs.typeReference("int").create();
		TypeArgumentContext typeArgumentContext = typeArgCtxProvider.get(primitiveRef);
		JvmTypeReference reference = typeRefs.typeReference("java.util.List").wildCardExtends("java.lang.CharSequence").create();
		JvmTypeReference argument = typeArgumentContext.getBoundArgument(((JvmGenericType)reference.getType()).getTypeParameters().get(0));
		assertNull(argument);
	}
	
	public void testNotRecursive() throws Exception {
		JvmTypeReference reference = typeRefs.typeReference("java.util.List").wildCardExtends("java.lang.CharSequence").create();
		
		JvmGenericType collType = (JvmGenericType) typeProvider.findTypeByName(Collection.class.getCanonicalName());
		JvmTypeReference collArgument = typeArgCtxProvider.get(reference).getBoundArgument(collType.getTypeParameters().get(0));
		
		JvmGenericType listType = (JvmGenericType) typeProvider.findTypeByName(List.class.getCanonicalName());
		JvmTypeReference listArgument = ((JvmParameterizedTypeReference)listType.getSuperTypes().get(0)).getArguments().get(0);
		
		assertTrue(EcoreUtil.equals(listArgument, collArgument));
	}
	
	public void testResolve_0() throws Exception {
		JvmTypeReference reference = typeRefs.typeReference("java.util.ArrayList").wildCardExtends("java.lang.CharSequence").create();
		TypeArgumentContext context = typeArgCtxProvider.get(reference);
		JvmOperation jvmOperation = findOperation("java.util.List", "add(E)");
		
		assertEquals(null, context.getLowerBound(jvmOperation.getParameters().get(0).getParameterType()));
		JvmOperation get = findOperation("java.util.List", "get(int)");
		assertEquals("java.lang.CharSequence",context.getUpperBound(get.getReturnType(), resourceSet).getCanonicalName());
	}
	
	public void testResolve_1() throws Exception {
		JvmTypeReference reference = typeRefs.typeReference("java.util.ArrayList").wildCardSuper("java.lang.CharSequence").create();
		TypeArgumentContext context = typeArgCtxProvider.get(reference);
		JvmOperation jvmOperation = findOperation("java.util.List", "add(E)");
		
		JvmTypeReference resolvedParameter = context.getLowerBound(jvmOperation.getParameters().get(0).getParameterType());
		assertEquals("java.lang.CharSequence", resolvedParameter.getCanonicalName());
		JvmOperation get = findOperation("java.util.List", "get(int)");
		assertEquals("java.lang.Object",context.getUpperBound(get.getReturnType(),resourceSet).getCanonicalName());
	}
	
	public void testResolve_WithUnResolved() throws Exception {
		JvmTypeReference reference = typeRefs.typeReference("java.util.ArrayList").create();
		TypeArgumentContext context = typeArgCtxProvider.get(reference);
		JvmOperation jvmOperation = findOperation("java.util.List", "add(E)");
		
		JvmTypeReference resolvedParameter = context.getLowerBound(jvmOperation.getParameters().get(0).getParameterType());
		assertEquals("E", resolvedParameter.getCanonicalName());
		JvmOperation get = findOperation("java.util.List", "get(int)");
		assertEquals("E", context.getUpperBound(get.getReturnType(),resourceSet).getCanonicalName());
	}
	
	public void testResolveDeeplyNested() throws Exception {
		JvmTypeReference reference = typeRefs
			.typeReference("java.util.ArrayList")
				.wildCardExtends("java.util.Map")
					.wildCardSuper("java.lang.String").x()
					.wildCardExtends("java.lang.Number").x()
				.create();
		TypeArgumentContext context = typeArgCtxProvider.get(reference);
		
		JvmOperation get = findOperation("java.util.List", "get(int)");
		assertEquals("java.util.Map<? super java.lang.String,? extends java.lang.Number>",context.getUpperBound(get.getReturnType(),resourceSet).getCanonicalName());
	}
	
	public void testResolveArray() throws Exception {
		JvmArrayType arrayType = TypesFactory.eINSTANCE.createJvmArrayType();
		arrayType.setComponentType(typeRefs.typeReference("java.lang.String").create());
		//TODO
	}
	
	protected JvmOperation findOperation(String typeName, String methodSignature) {
		JvmType type = typeProvider.findTypeByName(typeName);
		return (JvmOperation) type.eResource().getEObject(typeName+"."+methodSignature);
	}

}

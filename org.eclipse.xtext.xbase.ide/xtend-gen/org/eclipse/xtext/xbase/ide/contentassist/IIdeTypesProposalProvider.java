/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.ide.contentassist;

import com.google.common.base.Predicate;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.common.types.descriptions.ITypeDescriptor;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor;

@SuppressWarnings("all")
public interface IIdeTypesProposalProvider {
  void createTypeProposals(final EReference reference, final ContentAssistContext context, final Predicate<? super ITypeDescriptor> filter, final IIdeContentProposalAcceptor acceptor);
}

/*
 * generated by Xtext
 */
package org.eclipse.xtext.purexbase.ui.refactoring;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.ui.refactoring.IRenameStrategy;
import org.eclipse.xtext.ui.refactoring.impl.DefaultRenameStrategy;
import org.eclipse.xtext.ui.refactoring.ui.IRenameElementContext;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations;
import org.eclipse.xtext.xbase.ui.jvmmodel.refactoring.AbstractJvmModelRenameStrategy;

import com.google.inject.Inject;

/**
 * Encapsulates the model changes of a rename refactoring.
 */
@SuppressWarnings("restriction")
public class PureXbaseRenameStrategy extends AbstractJvmModelRenameStrategy {

	public static class Provider extends DefaultRenameStrategy.Provider {

		@Inject
		private IJvmModelAssociations jvmModelAssociations;

		@Override
		public IRenameStrategy get(EObject targetElement, IRenameElementContext renameElementContext) {
			EAttribute nameAttribute = getNameAttribute(targetElement);
			if(nameAttribute == null)
				return null;
			return new PureXbaseRenameStrategy(targetElement, nameAttribute, getOriginalNameRegion(targetElement,
					nameAttribute), getNameRuleName(targetElement, nameAttribute), getValueConverterService(),
					jvmModelAssociations);
		}
	}

	protected PureXbaseRenameStrategy(EObject targetElement, EAttribute nameAttribute, ITextRegion originalNameRegion,
			String nameRuleName, IValueConverterService valueConverterService,
			IJvmModelAssociations jvmModelAssociations) {
		super(targetElement, nameAttribute, originalNameRegion, nameRuleName, valueConverterService,
				jvmModelAssociations);
	}

	@Override
	protected void setInferredJvmElementName(String name, EObject renamedSourceElement) {
		/*
		 * TODO: rename inferred elements as you would in IJvmModelInferrer
		 */
	}
}

/*
 * generated by Xtext
 */
package org.eclipse.xtext.ui.tests.refactoring.parser.antlr;

import java.io.InputStream;
import org.eclipse.xtext.parser.antlr.IAntlrTokenFileProvider;

public class ReferringTestLanguageAntlrTokenFileProvider implements IAntlrTokenFileProvider {

	@Override
	public InputStream getAntlrTokenFile() {
		ClassLoader classLoader = getClass().getClassLoader();
		return classLoader.getResourceAsStream("org/eclipse/xtext/ui/tests/refactoring/parser/antlr/internal/InternalReferringTestLanguage.tokens");
	}
}

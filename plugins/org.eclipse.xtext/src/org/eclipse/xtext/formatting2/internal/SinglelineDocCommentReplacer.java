/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.formatting2.internal;

import org.eclipse.xtext.formatting2.ITextReplacerContext;
import org.eclipse.xtext.formatting2.ITextSegment;
import org.eclipse.xtext.formatting2.regionaccess.IComment;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class SinglelineDocCommentReplacer extends SinglelineCommentReplacer {

	public SinglelineDocCommentReplacer(IComment comment, String prefix) {
		super(comment, prefix);
	}

	public ITextReplacerContext createReplacements(ITextReplacerContext context) {
		ITextSegment firstSpace = getFirstSpace();
		if (firstSpace != null) {
			if (hasEmptyBody())
				context.replaceText(firstSpace, "");
			else
				context.replaceText(firstSpace, " ");
		}
		return context;
	}

	@Override
	public void configureWhitespace(WhitespaceReplacer leading, WhitespaceReplacer trailing) {
	}

}

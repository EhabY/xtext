/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.junit4.formatter;

import java.util.LinkedHashMap;

import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.eclipse.xtext.xbase.lib.Procedures;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class FormatterTestRequest {
	boolean allowSyntaxErrors = false;
	private CharSequence expectation;
	private FormatterRequest request;
	private CharSequence toBeFormatted;

	// boolean allowUnformattedGaps = false
	// boolean allowReplacementsAcrossTokens = false;

	public CharSequence getExpectation() {
		return expectation;
	}

	public CharSequence getExpectationOrToBeFormatted() {
		if (expectation != null)
			return expectation;
		return toBeFormatted;
	}

	public MapBasedPreferenceValues getOrCreateMapBasedPreferences() {
		ITypedPreferenceValues preferences = request.getPreferences();
		if (preferences instanceof MapBasedPreferenceValues)
			return (MapBasedPreferenceValues) preferences;
		LinkedHashMap<String, String> newMap = Maps.<String, String> newLinkedHashMap();
		MapBasedPreferenceValues result = new MapBasedPreferenceValues(preferences, newMap);
		request.setPreferenceValues(result);
		return result;
	}

	public FormatterRequest getRequest() {
		return request;
	}

	public CharSequence getToBeFormatted() {
		return toBeFormatted;
	}

	public boolean isAllowSyntaxErrors() {
		return allowSyntaxErrors;
	}

	public FormatterTestRequest preferences(Procedures.Procedure1<MapBasedPreferenceValues> preferences) {
		MapBasedPreferenceValues map = getOrCreateMapBasedPreferences();
		preferences.apply(map);
		return this;
	}

	public FormatterTestRequest setAllowSyntaxErrors(boolean allowSyntaxErrors) {
		this.allowSyntaxErrors = allowSyntaxErrors;
		return this;
	}

	public FormatterTestRequest setExpectation(CharSequence expectation) {
		this.expectation = expectation;
		return this;
	}

	@Inject
	public FormatterTestRequest setRequest(FormatterRequest request) {
		this.request = request;
		return this;
	}

	public FormatterTestRequest setToBeFormatted(CharSequence toBeFormatted) {
		this.toBeFormatted = toBeFormatted;
		return this;
	}

}
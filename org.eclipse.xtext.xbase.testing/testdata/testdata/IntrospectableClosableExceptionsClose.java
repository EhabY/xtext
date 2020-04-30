/**
 * Copyright (c) 2019, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package testdata;

public class IntrospectableClosableExceptionsClose extends AbstractIntrospectableClosable {
	@Override
	public void close() throws CloseException {
		this.isOpen = false;
		throw new CloseException();
	}
}

/*******************************************************************************
 * Copyright (c) 2012 - 2016 Signal Iduna Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Signal Iduna Corporation - initial API and implementation
 * akquinet AG
 * itemis AG
 *******************************************************************************/
package org.testeditor.fixture.core;

/**
 * Used for reporting actions during test execution. Each action can be
 * described unambiguously by a combination of SemanticUnit x Action x Message
 * 
 * Listeners can be registered to be informed about actions. 
 */
public interface TestRunReporter {
	enum SemanticUnit {
		TEST, SPECIFICATION_STEP, COMPONENT, STEP
	}; // order is relevant!

	enum Action {
		ENTER, LEAVE
	};

	/**
	 * called by test execution to indicate that unit x Action.ENTER x msg is
	 * executed
	 */
	void enter(SemanticUnit unit, String msg);

	/**
	 * called by test execution to indicate that unit x Action.LEAVE is
	 * executed
	 */
	void leave(SemanticUnit unit);

	/** listen to any action */
	void addListener(TestRunListener listener);

	void removeListener(TestRunListener listener);
}

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
 * described unambiguously by a combination of SemanticUnit x Position x Message
 * 
 * Listeners can be registered to be informed about actions. The can either
 * register for SemanticUnit x Position, Position, SemanticUnit or for all
 * actions.
 */
public interface TestRunReporter {
	enum SemanticUnit {
		TEST, SPECIFICATION, COMPONENT, STEP
	}; // order is relevant!

	enum Position {
		ENTER, LEAVE
	};

	/**
	 * called by test execution to indicate that unit x Position.ENTER x msg is
	 * executed
	 */
	void enter(SemanticUnit unit, String msg);

	/**
	 * called by test execution to indicate that unit x Position.LEAVE is
	 * executed
	 */
	void leave(SemanticUnit unit);

	/** listen to exact matches of unit x position */
	void addListener(SemanticUnit unit, Position position, TestRunListener listener);

	/** listen to actions of unit x any position */
	void addListener(SemanticUnit unit, TestRunListener listener);

	/** listen to actions of any unit x position */
	void addListener(Position position, TestRunListener listener);

	/** listen to any action */
	void addListener(TestRunListener listener);

	void removeListener(SemanticUnit unit, Position position, TestRunListener listener);

	void removeListener(Position position, TestRunListener listener);

	void removeListener(SemanticUnit unit, TestRunListener listener);

	void removeListener(TestRunListener listener);
}

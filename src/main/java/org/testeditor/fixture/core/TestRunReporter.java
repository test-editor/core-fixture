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
public interface TestRunReporter extends StringMasker {
	enum SemanticUnit {
		TEST(4), SPECIFICATION_STEP(3), COMPONENT(2), STEP(1);

		private final int rank; // internal, never to be used outside this enum

		private SemanticUnit(final int rank) {
			this.rank = rank;
		}

		/**
		 * compare two SemanticUnit ranks
		 * 
		 * @return < 0 if other is of lower rank<br/>
		 *         0 if both are of equal rank<br/>
		 *         > 0 if other is of higher rank<br/>
		 */
		public int compareRank(SemanticUnit other) {
			return rank - other.rank;
		}

	};

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

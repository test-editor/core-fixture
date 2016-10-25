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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Implementation that automatically executes leave when entering equal level or
 * higher level ordinal of SemanticUnit (see
 * TestDefaultTestRunReport.testSemanticUnitLeaveMultipleLevel)
 */
public class DefaultTestRunReporter implements TestRunReporter {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractTestCase.class);
	
	// logger (implemented as listener) is always reported on before all other listeners!
	private final TestRunListener logListener = new DefaultLoggingListener();

	// per semantic unit only one may be active
	private Map<SemanticUnit, String> enteredUnits = new LinkedHashMap<SemanticUnit, String>();

	private List<TestRunListener> listeners = new ArrayList<TestRunListener>();

	@Override
	public void enter(SemanticUnit unit, String msg) {
		if (enteredUnits.containsKey(unit)) {
			leave(unit); // must leave before entering a new one
		}
		
		if(unit==SemanticUnit.TEST){
			MDC.put("TestName", "TE-Test: " + msg.replaceAll("^.*\\.",""));
		}

		informListeners(unit, Action.ENTER, msg);
		enteredUnits.put(unit, msg);
	}

	/**
	 * when leaving this unit, make sure that all lower level semantic units
	 * that were entered are left, too => when leaving SemanticUnit.TEST
	 * (highest level), all other entered units are left!
	 */
	@Override
	public void leave(SemanticUnit unit) {
		for (SemanticUnit unitToLeave : enteredSortedUnitsOfEqualOrHigherOrder(unit)) {
			informListeners(unitToLeave, Action.LEAVE, enteredUnits.get(unitToLeave));
			enteredUnits.remove(unitToLeave);
		}

		if(unit==SemanticUnit.TEST){
			MDC.remove("TestName");
		}
	}

	/**
	 * return a reverse sorted list of SemanticUnits that were entered of equal
	 * or higher ordinal
	 */
	private List<SemanticUnit> enteredSortedUnitsOfEqualOrHigherOrder(SemanticUnit unit) {
		return enteredUnits.keySet().stream() //
				.sorted((u1, u2) -> Integer.compare(u2.ordinal(), u1.ordinal())) // reverse
				.filter((u) -> u.ordinal() >= unit.ordinal()) // only >= ordinal
				.collect(Collectors.toList());
	}

	/**
	 * make sure that all registered listeners are informed, order is not
	 * guaranteed
	 */
	private void informListeners(SemanticUnit unit, Action action, String msg) {
		logListener.reported(unit, action, msg); // logListener is always reported to first!
		for (TestRunListener listener : listeners) {
			try { // make sure that an exception is handled gracefully, so that
					// other listeners are informed, too
				listener.reported(unit, action, msg);
			} catch (Exception e) {
				logger.warn("Listener threw an exception processing unit='" + unit + "', action='" + action
						+ "', msg='" + msg + "'.", e);
			}
		}
	}


	@Override
	public void addListener(TestRunListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(TestRunListener listener) {
		listeners.remove(listener);
	}

}

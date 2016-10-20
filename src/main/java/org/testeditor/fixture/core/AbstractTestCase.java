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

import org.junit.After;
import org.junit.Before;
import org.slf4j.MDC;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;

/**
 * Class from which all generated unit tests are (transitively) derived
 */
public class AbstractTestCase {

	protected final TestRunReporter reporter;
	private final TestRunListener logListener;
	
	public AbstractTestCase() {
		// initialization is done in ctor to allow other ctors to access reporter
		// to allow registration before the first event is reported (ENTER TEST)
		reporter=createTestRunReporter();
		logListener=createLoggingListener();
	}

	@Before
	public void initTestLaunch() {
		MDC.put("TestName", "TE-Test: " + getClass().getSimpleName());
		reporter.addListener(logListener); // listen to all events!
		reporter.enter(SemanticUnit.TEST, getClass().getName());
	}

	@After
	public void finishtestLaunch() {
		reporter.leave(SemanticUnit.TEST);
		reporter.removeListener(logListener);
		MDC.remove("TestName");
	}
	
	// may be overridden to provide alternate implementations of the test run reporter
	protected TestRunReporter createTestRunReporter() {
		return new DefaultTestRunReporter();
	}
	
	// may be overriden to provide alternate implementation of the logging listener
	protected TestRunListener createLoggingListener() {
		return new DefaultLoggingListener();
	}

}

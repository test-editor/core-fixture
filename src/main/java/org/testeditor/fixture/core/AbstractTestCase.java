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

public class AbstractTestCase {

	protected TestRunReporter reporter = new DefaultTestRunReporter();
	private TestRunListener logListener = new DefaultLoggingListener();

	@Before
	public void initTestLaunch() {
		MDC.put("TestName", "TE-Test: " + getClass().getSimpleName());
		reporter.addListener(logListener);
		reporter.enter(SemanticUnit.TEST, getClass().getName());
	}

	@After
	public void finishtestLaunch() {
		reporter.leave(SemanticUnit.TEST);
		reporter.removeListener(logListener);
		MDC.remove("TestName");
	}

}

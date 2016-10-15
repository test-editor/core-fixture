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

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class AbstractTestCase {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractTestCase.class);
	private long start;

	@Before
	public void initTestLaunch() {
		MDC.put("TestName", "TE-Test: " + this.getClass().getSimpleName());
		logger.info("****************************************************");
		logger.info("Running test for {}", this.getClass().getName());
		start = System.currentTimeMillis();
		logger.info("****************************************************");
	}

	@After
	public void finishtestLaunch() {
		MDC.remove("TestName");
		logger.info("****************************************************");
		logger.info("Test {} finished with {} sec. duration.", this.getClass().getSimpleName(),
				TimeUnit.SECONDS.convert(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS));
		logger.info("****************************************************");
	}

}

/*******************************************************************************
 * Copyright (c) 2012 - 2015 Signal Iduna Corporation and others.
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

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class AbstractTestCase {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractTestCase.class);

	@Before
	public void initTestLaunch() {
		MDC.put("TestName", this.getClass().getSimpleName());
		logger.info("*****************************");
		logger.info("Running test for ()", this.getClass().getName());
	}

}

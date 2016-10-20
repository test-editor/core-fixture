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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testeditor.fixture.core.TestRunReporter.Position;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;

/**
 * Default implementation of a test run logger.
 * Logs enter and leave of TEST.
 * Logs enter (only) for SPECIFICATION, COMPONENT and STEP
 */
public class DefaultLoggingListener implements TestRunListener {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractTestCase.class);
	private long start;

	@Override
	public void reported(SemanticUnit unit, Position position, String message) {
		switch (unit) {
		case TEST:
			logTest(position, message);
			break;
		case SPECIFICATION:
			logSpecification(position, message);
			break;
		case COMPONENT:
			logComponent(position, message);
			break;
		case STEP:
			logStep(position, message);
			break;
		default:
			logger.error("Unknown semantic test unit='{}' encountered during logging through class='{}'.", unit, getClass().getName());
			break;
		}
	}

	private void logTest(Position position, String message) {
		switch (position) {
		case ENTER:
			logger.info("****************************************************");
			logger.info("Running test for {}", message);
			start = System.currentTimeMillis();
			logger.info("****************************************************");
			break;
		case LEAVE:
			logger.info("****************************************************");
			logger.info("Test {} finished with {} sec. duration.", message,
					TimeUnit.SECONDS.convert(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS));
			logger.info("****************************************************");
			break;
		default:
			logger.error("Unknown test position='{}' encountered during logging through class='{}'.", position, getClass().getName());
			break;
		}
	}
	
	private void logSpecification(Position position, String message) {
		if (position == Position.ENTER) {
			logger.info(" [Test spec] * {}", message);
		}
	}

	private void logComponent(Position position, String message) {
		if (position == Position.ENTER) {
			logger.trace(" [Component] {}:", message);
		}
	}

	private void logStep(Position position, String message) {
		if (position == Position.ENTER) {
			logger.trace(" [Test step] - {}", message);
		}
	}

}

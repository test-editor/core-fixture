/*******************************************************************************
 * Copyright (c) 2012 - 2017 Signal Iduna Corporation and others.
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
import org.testeditor.fixture.core.TestRunReporter.Action;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;

/**
 * Default implementation of a test run logger. Logs enter and leave of TEST.
 * Logs enter (only) for SPECIFICATION, COMPONENT and STEP
 */
public class DefaultLoggingListener implements TestRunListener {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractTestCase.class);
    private long start;

    @Override
    public void reported(SemanticUnit unit, Action action, String message) {
        switch (unit) {
            case TEST:
                logTest(action, message);
                break;
            case SPECIFICATION_STEP:
                logSpecification(action, message);
                break;
            case COMPONENT:
                logComponent(action, message);
                break;
            case STEP:
                logStep(action, message);
                break;
            default:
                logger.error("Unknown semantic test unit='{}' encountered during logging through class='{}'.", unit,
                        getClass().getName());
                break;
        }
    }

    private void logTest(Action action, String message) {
        switch (action) {
            case ENTER:
                logger.info("****************************************************");
                logger.info("Running test for {}", message);
                start = System.nanoTime();
                logger.info("****************************************************");
                break;
            case LEAVE:
                logger.info("****************************************************");
                logger.info("Test {} finished with {} sec. duration.", message,
                        TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
                logger.info("****************************************************");
                break;
            default:
                logger.error("Unknown test action='{}' encountered during logging through class='{}'.", action,
                        getClass().getName());
                break;
        }
    }

    private void logSpecification(Action action, String message) {
        if (action == Action.ENTER) {
            logger.info(" [Spec step] * {}", message);
        }
    }

    private void logComponent(Action action, String message) {
        if (action == Action.ENTER) {
            logger.trace(" [Component] ** {}:", message);
        }
    }

    private void logStep(Action action, String message) {
        if (action == Action.ENTER) {
            logger.trace(" [Test step] *** {}", message);
        }
    }

}

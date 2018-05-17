/*******************************************************************************
 * Copyright (c) 2012 - 2018 Signal Iduna Corporation and others.
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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testeditor.fixture.core.TestRunReporter.Action;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;
import org.testeditor.fixture.core.TestRunReporter.Status;

/**
 * Default implementation of a test run logger. Logs enter and leave of TEST.
 * Logs enter (only) for SPECIFICATION, COMPONENT and STEP
 */
public class DefaultLoggingListener implements TestRunListener {

    private static final int INDENT = 2;

    protected static final Logger logger = LoggerFactory.getLogger(AbstractTestCase.class);
    private long start;
    private int currentIndent = 0;

    @Override
    public void reported(SemanticUnit unit, Action action, String message, String id, Status status,
            Map<String, String> variables) {
        if (Action.ENTER.equals(action)) {
            logTechnicalReference(unit, action, message, id);
        }
        switch (unit) {
            case TEST:
                logTest(action, message, id, status);
                break;
            case SPECIFICATION_STEP:
                logUnit("Spec step", action, message, id, status);
                break;
            case COMPONENT:
                logUnit("Component", action, message, id, status);
                break;
            case STEP:
                logUnit("Test step", action, message, id, status);
                break;
            case MACRO_LIB:
                logUnit("Macro lib", action, message, id, status);
                break;
            case MACRO:
                logUnit("Macro    ", action, message, id, status);
                break;
            default:
                logger.error("Unknown semantic test unit='{}' encountered during logging through class='{}'.", unit,
                        getClass().getName());
                break;
        }
        switch (action) {
            case ENTER:
                currentIndent += INDENT;
                break;
            case LEAVE:
                currentIndent -= INDENT;
                break;
            default:
                logger.error("Unknown action='{}' encountered during logging through class='{}'.", action,
                        getClass().getName());
                break;
        }
        if (Action.LEAVE.equals(action)) {
            logTechnicalReference(unit, action, message, id);
        }
    }

    private void logTest(Action action, String message, String id, Status status) {
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

    private String indentPrefix() {
        return StringUtils.repeat(' ', currentIndent);
    }

    private void logUnit(String unitText, Action action, String message, String id, Status status) {
        switch (action) {
            case ENTER:
                logger.trace(indentPrefix() + "->{}[{}] {} [Status={}]", unitText, id, message, status);
                break;
            case LEAVE:
                logger.trace(indentPrefix() + "<-{}[{}] {} [Status={}]", unitText, id, message, status);
                break;
            default:
                // do nothing
                break;
        }
    }

    private void logTechnicalReference(SemanticUnit unit, Action action, String message, String id) {
        logger.info(indentPrefix() + "@" + unit.toString() + ":" + action.toString() + ":"
                + Integer.toHexString(message.hashCode()) + ":" + id);
    }

}

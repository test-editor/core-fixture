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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;
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

    protected static final Logger logger = LoggerFactory.getLogger(DefaultLoggingListener.class);
    private long start;
    private int currentIndent = 0;
    
    protected final Deque<String> openLeaveStack = new ArrayDeque<>();

    /** copy of StringEscapeUtils.ESCAPE_JAVA, except for the unicode translation */
    public static final CharSequenceTranslator ESCAPE_JAVA;

    static {
        final Map<CharSequence, CharSequence> escapeJavaMap = new HashMap<>();
        escapeJavaMap.put("\"", "\\\"");
        escapeJavaMap.put("\\", "\\\\");
        ESCAPE_JAVA = new AggregateTranslator(
            new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap)),
            new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE)/*,
            JavaUnicodeEscaper.outsideOf(32, 0x7f)*/ // difference to original definition: DO NOT USE UNICODE ESCAPING
        );
    }

    @Override
    public void reported(SemanticUnit unit, Action action, String message, String id, Status status,
            Map<String, String> variables) {
        if (Action.ENTER.equals(action)) {
            logTechnicalReference(unit, action, message, id);
            // don't push test leave marker, since stack unrolling is done within a test, not exiting it!
            if (!SemanticUnit.TEST.equals(unit)) {
                openLeaveStack.push(buildTechnicalReferenceString(unit, Action.LEAVE, message, id));
            }
        }
        switch (unit) {
            case TEST:
                logTest(action, message, id, status);
                break;
            case SPECIFICATION_STEP:
                logUnit("Spec step", action, message, id, status, variables);
                break;
            case COMPONENT:
                logUnit("Component", action, message, id, status, variables);
                break;
            case STEP:
                logUnit("Test step", action, message, id, status, variables);
                break;
            case MACRO_LIB:
                logUnit("Macro lib", action, message, id, status, variables);
                break;
            case MACRO:
                logUnit("Macro    ", action, message, id, status, variables);
                break;
            case CLEANUP:
                logUnit("Cleanup  ", action, message, id, status, variables);
                break;
            case SETUP:
                logUnit("Setup    ", action, message, id, status, variables);
                break;
            default:
                logTechSetupProblem();
                logger.debug("Unknown semantic test unit='{}' encountered during logging through class='{}'.", unit,
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
                logTechSetupProblem();
                logger.debug("Unknown action='{}' encountered during logging through class='{}'.", action,
                        getClass().getName());
                break;
        }
        if (Action.LEAVE.equals(action)) {
            // don't (try to) pop test leave marker, since stack unrolling is done within a test, not exiting it!
            if (!SemanticUnit.TEST.equals(unit)) {
                openLeaveStack.pop();
            }
            logTechnicalReference(unit, action, message, id);
        }
    }

    private void logTest(Action action, String message, String id, Status status) {
        switch (action) {
            case ENTER:
                logger.info("****************************************************");
                logger.info("Running test for {}", escape(message));
                start = System.nanoTime();
                logger.info("****************************************************");
                break;
            case LEAVE:
                logger.info("****************************************************");
                logger.info("Test {} finished with {} sec. duration.", escape(message),
                        TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));
                logger.info("****************************************************");
                break;
            default:
                logTechSetupProblem();
                logger.debug("Unknown test action='{}' encountered during logging through class='{}'.", action,
                        getClass().getName());
                break;
        }
    }

    private String indentPrefix() {
        return StringUtils.repeat(' ', currentIndent);
    }

    private void logUnit(String unitText, Action action, String message, String id, Status status,
                         Map<String, String> variables) {
        switch (action) {
            case ENTER:
                logger.trace(indentPrefix() + "->{}[{}] {} [Status={}]", unitText, id, escape(message), status);
                logger.info(indentPrefix() + escape(message));
                for (String variable: variables.keySet()) {
                    logger.info(indentPrefix() + "  with " + escape(variable) + " = \""
                                + escape(variables.get(variable)) + "\"");
                }
                break;
            case LEAVE:
                logger.trace(indentPrefix() + "<-{}[{}] {} [Status={}]", unitText, id, escape(message), status);
                break;
            default:
                // do nothing
                break;
        }
    }

    private String escape(String message) {
        return ESCAPE_JAVA.translate(message);
    }

    private String buildTechnicalReferenceString(SemanticUnit unit, Action action, String message, String id) {
        return indentPrefix() + "@" + unit.toString() + ":" + action.toString() + ":"
                + Integer.toHexString(message.hashCode()) + ":" + id;
    }

    private void logTechnicalReference(SemanticUnit unit, Action action, String message, String id) {
        logger.trace(buildTechnicalReferenceString(unit, action, message, id));
    }

    @Override
    public void reportFixtureExit(FixtureException fixtureException) {
        logger.error(indentPrefix() + "Test failed because of a fixture exception: "
                     + fixtureException.getLocalizedMessage());
        if (fixtureException.getKeyValueStore() != null) {
            for (String variable: fixtureException.getKeyValueStore().keySet()) {
                logger.error(indentPrefix() + "  with " + escape(variable) + " = \""
                            + escape(fixtureException.getKeyValueStore().get(variable).toString()) + "\"");
            }
        }
        logger.error(indentPrefix() + "Please contact an administrator.");
        logger.trace("FixtureException", fixtureException);
        logPendingTechnicalLeaveMessages();
        currentIndent = INDENT;
    }
    
    @Override
    public void reportExceptionExit(Exception exception) {
        logger.error("Test failed because of an unanticipated exception: " + exception.getLocalizedMessage());
        logger.error("Please contact an administrator.");
        logger.trace("Exception", exception);
        logPendingTechnicalLeaveMessages();
        currentIndent = INDENT;
    }

    @Override
    public void reportAssertionExit(AssertionError assertionError) {
        logger.error(indentPrefix() + "Assertion failed: " + assertionError.getLocalizedMessage());
        logger.error(indentPrefix() + "Please check the expectation and the actual value.");
        logger.trace("AssertionError: " + assertionError.getLocalizedMessage(), assertionError);
        logPendingTechnicalLeaveMessages();
        currentIndent = INDENT;
    }

    private void logTechSetupProblem() {
        logger.warn("Logging failed because of technical setup problems. Please contact an administrator.");
    }

    private void logPendingTechnicalLeaveMessages() {
        while (!openLeaveStack.isEmpty()) {
            logger.trace(openLeaveStack.peek());
            openLeaveStack.pop();
        }
    }

}

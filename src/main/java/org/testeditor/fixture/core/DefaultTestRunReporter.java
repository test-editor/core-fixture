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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Implementation that automatically executes leave when entering equal or
 * higher rank of SemanticUnit (see
 * TestDefaultTestRunReport.testSemanticUnitLeaveMultipleLevel)
 */
public class DefaultTestRunReporter implements TestRunReporter {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractTestCase.class);

    // on enter, logger (implemented as listener) is always reported on before all other
    // listeners; on leave, it is reported on after all others.
    private final TestRunListener logListener;

    // per semantic unit only one may be active
    private final List<TestRunListener> listeners = new ArrayList<TestRunListener>();

    public DefaultTestRunReporter() {
        this(new DefaultLoggingListener());
    }
    
    public DefaultTestRunReporter(TestRunListener logListener) {
        this.logListener = logListener;
    }
    
    @Override
    public void enter(SemanticUnit unit, String msg, String id, Status status, Map<String, String> variables) {
        if (unit == SemanticUnit.TEST) {
            MDC.put("TestName", "TE-Test: " + msg.replaceAll("^.*\\.", ""));
        }
        informAllListeners(unit, Action.ENTER, msg, id, status, variables);
    }

    /**
     * when leaving this unit, make sure that all lower ranked semantic units that
     * were entered are left, too => when leaving SemanticUnit.TEST (highest rank),
     * all other entered units are left!
     */
    @Override
    public void leave(SemanticUnit unit, String msg, String id, Status status, Map<String, String> variables) {
        informAllListeners(unit, Action.LEAVE, msg, id, status, variables);

        if (unit == SemanticUnit.TEST) {
            MDC.remove("TestName");
        }
    }

    /**
     * make sure that all registered listeners are informed, order is not guaranteed
     */
    private void informAllListeners(SemanticUnit unit, Action action, String msg, String id, Status status,
            Map<String, String> variables) {
        if (action == action.ENTER) {
            informLogListener(unit, action, msg, id, status, variables);
            informRegisteredListeners(unit, action, msg, id, status, variables);
        } else {
            informRegisteredListeners(unit, action, msg, id, status, variables);
            informLogListener(unit, action, msg, id, status, variables);            
        }
    }
    
    private void informLogListener(SemanticUnit unit, Action action, String msg, String id, Status status,
            Map<String, String> variables) {
        try {
            logListener.reported(unit, action, msg, id, status, variables); 
        } catch (Exception e) {
            logger.warn("Log Listener " + logListener.getClass().getName() + " threw an exception processing unit='"
                    + unit + "', action='" + action + "', msg='" + msg + "'.", e);
        }
    }
    
    private void informRegisteredListeners(SemanticUnit unit, Action action, String msg, String id, Status status,
            Map<String, String> variables) {
        for (TestRunListener listener : listeners) {
            try {
                // make sure that an exception is handled gracefully, so that
                // other listeners are informed, too
                listener.reported(unit, action, msg, id, status, variables);
            } catch (Exception e) {
                logger.warn("Listener " + listener.getClass().getName() + " threw an exception processing unit='" + unit
                        + "', action='" + action + "', msg='" + msg + "'.", e);
            }
        }
    }

    @Override
    public void addListener(TestRunListener listener) {
        if (listener != null) {
            listeners.add(listener);
        } else {
            logger.warn("Cannot add listener that is NULL!");
        }
    }

    @Override
    public void removeListener(TestRunListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void fixtureExit(FixtureException fixtureException) {
        try {
            logListener.reportFixtureExit(fixtureException); // logListener is always reported to first!
        } catch (Exception e) {
            logger.warn(
                    "Log listener '" + logListener.getClass().getName() + "' threw an exception reporting fixture exit",
                    e);
        }
        for (TestRunListener listener : listeners) {
            try {
                // make sure that an exception is handled gracefully, so that
                // other listeners are informed, too
                listener.reportFixtureExit(fixtureException);
            } catch (Exception e) {
                logger.warn(
                        "Listener '" + listener.getClass().getName() + "' threw an exception reporting fixture exit",
                        e);
            }
        }
    }

    @Override
    public void exceptionExit(Exception exception) {
        try {
            logListener.reportExceptionExit(exception); // logListener is always reported to first!
        } catch (Exception e) {
            logger.warn("Log listener '" + logListener.getClass().getName()
                    + "' threw an exception reporting exception exit", e);
        }
        for (TestRunListener listener : listeners) {
            try {
                // make sure that an exception is handled gracefully, so that
                // other listeners are informed, too
                listener.reportExceptionExit(exception);
            } catch (Exception e) {
                logger.warn(
                        "Listener '" + listener.getClass().getName() + "' threw an exception reporting exception exit",
                        e);
            }
        }
    }

    @Override
    public void assertionExit(AssertionError assertionError) {
        try {
            logListener.reportAssertionExit(assertionError); // logListener is always reported to first!
        } catch (Exception e) {
            logger.warn("Log listener '" + logListener.getClass().getName()
                    + "' threw an exception reporting assertion exit", e);
        }
        for (TestRunListener listener : listeners) {
            try {
                // make sure that an exception is handled gracefully, so that
                // other listeners are informed, too
                listener.reportAssertionExit(assertionError);
            } catch (Exception e) {
                logger.warn(
                        "Listener '" + listener.getClass().getName() + "' threw an exception reporting assertion exit",
                        e);
            }
        }
    }

}

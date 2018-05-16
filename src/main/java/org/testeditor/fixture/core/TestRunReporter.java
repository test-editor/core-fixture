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

/**
 * Used for reporting actions during test execution. Each action can be
 * described unambiguously by a combination of SemanticUnit x Action x Message
 * 
 * Listeners can be registered to be informed about actions.
 */
public interface TestRunReporter {
    enum SemanticUnit {
        TEST, SPECIFICATION_STEP, COMPONENT, STEP, MACRO_LIB, MACRO, SETUP, CLEANUP
    }

    enum Action {
        ENTER, LEAVE
    }

    enum Status {
        OK, // Was run, everything ok
        UNKNOWN, // Not run yet, no status
        STARTED, // Running
        ABORTED, // Aborted because of unexpected problems during execution
        ERROR, // Was run but ran into an assertion error
        WARNING, // Was run (and continues) but ran into a warning
        INFO // Was run (and continues) and ran into an information
    }

    /**
     * called by test execution to indicate that unit x Action.ENTER x msg is
     * executed
     */
    void enter(SemanticUnit unit, String msg, String id, Status status, Map<String, String> variableParameters);

    /**
     * called by test execution to indicate that unit x Action.LEAVE is executed
     */
    void leave(SemanticUnit unit, String msg, String id, Status status, Map<String, String> variableParameters);

    /** listen to any action */
    void addListener(TestRunListener listener);

    void removeListener(TestRunListener listener);

}

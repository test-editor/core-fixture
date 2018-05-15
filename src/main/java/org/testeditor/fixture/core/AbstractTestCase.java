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

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;

/**
 * Class from which all generated unit tests are (transitively) derived
 */
public class AbstractTestCase {

    protected final TestRunReporter reporter;
    protected long runningNumber;

    /**
     * default ctor
     */
    public AbstractTestCase() {
        // initialization is done in ctor to allow other ctors to access reporter
        // to allow registration before the first event is reported (ENTER TEST)
        reporter = createTestRunReporter();
        runningNumber = 0;
    }
    
    protected String getNewId() {
        runningNumber++;
        return "ID" + Long.toString(runningNumber);
    }

    @Before
    public void initTestLaunch() {
        reporter.enter(SemanticUnit.TEST, getClass().getName(), "IDROOT", "?", null);
    }

    @After
    public void finishtestLaunch() {
        reporter.leave(SemanticUnit.TEST, getClass().getName(), "IDROOT", "OK", null);
    }

    // may be overridden to provide alternate implementations of the test run
    // reporter
    protected TestRunReporter createTestRunReporter() {
        return new DefaultTestRunReporter();
    }
    
    /**
     * utility builder function to pass varable-names and values as hashmap
     * @param strings make sure there is an even number of parameters used!
     * @return
     */
    public static Map<String, String> variables(String...strings) {
        HashMap<String,String> result = new HashMap<>();

        for (int i=0; i<strings.length / 2; i++) {
            result.put(strings[i*2], strings[i*2+1]);
        }

        return result;
    }

}

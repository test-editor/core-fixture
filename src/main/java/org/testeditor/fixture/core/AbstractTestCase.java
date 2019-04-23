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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;
import org.testeditor.fixture.core.TestRunReporter.Status;

/**
 * Class from which all generated unit tests are (transitively) derived
 *
 * Every generated unit test class inherits (transitively in case of configs)
 * from this abstract test class! Every generated class makes use of the
 * interface of this class:
 *
 * <pre>
 * - calls to the reporter are generated (to report current test status)
 * - newVarId s are generated to hold call tree ids used for enter/leave
 *   reporting
 * - initializsation/cleanup of junit
 * - finishTestWith to inform the test of the final status
 * </pre>
 */
public class AbstractTestCase {
    private static final String PARAMETER_INDEX_DELIMITER = "*"; // delimiter should ideally be URL-safe (and '*' is)
    private static final Pattern TEST_NAME_PATTERN = Pattern.compile("\\[(\\d+)\\]");

    // reporter for current test status, register with this reported for addition listeners
    protected final TestRunReporter reporter;
    // a number that is used to generate variables, holding call tree ids (used for enter/leave reporting)
    private long runningNumber;
    // (probably an) assertion error if test is not finished as expected
    private Status finalStatus = Status.ERROR;
    
    protected static final Logger logger = LoggerFactory.getLogger(AbstractTestCase.class);
    
    // a map of running numbers that are used to provide call tree ids (that are structural stable)
    private Map<String, Long> runningNumberMap;

    /**
     * default constructor
     */
    public AbstractTestCase() {
        this("");
    }
    
    /**
     * Create a new test with a test name.
     * This is used for parameterized tests; the test name is appended to the
     * test run id, so runs with different parameter sets can be distinguished
     * and identified.
     * @param testName the name of the test
     */
    public AbstractTestCase(String testName) {
        // initialization is done in ctor to allow other ctors to access reporter
        // to allow registration before the first event is reported (ENTER TEST)
        reporter = createTestRunReporter();
        List<TestRunListener> listeners = additionalListeners();
        if (listeners != null) {
            additionalListeners().stream().forEach(listener -> {
                reporter.addListener(listener);
            });
        }
        runningNumber = 0;
        runningNumberMap = new HashMap<String, Long>();

        initializeCallTreeListener(testName);

    }

    protected void initializeCallTreeListener(String testName) {
        try {
            String yamlFileName = getEnvVar("TE_CALL_TREE_YAML_FILE");
            if (yamlFileName != null) {
                File yamlFile = new File(yamlFileName);

                String testCaseName = getEnvVar("TE_TESTCASENAME");
                String testRunId = getEnvVar("TE_TESTRUNID");
                if (testName != null && testName.length() > 0) {
                    var matcher = TEST_NAME_PATTERN.matcher(testName.trim());
                    if (matcher.matches()) {
                        testRunId = testRunId + PARAMETER_INDEX_DELIMITER + matcher.group(1);
                    } else {
                        testRunId = testRunId + PARAMETER_INDEX_DELIMITER + testName.trim();
                    }
                }
                String testCommitId = getEnvVar("TE_TESTRUNCOMMITID");
                
                reporter.addListener(new DefaultYamlCallTreeListener(new FileOutputStream(yamlFile, true), 
                        testCaseName, testRunId, testCommitId));
                logger.info("Added yaml call tree listener to test excecution writing to file = \"" 
                        + yamlFileName + "\".");
            }
        } catch (Exception e) {
            logger.warn("Failed to add yaml cal tree listener.", e);
        }
    }

    /**
     * please use nextSubId instead!
     * @return next unique id, generated during runtime of the test
     */
    @Deprecated
    protected String newVarId() {
        logger.warn("Generated code is using deprecated feature."
                + " Please upgrade your testeditor language version (generator).");
        runningNumber++;
        return "ID" + Long.toString(runningNumber);
    }
    
    /**
     * return next sub id of the passed id, generated during runtime of the test
     * @param id  parent id
     * @return next unique sub id
     */
    protected String nextSubId(String id) {
        if (runningNumberMap.containsKey(id)) {
            Long counter = runningNumberMap.get(id);
            counter++;
            runningNumberMap.put(id, counter);
            return id + "-" + counter.toString();
        } else {
            runningNumberMap.put(id, Long.valueOf(0));
            return id + "-0";
        }
    }

    @Before
    public void initTestLaunch() {
        reporter.enter(SemanticUnit.TEST, getClass().getName(), "IDROOT", Status.STARTED, null);
    }

    @After
    public void finishtestLaunch() {
        reporter.leave(SemanticUnit.TEST, getClass().getName(), "IDROOT", this.finalStatus, null);
    }

    /**
     * Call this method with the real test result status, before finishing the
     * execute test method!
     *
     * @param status
     */
    protected void finishedTestWith(Status status) {
        this.finalStatus = status;
    }

    // may be overridden to provide alternate implementations of the test run
    // reporter
    protected TestRunReporter createTestRunReporter() {
        return new DefaultTestRunReporter();
    }

    // may be override to add aditional test run listeners to the reporter
    protected List<TestRunListener> additionalListeners() {
        return Collections.emptyList();
    }

    /**
     * utility builder function to pass varable-names and values as hashmap
     *
     * @param strings make sure there is an even number of parameters used, since
     *            only the first even numbered parameters are packed into the map
     * @return
     */
    public static Map<String, String> variables(String... strings) {
        HashMap<String, String> result = new HashMap<>();

        for (int i = 0; i < strings.length / 2; i++) {
            result.put(strings[i * 2], strings[i * 2 + 1]);
        }

        return result;
    }
    
    private String getEnvVar(String key) {
        String result = System.getenv(key);
        logWarningIfNull(result, "expected environment variable = \"" + key + "\" is empty");
        return result;
    }
    
    private void logWarningIfNull(Object value, String warning) {
        if (value == null) {
            logger.warn(warning);
        }
    }

}

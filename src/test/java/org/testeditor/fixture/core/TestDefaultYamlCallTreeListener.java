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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.testeditor.fixture.core.TestRunReporter.Action;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;
import org.testeditor.fixture.core.TestRunReporter.Status;

import junit.framework.AssertionFailedError;

public class TestDefaultYamlCallTreeListener {

    private DefaultYamlCallTreeListener yamlCallTreeListenerUnderTest = null;
    private ByteArrayOutputStream outputStream = null;

    @Before
    public void setup() {
        outputStream = new ByteArrayOutputStream();
        yamlCallTreeListenerUnderTest = new DefaultYamlCallTreeListener(outputStream, "testcase", "1", "decaf");
    }

    @Test
    public void testYamlOfEnteredTest() throws IOException {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", Status.STARTED,
                AbstractTestCase.variables("a.\"my key\"", "5'\");System.exit(1);", "b", "7"));

        // then
        assertOutputWithoutNanosToEqual(//
                "- \"source\": \"testcase\"\n" + //
                        "  \"testRunId\": \"1\"\n" + //
                        "  \"commitId\": \"decaf\"\n" + //
                        "  \"children\":\n" + //
                        "  - \"node\": \"TEST\"\n" + //
                        "    \"message\": \"test\"\n" + //
                        "    \"id\": \"4711\"\n" + //
                        "    \"preVariables\":\n" + //
                        "      \"b\": \"7\"\n" + //
                        "      \"a.\\\"my key\\\"\": \"5'\\\");System.exit(1);\"\n" + //
                        "    \"children\":\n");
        assertOutputContainsRegex("(?s).*\n    \"enter\": \"[0-9]+\"\n.*");
        assertOutputContainsNoRegex("(?s).*\"leave\":.*");
    }

    @Test
    public void testYamlOfEnteredAndLeftTest() {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.LEAVE, "test", "4711", Status.OK,
                AbstractTestCase.variables("a", "5", "b", "7"));

        // then
        assertOutputWithoutNanosToEqual(//
                "- \"source\": \"testcase\"\n" + //
                        "  \"testRunId\": \"1\"\n" + //
                        "  \"commitId\": \"decaf\"\n" + //
                        "  \"children\":\n" + //
                        "  - \"node\": \"TEST\"\n" + //
                        "    \"message\": \"test\"\n" + //
                        "    \"id\": \"4711\"\n" + //
                        "    \"preVariables\":\n" + //
                        "    \"children\":\n" + //
                        "    \"status\": \"OK\"\n" + //
                        "    \"postVariables\":\n" + //
                        "      \"a\": \"5\"\n" + //
                        "      \"b\": \"7\"\n");
        assertOutputContainsRegex("(?s).*\n    \"enter\": \"[0-9]+\"\n.*");
        assertOutputContainsRegex("(?s).*\n    \"leave\": \"[0-9]+\"\n.*");
    }

    @Test
    public void testYamlEnteredAndLeftDownToTestStep() {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.SPECIFICATION_STEP, Action.ENTER, "spec step", "4712",
                Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.COMPONENT, Action.ENTER, "component", "4713",
                Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.STEP, Action.ENTER, "step", "4714", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.STEP, Action.LEAVE, "step", "4714", Status.OK, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.STEP, Action.ENTER, "step", "4715", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.STEP, Action.LEAVE, "step", "4715", Status.OK, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.COMPONENT, Action.LEAVE, "component", "4713", Status.OK,
                null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.SPECIFICATION_STEP, Action.LEAVE, "spec step", "4712",
                Status.OK, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.LEAVE, "test", "4711", Status.OK, null);

        // then
        assertOutputWithoutNanosToEqual(//
                "- \"source\": \"testcase\"\n" + //
                        "  \"testRunId\": \"1\"\n" + //
                        "  \"commitId\": \"decaf\"\n" + //
                        "  \"children\":\n" + //
                        "  - \"node\": \"TEST\"\n" + //
                        "    \"message\": \"test\"\n" + //
                        "    \"id\": \"4711\"\n" + //
                        "    \"preVariables\":\n" + //
                        "    \"children\":\n" + //
                        "    - \"node\": \"SPECIFICATION_STEP\"\n" + //
                        "      \"message\": \"spec step\"\n" + //
                        "      \"id\": \"4712\"\n" + //
                        "      \"preVariables\":\n" + //
                        "      \"children\":\n" + //
                        "      - \"node\": \"COMPONENT\"\n" + //
                        "        \"message\": \"component\"\n" + //
                        "        \"id\": \"4713\"\n" + //
                        "        \"preVariables\":\n" + //
                        "        \"children\":\n" + //
                        "        - \"node\": \"STEP\"\n" + //
                        "          \"message\": \"step\"\n" + //
                        "          \"id\": \"4714\"\n" + //
                        "          \"preVariables\":\n" + //
                        "          \"children\":\n" + //
                        "          \"status\": \"OK\"\n" + //
                        "          \"postVariables\":\n" + //
                        "        - \"node\": \"STEP\"\n" + // cbuf
                        "          \"message\": \"step\"\n" + //
                        "          \"id\": \"4715\"\n" + //
                        "          \"preVariables\":\n" + //
                        "          \"children\":\n" + //
                        "          \"status\": \"OK\"\n" + //
                        "          \"postVariables\":\n" + //
                        "        \"status\": \"OK\"\n" + //
                        "        \"postVariables\":\n" + //
                        "      \"status\": \"OK\"\n" + //
                        "      \"postVariables\":\n" + //
                        "    \"status\": \"OK\"\n" + //
                        "    \"postVariables\":\n");
    }

    @Test
    public void testYamlOnAssertionError() {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.SPECIFICATION_STEP, Action.ENTER, "spec step", "4712",
                Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reportAssertionExit(new AssertionFailedError("my message\n\"with"));

        // then
        assertOutputWithoutNanosToEqual(//
                "- \"source\": \"testcase\"\n" + //
                        "  \"testRunId\": \"1\"\n" + //
                        "  \"commitId\": \"decaf\"\n" + //
                        "  \"children\":\n" + //
                        "  - \"node\": \"TEST\"\n" + //
                        "    \"message\": \"test\"\n" + //
                        "    \"id\": \"4711\"\n" + //
                        "    \"preVariables\":\n" + //
                        "    \"children\":\n" + //
                        "    - \"node\": \"SPECIFICATION_STEP\"\n" + //
                        "      \"message\": \"spec step\"\n" + //
                        "      \"id\": \"4712\"\n" + //
                        "      \"preVariables\":\n" + //
                        "      \"children\":\n" + //
                        "      \"assertionError\": \"my message\\n\\\"with\"\n" + //
                        "      \"status\": \"ERROR\"\n" + //
                        "      \"postVariables\":\n");
    }

    @Test
    public void testYamlOnException() {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.SPECIFICATION_STEP, Action.ENTER, "spec step", "4712",
                Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reportExceptionExit(new RuntimeException("my message\n\"with"));

        // then
        assertOutputWithoutNanosToEqual(//
                "- \"source\": \"testcase\"\n" + //
                        "  \"testRunId\": \"1\"\n" + //
                        "  \"commitId\": \"decaf\"\n" + //
                        "  \"children\":\n" + //
                        "  - \"node\": \"TEST\"\n" + //
                        "    \"message\": \"test\"\n" + //
                        "    \"id\": \"4711\"\n" + //
                        "    \"preVariables\":\n" + //
                        "    \"children\":\n" + //
                        "    - \"node\": \"SPECIFICATION_STEP\"\n" + //
                        "      \"message\": \"spec step\"\n" + //
                        "      \"id\": \"4712\"\n" + //
                        "      \"preVariables\":\n" + //
                        "      \"children\":\n" + //
                        "      \"exception\": \"my message\\n\\\"with\"\n" + //
                        "      \"status\": \"ERROR\"\n" + //
                        "      \"postVariables\":\n");
    }

    @Test
    public void testYamlOnFixtureException() {
        // given
        Map<String, Object> someMap = new HashMap<>();
        someMap.put("a map key to long", Long.valueOf(42));
        someMap.put("keyToString", "someString");

        Map<String, Object> longMap = new HashMap<>();
        longMap.put("first", Long.valueOf(1));
        longMap.put("second", Long.valueOf(-2));
        longMap.put("nullKey", null);

        Map<String, Object> stringMap = new HashMap<>();
        stringMap.put("1st string", "one");
        stringMap.put("2nd string", "two");
        stringMap.put("nullKey", null);

        ArrayList<Object> someArray = new ArrayList<>();
        someArray.add(longMap);
        someArray.add(stringMap);
        someArray.add(Collections.emptyMap());
        someArray.add(Long.valueOf(44));
        someArray.add("lllaaa");

        Map<String, Object> keyValueStore = new HashMap<>();
        keyValueStore.put("a Map", someMap);
        keyValueStore.put("an Array", someArray);
        keyValueStore.put("an Number", Long.valueOf(100));
        keyValueStore.put("an String", "some string \"\' that needs escaping");

        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.SPECIFICATION_STEP, Action.ENTER, "spec step", "4712",
                Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reportFixtureExit(new FixtureException("message", keyValueStore));

        // then
        assertOutputWithoutNanosToEqual(//
                "- \"source\": \"testcase\"\n" + //
                        "  \"testRunId\": \"1\"\n" + //
                        "  \"commitId\": \"decaf\"\n" + //
                        "  \"children\":\n" + //
                        "  - \"node\": \"TEST\"\n" + //
                        "    \"message\": \"test\"\n" + //
                        "    \"id\": \"4711\"\n" + //
                        "    \"preVariables\":\n" + //
                        "    \"children\":\n" + //
                        "    - \"node\": \"SPECIFICATION_STEP\"\n" + //
                        "      \"message\": \"spec step\"\n" + //
                        "      \"id\": \"4712\"\n" + //
                        "      \"preVariables\":\n" + //
                        "      \"children\":\n" + //
                        "      \"fixtureException\":\n" + //
                        "        \"an Number\": 100\n" + //
                        "        \"fixtureExceptionMessage\": \"message\"\n" + // 
                        "        \"an Array\":\n" + //
                        "          -\n" + //
                        "            \"nullKey\":\n" + //
                        "            \"first\": 1\n" + //
                        "            \"second\": -2\n" + //
                        "          -\n" + //
                        "            \"nullKey\":\n" + //
                        "            \"2nd string\": \"two\"\n" + //
                        "            \"1st string\": \"one\"\n" + //
                        "          -\n" + //
                        "          - 44\n" + //
                        "          - \"lllaaa\"\n" + //
                        "        \"a Map\":\n" + //
                        "          \"keyToString\": \"someString\"\n" + //
                        "          \"a map key to long\": 42\n" + //
                        "        \"an String\": \"some string \\\"' that needs escaping\"\n" + //
                        "      \"status\": \"ERROR\"\n" + //
                        "      \"postVariables\":\n" + //
                        "");
    }

    @Test
    public void testYamlEnteredSomeLeftOnlyTestStatusOnlyOnTestNode() {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.SPECIFICATION_STEP, Action.ENTER, "spec step", "4712",
                Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.COMPONENT, Action.ENTER, "component", "4713",
                Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.STEP, Action.ENTER, "step", "4714", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.LEAVE, "test", "4711", Status.ABORTED, null);

        // then
        assertOutputWithoutNanosToEqual(//
                "- \"source\": \"testcase\"\n" + //
                        "  \"testRunId\": \"1\"\n" + //
                        "  \"commitId\": \"decaf\"\n" + //
                        "  \"children\":\n" + //
                        "  - \"node\": \"TEST\"\n" + //
                        "    \"message\": \"test\"\n" + //
                        "    \"id\": \"4711\"\n" + //
                        "    \"preVariables\":\n" + //
                        "    \"children\":\n" + //
                        "    - \"node\": \"SPECIFICATION_STEP\"\n" + //
                        "      \"message\": \"spec step\"\n" + //
                        "      \"id\": \"4712\"\n" + //
                        "      \"preVariables\":\n" + //
                        "      \"children\":\n" + //
                        "      - \"node\": \"COMPONENT\"\n" + //
                        "        \"message\": \"component\"\n" + //
                        "        \"id\": \"4713\"\n" + //
                        "        \"preVariables\":\n" + //
                        "        \"children\":\n" + //
                        "        - \"node\": \"STEP\"\n" + //
                        "          \"message\": \"step\"\n" + //
                        "          \"id\": \"4714\"\n" + //
                        "          \"preVariables\":\n" + //
                        "          \"children\":\n" + //
                        "          \"status\": \"UNKNOWN\"\n" + //
                        "          \"postVariables\":\n" + //
                        "        \"status\": \"UNKNOWN\"\n" + //
                        "        \"postVariables\":\n" + //
                        "      \"status\": \"UNKNOWN\"\n" + //
                        "      \"postVariables\":\n" + //
                        "    \"status\": \"ABORTED\"\n" + //
                        "    \"postVariables\":\n");

    }

    private void assertOutputContainsNoRegex(String expectedRegex) {
        try {
            String output = outputStream.toString(StandardCharsets.UTF_8.name());
            assertFalse("Unexpected output to contain regex'" + expectedRegex + "'", output.matches(expectedRegex));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void assertOutputContainsRegex(String expectedRegex) {
        try {
            String output = outputStream.toString(StandardCharsets.UTF_8.name());
            assertTrue("Expected output to contain regex'" + expectedRegex + "'", output.matches(expectedRegex));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void assertOutputWithoutNanosToEqual(String expected) {
        try {
            String output = outputStream.toString(StandardCharsets.UTF_8.name());
            String outputWithoutNanos = output.replaceAll(" *\"(enter|leave|started)\": \"[0-9-.ZT:]*\" *\n", "");
            assertEquals(expected, outputWithoutNanos);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}

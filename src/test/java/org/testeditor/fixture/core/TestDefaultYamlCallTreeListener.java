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
        yamlCallTreeListenerUnderTest = new DefaultYamlCallTreeListener(outputStream, "testcase", "decaf");
    }

    @Test
    public void testYamlOfEnteredTest() throws IOException {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", Status.STARTED,
                AbstractTestCase.variables("a.\"my key\"", "5'\");System.exit(1);", "b", "7"));

        // then
        assertOutputWithoutNanosToEqual(//
                "  \"Source\": \"testcase\"\n" + //
                        "  \"CommitID\": \"decaf\"\n" + //
                        "  \"Children\":\n" + //
                        "  - \"Node\": \"TEST\"\n" + //
                        "    \"Message\": \"test\"\n" + //
                        "    \"ID\": \"4711\"\n" + //
                        "    \"PreVariables\":\n" + //
                        "      \"b\": \"7\"\n" + //
                        "      \"a.\\\"my key\\\"\": \"5'\\\");System.exit(1);\"\n" + //
                        "    \"Children\":\n");
        assertOutputContainsRegex("(?s).*\n    \"Enter\": \"[0-9]+\"\n.*");
        assertOutputContainsNoRegex("(?s).*\"Leave\":.*");
    }

    @Test
    public void testYamlOfEnteredAndLeftTest() {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.LEAVE, "test", "4711", Status.OK,
                AbstractTestCase.variables("a", "5", "b", "7"));

        // then
        assertOutputWithoutNanosToEqual(//
                "  \"Source\": \"testcase\"\n" + //
                        "  \"CommitID\": \"decaf\"\n" + //
                        "  \"Children\":\n" + //
                        "  - \"Node\": \"TEST\"\n" + //
                        "    \"Message\": \"test\"\n" + //
                        "    \"ID\": \"4711\"\n" + //
                        "    \"PreVariables\":\n" + //
                        "    \"Children\":\n" + //
                        "    \"Status\": \"OK\"\n" + //
                        "    \"PostVariables\":\n" + //
                        "      \"a\": \"5\"\n" + //
                        "      \"b\": \"7\"\n");
        assertOutputContainsRegex("(?s).*\n    \"Enter\": \"[0-9]+\"\n.*");
        assertOutputContainsRegex("(?s).*\n    \"Leave\": \"[0-9]+\"\n.*");
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
                "  \"Source\": \"testcase\"\n" + //
                        "  \"CommitID\": \"decaf\"\n" + //
                        "  \"Children\":\n" + //
                        "  - \"Node\": \"TEST\"\n" + //
                        "    \"Message\": \"test\"\n" + //
                        "    \"ID\": \"4711\"\n" + //
                        "    \"PreVariables\":\n" + //
                        "    \"Children\":\n" + //
                        "    - \"Node\": \"SPECIFICATION_STEP\"\n" + //
                        "      \"Message\": \"spec step\"\n" + //
                        "      \"ID\": \"4712\"\n" + //
                        "      \"PreVariables\":\n" + //
                        "      \"Children\":\n" + //
                        "      - \"Node\": \"COMPONENT\"\n" + //
                        "        \"Message\": \"component\"\n" + //
                        "        \"ID\": \"4713\"\n" + //
                        "        \"PreVariables\":\n" + //
                        "        \"Children\":\n" + //
                        "        - \"Node\": \"STEP\"\n" + //
                        "          \"Message\": \"step\"\n" + //
                        "          \"ID\": \"4714\"\n" + //
                        "          \"PreVariables\":\n" + //
                        "          \"Children\":\n" + //
                        "          \"Status\": \"OK\"\n" + //
                        "          \"PostVariables\":\n" + //
                        "        - \"Node\": \"STEP\"\n" + // cbuf
                        "          \"Message\": \"step\"\n" + //
                        "          \"ID\": \"4715\"\n" + //
                        "          \"PreVariables\":\n" + //
                        "          \"Children\":\n" + //
                        "          \"Status\": \"OK\"\n" + //
                        "          \"PostVariables\":\n" + //
                        "        \"Status\": \"OK\"\n" + //
                        "        \"PostVariables\":\n" + //
                        "      \"Status\": \"OK\"\n" + //
                        "      \"PostVariables\":\n" + //
                        "    \"Status\": \"OK\"\n" + //
                        "    \"PostVariables\":\n");
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
                "  \"Source\": \"testcase\"\n" + //
                        "  \"CommitID\": \"decaf\"\n" + //
                        "  \"Children\":\n" + //
                        "  - \"Node\": \"TEST\"\n" + //
                        "    \"Message\": \"test\"\n" + //
                        "    \"ID\": \"4711\"\n" + //
                        "    \"PreVariables\":\n" + //
                        "    \"Children\":\n" + //
                        "    - \"Node\": \"SPECIFICATION_STEP\"\n" + //
                        "      \"Message\": \"spec step\"\n" + //
                        "      \"ID\": \"4712\"\n" + //
                        "      \"PreVariables\":\n" + //
                        "      \"Children\":\n" + //
                        "      \"AssertionError\": \"my message\\n\\\"with\"\n");
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
                "  \"Source\": \"testcase\"\n" + //
                        "  \"CommitID\": \"decaf\"\n" + //
                        "  \"Children\":\n" + //
                        "  - \"Node\": \"TEST\"\n" + //
                        "    \"Message\": \"test\"\n" + //
                        "    \"ID\": \"4711\"\n" + //
                        "    \"PreVariables\":\n" + //
                        "    \"Children\":\n" + //
                        "    - \"Node\": \"SPECIFICATION_STEP\"\n" + //
                        "      \"Message\": \"spec step\"\n" + //
                        "      \"ID\": \"4712\"\n" + //
                        "      \"PreVariables\":\n" + //
                        "      \"Children\":\n" + //
                        "      \"Exception\": \"my message\\n\\\"with\"\n");
    }

    @Test
    public void testYamlOnFixtureException() {
        // given
        Map<String, Object> keyValueStore = new HashMap<>();

        Map<String, Object> someMap = new HashMap<>();
        someMap.put("a map key to long", new Long(42));
        someMap.put("keyToString", "someString");

        Map<String, Object> longMap = new HashMap<>();
        longMap.put("first", new Long(1));
        longMap.put("second", new Long(-2));
        longMap.put("nullKey", null);

        Map<String, Object> stringMap = new HashMap<>();
        stringMap.put("1st string", "one");
        stringMap.put("2nd string", "two");
        stringMap.put("nullKey", null);

        ArrayList<Object> someArray = new ArrayList<>();
        someArray.add(longMap);
        someArray.add(stringMap);
        someArray.add(Collections.emptyMap());
        someArray.add(new Long(44));
        someArray.add("lllaaa");

        keyValueStore.put("a Map", someMap);
        keyValueStore.put("an Array", someArray);
        keyValueStore.put("an Number", new Long(100));
        keyValueStore.put("an String", "some string \"\' that needs escaping");

        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.SPECIFICATION_STEP, Action.ENTER, "spec step", "4712",
                Status.STARTED, null);
        yamlCallTreeListenerUnderTest.reportFixtureExit(new FixtureException("message", keyValueStore));

        // then
        assertOutputWithoutNanosToEqual(//
                "  \"Source\": \"testcase\"\n" + //
                        "  \"CommitID\": \"decaf\"\n" + //
                        "  \"Children\":\n" + //
                        "  - \"Node\": \"TEST\"\n" + //
                        "    \"Message\": \"test\"\n" + //
                        "    \"ID\": \"4711\"\n" + //
                        "    \"PreVariables\":\n" + //
                        "    \"Children\":\n" + //
                        "    - \"Node\": \"SPECIFICATION_STEP\"\n" + //
                        "      \"Message\": \"spec step\"\n" + //
                        "      \"ID\": \"4712\"\n" + //
                        "      \"PreVariables\":\n" + //
                        "      \"Children\":\n" + //
                        "      \"FixtureException\":\n" + //
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
                        "        \"an String\": \"some string \\\"' that needs escaping\"\n" + //
                        "        \"an Number\": 100\n" + //
                        "        \"a Map\":\n" + //
                        "          \"keyToString\": \"someString\"\n" + //
                        "          \"a map key to long\": 42\n");
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
                "  \"Source\": \"testcase\"\n" + //
                        "  \"CommitID\": \"decaf\"\n" + //
                        "  \"Children\":\n" + //
                        "  - \"Node\": \"TEST\"\n" + //
                        "    \"Message\": \"test\"\n" + //
                        "    \"ID\": \"4711\"\n" + //
                        "    \"PreVariables\":\n" + //
                        "    \"Children\":\n" + //
                        "    - \"Node\": \"SPECIFICATION_STEP\"\n" + //
                        "      \"Message\": \"spec step\"\n" + //
                        "      \"ID\": \"4712\"\n" + //
                        "      \"PreVariables\":\n" + //
                        "      \"Children\":\n" + //
                        "      - \"Node\": \"COMPONENT\"\n" + //
                        "        \"Message\": \"component\"\n" + //
                        "        \"ID\": \"4713\"\n" + //
                        "        \"PreVariables\":\n" + //
                        "        \"Children\":\n" + //
                        "        - \"Node\": \"STEP\"\n" + //
                        "          \"Message\": \"step\"\n" + //
                        "          \"ID\": \"4714\"\n" + //
                        "          \"PreVariables\":\n" + //
                        "          \"Children\":\n" + //
                        "    \"Status\": \"ABORTED\"\n" + //
                        "    \"PostVariables\":\n");

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
            String outputWithoutNanos = output.replaceAll(" *\"(Enter|Leave|Started)\": \"[0-9-.ZT:]*\" *\n", "");
            assertEquals(expected, outputWithoutNanos);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}

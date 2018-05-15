
package org.testeditor.fixture.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.testeditor.fixture.core.TestRunReporter.Action;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;

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
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", "?", AbstractTestCase.variables("a","5","b","7"));

        // then
        assertOutputWithoutNanosToEqual( //
                        "Source: \"testcase\"\n" + //
                        "CommitID: \"decaf\"\n" + //
                        "- Node: \"TEST\"\n" + //
                        "  Message: \"test\"\n" + //
                        "  ID: \"4711\"\n" + //
                        "  PreVariables:\n" + //
                        "  - a: \"5\"\n" + //
                        "  - b: \"7\"\n" + //
                        "  Children:\n");
        assertOutputContainsRegex("(?s).*\n  Enter: \"[0-9]+\"\n.*");
        assertOutputContainsNoRegex("(?s).*Leave:.*");
    }

    @Test
    public void testYamlOfEnteredAndLeftTest() {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", "?", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.LEAVE, "test", "4711", "OK", AbstractTestCase.variables("a","5","b","7"));

        // then
        assertOutputWithoutNanosToEqual( //
                        "Source: \"testcase\"\n" + //
                        "CommitID: \"decaf\"\n" + //
                        "- Node: \"TEST\"\n" + //
                        "  Message: \"test\"\n" + //
                        "  ID: \"4711\"\n" + //                       
                        "  PreVariables:\n" + //
                        "  Children:\n" + //
                        "  Status: \"OK\"\n" + //
                        "  PostVariables:\n" + //
                        "  - a: \"5\"\n" + //
                        "  - b: \"7\"\n" 
                        );
        assertOutputContainsRegex("(?s).*\n  Enter: \"[0-9]+\"\n.*");
        assertOutputContainsRegex("(?s).*\n  Leave: \"[0-9]+\"\n.*");
    }
    
    @Test
    public void testYamlEnteredAndLeftDownToTestStep() {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", "?", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.SPECIFICATION_STEP, Action.ENTER, "spec step", "4712", "?", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.COMPONENT, Action.ENTER, "component", "4713", "?", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.STEP, Action.ENTER, "step", "4714", "?", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.STEP, Action.LEAVE, "step", "4714", "OK", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.STEP, Action.ENTER, "step", "4715", "?", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.STEP, Action.LEAVE, "step", "4715", "OK", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.COMPONENT, Action.LEAVE, "component", "4713", "OK", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.SPECIFICATION_STEP, Action.LEAVE, "spec step", "4712", "OK", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.LEAVE, "test", "4711", "OK", null);
        
        // then
        assertOutputWithoutNanosToEqual( //
                "Source: \"testcase\"\n" + //
                "CommitID: \"decaf\"\n" + //
                "- Node: \"TEST\"\n" + //
                "  Message: \"test\"\n" + //
                "  ID: \"4711\"\n" + //
                "  PreVariables:\n" + //
                "  Children:\n" + //
                "  - Node: \"SPECIFICATION_STEP\"\n" + //
                "    Message: \"spec step\"\n" + //
                "    ID: \"4712\"\n" + //
                "    PreVariables:\n" + //
                "    Children:\n" + //
                "    - Node: \"COMPONENT\"\n" + //
                "      Message: \"component\"\n" + //
                "      ID: \"4713\"\n" + //
                "      PreVariables:\n" + //
                "      Children:\n" + //
                "      - Node: \"STEP\"\n" + //
                "        Message: \"step\"\n" + //
                "        ID: \"4714\"\n" + //
                "        PreVariables:\n" + //
                "        Children:\n" + //
                "        Status: \"OK\"\n" + //
                "        PostVariables:\n" + //
                "      - Node: \"STEP\"\n" + //
                "        Message: \"step\"\n" + //
                "        ID: \"4715\"\n" + //
                "        PreVariables:\n" + //
                "        Children:\n" + //
                "        Status: \"OK\"\n" + //
                "        PostVariables:\n" + //
                "      Status: \"OK\"\n" + //
                "      PostVariables:\n" + //
                "    Status: \"OK\"\n" + //
                "    PostVariables:\n" + //
                "  Status: \"OK\"\n" + //
                "  PostVariables:\n"
        );
    }
    
    @Test
    public void testYamlEnteredSomeLeftOnlyTestStatusOnlyOnTestNode() {
        // when
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.ENTER, "test", "4711", "?", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.SPECIFICATION_STEP, Action.ENTER, "spec step", "4712", "?", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.COMPONENT, Action.ENTER, "component", "4713", "?", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.STEP, Action.ENTER, "step", "4714", "?", null);
        yamlCallTreeListenerUnderTest.reported(SemanticUnit.TEST, Action.LEAVE, "test", "4711", "FAILED", null);
        
        // then
        assertOutputWithoutNanosToEqual( //
                "Source: \"testcase\"\n" + //
                "CommitID: \"decaf\"\n" + //
                "- Node: \"TEST\"\n" + //
                "  Message: \"test\"\n" + //
                "  ID: \"4711\"\n" + //
                "  PreVariables:\n" + //
                "  Children:\n" + //
                "  - Node: \"SPECIFICATION_STEP\"\n" + //
                "    Message: \"spec step\"\n" + //
                "    ID: \"4712\"\n" + //
                "    PreVariables:\n" + //
                "    Children:\n" + //
                "    - Node: \"COMPONENT\"\n" + //
                "      Message: \"component\"\n" + //
                "      ID: \"4713\"\n" + //
                "      PreVariables:\n" + //
                "      Children:\n" + //
                "      - Node: \"STEP\"\n" + //
                "        Message: \"step\"\n" + //
                "        ID: \"4714\"\n" + //
                "        PreVariables:\n" + //
                "        Children:\n" + //
                "  Status: \"FAILED\"\n" + //
                "  PostVariables:\n"
        );
        
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
            String outputWithoutNanos = output.replaceAll(" *(Enter|Leave|Started): \"[0-9-.ZT:]*\" *\n", "");
            assertEquals(expected, outputWithoutNanos);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}

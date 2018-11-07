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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testeditor.fixture.core.TestRunReporter.Action;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;
import org.testeditor.fixture.core.TestRunReporter.Status;

public class DefaultYamlCallTreeListener implements TestRunListener {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultYamlCallTreeListener.class);

    // @formatter:off
    /* indentation
     * 0 = test suite run
     * 2 = test run
     * 4 = setup/cleanup/specification step
     * ... the other indentation is related to the actual implementation of the setup/cleanup/spec steps
     *     and is no longer associated with a fixed indentation level
     */
    // @formatter:on
    public static final int YAML_INDENTATION = 2;

    protected Map<String, Node> callTreeNodeMap = new HashMap<>();
    protected String testCaseSource;
    protected String testRunId;
    protected String commitId;
    protected OutputStreamWriter outputStreamWriter;
    private int currentIndentation = 0;
    
    private Deque<Node> enteredNodes = new ArrayDeque<>();

    protected static class Node {
        public SemanticUnit unit;
        public String message;
        public String id;
        public long nanoTimeEntered;
        public long nanoTimeLeft;
        public Status status;
        public int parentIndentation;

        public Node(SemanticUnit unit, String message, String id) {
            this.unit = unit;
            this.message = message;
            this.id = id;
        }

        public void enterNode(int parentIndentation, Status status) {
            this.parentIndentation = parentIndentation;
            this.nanoTimeEntered = System.nanoTime();
            this.status = status;
        }

        public void leaveNode(Status status) {
            this.nanoTimeLeft = System.nanoTime();
            this.status = status;
        }

    }

    /**
     * Ctor
     *
     * @param outputStream   where yaml is written to
     * @param testCaseSource file/resource path identifying this test within the
     *                       repo
     * @param commitId       repo commit id identifying this test version
     */
    public DefaultYamlCallTreeListener(OutputStream outputStream, String testCaseSource, String testRunId,
            String commitId) {
        this.outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        this.testCaseSource = testCaseSource;
        this.testRunId = testRunId;
        this.commitId = commitId;
    }

    // @formatter:off
    /*
     * Node: "SPEC_STEP"
     * Message: "message"
     * ID: "id"
     * Enter: "timestamp of enter"
     * Children:
     * - Node: "COMPONENT"
     *   Message: "message"
     *   ID: "id" Enter: "..."
     *   Children:
     *   - Node: "STEP"
     *     Message: "message"
     *     ID: "ID"
     *     Enter: "..."
     *     Children:
     *     Leave: "..."
     *     Status: "..."
     *   - Node: "STEP"
     *     Message: "Message"
     *     ID: "ID"
     *     Enter: "..."
     *     Children:
     *     Leave: "..."
     *     Status: "..."
     *   Leave: "..."
     *   Status: ""
     * Leave: "timestamp of leave"
     * Status: "OK|UNKNOWN|ERROR"
     */
    // @formatter:ON
    @Override
    public void reported(SemanticUnit unit, Action action, String message, String id, Status status,
            Map<String, String> variables) {
        switch (unit) {
            case TEST:
                writeTestNode(action, message, id, status, variables);
                break;
            default:
                writeNode(unit, action, message, id, status, variables);
                break;
        }
    }

    @Override
    public void reportFixtureExit(FixtureException fixtureException) {
        Map<String, Object> keyValueStore = new HashMap<>(fixtureException.getKeyValueStore());
        keyValueStore.put("fixtureExceptionMessage", fixtureException.getLocalizedMessage());
        dispatchingWrite("fixtureException", keyValueStore);
        writeOpenNodeLeaves(Status.ERROR);
        currentIndentation = 2 * YAML_INDENTATION; // subsequent logging is done at test-run level
        flush();
    }

    @Override
    public void reportExceptionExit(Exception exception) {
        dispatchingWrite("exception", exception.getLocalizedMessage());
        writeOpenNodeLeaves(Status.ERROR);
        currentIndentation = 2 * YAML_INDENTATION;
        flush();
    }

    @Override
    public void reportAssertionExit(AssertionError assertionError) {
        dispatchingWrite("assertionError", assertionError.getLocalizedMessage());
        writeOpenNodeLeaves(Status.ERROR);
        currentIndentation = 2 * YAML_INDENTATION;
        flush();
    }

    private void writeNode(SemanticUnit unit, Action action, String message, String id, Status status,
            Map<String, String> variables) {
        switch (action) {
            case ENTER:
                enterNode(unit, message, id, status, variables);
                break;
            case LEAVE:
                leaveNode(id, status, variables);
                break;
            default:
                // do nothing
                break;
        }
    }

    private void enterNode(SemanticUnit unit, String message, String id, Status status, Map<String, String> variables) {
        Node node = new Node(unit, message, id);
        node.enterNode(currentIndentation, status);
        callTreeNodeMap.put(id, node);
        writePrefixedString("-", "node", unit.toString());
        increaseIndentation();
        writeString("message", node.message);
        writeString("id", node.id);
        writeString("enter", Long.toString(node.nanoTimeEntered));
        writeVariables("pre", variables);
        writeString("children");
        flush();
        if (nodeKeptOnStack(node)) {
            enteredNodes.push(node);
        }
    }

    private void increaseIndentation() {
        currentIndentation += YAML_INDENTATION;
    }

    private void decreaseIndentation() {
        currentIndentation -= YAML_INDENTATION;
    }

    private void writeVariables(String prefix, Map<String, String> variables) {
        dispatchingWrite(prefix + "Variables", variables);
    }
    
    private void leaveNodePredatingThisFromStack(String id) {
        while (!enteredNodes.isEmpty() && !id.equals(enteredNodes.peek().id)) {
            Node intermediateLeave = enteredNodes.peek();
            writeLeavingNode(intermediateLeave, Status.UNKNOWN, null);
            enteredNodes.pop();
        }
    }
    
    /** Test itself is not kept on the stack, since unrolling the stack does 
     *  not leave the test context */
    private boolean nodeKeptOnStack(Node node) {
        return !SemanticUnit.TEST.equals(node.unit);
    }
    
    private void leaveNode(String id, Status status, Map<String, String> variables) {
        Node node = callTreeNodeMap.get(id);
        if (node != null) {
            leaveNodePredatingThisFromStack(id);
            writeLeavingNode(node, status, variables);
            if (nodeKeptOnStack(node)) {
                enteredNodes.pop();
            }
        } else {
            logger.error("Left unknown node with ID '" + StringEscapeUtils.escapeJava(id) + "'");
        }
        decreaseIndentation();
    }
    
    private void writeLeavingNode(Node node, Status status, Map<String, String> variables) {
        node.leaveNode(status);
        currentIndentation = node.parentIndentation;
        increaseIndentation();
        writeString("leave", Long.toString(node.nanoTimeLeft));
        writeString("status", status.toString());
        writeVariables("post", variables);
        flush();
    }

    private void writeTestNode(Action action, String message, String id, Status status, Map<String, String> variables) {
        try {
            switch (action) {
                case ENTER:
                    writePrefixedString("-", "source", testCaseSource);
                    increaseIndentation();
                    writeString("testRunId", testRunId);
                    writeString("commitId", commitId);
                    writeString("started", Instant.now().toString());
                    writeString("children");
                    enterNode(SemanticUnit.TEST, message, id, status, variables);
                    break;
                case LEAVE:
                    leaveNode(id, status, variables);
                    break;
                default:
                    // do nothing
                    break;
            }
        } catch (Exception e) {
            logger.error("writing yaml call tree entry failed", e);
        }
    }

    private void writeMap(Map<String, Object> keyValue) {
        keyValue.keySet().stream().forEach(key -> {
            dispatchingWrite(key, keyValue.get(key));
        });
    }

    private void writeArray(ArrayList<Object> array) {
        array.stream().forEach(value -> {
            dispatchingWrite("-", value);
        });
    }

    private void writeNumber(Number number) {
        try {
            outputStreamWriter.write(number.toString());
        } catch (Exception e) {
            logger.error("writing yaml number failed", e);
        }
    }

    private void writeAttributePrefixed(String prefix, String attribute) {
        try {
            outputStreamWriter.write(StringUtils.repeat(' ', currentIndentation));
            if ((prefix != null) && (!prefix.isEmpty())) {
                outputStreamWriter.write(prefix + " ");
            }
            if (!attribute.equals("-")) {
                String escapedAttribute = StringEscapeUtils.escapeJava(attribute);
                outputStreamWriter.write("\"" + escapedAttribute + "\":");
            } else {
                outputStreamWriter.write(attribute);
            }
        } catch (IOException e) {
            logger.error("writing prefixed attribute", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatchingWritePrefixed(String prefix, String attribute, Object object) {
        writeAttributePrefixed(prefix, attribute);
        if (object instanceof ArrayList) {
            writeNewLine();
            increaseIndentation();
            writeArray((ArrayList<Object>) object);
            decreaseIndentation();
        } else if (object instanceof Map) {
            writeNewLine();
            increaseIndentation();
            writeMap((Map<String, Object>) object);
            decreaseIndentation();
        } else if (object instanceof String) {
            writeSpace();
            writeStringObject((String) object);
            writeNewLine();
        } else if (object instanceof Number) {
            writeSpace();
            writeNumber((Number) object);
            writeNewLine();
        } else if (object != null) {
            writeSpace();
            writeStringObject(object.toString());
            writeNewLine();
        } else {
            writeNewLine();
        }
    }

    private void dispatchingWrite(String attribute, Object object) {
        dispatchingWritePrefixed("", attribute, object);
    }

    private void writeStringObject(String string) {
        try {
            if (string != null) {
                String escapedValue = StringEscapeUtils.escapeJava(string);
                outputStreamWriter.write("\"" + escapedValue + "\"");
            }
        } catch (IOException e) {
            logger.error("writing string object failed", e);
        }
    }

    private void writePrefixedString(String prefix, String attribute, String value) {
        dispatchingWritePrefixed(prefix, attribute, value);
    }

    private void writeString(String attribute) {
        writeString(attribute, null);
    }

    private void writeString(String attribute, String value) {
        dispatchingWrite(attribute, value);
    }

    private void writeNewLine() {
        try {
            outputStreamWriter.write("\n");
        } catch (IOException e) {
            logger.error("writing new line failed", e);
        }
    }

    private void writeSpace() {
        try {
            outputStreamWriter.write(" ");
        } catch (IOException e) {
            logger.error("writing space failed", e);
        }
    }

    private void flush() {
        try {
            outputStreamWriter.flush();
        } catch (IOException e) {
            logger.error("flushing yaml call tree entry failed", e);
        }
    }

    private void writeOpenNodeLeaves(Status status) {
        enteredNodes.forEach((node) -> {
            writeLeavingNode(node, status, null);
        });
        enteredNodes.clear();
    }

}

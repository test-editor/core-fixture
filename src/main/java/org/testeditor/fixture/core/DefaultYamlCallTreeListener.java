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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testeditor.fixture.core.TestRunReporter.Action;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;
import org.testeditor.fixture.core.TestRunReporter.Status;

public class DefaultYamlCallTreeListener implements TestRunListener {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultYamlCallTreeListener.class);

    public static final int YAML_INDENTATION = 2;

    protected Map<String, Node> callTreeNodeMap = new HashMap<>();
    protected String testCaseSource;
    protected String commitId;
    protected OutputStreamWriter outputStreamWriter;
    private int currentIndentation = YAML_INDENTATION;

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
     * @param outputStream where yaml is written to
     * @param testCaseSource file/resource path identifying this test within the
     *            repo
     * @param commitId repo commit id identifying this test version
     */
    public DefaultYamlCallTreeListener(OutputStream outputStream, String testCaseSource, String commitId) {
        this.outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        this.testCaseSource = testCaseSource;
        this.commitId = commitId;
    }

    /*
     * Node: "SPEC_STEP" Message: "message" ID: "id" Enter: "timestamp of enter"
     * Children: - Node: "COMPONENT" Message: "message" ID: "id" Enter: "..."
     * Children: - Node: "STEP" Message: "message" ID: "ID" Enter: "..." Children:
     * Leave: "..." Status: "..." - Node: "STEP" Message: "Message" ID: "ID" Enter:
     * "..." Children: Leave: "..." Status: "..." Leave: "..." Status: "" Leave:
     * "timestamp of leave" Status: "OK|UNKNOWN|ERROR"
     */

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
        writePrefixedString("-", "Node", unit.toString());
        increaseIndentation();
        writeString("Message", node.message);
        writeString("ID", node.id);
        writeString("Enter", Long.toString(node.nanoTimeEntered));
        writeVariables("Pre", variables);
        writeString("Children");
        flush();
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

    private void leaveNode(String id, Status status, Map<String, String> variables) {
        Node node = callTreeNodeMap.get(id);
        if (node != null) {
            node.leaveNode(status);
            currentIndentation = node.parentIndentation;
            increaseIndentation();
            writeString("Leave", Long.toString(node.nanoTimeLeft));
            writeString("Status", status.toString());
            writeVariables("Post", variables);
            flush();
        } else {
            logger.error("Left unknown node with ID '" + StringEscapeUtils.escapeJava(id) + "'");
        }
        decreaseIndentation();
    }

    private void writeTestNode(Action action, String message, String id, Status status, Map<String, String> variables) {
        try {
            switch (action) {
                case ENTER:
                    writeString("Source", testCaseSource);
                    writeString("CommitID", commitId);
                    writeString("Started", Instant.now().toString());
                    writeString("Children");
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

    private void writePrefixedString(String prefix, String attribute, String value) {
        try {
            outputStreamWriter.write(StringUtils.repeat(' ', currentIndentation));
            writeUnindentedString(prefix + " ", attribute, value);
        } catch (Exception e) {
            logger.error("writing yaml call tree entry failed", e);
        }
    }

    private void writeString(String attribute) {
        writeString(attribute, null);
    }

    private void writeString(String attribute, String value) {
        try {
            outputStreamWriter.write(StringUtils.repeat(' ', currentIndentation));
            writeUnindentedString("", attribute, value);
        } catch (Exception e) {
            logger.error("writing yaml call tree entry failed", e);
        }
    }

    private void writeUnindentedString(String prefix, String attribute, String value) {
        try {
            outputStreamWriter.write(prefix);
            if (!attribute.equals("-")) {
                String escapedAttribute = StringEscapeUtils.escapeJava(attribute);
                outputStreamWriter.write("\"" + escapedAttribute + "\":");
            } else {
                outputStreamWriter.write(attribute);
            }
            if (value != null) {
                String escapedValue = StringEscapeUtils.escapeJava(value);
                outputStreamWriter.write(" \"" + escapedValue + "\"");
            }
            outputStreamWriter.write("\n");
        } catch (Exception e) {
            logger.error("writing yaml call tree entry failed", e);
        }
    }

    @Override
    public void reportFixtureExit(FixtureException fixtureException) {
        dispatchingWrite("FixtureException", fixtureException.getKeyValueStore());
        flush();
    }

    @Override
    public void reportExceptionExit(Exception exception) {
        dispatchingWrite("Exception", exception.getLocalizedMessage());
        flush();
    }

    @Override
    public void reportAssertionExit(AssertionError assertionError) {
        dispatchingWrite("AssertionError", assertionError.getLocalizedMessage());
        flush();
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

    private void dispatchingWritePrefixed(String prefix, String attribute, Object object) {
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
            if (object instanceof ArrayList) {
                writeNL();
                increaseIndentation();
                writeArray((ArrayList<Object>) object);
                decreaseIndentation();
            } else if (object instanceof Map) {
                writeNL();
                increaseIndentation();
                writeMap((Map<String, Object>) object);
                decreaseIndentation();
            } else if (object instanceof String) {
                outputStreamWriter.write(" ");
                writeStringObject((String) object);
                writeNL();
            } else if (object instanceof Number) {
                outputStreamWriter.write(" ");
                writeNumber((Number) object);
                writeNL();
            } else if (object != null) {
                outputStreamWriter.write(" ");
                writeStringObject(object.toString());
                writeNL();
            } else {
                writeNL();
            }
        } catch (Exception e) {
            logger.error("writing yaml call tree entry failed", e);
        }
    }

    private void dispatchingWrite(String attribute, Object object) {
        dispatchingWritePrefixed("", attribute, object);
    }

    private void writeStringObject(String string) throws IOException {
        if (string != null) {
            String escapedValue = StringEscapeUtils.escapeJava(string);
            outputStreamWriter.write("\"" + escapedValue + "\"");
        }
    }

    private void writeNL() throws IOException {
        outputStreamWriter.write("\n");
    }

    private void flush() {
        try {
            outputStreamWriter.flush();
        } catch (IOException e) {
            logger.error("flushing yaml call tree entry failed", e);
        }
    }
}

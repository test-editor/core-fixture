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

    public final static int YAML_INDENTIATION = 2;

    protected Map<String, Node> callTreeNodeMap = new HashMap<>();
    protected String testCaseSource;
    protected String commitID;

    protected static class Node {
        public SemanticUnit unit;
        public String message;
        public String ID;
        public long nanoTimeEntered;
        public long nanoTimeLeft;
        public Status status;
        public int parentIndentation;

        public Node(SemanticUnit unit, String message, String ID) {
            this.unit = unit;
            this.message = message;
            this.ID = ID;
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

    protected OutputStreamWriter outputStreamWriter;
    int currentIndentation = 0;

    public DefaultYamlCallTreeListener(OutputStream outputStream, String testCaseSource, String commitID) {
        this.outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        this.testCaseSource = testCaseSource;
        this.commitID = commitID;
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
    public void reported(SemanticUnit unit, Action action, String message, String ID, Status status,
            Map<String, String> variables) {
        switch (unit) {
            case TEST:
                writeTestNode(action, message, ID, status, variables);
                break;
            default:
                writeNode(unit, action, message, ID, status, variables);
                break;

        }
    }

    private void writeNode(SemanticUnit unit, Action action, String message, String ID, Status status,
            Map<String, String> variables) {
        switch (action) {
            case ENTER:
                enterNode(unit, message, ID, status, variables);
                break;
            case LEAVE:
                leaveNode(ID, status, variables);
        }
    }

    private void enterNode(SemanticUnit unit, String message, String ID, Status status, Map<String, String> variables) {
        Node node = new Node(unit, message, ID);
        node.enterNode(currentIndentation, status);
        callTreeNodeMap.put(ID, node);
        writePrefixedString("-", "Node", unit.toString());
        currentIndentation += YAML_INDENTIATION;
        writeString("Message", node.message);
        writeString("ID", node.ID);
        writeString("Enter", Long.toString(node.nanoTimeEntered));
        writeVariables("Pre", variables);
        writeString("Children", null);
        try {
            outputStreamWriter.flush();
        } catch (IOException e) {
            logger.error("flushing yaml call tree entry failed", e);
        }
    }

    private void writeVariables(String prefix, Map<String, String> variables) {
        writeString(prefix + "Variables", null);
        if (variables != null) {
            variables.entrySet().stream().forEach(keyVal -> {
                writePrefixedString("-", StringEscapeUtils.escapeJava(keyVal.getKey()), keyVal.getValue());
            });
        }
    }

    private void leaveNode(String ID, Status status, Map<String, String> variables) {
        Node node = callTreeNodeMap.get(ID);
        if (node != null) {
            node.leaveNode(status);
            currentIndentation = node.parentIndentation + YAML_INDENTIATION;
            writeString("Leave", Long.toString(node.nanoTimeLeft));
            writeString("Status", status.toString());
            writeVariables("Post", variables);
            try {
                outputStreamWriter.flush();
            } catch (IOException e) {
                logger.error("flushing yaml call tree entry failed", e);
            }
        } else {
            logger.error("Left unknown node with ID '" + StringEscapeUtils.escapeJava(ID) + "'");
        }
        currentIndentation -= YAML_INDENTIATION;
    }

    private void writeTestNode(Action action, String message, String ID, Status status, Map<String, String> variables) {
        try {
            switch (action) {
                case ENTER:
                    writeString("Source", testCaseSource);
                    writeString("CommitID", commitID);
                    writeString("Started", Instant.now().toString());
                    enterNode(SemanticUnit.TEST, message, ID, status, variables);
                    break;
                case LEAVE:
                    leaveNode(ID, status, variables);
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
            outputStreamWriter.write(prefix + attribute + ":");
            if (value != null) {
                String escapedValue = StringEscapeUtils.escapeJava(value);
                outputStreamWriter.write(" \"" + escapedValue + "\"");
            }
            outputStreamWriter.write("\n");
        } catch (Exception e) {
            logger.error("writing yaml call tree entry failed", e);
        }
    }

}

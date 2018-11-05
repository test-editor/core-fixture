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

package org.testeditor.fixture.core.logging;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.Before;
import org.junit.Test;
import org.testeditor.fixture.core.DefaultLoggingListener;
import org.testeditor.fixture.core.FixtureException;
import org.testeditor.fixture.core.TestRunReporter.Action;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;
import org.testeditor.fixture.core.TestRunReporter.Status;

public class DefaultLoggingListenerTest extends AbstractMockedLoggingTest {

    DefaultLoggingListener loggingListenerUnderTest;

    @Before
    public void setupLoggingListener() {
        loggingListenerUnderTest = new DefaultLoggingListener();
    }

    @Test
    public void testInfoLoggingWithoutVariables() {
        // given

        // when
        loggingListenerUnderTest.reported(SemanticUnit.STEP, Action.ENTER, "message", "id", Status.OK, variables());

        // then
        List<LogEvent> logLines = getLogEventsWithLevel(Level.INFO);
        assertThat(logLines.size(), equalTo(1));
        assertThat("the log contains only the message logged", getMessageString(logLines, 0), equalTo("message"));
    }

    @Test
    public void testInfoLoggingWithOneVariable() {
        // given

        // when
        loggingListenerUnderTest.reported(SemanticUnit.STEP, Action.ENTER, "message", "id", Status.OK,
                variables("key", "value"));

        // then
        List<LogEvent> logLines = getLogEventsWithLevel(Level.INFO);
        assertThat(logLines.size(), equalTo(2));
        assertThat("the log contains the message logged", getMessageString(logLines, 0), equalTo("message"));
        assertThat("the log contains the variable logged", getMessageString(logLines, 1),
                containsString("key = \"value\""));

    }

    @Test
    public void testInfoLoggingWithEncodings() {
        // given

        // when
        loggingListenerUnderTest.reported(SemanticUnit.STEP, Action.ENTER, "message", "id", Status.OK,
                variables("keyThatHasUtf8_µ", "value \"\n with some characters to be encoded like ä and Ö"));

        // then
        List<LogEvent> logLines = getLogEventsWithLevel(Level.INFO);
        assertThat(logLines.size(), equalTo(2));
        assertThat("the log contains the message logged", getMessageString(logLines, 0), equalTo("message"));
        assertThat("the log contains the variable logged", getMessageString(logLines, 1),
                containsString("keyThatHasUtf8_µ = \"value \\\"\\n with some characters to be encoded like ä and Ö\""));
    }

    @Test
    public void testInfoLoggingWithMultipleVariables() {
        // given

        // when
        loggingListenerUnderTest.reported(SemanticUnit.STEP, Action.ENTER, "message", "id", Status.OK,
                variables("key1", "value1", "key2", "value2"));

        // then
        List<LogEvent> logLines = getLogEventsWithLevel(Level.INFO);
        assertThat(logLines.size(), equalTo(3));
        assertThat("the log contains the message logged", getMessageString(logLines, 0), equalTo("message"));
        assertThat("the log contains the first variable logged", getMessageString(logLines, 1),
                   containsString("key1 = \"value1\""));
        assertThat("the log contains the second variable logged", getMessageString(logLines, 2),
                   containsString("key2 = \"value2\""));
    }

    @Test
    public void testAssertionErrorLog() {
        // given
        // when
        loggingListenerUnderTest
                .reportAssertionExit(new AssertionError("detailed message", new RuntimeException("root cause")));

        // then
        List<LogEvent> logLines = getLogEventsWithLevel(Level.ERROR);
        assertThat(logLines.size(), equalTo(2));
        assertThat("the log contains the message logged", getMessageString(logLines, 0),
                   equalTo("Assertion failed: detailed message"));
        assertThat("the second line contains a reminder to check expectation and actual value",
                   getMessageString(logLines, 1), allOf(containsString("expectation"), containsString("actual")));

    }

    @Test
    public void testFixtureExceptionWithoutVariables() {
        // given
        // when
        loggingListenerUnderTest.reportFixtureExit(
                new FixtureException("detailed message", valueMap(), new RuntimeException("root cause")));

        // then
        List<LogEvent> logLines = getLogEventsWithLevel(Level.ERROR);
        assertThat(logLines.size(), equalTo(2));
        assertThat("error log entry with the detailed message of the fixture exception is present",
                   getMessageString(logLines, 0),
                   allOf(containsString("detailed message"), containsString("fixture exception")));
        assertThat("error log entry contains info to contact the administrator", getMessageString(logLines, 1),
                allOf(containsString("contact"), containsString("administrator")));
    }

    @Test
    public void testFixtureExceptionWithVariables() {
        // given
        final List<String> aList = Arrays.asList("a", "b");

        // when
        loggingListenerUnderTest.reportFixtureExit(new FixtureException("detailed message",
                valueMap("key", "value", "someList", aList, "<element>", "locator: a, locatorType: id"),
                new RuntimeException("root cause")));

        // then
        List<LogEvent> logLines = getLogEventsWithLevel(Level.ERROR);
        assertThat(logLines.size(), equalTo(5));
        assertTrue("key value is logged alongside error message", containsLineMatching(logLines, ".*key = \"value\""));
        assertTrue("list is logged via toString",
                containsLineContaining(logLines, "someList = \"" + aList.toString() + "\""));
        assertTrue(containsLineMatching(logLines, ".*<element> = \"locator: a, locatorType: id\""));
    }

    @Test
    public void testExceptionLogsErrorLines() {
        // given
        // when
        loggingListenerUnderTest.reportExceptionExit(new RuntimeException("detailed message"));

        // then
        List<LogEvent> logLines = getLogEventsWithLevel(Level.ERROR);
        assertThat(logLines.size(), equalTo(2));
        assertThat("error log entry with the detailed message of the exception is present",
                   getMessageString(logLines, 0), containsString("detailed message"));
        assertThat("error log entry contains info to contact the administrator", getMessageString(logLines, 1),
                allOf(containsString("contact"), containsString("administrator")));
    }

}

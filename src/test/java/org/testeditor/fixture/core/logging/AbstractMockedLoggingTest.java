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

import static org.apache.logging.log4j.Level.TRACE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.junit.After;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class AbstractMockedLoggingTest {

    private Appender logAppender;
    private List<LogEvent> loggedEvents;

    @Before
    public void setupAppender() {
        loggedEvents = new ArrayList<LogEvent>();
        logAppender = mock(Appender.class);
        when(logAppender.getName()).thenReturn("MockAppender");
        when(logAppender.isStarted()).thenReturn(true);

        // necessary because log4j uses one single instance of the MutableLogEvent,
        // overwriting previous entries
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                LogEvent logEvent = invocation.getArgumentAt(0, LogEvent.class);
                MutableLogEvent copy = new MutableLogEvent();
                copy.initFrom(logEvent);
                loggedEvents.add(copy);
                return null;
            }
        }).when(logAppender).append(any(LogEvent.class));

        Logger log4jRootLogger = (Logger) LogManager.getRootLogger();
        log4jRootLogger.addAppender(logAppender);
        Configurator.setAllLevels(log4jRootLogger.getName(), TRACE);
    }

    @After
    public void removeAppender() {
        Logger log4jRootLogger = (Logger) LogManager.getRootLogger();
        if (logAppender != null && logAppender.getName() != null) {
            log4jRootLogger.removeAppender(logAppender);
        }
    }

    protected Map<String, String> variables(String... strings) {
        Map<String, String> hashMap = new HashMap<String, String>();
        valueMap((Object[]) strings).forEach((key, value) -> hashMap.put(key, value.toString()));
        return hashMap;
    }

    Map<String, Object> valueMap(Object... objects) {
        Map<String, Object> hashMap = new HashMap<String, Object>();

        for (int i = 0; i < objects.length; i += 2) {
            if (i + 1 < objects.length) {
                hashMap.put(objects[i].toString(), objects[i + 1]);
            }
        }

        return hashMap;
    }

    protected boolean containsLineMatching(List<LogEvent> events, String pattern) {
        return events.stream().filter(line -> {
            String entry = line.getMessage().getFormattedMessage();
            return entry.matches(pattern);
        }).findFirst().isPresent();
    }

    protected boolean containsLineContaining(List<LogEvent> events, String subString) {
        return events.stream().filter(line -> line.getMessage().getFormattedMessage().contains(subString)).findFirst()
                .isPresent();
    }

    protected List<LogEvent> getLogEvents() {
        return loggedEvents;
    }

    protected List<LogEvent> getLogEventsWithLevel(Level level) {
        return getLogEvents().stream()//
                .filter(line -> line.getLevel() == level)//
                .collect(Collectors.toList());
    }

    protected String getMessageString(List<LogEvent> events, int index) {
        return events.get(index).getMessage().getFormattedMessage();
    }
}

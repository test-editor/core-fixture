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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testeditor.fixture.core.TestRunReporter.Action.ENTER;
import static org.testeditor.fixture.core.TestRunReporter.Action.LEAVE;
import static org.testeditor.fixture.core.TestRunReporter.SemanticUnit.COMPONENT;
import static org.testeditor.fixture.core.TestRunReporter.SemanticUnit.SPECIFICATION_STEP;
import static org.testeditor.fixture.core.TestRunReporter.SemanticUnit.STEP;
import static org.testeditor.fixture.core.TestRunReporter.SemanticUnit.TEST;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testeditor.fixture.core.TestRunReporter.Action;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;
import org.testeditor.fixture.core.TestRunReporter.Status;

public class TestDefaultTestRunReporter {
    private TestRunReporter classUnderTest = null;

    @Mock
    TestRunListener listener;
    @Mock
    TestRunListener secondListener;
    @Mock
    TestRunListener brokenListener1;
    @Mock
    TestRunListener brokenListener2;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new DefaultTestRunReporter();
        doThrow(new RuntimeException("exception")).when(brokenListener1).reported(any(SemanticUnit.class),
                any(Action.class), any(String.class), any(String.class), any(Status.class), any());
        doThrow(new RuntimeException("exception")).when(brokenListener2).reported(any(SemanticUnit.class),
                any(Action.class), any(String.class), any(String.class), any(Status.class), any());
    }

    @Test
    public void testAddingListeners() {
        // given
        classUnderTest.addListener(listener);

        // when
        classUnderTest.enter(TEST, "Test", "ID", Status.OK, null);

        // then
        verify(listener).reported(TEST, ENTER, "Test", "ID", Status.OK, null);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testRemoval() {
        // given
        classUnderTest.addListener(listener);
        classUnderTest.removeListener(listener);

        // when
        classUnderTest.enter(TEST, "Test", "ID", Status.OK, null);

        // then
        verify(listener, never()).reported(TEST, ENTER, "Test", "ID", Status.OK, null);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testSemanticUnitLeaveOrder() {
        // given
        classUnderTest.addListener(listener);

        // when
        classUnderTest.enter(TEST, "Test", "ID1", Status.STARTED, null);
        classUnderTest.enter(SPECIFICATION_STEP, "Specification1", "ID2", Status.STARTED, null);
        classUnderTest.enter(COMPONENT, "Component1.1", "ID3", Status.STARTED, null);
        classUnderTest.enter(STEP, "Step1.1.1", "ID4", Status.STARTED, null);
        classUnderTest.leave(STEP, "Step1.1.1", "ID4", Status.OK, null);
        classUnderTest.enter(STEP, "Step1.1.2", "ID6", Status.STARTED, null);
        classUnderTest.leave(STEP, "Step1.1.2", "ID6", Status.OK, null);
        classUnderTest.leave(COMPONENT, "Component1.1", "ID3", Status.OK, null);
        classUnderTest.enter(COMPONENT, "Component1.2", "ID9", Status.STARTED, null);
        classUnderTest.enter(STEP, "Step1.2.1", "ID10", Status.STARTED, null);
        classUnderTest.leave(STEP, "Step1.2.1", "ID10", Status.OK, null);
        classUnderTest.leave(COMPONENT, "Component1.2", "ID9", Status.OK, null);
        classUnderTest.leave(SPECIFICATION_STEP, "Specification1", "ID2", Status.OK, null);
        classUnderTest.enter(SPECIFICATION_STEP, "Specification2", "ID14", Status.STARTED, null);
        classUnderTest.enter(COMPONENT, "Component2.1", "ID15", Status.STARTED, null);
        classUnderTest.enter(STEP, "Step2.1.1", "ID16", Status.STARTED, null);
        classUnderTest.leave(STEP, "Step2.1.1", "ID16", Status.OK, null);
        classUnderTest.leave(COMPONENT, "Component2.1", "ID15", Status.OK, null);
        classUnderTest.leave(SPECIFICATION_STEP, "Specification2", "ID14", Status.OK, null);
        classUnderTest.leave(TEST, "Test", "ID1", Status.OK, null);

        // then
        InOrder listenerOrder = inOrder(listener);
        listenerOrder.verify(listener).reported(TEST, ENTER, "Test", "ID1", Status.STARTED, null);
        listenerOrder.verify(listener).reported(SPECIFICATION_STEP, ENTER, "Specification1", "ID2", Status.STARTED,
                null);
        listenerOrder.verify(listener).reported(COMPONENT, ENTER, "Component1.1", "ID3", Status.STARTED, null);
        listenerOrder.verify(listener).reported(STEP, ENTER, "Step1.1.1", "ID4", Status.STARTED, null);
        listenerOrder.verify(listener).reported(STEP, LEAVE, "Step1.1.1", "ID4", Status.OK, null);
        listenerOrder.verify(listener).reported(STEP, ENTER, "Step1.1.2", "ID6", Status.STARTED, null);
        listenerOrder.verify(listener).reported(STEP, LEAVE, "Step1.1.2", "ID6", Status.OK, null);
        listenerOrder.verify(listener).reported(COMPONENT, LEAVE, "Component1.1", "ID3", Status.OK, null);
        listenerOrder.verify(listener).reported(COMPONENT, ENTER, "Component1.2", "ID9", Status.STARTED, null);
        listenerOrder.verify(listener).reported(STEP, ENTER, "Step1.2.1", "ID10", Status.STARTED, null);
        listenerOrder.verify(listener).reported(STEP, LEAVE, "Step1.2.1", "ID10", Status.OK, null);
        listenerOrder.verify(listener).reported(COMPONENT, LEAVE, "Component1.2", "ID9", Status.OK, null);
        listenerOrder.verify(listener).reported(SPECIFICATION_STEP, LEAVE, "Specification1", "ID2", Status.OK, null);
        listenerOrder.verify(listener).reported(SPECIFICATION_STEP, ENTER, "Specification2", "ID14", Status.STARTED,
                null);
        listenerOrder.verify(listener).reported(COMPONENT, ENTER, "Component2.1", "ID15", Status.STARTED, null);
        listenerOrder.verify(listener).reported(STEP, ENTER, "Step2.1.1", "ID16", Status.STARTED, null);
        listenerOrder.verify(listener).reported(STEP, LEAVE, "Step2.1.1", "ID16", Status.OK, null);
        listenerOrder.verify(listener).reported(COMPONENT, LEAVE, "Component2.1", "ID15", Status.OK, null);
        listenerOrder.verify(listener).reported(SPECIFICATION_STEP, LEAVE, "Specification2", "ID14", Status.OK, null);
        listenerOrder.verify(listener).reported(TEST, LEAVE, "Test", "ID1", Status.OK, null);
        listenerOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testBrokenListener() {
        // given
        classUnderTest.addListener(brokenListener1);
        classUnderTest.addListener(brokenListener2);
        classUnderTest.addListener(listener);

        // when
        classUnderTest.enter(TEST, "Test", "ID", Status.STARTED, null);

        // then
        verify(brokenListener1).reported(TEST, ENTER, "Test", "ID", Status.STARTED, null);
        verify(brokenListener2).reported(TEST, ENTER, "Test", "ID", Status.STARTED, null);
        verify(listener).reported(TEST, ENTER, "Test", "ID", Status.STARTED, null);
        verifyNoMoreInteractions(listener, brokenListener1, brokenListener2);
    }

}

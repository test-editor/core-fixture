package org.testeditor.fixture.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testeditor.fixture.core.TestRunReporter.Position;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;

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
		Mockito.doThrow(new RuntimeException("exception")).when(brokenListener1)
				.reported(Mockito.any(SemanticUnit.class), Mockito.any(Position.class), Mockito.any(String.class));
		Mockito.doThrow(new RuntimeException("exception")).when(brokenListener2)
				.reported(Mockito.any(SemanticUnit.class), Mockito.any(Position.class), Mockito.any(String.class));
	}

	@Test
	public void testAddingListeners() {
		// given
		classUnderTest.addListener(listener);

		// when
		classUnderTest.enter(SemanticUnit.TEST, "Test");

		// then
		Mockito.verify(listener).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		Mockito.verifyNoMoreInteractions(listener);
	}

	@Test
	public void testAddSemanticPositionListener() {
		// given
		classUnderTest.addListener(SemanticUnit.COMPONENT, Position.LEAVE, listener);

		// when
		classUnderTest.enter(SemanticUnit.TEST, "Test");
		classUnderTest.enter(SemanticUnit.COMPONENT, "Component");
		classUnderTest.leave(SemanticUnit.TEST);

		// then
		Mockito.verify(listener).reported(SemanticUnit.COMPONENT, Position.LEAVE, "Component");
		Mockito.verifyNoMoreInteractions(listener);
	}

	@Test
	public void testRemoval() {
		// given
		classUnderTest.addListener(listener);
		classUnderTest.removeListener(listener);

		// when
		classUnderTest.enter(SemanticUnit.TEST, "Test");

		// then
		Mockito.verify(listener, Mockito.never()).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		Mockito.verifyNoMoreInteractions(listener);
	}

	@Test
	public void testMultipleListeners() {
		// given
		classUnderTest.addListener(listener);
		classUnderTest.addListener(SemanticUnit.TEST, Position.ENTER, secondListener);

		// when
		classUnderTest.enter(SemanticUnit.TEST, "Test");
		classUnderTest.enter(SemanticUnit.COMPONENT, "Component");
		classUnderTest.leave(SemanticUnit.TEST);

		// then
		InOrder listenerOrder = Mockito.inOrder(listener);
		listenerOrder.verify(listener).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		listenerOrder.verify(listener).reported(SemanticUnit.COMPONENT, Position.ENTER, "Component");
		listenerOrder.verify(listener).reported(SemanticUnit.COMPONENT, Position.LEAVE, "Component");
		listenerOrder.verify(listener).reported(SemanticUnit.TEST, Position.LEAVE, "Test");

		Mockito.verify(secondListener).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		Mockito.verifyNoMoreInteractions(listener, secondListener);
	}

	@Test
	public void testSemanticUnitLeaveOrder() {
		// given
		classUnderTest.addListener(listener);

		// when
		classUnderTest.enter(SemanticUnit.TEST, "Test");
		classUnderTest.enter(SemanticUnit.SPECIFICATION, "Specification1");
		classUnderTest.enter(SemanticUnit.COMPONENT, "Component1.1");
		classUnderTest.enter(SemanticUnit.STEP, "Step1.1.1");
		classUnderTest.enter(SemanticUnit.STEP, "Step1.1.2");
		classUnderTest.enter(SemanticUnit.COMPONENT, "Component1.2");
		classUnderTest.enter(SemanticUnit.STEP, "Step1.2.1");
		classUnderTest.enter(SemanticUnit.SPECIFICATION, "Specification2");
		classUnderTest.enter(SemanticUnit.COMPONENT, "Component2.1");
		classUnderTest.enter(SemanticUnit.STEP, "Step2.1.1");
		classUnderTest.leave(SemanticUnit.TEST);

		// then
		InOrder listenerOrder = Mockito.inOrder(listener);
		listenerOrder.verify(listener).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		listenerOrder.verify(listener).reported(SemanticUnit.SPECIFICATION, Position.ENTER, "Specification1");
		listenerOrder.verify(listener).reported(SemanticUnit.COMPONENT, Position.ENTER, "Component1.1");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.ENTER, "Step1.1.1");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.LEAVE, "Step1.1.1");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.ENTER, "Step1.1.2");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.LEAVE, "Step1.1.2");
		listenerOrder.verify(listener).reported(SemanticUnit.COMPONENT, Position.LEAVE, "Component1.1");
		listenerOrder.verify(listener).reported(SemanticUnit.COMPONENT, Position.ENTER, "Component1.2");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.ENTER, "Step1.2.1");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.LEAVE, "Step1.2.1");
		listenerOrder.verify(listener).reported(SemanticUnit.COMPONENT, Position.LEAVE, "Component1.2");
		listenerOrder.verify(listener).reported(SemanticUnit.SPECIFICATION, Position.LEAVE, "Specification1");
		listenerOrder.verify(listener).reported(SemanticUnit.SPECIFICATION, Position.ENTER, "Specification2");
		listenerOrder.verify(listener).reported(SemanticUnit.COMPONENT, Position.ENTER, "Component2.1");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.ENTER, "Step2.1.1");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.LEAVE, "Step2.1.1");
		listenerOrder.verify(listener).reported(SemanticUnit.COMPONENT, Position.LEAVE, "Component2.1");
		listenerOrder.verify(listener).reported(SemanticUnit.SPECIFICATION, Position.LEAVE, "Specification2");
		listenerOrder.verify(listener).reported(SemanticUnit.TEST, Position.LEAVE, "Test");
		listenerOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSemanticUnitLeaveSparseLevel() {
		// given
		classUnderTest.addListener(listener);

		// when
		classUnderTest.enter(SemanticUnit.TEST, "Test");
		classUnderTest.enter(SemanticUnit.STEP, "Step");
		classUnderTest.leave(SemanticUnit.TEST);

		// then
		InOrder listenerOrder = Mockito.inOrder(listener);
		listenerOrder.verify(listener).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.ENTER, "Step");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.LEAVE, "Step");
		listenerOrder.verify(listener).reported(SemanticUnit.TEST, Position.LEAVE, "Test");
		listenerOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSemanticUnitLeaveMultipleLevel() {
		// given
		classUnderTest.addListener(listener);

		// when
		classUnderTest.enter(SemanticUnit.TEST, "Test");
		classUnderTest.enter(SemanticUnit.SPECIFICATION, "Specification");
		classUnderTest.enter(SemanticUnit.COMPONENT, "Component");
		classUnderTest.enter(SemanticUnit.STEP, "Step");
		classUnderTest.leave(SemanticUnit.TEST);

		// then
		InOrder listenerOrder = Mockito.inOrder(listener);
		listenerOrder.verify(listener).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		listenerOrder.verify(listener).reported(SemanticUnit.SPECIFICATION, Position.ENTER, "Specification");
		listenerOrder.verify(listener).reported(SemanticUnit.COMPONENT, Position.ENTER, "Component");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.ENTER, "Step");
		listenerOrder.verify(listener).reported(SemanticUnit.STEP, Position.LEAVE, "Step");
		listenerOrder.verify(listener).reported(SemanticUnit.COMPONENT, Position.LEAVE, "Component");
		listenerOrder.verify(listener).reported(SemanticUnit.SPECIFICATION, Position.LEAVE, "Specification");
		listenerOrder.verify(listener).reported(SemanticUnit.TEST, Position.LEAVE, "Test");
		listenerOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testBrokenListener() {
		// given
		classUnderTest.addListener(brokenListener1);
		classUnderTest.addListener(brokenListener2);
		classUnderTest.addListener(listener);

		// when
		classUnderTest.enter(SemanticUnit.TEST, "Test");

		// then
		Mockito.verify(brokenListener1).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		Mockito.verify(brokenListener2).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		Mockito.verify(listener).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		Mockito.verifyNoMoreInteractions(listener, brokenListener1, brokenListener2);
	}

	@Test
	public void testPartialPositionListeners() {
		// given
		classUnderTest.addListener(Position.LEAVE, listener);
		classUnderTest.addListener(Position.ENTER, secondListener);

		// when
		classUnderTest.enter(SemanticUnit.TEST, "Test");
		classUnderTest.enter(SemanticUnit.SPECIFICATION, "Specification");
		classUnderTest.leave(SemanticUnit.TEST);

		// then
		InOrder listenerOrder = Mockito.inOrder(listener,secondListener);
		listenerOrder.verify(secondListener).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		listenerOrder.verify(secondListener).reported(SemanticUnit.SPECIFICATION, Position.ENTER, "Specification");
		listenerOrder.verify(listener).reported(SemanticUnit.SPECIFICATION, Position.LEAVE, "Specification");
		listenerOrder.verify(listener).reported(SemanticUnit.TEST, Position.LEAVE, "Test");
		listenerOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testPartialSemanticUnitListeners() {
		// given
		classUnderTest.addListener(SemanticUnit.SPECIFICATION, listener);
		classUnderTest.addListener(SemanticUnit.TEST, secondListener);

		// when
		classUnderTest.enter(SemanticUnit.TEST, "Test");
		classUnderTest.enter(SemanticUnit.SPECIFICATION, "Specification");
		classUnderTest.leave(SemanticUnit.TEST);

		// then
		InOrder listenerOrder = Mockito.inOrder(listener, secondListener);
		listenerOrder.verify(secondListener).reported(SemanticUnit.TEST, Position.ENTER, "Test");
		listenerOrder.verify(listener).reported(SemanticUnit.SPECIFICATION, Position.ENTER, "Specification");
		listenerOrder.verify(listener).reported(SemanticUnit.SPECIFICATION, Position.LEAVE, "Specification");
		listenerOrder.verify(secondListener).reported(SemanticUnit.TEST, Position.LEAVE, "Test");
		listenerOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testPartialUnitListenerRemoval() {
		// given
		classUnderTest.addListener(SemanticUnit.SPECIFICATION, listener);
		classUnderTest.addListener(SemanticUnit.SPECIFICATION, secondListener);

		// when
		classUnderTest.removeListener(SemanticUnit.SPECIFICATION, listener);
		classUnderTest.enter(SemanticUnit.TEST, "Test");
		classUnderTest.enter(SemanticUnit.SPECIFICATION, "Specification");
		classUnderTest.leave(SemanticUnit.TEST);

		// then
		Mockito.verify(secondListener).reported(SemanticUnit.SPECIFICATION, Position.ENTER, "Specification");
		Mockito.verify(secondListener).reported(SemanticUnit.SPECIFICATION, Position.LEAVE, "Specification");
		Mockito.verifyNoMoreInteractions(listener, secondListener);
	}

	@Test
	public void testPartialPositionListenerRemoval() {
		// given
		classUnderTest.addListener(Position.LEAVE, listener);
		classUnderTest.addListener(Position.LEAVE, secondListener);

		// when
		classUnderTest.removeListener(Position.LEAVE, listener);
		classUnderTest.enter(SemanticUnit.TEST, "Test");
		classUnderTest.enter(SemanticUnit.SPECIFICATION, "Specification");
		classUnderTest.leave(SemanticUnit.TEST);

		// then
		Mockito.verify(secondListener).reported(SemanticUnit.SPECIFICATION, Position.LEAVE, "Specification");
		Mockito.verify(secondListener).reported(SemanticUnit.TEST, Position.LEAVE, "Test");
		Mockito.verifyNoMoreInteractions(listener, secondListener);
	}

}

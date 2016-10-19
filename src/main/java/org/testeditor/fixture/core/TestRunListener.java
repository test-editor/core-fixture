package org.testeditor.fixture.core;

/**
 * listener called by TestRunReporter if registered accordingly
 */
public interface TestRunListener {
	void reported(TestRunReporter.SemanticUnit unit, TestRunReporter.Position position, String message);
}

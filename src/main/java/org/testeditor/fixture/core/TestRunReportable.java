package org.testeditor.fixture.core;

/**
 * Any variable instantiated by the test, implementing this interface, will be called to ensure that e.g. fixtures 
 * will have a chance before actually being called to get hold of the instance of the TestRunReporter that is used 
 * to be able to register listeners (see SWTFixture).
 * 
 * The test generator will generate a constructor which will call this method on all instance variables of this class
 * that implement this interface.
 */
public interface TestRunReportable {
	void initWithReporter(TestRunReporter reporter);
}

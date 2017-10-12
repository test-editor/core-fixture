package org.testeditor.fixture.core;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class TestMasking {

	StringMasker loggingMasker; // class under test

	@Mock
	TestRunListener delegate;

	@Before
	public void setupClassUnderTest() {
		loggingMasker = new DefaultLoggingMessageMasker(delegate);
	}

	@Test
	public void testNoPatternNoMasking() {
		// when
		String message = "Some message";

		// when
		String masked = loggingMasker.mask(message);

		// then
		Assert.assertEquals(message, masked);
	}

	@Test
	public void testOnePatternMatching() {
		// given
		loggingMasker.registerMaskPattern(Pattern.compile(".*(mes).*"));
		String message = "Some message";
		String expectedMessage = "Some *****sage";

		// when
		String masked = loggingMasker.mask(message);

		// then
		Assert.assertEquals(expectedMessage, masked);
	}

	@Test
	public void testMultiOccuranceOfPattern() {
		// given
		loggingMasker.registerMaskPattern(Pattern.compile(".*(mes).*"));
		String message = "Some message and another message";
		String expectedMessage = "Some *****sage and another *****sage";

		// when
		String masked = loggingMasker.mask(message);

		// then
		Assert.assertEquals(expectedMessage, masked);
	}

	@Test
	public void testMultiplePatterns() {
		// given
		loggingMasker.registerMaskPattern(Pattern.compile(".*(mes).*"));
		loggingMasker.registerMaskPattern(Pattern.compile(".*(other).*"));
		String message = "Some message and another message";
		String expectedMessage = "Some *****sage and an***** *****sage";

		// when
		String masked = loggingMasker.mask(message);

		// then
		Assert.assertEquals(expectedMessage, masked);
	}

	@Test
	public void unregisterPatternLeavesMessageUntouched() {
		// given
		Pattern pattern = Pattern.compile(".*(mes).*");
		String message = "Some message";

		// when
		loggingMasker.registerMaskPattern(pattern);
		loggingMasker.unregisterMaskPattern(pattern);
		String masked = loggingMasker.mask(message);

		// then
		Assert.assertEquals(message, masked);
	}

	@Test(expected = RuntimeException.class)
	public void registerPatternIllegallyHavingNoGroup() {
		loggingMasker.registerMaskPattern(Pattern.compile("abc"));
	}

}

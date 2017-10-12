package org.testeditor.fixture.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

public class MaskingLoggerTest {

	Logger maskingLogger; // class under test

	@Mock
	Logger delegatedToLogger;
	@Mock
	StringMasker masker;

	@Before
	public void setupClassUnderTest() {
		MockitoAnnotations.initMocks(this);
		maskingLogger = new DefaultMaskingLogger(delegatedToLogger, masker);
	}

	@Test
	public void testThatDelegatedLoggerGetsMaskedMessage() {
		// given
		final String msg = "somemsg";
		final String maskedMsg = "maskedmsg";
		Mockito.when(masker.mask(msg)).thenReturn(maskedMsg);

		// when
		maskingLogger.debug(msg);

		// then
		Mockito.verify(masker).mask(msg);
		Mockito.verify(delegatedToLogger).debug(maskedMsg);
	}

	@Test
	public void testThatDelegatedLoggerGetsFormattedMaskedMessage() {
		// given
		final String format = "some {} messages";
		final long someLong = 42;
		final String formattedMsg = "some 42 messages";
		final String maskedMsg = "my masked message";
		Mockito.when(masker.mask(formattedMsg)).thenReturn(maskedMsg);

		// when
		maskingLogger.debug(format, someLong);

		// then
		Mockito.verify(masker).mask(formattedMsg);
		Mockito.verify(delegatedToLogger).debug(maskedMsg);
	}

}

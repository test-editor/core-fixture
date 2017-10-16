package org.testeditor.fixture.core;

/**
 * This class is intended as a wrapper for confidential information in strings
 * that should not end up within logs or other unwanted artifacts.
 *
 * <br/><br/>
 * It implements a custom toString method that simply returns *****. In order to
 * retrieve the actual value get must be called.
 *
 * Example usage:
 * <pre>
 *   private MaskingString stringWrapped = new MaskingString("Some confidential information");
 *
 *   // usage with actual value
 *   service.call(stringWrapped.getValue());
 *
 *   // usage for logging (will log ***** instead of actual value)
 *   logger.warn("Unexpected behaviour using parameter '{}'.", stringWrapped);
 * </pre>
 */
public class MaskingString {

	private String wrappedString;

	public MaskingString(String wrappedString) {
		this.wrappedString = wrappedString;
	}

	public String get() {
		return this.wrappedString;
	}

	@Override
	public String toString() {
		return "*****";
	}

}

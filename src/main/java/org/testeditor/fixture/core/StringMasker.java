package org.testeditor.fixture.core;

import java.util.regex.Pattern;

/**
 * Mask found patterns in a String.
 * 
 * Allow for register/deregister patterns that should be masked.
 */
public interface StringMasker {
	/** 
	 * Mask the string given trying all known patterns (order not guaranteed) with 5 asteriks (*)
	 * 
	 * @param unmasked
	 * @return masked string
	 */
	String mask(String unmasked);
	
	/**
	 * Register a pattern that must have exactly one group. This group is masked (in a masking step) if the pattern matches.
	 * @param pattern
	 */
	void registerMaskPattern(Pattern pattern);
	
	/**
	 * Unregister a pattern that was previously registered. Subsequent calls to mask will no longer check this pattern.
	 * @param pattern
	 */
	void unregisterMaskPattern(Pattern pattern);
}

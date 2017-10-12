package org.testeditor.fixture.core;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testeditor.fixture.core.TestRunReporter.Action;
import org.testeditor.fixture.core.TestRunReporter.SemanticUnit;

public class DefaultLoggingMessageMasker implements TestRunListener, StringMasker {

	private TestRunListener delegate;
	private List<Pattern> patterns = new LinkedList<>();

	public DefaultLoggingMessageMasker(TestRunListener wrappedLoggingListener) {
		this.delegate = wrappedLoggingListener;
	}

	@Override
	public void reported(SemanticUnit unit, Action action, String message) {
		this.delegate.reported(unit, action, mask(message));
	}

	@Override
	public String mask(String unmasked) {
		CharSequence partiallyMasked = unmasked;
		for (Pattern pattern : patterns) {
			partiallyMasked = repeatedlyApplyMaskingPattern(pattern, partiallyMasked);
		}
		return partiallyMasked.toString();
	}

	private CharSequence repeatedlyApplyMaskingPattern(Pattern pattern, CharSequence unmasked) {
		CharSequence partiallyMasked = unmasked;
		boolean worthATry = true;
		while (worthATry) {
			Matcher matcher = pattern.matcher(partiallyMasked);
			if (matcher.matches()) {
				partiallyMasked = applyMaskingMatcher(matcher, partiallyMasked);
			} else {
				worthATry = false;
			}
		}
		return partiallyMasked;
	}

	private CharSequence applyMaskingMatcher(Matcher matcher, CharSequence unmasked) {
		int start = matcher.start(1);
		int end = matcher.end(1);
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(unmasked.subSequence(0, start));
		stringBuffer.append("*****");
		stringBuffer.append(unmasked.subSequence(end, unmasked.length()));
		return stringBuffer;
	}

	@Override
	public void registerMaskPattern(Pattern pattern) {
		if (pattern.matcher("").groupCount() != 1) {
			throw new RuntimeException("Pattern must contain exactly one capturing group (see https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#cg)");
		}
		this.patterns.add(pattern);
	}

	@Override
	public void unregisterMaskPattern(Pattern pattern) {
		this.patterns.remove(pattern);
	}

}

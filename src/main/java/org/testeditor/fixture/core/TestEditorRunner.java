/*******************************************************************************
 * Copyright (c) 2012 - 2015 Signal Iduna Corporation and others.

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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class TestEditorRunner extends ParentRunner<FrameworkMethod> {

	private final ConcurrentHashMap<FrameworkMethod, Description> methodDescriptions = new ConcurrentHashMap<FrameworkMethod, Description>();
	private Object tclTest;
	private FrameworkMethod lastTestStep;

	public TestEditorRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected List<FrameworkMethod> getChildren() {
		Stream<Method> methods = Arrays.stream(getTestClass().getJavaClass().getMethods()).filter(
				m -> Arrays.stream(m.getAnnotations()).anyMatch(a -> a.annotationType().equals(TestStepMethod.class)));
		List<FrameworkMethod> result = methods.map(m -> new FrameworkMethod(m)).collect(Collectors.toList());
		result.sort(getComparator());
		lastTestStep = result.get(result.size() - 1);
		return result;
	}

	private Comparator<? super FrameworkMethod> getComparator() {
		return new Comparator<FrameworkMethod>() {

			@Override
			public int compare(FrameworkMethod o1, FrameworkMethod o2) {
				return Integer.parseInt(o1.getAnnotation(TestStepMethod.class).value()[0])
						- Integer.parseInt(o2.getAnnotation(TestStepMethod.class).value()[0]);
			}
		};
	}

	@Override
	protected Description describeChild(FrameworkMethod method) {
		Description description = methodDescriptions.get(method);
		if (description == null) {
			description = Description.createTestDescription(getTestClass().getJavaClass(),
					method.getAnnotation(TestStepMethod.class).value()[1], method.getAnnotations());
			methodDescriptions.putIfAbsent(method, description);
		}
		return description;
	}

	@Override
	protected void runChild(FrameworkMethod child, RunNotifier notifier) {
		Description description = describeChild(child);
		try {
			if (tclTest == null) {
				tclTest = getTestClass().getOnlyConstructor().newInstance();
				List<FrameworkMethod> beforeMethods = getTestClass().getAnnotatedMethods(Before.class);
				invodeSurroundingMethods(beforeMethods, tclTest);
			}
			EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
			eachNotifier.fireTestStarted();
			child.invokeExplosively(tclTest);
			eachNotifier.fireTestFinished();
			if (child == lastTestStep) {
				List<FrameworkMethod> afterMethods = getTestClass().getAnnotatedMethods(After.class);
				invodeSurroundingMethods(afterMethods, tclTest);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			notifier.fireTestFailure(new Failure(description, e));
		}
	}

	private void invodeSurroundingMethods(List<FrameworkMethod> methods, Object instance) throws Throwable {
		for (FrameworkMethod frameworkMethod : methods) {
			frameworkMethod.invokeExplosively(instance);
		}
	}

}

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

package org.testeditor.fixture.core.parameterized;

import org.junit.runners.model.InitializationError;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.TestWithParameters;

public class TestEditorParameterizedRunner extends BlockJUnit4ClassRunnerWithParameters {
    
    private final Object[] parametersWithName; 

    /**
     * Creates a new runner for parameterized tests that also injects the test
     * name.
     * Test class must use constructor injection, field injection is not supported.
     * There must be exactly one constructor, which has to accept exactly n+1
     * arguments, where n is the number of parameters. The name of the test will
     * be injected as last constructor argument, which needs to be of type String.
     * @param test a TestWithParameters object from which to initialize the test
     * @throws InitializationError if the test class is malformed.
     */
    public TestEditorParameterizedRunner(TestWithParameters test) throws InitializationError {
        super(test);
        var parameters = test.getParameters();
        parametersWithName = parameters.toArray(new Object[parameters.size() + 1]);
        parametersWithName[parameters.size()] = test.getName();
    }
    
    @Override
    public Object createTest() throws Exception {
        return getTestClass().getOnlyConstructor().newInstance(parametersWithName);
    }

}

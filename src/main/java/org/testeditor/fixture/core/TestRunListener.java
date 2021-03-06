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

package org.testeditor.fixture.core;

import java.util.Map;

import org.testeditor.fixture.core.TestRunReporter.Status;

/**
 * listener called by TestRunReporter if registered accordingly
 */
public interface TestRunListener {
    void reported(TestRunReporter.SemanticUnit unit, TestRunReporter.Action action, String message, String id,
            Status status, Map<String, String> variables);

    void reportFixtureExit(FixtureException fixtureException);

    void reportExceptionExit(Exception exception);

    void reportAssertionExit(AssertionError assertionError);

}

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

package org.testeditor.fixture.core.artifacts;

/**
 * Represents any kind of file that was written during test execution.
 * 
 * Examples include log files, screenshots or screencasts of the system while it
 * is being tested, or any other kind of test report.
 */
public class TestArtifact {
    private final String type;
    private final String path;

    /**
     * Creates a new test artifact.
     * @param type a string identifying the type of artifact, e.g. "screenshot".
     * @param path the file system path pointing to the artifact.
     */
    public TestArtifact(String type, String path) {
        this.type = type;
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }
}

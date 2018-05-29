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

import java.util.Collections;
import java.util.Map;

/**
 * Every fixture should throw an FixtureException if an error occurs that can be
 * interpreted/understood by the fixture itself. It should then provide
 * additional information that will help the fixture user to identify the
 * problem. Every other exception that is thrown during a call to a fixture is
 * then related to low level issues that (usually) cannot be remedied by a
 * tester using the fixture.
 * 
 * Every user of the fixture must handle fixture exceptions (thus a checked
 * exception is used).
 * 
 * The keyValueStore should hold only data that can easily be serialized into a
 * json object (since this data is send over the wire): String, Number, Map,
 * Array, List
 */
public class FixtureException extends Exception {

    private static final long serialVersionUID = -1424461459089117086L;

    private Map<String, Object> keyValueStore;

    public Map<String, Object> getKeyValueStore() {
        return Collections.unmodifiableMap(keyValueStore);
    }

    public FixtureException(String message, Map<String, Object> keyValueStore, Exception root) {
        super(message, root);
        this.keyValueStore = keyValueStore;
    }

    public FixtureException(String message, Map<String, Object> keyValueStore) {
        super(message);
        this.keyValueStore = keyValueStore;
    }

    public FixtureException(String message) {
        super(message);
        this.keyValueStore = Collections.emptyMap();
    }

    public FixtureException(String message, Exception root) {
        super(message, root);
        this.keyValueStore = Collections.emptyMap();
    }

}

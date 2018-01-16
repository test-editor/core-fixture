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

package org.testeditor.fixture.core.logging;

import org.junit.Assert;
import org.junit.Test;

public class FilenameHelperTest {

    private FilenameHelper helper = new FilenameHelper();

    @Test
    public void createFilename() {
        // given
        String filebaseName = "Host";
        String testCaseName = "LoginTest";
        String pathName = "screenshots";
        String fileType = "html";

        // when
        String constructedFilename = helper.constructFilename(pathName, testCaseName, filebaseName, fileType);

        // then
        Assert.assertTrue(constructedFilename.matches("screenshots\\/LoginTest\\/\\d{8}\\/\\d{6}\\.\\d{3}-Host.html"));
    }

    @Test
    public void createLongTestCaseName() {
        // given
        String filebaseName = "Host";
        String testCaseName = "ThisIsaVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVery"
                + "VeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLoginTest";
        String pathName = "screenshots";
        String fileType = "html";

        // when
        String constructedFilename = helper.constructFilename(pathName, testCaseName, filebaseName, fileType);

        // then
        Assert.assertTrue(constructedFilename.matches(
                "screenshots\\/ThisIsaVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVery"
                        + "VeryVeryVeryVeryVeryVeryVeryVeryVeryVeryV\\/\\d{8}\\/\\d{6}\\.\\d{3}-Host.html"));
    }

    @Test
    public void createLongFileBaseName() {
        // given
        String filebaseName = "ThisIsaVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVery"
                + "VeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVerysHost";
        String testCaseName = "LoginTest";
        String pathName = "screenshots";
        String fileType = "html";

        // when
        String constructedFilename = helper.constructFilename(pathName, testCaseName, filebaseName, fileType);

        // then
        Assert.assertTrue(constructedFilename.matches(
                "screenshots\\/LoginTest\\/\\d{8}\\/\\d{6}.\\d{3}-ThisIsaVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVery"
                        + "VeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryV.html"));
    }

}

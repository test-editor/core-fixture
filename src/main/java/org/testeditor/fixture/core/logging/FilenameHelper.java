/*******************************************************************************
 * Copyright (c) 2012 - 2017 Signal Iduna Corporation and others.
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class FilenameHelper {

    private static final int FILENAME_MAXLEN = 128;

    /**
     * Constructs a file name, based on the basic filenameBase provided. The
     * final filename is constructed using the testcase a hash of the fixture
     * itself and a shortened timestamp.
     * 
     * @param pathName
     *            the path in which the file is stored
     * @param testcase
     *            name of the test case which is executed
     * @param filenameBase
     *            user definable part of the final filename
     * @param type
     *            File type like "html", "png" or equivalent, depends which type
     *            should be created
     * @return the constructed filename with the above attributes
     */
    public String constructFilename(String pathName, String testcase, String filenameBase, String type) {
        String additionalGraphicType = "." + type;
        String escapedBaseName = filenameBase.replaceAll("[^a-zA-Z0-9.-]", "_").replaceAll("_+", "_")
                .replaceAll("_+\\.", ".").replaceAll("\\._+", ".");
        String timeStr = new SimpleDateFormat("HHmmss.SSS").format(new Date());
        String dateStr = new SimpleDateFormat("YYYYMMdd").format(new Date());
        StringBuffer finalFilenameBuffer = new StringBuffer();
        int lenOfFixedElements = timeStr.length() + additionalGraphicType.length() + 1/* hyphen */;
        finalFilenameBuffer //
                .append(pathName) //
                .append('/').append(reduceToMaxLen(testcase, FILENAME_MAXLEN))//
                .append('/').append(dateStr) //
                .append('/').append(timeStr).append('-') //
                .append(reduceToMaxLen(escapedBaseName, FILENAME_MAXLEN - lenOfFixedElements))//
                .append(additionalGraphicType);
        return finalFilenameBuffer.toString();
    }

    private String reduceToMaxLen(String base, int maxLen) {
        if (base.length() < maxLen) {
            return base;
        } else {
            return base.substring(0, maxLen);
        }
    }

}

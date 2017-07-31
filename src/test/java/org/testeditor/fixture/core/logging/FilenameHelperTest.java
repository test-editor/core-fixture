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
        String testCaseName = "ThisIsaVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLoginTest";
        String pathName = "screenshots";
        String fileType = "html";

        // when
        String constructedFilename = helper.constructFilename(pathName, testCaseName, filebaseName, fileType);

        // then
        Assert.assertTrue(constructedFilename.matches(
                "screenshots\\/ThisIsaVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryV\\/\\d{8}\\/\\d{6}\\.\\d{3}-Host.html"));
    }

    @Test
    public void createLongFileBaseName() {
        // given
        String filebaseName = "ThisIsaVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVerysHost";
        String testCaseName = "LoginTest";
        String pathName = "screenshots";
        String fileType = "html";

        // when
        String constructedFilename = helper.constructFilename(pathName, testCaseName, filebaseName, fileType);

        // then
        Assert.assertTrue(constructedFilename.matches(
                "screenshots\\/LoginTest\\/\\d{8}\\/\\d{6}.\\d{3}-ThisIsaVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryV.html"));
    }

}

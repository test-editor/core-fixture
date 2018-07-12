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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.Level.WARN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testeditor.fixture.core.artifacts.TestArtifactRegistry.EnvironmentAccess;
import org.testeditor.fixture.core.artifacts.TestArtifactRegistry.FileSystemAccess;

public class TestArtifactRegistryTest {

    Appender logAppender;
    ArgumentCaptor<LogEvent> logCaptor;

    private FileSystemAccess brokenFileSystem = new FileSystemAccess() {
        @Override
        public Path createDirectories(String basePath, String... pathElements) throws IOException {
            throw new IOException("File System is broken");
        }
    };

    private FileSystemAccess flakyFileSystem() {
        Writer mockWriter = mock(Writer.class);
        try {
            when(mockWriter.append(Mockito.any()))
                .thenReturn(mockWriter)
                .thenReturn(mockWriter)
                .thenThrow(new IOException());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Problem during test setup");
        }
        return new FileSystemAccess() {
            @Override
            public Writer getBufferedWriter(Path target) throws IOException {
                return mockWriter;
            }
        };
    }

    private EnvironmentAccess defaultEnvironment = new EnvironmentAccess() {
        public String get(String key) {
            switch (key) {
                case "TE_SUITEID": return "suite0";
                case "TE_SUITERUNID": return "suiterun23";
                case "TE_TESTRUNID": return "testrun42";
                default:
                    return null;
            }
        }
    };

    private EnvironmentAccess emptyEnvironment = new EnvironmentAccess() {
        public String get(String key) {
            return null;
        }
    };

    private TestArtifact screenshotArtifact = new TestArtifact("screenshot", "screenshots/sampleScreenshot.png");
    private TestArtifact screencastArtifact = new TestArtifact("screencast", "videos/sampleScreencast.mov");
    private static final String ARTIFACT_FILE_PATH = 
            ".testexecution/artifacts/suite0/suiterun23/testrun42/testStepWithArtifacts.yaml";

    private void mockedLogging() {
        logAppender = mock(Appender.class);
        logCaptor = ArgumentCaptor.forClass(LogEvent.class);
        when(logAppender.getName()).thenReturn("MockAppender");
        when(logAppender.isStarted()).thenReturn(true);

        Logger log4jRootLogger = (Logger) LogManager.getRootLogger();
        log4jRootLogger.addAppender(logAppender);
        Configurator.setAllLevels(log4jRootLogger.getName(), WARN);
    }

    @After
    public void removeMockedLogAppender() {
        Logger log4jRootLogger = (Logger) LogManager.getRootLogger();
        if (logAppender != null && logAppender.getName() != null) {
            log4jRootLogger.removeAppender(logAppender);
        }
    }

    @After
    public void removeGeneratedFiles() throws IOException {
        Path targetDir = Paths.get(".testexecution");
        if (Files.exists(targetDir)) {
            Files.walk(targetDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    public void getInstanceShouldReturnInitializedRegistry() {
        // given + when
        TestArtifactRegistry registry = new TestArtifactRegistry(new FileSystemAccess() {}, defaultEnvironment);

        // then
        assertThat(registry, notNullValue());
    }

    @Test
    public void brokenFileSystemDisablesRegistryButDoesNotThrowAnException() {
        // given
        mockedLogging();

        // when
        new TestArtifactRegistry(brokenFileSystem, defaultEnvironment);

        // then
        verify(logAppender).append(logCaptor.capture());
        
        assertThat(logCaptor.getValue().getMessage().getFormattedMessage(),
                is("Test artifact registry is disabled: Failed to create directory "
                 + "'.testexecution/artifacts/suite0/suiterun23/testrun42'."));
        assertThat(logCaptor.getValue().getLevel(), is(ERROR));
    }


    @Test
    public void registryWithBrokenFileSystemRejectsRegistrationsButDoesNotThrowAnException() {
        // given
        mockedLogging();
        TestArtifactRegistry registry = new TestArtifactRegistry(brokenFileSystem, defaultEnvironment);

        // when
        registry.register(screenshotArtifact, "0");

        // then
        verify(logAppender, Mockito.times(2)).append(logCaptor.capture());

        assertThat(logCaptor.getValue().getMessage().getFormattedMessage(), is(
                "The test artifact registry is disabled (error message should have been logged earlier)."
                + " Test artifacts are not recorded."));
        assertThat(logCaptor.getValue().getLevel(), is(WARN));
    }

    @Test
    public void missingAllEnvironmentVariablesCauseException() {
        // given + when
        try {
            new TestArtifactRegistry(new FileSystemAccess() {}, emptyEnvironment);

            fail("Expected exception, but none was thrown");
            // then
        } catch (IllegalStateException ex) {
            assertThat(ex.getMessage(), is(equalTo("One or more of the following, "
                    + "mandatory environment variables were not set: "
                    + "'TE_SUITEID', 'TE_SUITERUNID', 'TE_TESTRUNID'.")));
        }
    }

    @Test
    public void ioProblemDuringWriteCancelsRegistrationButDoesNotThrowAnException() {
        // given
        mockedLogging();
        TestArtifactRegistry registryUnderTest = new TestArtifactRegistry(flakyFileSystem(), defaultEnvironment);

        // when
        registryUnderTest.register(screenshotArtifact, "0");

        // then
        verify(logAppender).append(logCaptor.capture());

        assertThat(logCaptor.getValue().getMessage().getFormattedMessage(), is(
                "Failed to record information about a test artifact "
                + "(type: 'screenshot', path: 'screenshots/sampleScreenshot.png') for test step '0'."));
        assertThat(logCaptor.getValue().getLevel(), is(ERROR));
    }

    @Test
    public void registryWritesNewFileOnFirstRequest() throws IOException {
        // given
        Path expectedOutputFile = Paths.get(ARTIFACT_FILE_PATH);
        TestArtifactRegistry registryUnderTest = new TestArtifactRegistry(
                new FileSystemAccess() {}, defaultEnvironment);
        assertTrue(Files.notExists(expectedOutputFile));

        // when
        registryUnderTest.register(screenshotArtifact, "testStepWithArtifacts");

        // then
        assertTrue(Files.exists(expectedOutputFile));

        List<String> fileLines = Files.lines(expectedOutputFile, UTF_8).collect(toList());
        assertThat(fileLines.size(), is(equalTo(1)));
        assertThat(fileLines.get(0), is(equalTo("\"screenshot\": \"screenshots/sampleScreenshot.png\"")));
    }

    @Test
    public void registryAppendsToExistingFileOnSubsequentRequests() throws IOException {
        // given
        Path expectedOutputFile = Paths.get(ARTIFACT_FILE_PATH);
        TestArtifactRegistry registryUnderTest = new TestArtifactRegistry(
                new FileSystemAccess() {}, defaultEnvironment);

        // when
        registryUnderTest.register(screenshotArtifact, "testStepWithArtifacts");
        registryUnderTest.register(screencastArtifact, "testStepWithArtifacts");

        // then
        assertTrue(Files.exists(expectedOutputFile));

        List<String> fileLines = Files.lines(expectedOutputFile, UTF_8).collect(toList());
        assertThat(fileLines.size(), is(equalTo(2)));
        assertThat(fileLines.get(0), is(equalTo("\"screenshot\": \"screenshots/sampleScreenshot.png\"")));
        assertThat(fileLines.get(1), is(equalTo("\"screencast\": \"videos/sampleScreencast.mov\"")));
    }

    @Test
    public void throwsExceptionWhenArgumentsAreNull() throws IOException {
        // given
        TestArtifactRegistry registryUnderTest = new TestArtifactRegistry(new FileSystemAccess() {
        }, defaultEnvironment);

        // when
        try {
            registryUnderTest.register(null, null);
            fail("Expected exception but none was thrown");

            // then
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage(), is(equalTo("Arguments must not be null!")));
        }
    }

    @Test
    public void throwsExceptionWhenTestStepIdIsNull() throws IOException {
        // given
        TestArtifactRegistry registryUnderTest = new TestArtifactRegistry(new FileSystemAccess() {
        }, defaultEnvironment);

        // when
        try {
            registryUnderTest.register(screenshotArtifact, null);
            fail("Expected exception but none was thrown");

            // then
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage(), is(equalTo("Arguments must not be null!")));
        }
    }
}

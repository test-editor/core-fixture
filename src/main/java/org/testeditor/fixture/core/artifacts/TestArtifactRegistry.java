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
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A central place with which artifacts created during test execution can be
 * registered. When an artifact is registered, the information is always
 * persisted immediately, to minimize the risk of errors during test execution
 * leading to data loss. For the same reasons, an individual file is created and
 * written to for every test step. This way, each file holds a flat series of
 * key-value pairs (type of artifact mapped to its path). The generated files
 * are technically YAML-compliant.
 */
public class TestArtifactRegistry {
    private static TestArtifactRegistry singleton;
    private static final Logger logger = LoggerFactory.getLogger(TestArtifactRegistry.class);

    private final Path outDir;
    private final FileSystemAccess fsAccess;

    public static final String BASE_DIR = ".testexecution/artifacts";

    /**
     * Simple wrapper around java.nio.file.Files.createDirectories and
     * java.nio.file.Paths.get, as well as java.nio.file.Files.newBufferedWriter.
     * This is here for testing purposes.
     */
    public interface FileSystemAccess {
        default Path createDirectories(String basePath, String... pathElements) throws IOException {
            return Files.createDirectories(Paths.get(basePath, pathElements));
        }

        default Writer getBufferedWriter(Path target) throws IOException {
            return Files.newBufferedWriter(target, UTF_8, CREATE, WRITE, APPEND);
        }
    }

    /**
     * 
     * Simple wrapper around java.lang.System.getenv. This is here for testing
     * purposes.
     *
     */
    public interface EnvironmentAccess {
        default String get(String key) {
            return System.getenv(key);
        }
    }
    
    /**
     * Provides a singleton instance of the registry, lazily initializing it on demand.
     * @return the registry instance
     */
    public static synchronized TestArtifactRegistry getInstance() {
        if (singleton == null) {
            singleton = new TestArtifactRegistry();
        }
        return singleton;
    }

    TestArtifactRegistry() {
        this(new FileSystemAccess() {
        }, new EnvironmentAccess() {
        });
    }

    TestArtifactRegistry(FileSystemAccess fsAccess, EnvironmentAccess envAccess) {
        this.fsAccess = fsAccess;
        Path tmpOutDir;
        String suiteId = envAccess.get("TE_SUITEID");
        String suiteRunId = envAccess.get("TE_SUITERUNID");
        String testRunId = envAccess.get("TE_TESTRUNID");

        if (suiteId == null || suiteRunId == null || testRunId == null) {
            throw new IllegalStateException("One or more of the following, "
                    + "mandatory environment variables were not set: "
                    + "'TE_SUITEID', 'TE_SUITERUNID', 'TE_TESTRUNID'.");
        }

        try {

            tmpOutDir = fsAccess.createDirectories(BASE_DIR, suiteId, suiteRunId, testRunId);
        } catch (IOException e) {
            tmpOutDir = null;
            logger.error("Test artifact registry is disabled: Failed to create directory '{}/{}/{}/{}'.",
                    BASE_DIR, suiteId, suiteRunId, testRunId, e);
        }
        outDir = tmpOutDir;
    }

    /**
     * Registers an artifact created during a test step.
     * @param artifact the test artifact to be registered, specifying its type 
     * and the path where it is stored in the file system.
     * @param testStepId the ID of the test step to which this artifact is to
     * be associated with.
     */
    public void register(TestArtifact artifact, String testStepId) {
        if (outDir != null) {
            if (artifact != null && testStepId != null) {
                try (Writer writer = fsAccess.getBufferedWriter(outDir.resolve(testStepId + ".yaml"))) {

                    writer.append("\"").append(artifact.getType()).append("\": \"")
                          .append(artifact.getPath()).append("\"\n").flush();

                } catch (IOException e) {
                    // this is severe, but may be a temporary, external IO problem. Don't crash the
                    // test execution in this case.
                    logger.error("Failed to record information about a test artifact (type: '{}', path: '{}') "
                            + "for test step '{}'.", artifact.getType(), artifact.getPath(), testStepId, e);
                }
            } else {
                // this, on the other hand, indicates a programming error on the caller's side.
                throw new IllegalArgumentException("Arguments must not be null!");
            }
        } else {
            logger.warn("The test artifact registry is disabled (error message should have been logged earlier). "
                    + "Test artifacts are not recorded.");
        }
    }

}

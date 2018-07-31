core-fixture
============

[![License](http://img.shields.io/badge/license-EPL-blue.svg?style=flat)](https://www.eclipse.org/legal/epl-v10.html)
[![Build Status](https://travis-ci.org/test-editor/core-fixture.svg?branch=master)](https://travis-ci.org/test-editor/core-fixture)
[![Download](https://api.bintray.com/packages/test-editor/Fixtures/core-fixture/images/download.svg)](https://bintray.com/test-editor/Fixtures/core-fixture/_latestVersion)

A core fixture will be used by a concrete test driver like e.g. web-fixture.

## Development

### Build

    git submodule update --init --recursive
    ./gradlew build

### Release process

In order to create a release switch to the `master` branch and execute

    ./gradlew release

and enter the new version. After the commit and tag is pushed Travis will automatically build and deploy the tagged version to Bintray.

### Test execution

#### Environment variables

The following environment variables are expected to be passed via test execution such that the AbtractTestCase can successfully write the executed call tree and/or register files associated with the test run (e.g. screenshots):

```
TE_CALL_TREE_YAML_FILE: String with the filename for the call tree yaml file that is to be written during test execution
TE_SUITEID: String with the unique test suite id (globally unique)
TE_SUITERUNID : String with the unique test suite run id (unique within the test suite)
TE_TESTRUNID: String with the unique id within the test suite run
TE_TESTRUNCOMMITID : String with the commit id that is associated with the given test run
TE_TESTCASENAME : String with the name of the test to execute (just informational)
```

Test artifact registration works only if TE_SUITEID, TE_SUITERUNID and TE_TESTRUNID are passed.
Call tree yaml file generation works only if TE_CALL_TREE_YAML_FILE, TE_TESTRUNID are passed, TE_TESTRUNCOMMITID and TE_TESTCASENAME are recommended but optional.

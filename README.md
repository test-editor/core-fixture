core-fixture
============

[![License](http://img.shields.io/badge/license-EPL-blue.svg?style=flat)](https://www.eclipse.org/legal/epl-v10.html)
[![Build Status](https://travis-ci.org/test-editor/core-fixture.svg?branch=develop)](https://travis-ci.org/test-editor/core-fixture)
[![Download](https://api.bintray.com/packages/test-editor/Fixtures/core-fixture/images/download.svg)](https://bintray.com/test-editor/Fixtures/core-fixture/_latestVersion)

A core fixture will be used by a concrete test driver like e.g. web-fixture.

## Development

### Build

    ./gradlew build

### Release process

In order to create a release switch to the `master` branch and execute

    ./gradlew release

and enter the new version. After the commit and tag is pushed Travis will automatically build and deploy the tagged version to Bintray.
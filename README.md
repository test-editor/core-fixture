core-fixture
============

[![License](http://img.shields.io/badge/license-EPL-blue.svg?style=flat)](https://www.eclipse.org/legal/epl-v10.html)
[![Build Status](https://travis-ci.org/test-editor/core-fixture.svg?branch=master)](https://travis-ci.org/test-editor/core-fixture)
[![Download](https://api.bintray.com/packages/test-editor/Fixtures/core-fixture/images/download.svg)](https://bintray.com/test-editor/Fixtures/core-fixture/_latestVersion)

A core fixture will be used by a concrete test driver like e.g. web-fixture.

## Development

### Build

    ./gradlew build

### Release process

In order to create a release switch to the `master` branch and execute

    ./gradlew release

and enter the new version. After the commit and tag is pushed Travis will automatically build and deploy the tagged version to Bintray.


## Usage (for fixture authors)

### Example of using the `reporter`

The generated unit tests using the core-fixture is automatically of the form (or similar to):

``` java
public class GreetingTest extends AbstractTestCase {
  private SomeFixture someFixture = new SomeFixture();
  
  @Test
  public void execute() throws Exception {
    someFixture.initWithReporter(reporter); // SomeFixture must implement TestRunReportable, else this line is missing!
    // ...
  }
}
```

If `SomeFixture` implements the interface `TestRunReportable`, it will automatically be initialized with the `reporter` for this test run. The `reporter` being of type `TestRunReporter` provides a method `buildMaskingLogger` that returns an `org.slf4j.Logger` masking all messages before delegating it to the logger that is passed to `buildMaskingLogger`.

Additional masks can be registered (via `reporter.registerMaskPattern`) and unregistered again (via `reporter.unregisterMaskPattern`).



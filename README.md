  [![Build Status](https://travis-ci.com/ballerina-platform/ballerina-lang.svg?branch=master)](https://travis-ci.com/ballerina-platform/ballerina-lang)
  [![GitHub (pre-)release](https://img.shields.io/github/release/ballerina-platform/ballerina-lang/all.svg)](https://github.com/ballerina-platform/ballerina-lang/releases)
  [![GitHub (Pre-)Release Date](https://img.shields.io/github/release-date-pre/ballerina-platform/ballerina-lang.svg)](https://github.com/ballerina-platform/ballerina-lang/releases)
  [![GitHub last commit](https://img.shields.io/github/last-commit/ballerina-platform/ballerina-lang.svg)](https://github.com/ballerina-platform/ballerina-lang/commits/master)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# The Ballerina Programming Language

Ballerina is an open source programming language and platform for cloud-era application programmers to easily write software that just works.

#### Providing and consuming services
It has inherently concurrent first-class language constructs for providing and consuming services.

#### Sequence diagrams
Sequence diagram based graphical view shows the most fundamental aspect of the semantics of a network distributed application.

#### Structural typing
It allows for looser coupling between distributed components and eliminates the friction of data binding.

#### Metadata
Extensible metadata enables easy integration of Ballerina programs with cloud platforms. 

## Getting started

You can use one of the following options to try out Ballerina.

* [Getting Started](https://ballerina.io/learn/getting-started/)
* [Quick Tour](https://ballerina.io/learn/quick-tour/)
* [Ballerina by Example](https://ballerina.io/learn/by-example/) 

## Download and install

### Download the binary

You can download the Ballerina distribution at https://ballerina.io.

### Install from source

Alternatively, you can install Ballerina from the source using the following instructions.

#### Prerequisites

* [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or [OpenJDK 8](http://openjdk.java.net/install/)
* [Node (v8.9.x) + npm (v5.6.0 or later)](https://nodejs.org/en/download/)
* [Docker](https://www.docker.com/get-docker)

#### Building the source

1. Clone this repository using the following command.

    ```bash
    git clone --recursive https://github.com/ballerina-platform/ballerina-lang
    ```
    
2. Build project with Gradle. Use following command on Unix/macOS:
    ```bash
    ./gradlew build
    ```  
    or the following command on Windows:
    ```bash
    gradlew build
    ```

3. Extract the Ballerina distribution created at `distribution/zip/jballerina-tools/build/extracted-distributions/ballerina-<version>-SNAPSHOT.zip`.

## Reporting issues and security flaws

Report an issue in the relevant repo out of the GitHub repos listed below. 

>**Tip:** If you are unsure whether you have found a bug, search existing issues in the corresponding repo on GitHub and raise it in the [Ballerina-Dev Google Group](#https://groups.google.com/forum/#!forum/ballerina-dev).
  - Compiler, runtime, standard library, or tooling: <a href="https://github.com/ballerina-platform/ballerina-lang/issues">ballerina-lang</a> repo
  - Language specification: <a href="https://github.com/ballerina-platform/ballerina-spec/issues">ballerina-spec</a> repo
  - Website: <a href="https://github.com/ballerina-platform/ballerina-dev-website/issues">ballerina-dev-website</a> repo
  - Security flaw: send an email to security@ballerina.io. For details, see the <a href="https://ballerina.io/security/">security policy</a>.


## Contributing to Ballerina

As an open source project, Ballerina welcomes contributions from the community. To start contributing, read these [contribution guidelines](https://ballerina.io/contribution-guide/) for information on how you should go about contributing to our project.

Check the issue tracker for open issues that interest you. We look forward to receiving your contributions.

## License

Ballerina code is distributed under [Apache license 2.0](https://github.com/ballerina-platform/ballerina-lang/blob/master/LICENSE).

## Useful links

* The ballerina-dev@googlegroups.com mailing list is for discussing code changes to the Ballerina project.
* Chat live with us on our [Slack channel](https://ballerina.io/community/slack/).
* Technical questions should be posted on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
* Ballerina performance test results are available [here](performance/benchmarks/summary.md).

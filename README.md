&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=ballerina-lang/ballerina)](https://wso2.org/jenkins/view/All%20Builds/job/ballerina-lang/job/ballerina/)
[![GitHub (pre-)release](https://img.shields.io/github/release/ballerina-lang/ballerina/all.svg)](https://github.com/ballerina-platform/ballerina-lang/releases)
[![GitHub (Pre-)Release Date](https://img.shields.io/github/release-date-pre/ballerina-lang/ballerina.svg)](https://github.com/ballerina-platform/ballerina-lang/releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/ballerina-platform/ballerina-lang.svg)](https://github.com/ballerina-platform/ballerina-lang/commits/master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Ballerina allows you to code with a statically-typed, interaction-centric programming language where microservices, APIs, and streams are first-class constructs. You can use your preferred IDE and CI/CD tools. Discover, consume, and share packages that integrate endpoints with Ballerina Central. Build binaries, containers, and Kubernetes artifacts and deploy as chaos-ready services on cloud and serverless infrastructure. Integrate distributed endpoints with simple syntax for resiliency, circuit breakers, transactions, and events.

## Table of contents

- [Getting started](#getting-started)
- [Download and install](#download-and-install)
- [Contributing to Ballerina](#contributing-to-ballerina)
- [License](#license)
- [Useful links](#useful-links)

## Getting started

You can use one of the following options to try out Ballerina.

* Getting Started 
* [Quick Tour](https://github.com/ballerina-platform/ballerina-lang/blob/master/docs/quick-tour.md)
* [Ballerina by Example](https://ballerina.io/) 
* [Ballerina by Guide](https://ballerina.io/)
* [Language Specification](https://github.com/ballerina-platform/ballerina-specs/tree/master/language)

## Download and install

### Download the binary

You can download the Ballerina distribution at http://ballerina.io.

### Install from source

Alternatively, you can install Ballerina from the source using the following instructions.

#### Prerequisites

* [Maven](https://maven.apache.org/download.cgi)
* [Node (v8.9.x or latest LTS release) + npm (v5.6.0 or later)](https://nodejs.org/en/download/)
* [Docker](https://www.docker.com/get-docker)

#### Building the source

1. Clone this repository using the following command.

    ```bash
    git clone --recursive https://github.com/ballerina-platform/ballerina-lang
    ```

    If you download the sources, you need to update the git submodules using the following command.
    
    ```bash
    git submodule update --init 
    ```
2. Run the Maven command ``mvn clean install`` from the repository root directory.
3. Extract the Ballerina distribution created at `distribution/zip/ballerina/target/ballerina-<version>-SNAPSHOT.zip`.

## Contributing to Ballerina

As an open source project, Ballerina welcomes contributions from the community. To start contributing, read these [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md) for information on how you should go about contributing to our project.

Check the issue tracker for open issues that interest you. We look forward to receiving your contributions.

## License

Ballerina code is distributed under [Apache license 2.0](https://github.com/ballerina-platform/ballerina-lang/blob/master/LICENSE).

## Useful links

* The ballerina-dev@googlegroups.com mailing list is for discussing code changes to the Ballerina project.
* Chat live with us on our [Slack channel](https://ballerina-platform.slack.com/).
* User support should be posted on Stack Overflow with the #ballerina tag.

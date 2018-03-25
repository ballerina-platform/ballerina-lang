![Ballerina](https://ballerina.io/images/ballerina-logo.svg)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=ballerina-lang/ballerina)](https://wso2.org/jenkins/view/All%20Builds/job/ballerina-lang/job/ballerina/)
[![GitHub (pre-)release](https://img.shields.io/github/release/ballerina-lang/ballerina/all.svg)](https://github.com/ballerina-lang/ballerina/releases)
[![GitHub (Pre-)Release Date](https://img.shields.io/github/release-date-pre/ballerina-lang/ballerina.svg)](https://github.com/ballerina-lang/ballerina/releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/ballerina-lang/ballerina.svg)](https://github.com/ballerina-lang/ballerina/commits/master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


# Hello, I'm Ballerina

In the forseeable future, we will see a continous increase in the number of applications written in order to meet the growing demand. All these apps will increasingly depend on programmable endpoints and eventually everything we integrate with becomes an endpoint. Integration is the discipline of resilient communication between endpoints. The challenge with integration is to not only solve hard integration problems but to also do so in an agile manner.

Ballerina is a simple programming language whose syntax and runtime addresses the hard problems of integration in an agile way. As a Turing complete language, Ballerina enables agility with edit, build, and run cycles. Ballerina code is compiled into services that include transactions, embedded brokers, and gateway runtimes.

Ballerina is created for and dedicated to developers who want to solve integration problems. Ballerina is inspired by Java, Go, and other languages, and leverages years of experience in the integration domain.

## Table of contents

- [Download and install](#download-and-install)
- [Getting started](#getting-started)
- [Contributing to Ballerina](#contributing-to-ballerina)
- [License](#license)
- [Useful links](#useful-links)

## Download and install

### Download the binary

You can download the Ballerina distribution at http://ballerinalang.org.

[(Back to top)](#table-of-contents)

### Install from source

#### Prerequisites

* [Maven](https://maven.apache.org/download.cgi)
* [Node (v8.9.x or latest LTS release) + npm (v5.6.0 or later)](https://nodejs.org/en/download/)
* [Docker](https://www.docker.com/get-docker)

#### Building the source

1. Clone this repository using the following command.

    ```bash
    git clone --recursive https://github.com/ballerinalang/ballerina
    ```

    If you download the sources, you need to update the git submodules using the following command.
    
    ```bash
    git submodule update --init 
    ```
2. Run the Maven command ``mvn clean install`` from the ``ballerina`` root directory.
3. Extract the Ballerina distribution created at `distribution/zip/ballerina/target/ballerina-<version>-SNAPSHOT.zip`.

[(Back to top)](#table-of-contents)

## Getting started

* The [Quick Tour](https://github.com/ballerina-lang/ballerina/blob/master/docs/quick-tour.md) gives you instructions on how to start Ballerina, run a simple code, and get a response within seconds. It also introduces the means to quickly get started and take Ballerina for its first spin.
* The [Ballerina website](http://ballerinalang.org) has a few selected samples that you can try out right there in the page. 
* [Ballerina by Example](https://ballerinalang.org/docs/by-example/) can be used to understand Ballerina examples as you try them out.  
* [Ballerina by Guide](https://github.com/ballerina-guides) is a series of long form examples that showcase a complete lifecycle of development with Ballerina including setting up the environment, using tools, build system, authoring code, running tests, and deploying within an architecture environment.

[(Back to top)](#table-of-contents)

## Contributing to Ballerina

As an open source project, Ballerina welcomes contributions from the community. To start contributing, read these [contribution guidelines](https://github.com/ballerina-lang/ballerina/blob/master/CONTRIBUTING.md) for information on how you should go about contributing to our project.

Check the issue tracker for open issues that interest you. We look forward to receiving your contributions.

[(Back to top)](#table-of-contents)

## License

Ballerina code is distributed under [Apache license 2.0](https://github.com/ballerina-lang/ballerina/blob/master/LICENSE).

[(Back to top)](#table-of-contents)

## Useful links

* The ballerina-dev@googlegroups.com mailing list is for discussing code changes to the Ballerina project.
* Reach out to us on our [Slack channel](https://ballerina-platform.slack.com/) and integrate with our community.

[(Back to top)](#table-of-contents)

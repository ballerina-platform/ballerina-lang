[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=ballerinalang/ballerina)](https://wso2.org/jenkins/job/ballerinalang/job/ballerina/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


# Hello, I'm Ballerina.

Ballerina is a general purpose, concurrent, and strongly typed programming language with both textual and graphical syntaxes. It is designed to make it easier to write programs that integrate with data sources, services, and network-connected APIs of all kinds. It is optimized primarily for such programs - while it can be use to program anything, it is not recommended to use Ballerina if a significant portion of the program is not related to integrating with data sources, services, or network-connected APIs.

Ballerina has been inspired by Java, Go, and other languages, but it has a concurrency model built around a sequence diagram metaphor.

## Getting started

You can download the Ballerina distribution, try samples, and read the documentation at http://ballerinalang.org.

## Building from the source

If you want to build Ballerina from the source code:

1. Get a clone or download the source from this repository (https://github.com/ballerinalang/ballerina).
1. Run the Maven command ``mvn clean install`` from the ``ballerina`` root directory.
1. Get a clone or download the source from tools-distribution repository (https://github.com/ballerinalang/tools-distribution).
1. Run the Maven command ``mvn clean install`` from ``tools-distribution`` root directory.
1. Extract the Ballerina distribution created at `tools-distribution/modules/ballerina/target/ballerina-<version>-SNAPSHOT.zip` to your local directory.

## Running samples

The website http://ballerinalang.org has several samples you can try out right there in the page. These samples are also available in the `<ballerina_home>/samples` directory in your Ballerina distribution. Or go to the [ballerina-by-example](https://ballerinalang.org/docs/by-example/) website to view the Ballerina examples.  

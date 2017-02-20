# Hello, I'm Ballerina.

Ballerina is a new programming language for integration built on a sequence diagram metaphor. Ballerina is:
- Simple
- Intuitive
- Visual
- Powerful
- Lightweight
- Cloud Native
- Container Native
- Fun

The conceptual model of Ballerina is that of a sequence diagram. Each participant in the integration gets its own lifeline and Ballerina defines a complete syntax and semantics for how the sequence diagram works and execute the desired integration.

Ballerina is not designed to be a general purpose language. Instead you should use Ballerina if you need to integrate a collection of network connected systems such as HTTP endpoints, Web APIs, JMS services, and databases. The result of the integration can either be just that - the integration that runs once or repeatedly on a schedule, or a reusable HTTP service that others can run.

## Getting started

You can download the Ballerina distribution, try samples, and read the documentation at http://ballerinalang.org.

## Building from the source

If you want to build Ballerina from the source code:

1. Get a clone or download the source from this repository (https://github.com/ballerinalang/ballerina).
1. Run the Maven command ``mvn clean install`` from the root directory.
1. Extract the Ballerina distribution created at `ballerina/modules/distribution/target/ballerina-0.8.0-SNAPSHOT.zip` to your local directory.

## Running samples

The website http://ballerinalang.org has several samples you can try out right there in the page. Or go to the [Samples](samples/getting_started) in Github to view all the Ballerina samples. These samples are also available in the `<ballerina_home>/samples` directory in your Ballerina distribution. 

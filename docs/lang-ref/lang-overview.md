# Language Overview

This page provides an overview of the Ballerina language and the main points you need to know about creating a Ballerina program. Be sure to read [Key Concepts](../key-concepts.md) to familiarize yourself with Ballerina entities such as services.

## Structure of a Ballerina program

A Ballerina file is structured as follows::

```
[package PackageName;]
[import PackageName [version ImportVersionNumber] [as Identifier];]*

(ServiceDefinition |
 FunctionDefinition |
 ConnectorDefinition |
 TypeDefinition |
 TypeMapperDefinition |
 ConstantDefinition)+
```

Note: Terminals of the language (keywords) are lowercase, whereas non-terminals are uppercase.

Each of the Ballerina elements such as services and connectors are described in detail in their own pages in this guide.

A Ballerina program can consist of a number of Ballerina files, which may be in one or more packages. Ballerina uses a modular approach for managing names and organizing code into files. In summary, Ballerina entities (functions, services, etc.) all have globally unique qualified names consisting of their package name and the entity name. For complete information, see [Packaging](packaging.md).

## Running a Ballerina program

The following commands are used to run a Ballerina program/service in its packaged or unpackaged format:

To execute `main()` from a `.bal` file or a package or archive file:

```
ballerina run main (filename | packagename | mainarchive)
```

To run named services:

```
ballerina run service (filename | packagename | servicearchive)+ 
```

To run a collection of service archives from service root:

```
ballerina run service [-sr serviceroot]
```

If a Docker image is built at build time, the execution of that image is done using normal Docker commands. For details, see Ballerina Docker Architecture.

TODO: add link to that doc once it's checked in.

## Dynamic configuration

Several Ballerina constructs such as connectors and resources have configurable parameters. Examples include the URI of an HTTP endpoint and timeout values. These values MAY be set explicitly within the program using annotations, but such values can be overridden from outside the program by applying appropriate property values. These values may be set via environment variables or other deployment management approaches.

TODO: provide more info when it becomes available

## Disabling a construct

In traditional programming languages, developers use commenting as a technique to disable a block of code from executing. In Ballerina, we do not allow comments arbitrarily - we only allow comments as statements.

Ballerina instead allows the developer (either visually or textually) to mark any statement or function, action, connector, resource, or service to be disabled by prefixing it with the `!` character. Disabling a construct does not prevent the language parser, type checker, and other validations; it simply stops that construct from being executed at runtime.

## Expressions
Similar to languages such as Java, Go, etc, Ballerina supports the following expressions: 

* Mathematical expressions `(x + y, x/y, etc.)`
* Function calls `(foo(a,b))`
* Action calls `(tweet(twitterActor, "hello"))`
* Complex expressions `(foo(a,bar(c,d)))`

## Testing your code

TODO

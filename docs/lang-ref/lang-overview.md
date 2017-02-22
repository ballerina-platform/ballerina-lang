# Overview

This page provides an overview of the Ballerina language and the main points you need to know about creating a Ballerina program. Be sure to read [Key Concepts](../key-concepts.md) to familiarize yourself with Ballerina entities such as services.

## Structure of a Ballerina program

A Ballerina file is structured as follows:

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

Each of the Ballerina entities such as services and connectors are described in detail in their own pages in this guide.

A Ballerina program can consist of a number of Ballerina files, which may be in one or more packages. Ballerina uses a modular approach for managing names and organizing code into files. In summary, Ballerina entities (functions, services, etc.) all have globally unique qualified names consisting of their package name and the entity name. For complete information, see [Packaging and Running Programs](packaging.md).

## Development tools

Ballerina provides several tools to help you create, document, and test your code. For more information, see [Tools](../tools.md).

## Expressions
Similar to languages such as Java, Go, etc, Ballerina supports the following expressions: 

* Mathematical expressions `(x + y, x/y, etc.)`
* Function calls `(foo(a,b))`
* Action calls `(tweet(twitterActor, "hello"))`
* Complex expressions `(foo(a,bar(c,d)))`

## Reserved names

When naming your Ballerina elements (services, resources, actions, etc.), Swagger files, program files, and packages, do not use the following terms for the name, as these terms are reserved in Ballerina:

- action
- all
- any
- as
- boolean
- break
- catch
- connector
- const
- datatable
- double
- else
- exception
- false
- fork
- function
- if
- import
- int 
- json
- map
- message
- native
- null
- package
- reply
- resource
- return
- service
- string
- struct
- throws
- timeout
- true
- try
- typemapper
- while
- worker
- xml
- xmldocument


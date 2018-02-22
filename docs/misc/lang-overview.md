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

## Names
When naming your elements (services, resources, actions, functions, etc.), Ballerina's support for identifier literals allows you to use quotes, foreign characters, and more in your names. If the name has space, be sure to surround the name with vertical bars (|) when declaring and invoking the element. For example:

```
int |this is a variable| = 5;
function |this is a sample function| (){}
```

The following example shows a simple program using Sinhalese characters. Notice there are vertical bars around both the invocation and declaration of the Sinhalese function and string, whereas the `main` function does not need vertical bars because its name does not include spaces:

```
import ballerina.lang.system;

function main (string[] args) {
    system:println(|සිංහල වාක්යක් දෙන්න|());
}   

function |සිංහල වාක්යක් දෙන්න|() (string) {
    string |සිංහල වාක්යක්| = "සිංහල වාක්යක්";
    return |සිංහල වාක්යක්|;
}
```

Here's another example with names in Greek. Notice that there are no vertical bars around the names, because they don't have spaces:

```
import ballerina.lang.system;

function main (string[] args) {
    system:println(Λάβετετοόνομα());
}

function Λάβετετοόνομα() (string) {
    return "sample";
}
```
For more detailed information, see "IdentifierLiteral" in the [Ballerina grammar definition](https://github.com/ballerinalang/ballerina/blob/master/docs/grammar/Ballerina.g4).

### Reserved names
When naming Ballerina elements, Swagger files, program files, and packages, do **not** use the following terms for the name, as these terms are reserved in Ballerina:

- action
- all
- any
- as
- boolean
- break
- catch
- connector
- const
- table
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


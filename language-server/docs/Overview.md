# Overview
1. <a href="#WhatisBallerina">What is Ballerina</a>
2. <a href="#LanguageServer">Language Server</a>
3. <a href="#LanguageServerforBallerina">Language Server for Ballerina</a>
4. <a href="#ExistingClients">Existing Clients</a>

<a name="WhatisBallerina" />

## What is Ballerina
Ballerina allows you to code with a statically-typed, interaction-centric programming language where microservices, APIs, and streams are first-class constructs. You can use your preferred IDE and CI/CD tools. Discover, consume, and share packages that integrate endpoints with Ballerina Central. Build binaries, containers, and Kubernetes artifacts and deploy as chaos-ready services on cloud and serverless infrastructure. Integrate distributed endpoints with simple syntax for resiliency, circuit breakers, transactions, and events

Ballerina has been inspired by Java, Go, and other languages, but it has a concurrency model built around a sequence diagram metaphor
* [Ballerinala Source](https://github.com/ballerina-lang/ballerina)
* [Official Documentation](https://ballerinalang.org/docs/)

<a name="LanguageServer" />

## Language Server
When we consider the language tools such as editors, it's common that the users expect various smart functionalities such as auto-completion, go to definition, code references, etc. When we consider editors, all those supporting a certain language, have to implement the similar set of functionalities individually. In order to avoid this particular redundancy, Language Server Protocol was introduced. Language tools which support the protocol implementation can use the same smartness provider implementation for a particular language. This allows having a similar set of functionalities and feature consistency without redundant implementations.
* [Language Server Protocol](https://microsoft.github.io/language-server-protocol/)
* [Protocol Specification](https://microsoft.github.io/language-server-protocol/specification)

<a name="LanguageServerforBallerina" />

## Language Server for Ballerina
Ballerina Language Server follows the latest Language Server Protocol implementation. Any editor which supports the protocol implementation can use the Ballerina LS as the smartness provider for their editor. Following features are supported in the current Implementation
* Auto-Completion

    As a part of auto completion users are facilitated with the following capabilities
   1. Template/Snippets suggestion for basic constructs such as functions, services and general statement constructs (object, while, etc)
   2. Iterable Operations suggestion over the variables defined.
   3. Annotations auto completion
   4. Keywords Suggestion
   5. Variables, Keywords and Types (Builtin and User Defined) Suggestion
   6. Functions and Action Suggestion

![](https://github.com/ballerina-lang/ballerina/blob/master/language-server/docs/images/autocompletion.gif)

* Hover Provider

    Hover Provider supports hover definition over the following language constructs
   1. Object
   2. Functions
   3. Variables
   4. Records
   5. Actions
   6. Endpoints

![](https://github.com/ballerina-lang/ballerina/blob/master/language-server/docs/images/hoverProvider.gif)

* Signature Help

    Signature help will provide the user information about the function or action signature. User will be provided with the information such as parameters and parameter description.
Parameter description are extracted from the documentation added for a certain function or action. Ballerina Documentation supports Markdown docs as well.
Signature Help is supported over the following constructs.
    1. Functions
    2. Actions

![](https://github.com/ballerina-lang/ballerina/blob/master/language-server/docs/images/signatureHelp.gif)

* Goto Definition

    Goto definition supports the following 
    1. Object
    2. Functions
    3. Variables
    4. Records
    5. Actions
    6. Endpoints
    
All the definition references supported needs to be in packages on the disk except the builtin functions and actions which are not supported in the current implementation

![](https://github.com/ballerina-lang/ballerina/blob/master/language-server/docs/images/gotodef.gif)

* Find All References

    All the references of a selected symbol is prompted and the following types are supported with the current implementation.

    1. Object
    2. Functions
    3. Variables
    4. Records
    5. Actions
    6. Endpoints

* Diagnostics

    Semantic and Syntactic Diagnostics are prompted similarly done in the Ballerina Compiler

![](https://github.com/ballerina-lang/ballerina/blob/master/language-server/docs/images/diagnostics.gif)

* Refactoring

    Under the various refactoring options, following set of refactoring options are supported in the current implementation.

    1. Add imports
    2. Add documentation for top level nodes (Services, Resources, functions)

<a name="ExistingClients" />

## Existing Clients
Currently, we have two active clients (editors) who have extensions implemented based on the language server implementation for Ballerina.
* [VSCode Ballerina Extension](https://marketplace.visualstudio.com/search?term=ballerina&target=VSCode&category=All%20categories&sortBy=Relevance)
* [Ballerina Composer](https://github.com/ballerina-lang/ballerina/tree/master/composer)
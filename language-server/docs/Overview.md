# Overview
1. <a href="#WhatisBallerina">What is Ballerina</a>
2. <a href="#LanguageServer">Language Server</a>
3. <a href="#LanguageServerforBallerina">Language Server for Ballerina</a>
4. <a href="#ExistingClients">Existing Clients</a>

<a name="WhatisBallerina" />

## What is Ballerina
Ballerina is a general purpose, concurrent, and strongly typed programming language with both textual and graphical syntaxes. It is designed to make it easier to write programs that integrate with data sources, services, and network-connected APIs of all kinds. It is optimized primarily for such programs - while it can be used to program anything, it is not recommended to use Ballerina if a significant portion of the program is not related to integrating with data sources, services, or network-connected APIs.

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

![](https://github.com/ballerina-lang/ballerina/blob/master/language-server/docs/images/autocompletion.gif)

* Hover Provider

![](https://github.com/ballerina-lang/ballerina/blob/master/language-server/docs/images/hoverProvider.gif)

* Signature Help

![](https://github.com/ballerina-lang/ballerina/blob/master/language-server/docs/images/signatureHelp.gif)

* Goto Definition

![](https://github.com/ballerina-lang/ballerina/blob/master/language-server/docs/images/gotodef.gif)

* Diagnostics

![](https://github.com/ballerina-lang/ballerina/blob/master/language-server/docs/images/diagnostics.gif)

<a name="ExistingClients" />

## Existing Clients
Currently, we have two active clients (editors) who have extensions implemented based on the language server implementation for Ballerina.
* [VSCode Ballerina Extension](https://marketplace.visualstudio.com/search?term=ballerina&target=VSCode&category=All%20categories&sortBy=Relevance)
* [Ballerina Composer](https://github.com/ballerina-lang/ballerina/tree/master/composer)
# Introduction

Ballerina is a general purpose, concurrent and statically typed programming language. Ballerina has both a textual syntax as well as a graphical syntax. It is designed to make it easier to write programs that integrate data, services and network-connected APIs of all kinds. It is optimized primarily for such programs - while it can be use to program anything, it is not recommended to use Ballerina if a significant portion of the program is not related to integrating data, services and/or network-connected APIs. To support Web oriented network interactions natively, Ballerina supports both JSON and XML as built-in data types. To support data interactions natively, Ballerina has a representation for data tables natively as well.

Ballerina has been inspired by Java, Go and other languages but comes with a concurrency model built around the sequence diagram metaphor.

Ballerina understands two styles of execution: running as any normal programming language as a separate process but also running as service invoked via some network protocol. That is, services are an inherent concept in Ballerina instead of a style of writing programs.

Ballerina comes with a standard library of capabilities (network connectors, utility functions and annotations) that allow developers to be productive without
Ballerina provides a non-exclusive but deep marriage to HTTP and Swagger and does so using extension points of the language. As a network-aware language, it ships with bindings for standard network protocols including HTTP/1.1, HTTP/2, WebSockets and JMS.

The textual syntax of Ballerina follows the C/Java heritage while also adopting some stylistic conventions from Go. The graphical syntax of Ballerina follows a sequence diagram metaphor. A unique characteristic of Ballerina is that the two syntaxes both offer the exact same expressive capability and are fully reversible. Ballerina learns from Java and Go and offers a platform independent programming model which abstracts the programmer from machine specific details.

## Organization of the Specification
## Examples
## Notation

The syntax of Ballerina is defined informally in this document using a pseudo grammar. Future revisions of this document will include more formal grammar notations as well.

The following syntactic conventions are followed in the pseudo grammar used in this document:
- All terminals of the language (keywords) are lowercase words.
- All non-terminals are uppercase words.
- All language keywords are in `this font`.

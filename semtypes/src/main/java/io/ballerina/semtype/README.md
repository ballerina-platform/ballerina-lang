# Notes about semtype port

This this a Java port of semantic subtype implementation found in 
[https://github.com/ballerina-platform/nballerina/tree/main/compiler/modules/types](https://github.com/ballerina-platform/nballerina/tree/main/compiler/modules/types)

The aim to resemble the original Ballerina implementation as closely as possible, any bug found here should be checked 
against the original implementation and if applicable, should be fixed there too.

- Operations on sem types are separated into XXXXOps classes and the data into derivatives of `SubtypeData`.
This way we can have the logic within the functions reflect the Ballerina code.

- `bitCount` and `numberOfTrailingZeros` are available in Java `Integer` class, hence not implemented here.
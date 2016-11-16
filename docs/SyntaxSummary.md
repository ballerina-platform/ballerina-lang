# Introducing Ballerina: A New Programming Language for Integration

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

Ballerina is not designed to be a general purpose language. Instead you should use Ballerina if you need to integrate a collection of network connected systems such as HTTP endpoints, Web APIs, JMS services, and databases. The result of the integration can either be just that - the integration that runs once or repeatedly ona  schedule, or a reusable HTTP service that others can run.

This is an informal introduction to the Ballerina language.

## Structure of a Ballerina Program
Every Ballerina program has both a textual representation and a normative visual representation. A Ballerina program can be modularized into a collection of files, with each file contributing one or more resources, functions or types. To access these from another file they must be explicitly imported by the other file.

The structure of a file in Ballerina is as follow:

```
[package PackageName;]
[import (PackageWildCard|PackageName);]*

(VariableDeclaration | TypeDefinition)*

(ResourceDefinition | FunctionDefinition)+
```

Resources are externally invokable whereas functions are internal subroutines that can only be invoked form a resource or based on a schedule.

The overall structure of a resource is as follows:

```
@Annotation+
resource ResourceName (Message VariableName
        [, (@Context | @QueryParam | @PathParam) VariableName]*) {
    Statement;+
}*
```

The visual representation of this (without the annotations) is as follows:

![bal-resource-skeleton.png]()

A file may also contain functions who’s structure is as follows:

```
function FunctionName ((TypeName VariableName)*) (TypeName*)
        [throws ExceptionName [, ExceptionName]*] {
}
```

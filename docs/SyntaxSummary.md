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
    VariableDeclaration*
    Statement+
}*
```

The visual representation of this (without the annotations) is as follows:

![bal-resource-skeleton.png]()

A file may also contain functions who’s structure is as follows:

```
function FunctionName ((TypeName VariableName)*) (TypeName*)
        [throws ExceptionName [, ExceptionName]*] {
    VariableDeclaration*
    Statement+
}
```
All functions are public. Functions can be invoked from a resource or a function in the same file. It may also be invoked from another file by either importing it first or by using its fully qualified name.

A VariableDeclaration has the following structure:

```
var TypeName VariableName;
```
A TypeName is one of the following built in types or a user defined type name.
- int
- long
- float
- double
- string

There are also built in types to represent XML and JSON valued objects. XMLElement and JSON objects may be further typed by indicating an XML Schema or JSON Schema, respectively.
- XMLDocument
- XMLElement
- JSON

User defined types are defined using a TypeDefinition as follows:
```
type TypeName {
    TypeName VariableName;+
}
```

Array types can be defined by using the array constructor as follows:
- int[]
- long[]
- float[]
- double[]
- string[]
- TypeName[]

All arrays are unbounded in length and support 0 based indexing. Array length can be determined by checking the ".length" property of the array typed variable.

A Statement may be one of the following:
- if statement
- switch statement
- foreach statement
- fork/join statement
- invocation statement
- try/catch statement
- return statement
- reply statement

A foreach statement provides a way to iterate through a list in order. A foreach statement has the following structure:
```
foreach (VariableName : ValueList) {
  VariableDeclaration*
  Statement+
}
```
A ValueList may be an array or any object which supports iteration.

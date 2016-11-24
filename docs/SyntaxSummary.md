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

The conceptual model of Ballerina is that of a sequence diagram. Each participant in the integration (referred to as a `connector` in Ballerina) gets its own lifeline and Ballerina defines a complete syntax and semantics for how the sequence diagram works and executes the desired integration.

Ballerina is not designed to be a general purpose language. Instead you should use Ballerina if you need to integrate a collection of network connected systems such as HTTP endpoints, Web APIs, JMS services, and databases. The result of the integration can either be just that - the integration that runs once or repeatedly on a schedule, or a reusable HTTP service that others can run.

NOTE: The initial release of the language only supports integrations that result in HTTP services.

This is an informal introduction to the Ballerina language.

## Introduction

Every Ballerina program has both a textual representation and a canonical visual representation.

### Concepts

- *Service*: A `service` is an HTTP web service described by a Swagger. A service is the discrete unit of functionality that can be remotely accessed.
- *Resource*: A `resource` is a single request handler within a service. The resource concept is designed to be access protocol independent - but in the initial release of the language it is intendended to work with HTTP.
- *Connector*: A `Connector` represents a participant in the integration. Connectors can be declared at a service level or within a resource.
- *Action*: An `action` is an operation one can execute against a `connector`
- *Function*: A `function` is an operation that is executed by a worker.
- *Worker*: A `worker` is a thread of execution that the integration developer programs as a lifeline.

NEED PICTURE HERE.

### Modularity

Ballerina programs can be written in one or more files organized into packages. A package defines a namespace and all public symbols defined in any file in the same package are visible within the package. A package is represented by a directory.

A single Ballerina file can define either a single `service`, a single `connector` or a collection of functions. A file that contains a `service` or `connector` may also define any number of functions. Functions that are not marked `public` are private to the package.

The unit of execution for Ballerina programs is a `service`.

## Structure of a Ballerina Program

The structure of a `service` file in Ballerina is as follow:

```
[package PackageName;]
[import (PackageWildCard|PackageName);]*

service ServiceName;

TypeDefinition*

(VariableDeclaration | ConnectionDeclaration)*

(ResourceDefinition | FunctionDefinition)+
```

### Resource Definition

Resources are externally invokable whereas functions are internal subroutines that can only be invoked form a resource. Actions are subroutines that are associated with an actor.

The overall structure of a resource is as follows:

```
@Annotation+
resource ResourceName (Message VariableName
        [, (@Context | @QueryParam | @PathParam) VariableName]*) {
    ActorDelcaration*
    (TypeDefinition | VariableDeclaration)*
    Statement+
}*
```

The visual representation of this (without the annotations) is as follows:

![bal-resource-skeleton.png]()

### Function Definition

A file may also contain functions who’s structure is as follows:

```
[public] function FunctionName ((TypeName VariableName)*) (TypeName*)
        [throws ExceptionName [, ExceptionName]*] {
    ActorDelcaration*
    (TypeDefinition | VariableDeclaration)*
    Statement+
}
```

All functions are private to the package unless explicitly declared to be public with the `public` keyword.. Functions can be invoked from a resource or a function in the same package without an import. It may also be invoked from another package by either importing it first or by using its fully qualified name.

### Variable Declaration

A VariableDeclaration has the following structure:

```
var TypeName VariableName[(, VariableName)*];
```
variable delcaration can assign a value as following.

```
var TypeName VariableName = value;
```

Value can be a integer, a float, a string, a map, or XML or Json literals. Following are examples.

```
var int age = 4;
var double price = 4.0;
var string name = "John";
var xmlElement address_xml = `<address><name>$name</name></address>`;
var json address_json = `{ "name":"$name", "streetName":"$street"}`;
var map = {"name":"John", "age":34 };
```
Here $name is a variable that is available at the current scope.

A TypeName is one of the following built in types or a user defined type name.
- boolean
- int
- long
- float
- double
- string
- xmlElement[<{XSD_namespace_name}type_name>]
- xmlDocument
- json\[\<json_schema_name\>\]
- message
- map
- exception

### Type Definition

User defined types are defined using a TypeDefinition as follows:
```
type TypeName {
    TypeName VariableName;+
}
```

### Array Types

Array types can be defined by using the array constructor as follows:
- int[]
- long[]
- float[]
- double[]
- string[]
- TypeName[]

All arrays are unbounded in length and support 0 based indexing. Array length can be determined by checking the `.length` property of the array typed variable.

### Type Conversion

For XML and JSON types we can declare a schema as discussed in [Variable Declaration](#variable-declaration).
To convert between types we can define a typeconverter as follows:
```
typeconverter TypeConverterName (TypeName VariableName) (TypeName) {
    Statement;+
}
```

When the typeconverter has been defined, we can do the type conversion through an assignment between types.
```
TypeName<{schema_type1}> VariableName1 = ...;   
TypeName<{schema_type2}> VariableName2 = VariableName1;
```
//todo Do we need to convert from multiple input types to a single output type

### Statements

A Statement may be one of the following:
- assignment statement
- if statement
- switch statement
- foreach statement
- fork/join statement
- try/catch statement
- return statement
- reply statement

#### Assignment Statement

Assignment statements look like the following:
```
VariableName = Expression;
```

#### If Statement

Provides a way to perform conditional execution.
```
if (condition) {
  VariableDeclaration*
  Statement+
}
[else if (condition){
  VariableDeclaration*
  Statement+
}]*
  else {
  VariableDeclaration*
  Statement+
}
```

#### Switch Statement

Provides a way to perform conditional execution.
```
switch (predicate) {
        (case valueX:)+
	default:
}*
```

#### Foreach Statement

A `foreach` statement provides a way to iterate through a list in order. A `foreach` statement has the following structure:
```
foreach (VariableType VariableName : ValueList) {
  VariableDeclaration*
  Statement+
}
```
A ValueList may be an array or any object which supports iteration.

#### Fork/join Statement

TODO: Fix the following definition

```
fork (MessageName) {
  worker workerName (message variableName) {
    Statement;+
    return MessageName;
  }+       
} join JoinCondition (message[] data) {
  Statement;*
}
```
When the `JoinCondition` has been satisfied, the corresponding slots of the message array will be filled with the returned messages from the workers in the order the workers' lexical order. If the condition asks for up to some number of results to be available to satisfy the condition, it may be the case that more than that number are avaialble by the time the statements within the join condition are executed. If a particular worker has not yet completed, the corresponding message slot will be null.

#### Worker Statement

```
worker WorkerName(message variableName) {
  Statement;+
  return MessageName;
}
```

#### Wait Statement

```
MessageName = wait WorkerName;
```

#### Try/catch Statement

```
try {
  VariableDeclaration*
  Statement+
} catch (exception e) {
  VariableDeclaration*
  Statement+
}
```

#### Exception Handling

We provide an exception type which has a type(string), message(string) and a property map.
We don't allow extension type for exceptions and different exception type should be handled using the type attribute.
We provide  following statements
 - `try` `catch` blocks to handle exception.
 - `throws` statement to signal that a function may throw an exception.
 - `throw` statement to throw an exception.
If a function is throwing an exception it must declare it.
If a function throws an exception the caller function can choose to handle it or let it propagate upwards.

#### Throw Statement

```
throw ExceptionVariableName;
```

#### Return Statement

```
return (Expression)*;
```

#### Reply Statement

```
reply Message?;
```

## Connectors and Actions

Conncetors are the participants of an integration: they are the vertical lines in the sequence diagram.
A `connector` provides a set of actions that workers can execute against them. A given `connector` may
also need to be configured with particular details.

### Connector Definition

```
[package PackageName;?]

connector ConnectorName (MandatoryParameters[, OptionalParameters);

(TypeDefinition | VariableDeclaration | ConnectionDeclaration)*

ActionDefinition+;
```
Example:

connector MyConnector (string s, int x = 10);
connector FooC (string s | (string s, float y), int x = 10);

var boolean loggedIn = false;

### Action Definition

Actions are operations (functions) that can be executed against an actor. The overall structure of an action is as follows:

```
action ActionName (ConnectorName VariableName, ((TypeName VariableName)*) (TypeName*)
        [throws exception] {
    (TypeDefinition | VariableDeclaration | ActorDeclaration)*
    Statement+
}
```

First argument of an action should be associated with an actor.

All actions are public. Actions can be invoked from a resource or a function in the same package without an import. It may also be invoked from another file by either importing it first or by using its fully qualified name.

### Connection Declaration

Connections represent a connection established via a connector. These can be declared at a
global level or within the scope of a function or action.

```
actor<ActorType> VariableName;
```

## Configuration Management

Several Ballerina constructs such as actors and resources have configurable parameters. Examples include the URI of an HTTP endpoint and timeout values. These values MAY be set explicitly within the program using annotations but such values can be overridden from outside the program by applying appropriate property values. These values may be set via environment variables or other deployment management approaches.

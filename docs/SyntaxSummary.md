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

[ServiceAnnotations]
service ServiceName;

TypeDefinition*

ConnectionDeclaration;*

VariableDeclaration;*

(ResourceDefinition | FunctionDefinition | TypeConvertor)+
```
### Types & Variables

Ballerina has variables of various types. The type system includes built-in primitives, a
collection of built-in structured types and array and record type constructors. All variables
of primitive types are allocated on the stack while all non-primitive types are allocated
on a heap using `new`.

#### Declaring Variables

A VariableDeclaration has the following structure:

```
TypeName VariableName [= Value];
```

A TypeName may be one of the following built-in primitive types:
- boolean
- int
- long
- float
- double
Primitive types do not have to be dynamically allocated as they are always allocated
on the stack.

A TypeName may also be one of the following built-in non-primitive types:
- string
- message
- map
- exception

A TypeName may also be the name of a user defined type.

### Constructed Types

User defined record types are defined using a TypeDefinition as follows:
```
[public] type TypeName {
    TypeName VariableName;+
}
```
If a type is marked `public` then it may be instantiated from another package.

Arrays may be defined using the array constructor `[]` as follows:
```
TypeName[]
```

All arrays are unbounded in length and support 0 based indexing. Array length can be
determined by checking the `.length` property of the array typed variable.

### XML & JSON Types

Ballerina has built-in support for XML elements, XML documents and JSON documents. TypeName
can be any of the following:
- json\[\<json_schema_name\>\]
- xml\[<{XSD_namespace_name}type_name\>\]
- xmlDocument

A variable of type `json` can hold any JSON document. The optional qualification of the TypeName
for a JSON document indicates the name of the JSON schema that the JSON value is assumed to
conform to.

A variable of type `xml` can hold any XML element. The optional qualification of the TypeName
for an XML document indicates the qualified type name of the XML Schema type that the XML
element is assumed to conform to.

A variable of type `xmlDocument` can hold any XML document.

### Allocating Variables

Primitive types do not have to be dynamically allocated as they are always allocated
on the stack. For all non-primitive types, user defined types and array types have to be
allocated on the heap using `new` as follows:
```
new TypeName[(ValueList)]
```
The optional ValueList can be used to give initial values for the fields of any record type. The order
of values must correspond to the order of field declarations.

### Default Values for Variables

Variables can be given values at time of declaration as follows:
```
TypeName VariableName = Value;
```

### Literal Values

The following are examples of literal values for various types:
```
int age = 4;
double price = 4.0;
string name = "John";
xmlElement address_xml = `<address><name>$name</name></address>`;
json address_json = `{"name" : "$name", "streetName" : "$street"}`;
map m = {"name" : "John", "age" : 34 };
int[] data = [1, 2, 3, 6, 10];
```

### Type Coercion and Conversion

The built-in `float` and `double` follow the standard IEEE 754 specifications. The `int` and `long` types follow
the standard 32- and 64-bit integer arithmetic, respectively. Implicit type conversions from
number types work the same way as defined by these specifications.

For lossy type conversions, one must explicitly cast the value to the lower type. For example:
```
float f;
int x;

x = (int) f;
```

In addition to these built in type coercions and conversions, Ballerina allows one to define
arbitrary conversions from one non-primitive type to another non-primitive and have the language apply it automatically.

A TypeConvertor is defined as follows:
```
typeconverter TypeConverterName (TypeName VariableName) (TypeName) {
    Statement;+
}
```
If a TypeConvertor has been defined from Type1 to Type2, then it will be invoked by the runtime upon
executing the following statement:
```
Type1 t1;
Type2 t2;

t2 = (Type2) t1;
```

That is, the registered type convertor is invoked by indicating the type cast as above.

### Resource Definition

The structure of a resource is as follows:

```
[ResourceAnnotations]
resource ResourceName (Message VariableName[, ([ResourceParamAnnotations] TypeName VariableName)+]) {
    ConnectionDeclaration;*
    VariableDeclaration;*
    Statement;+
}*
```

The visual representation of this (without the annotations) is as follows:

![bal-resource-skeleton.png]()

### Function Definition

The structure of a function is as follows:

```
[public] function FunctionName (((TypeName VariableName)[(, TypeName VariableName)*])?)
        ((TypeName[(, TypeName)*])?) [throws exception] {
    ConnectionDeclaration;*
    VariableDeclaration;*
    Statement;+
}
```

All functions are private to the package unless explicitly declared to be public with the `public` keyword. Functions may be invoked within the same package from a resource or a function in the same package without importing. Functions marked `public` can be invoked from another package after importing the package.


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

## Connectors and Connections

Connectors are the participants of an integration; they are the vertical lines in the sequence diagram.
A `connector` provides a set of `action`s that workers can execute against them.

A given `connector` would need to be configured with particular details and a configured connector instance is called a `connection`.

### Connector Definition

```
[package PackageName;?]

connector ConnectorName (
  (TypeName VariableName)(, TypeName VariableName)*
  (, options { (string : Value)+ })?
);

TypeDefinition*

ConnectionDeclaration;*

VariableDeclaration;*

(ActionDefinition | FunctionDefinition | TypeConvertor)+
```

### Action Definition

Actions are operations (functions) that can be executed against a connector.
The overall structure of an action is as follows:

```
action ActionName (ConnectorName VariableName, ((TypeName VariableName)*) (TypeName*)
        [throws exception] {
    ConnectionDeclaration;*
    VariableDeclaration;*
    Statement;+
}
```

All actions are public.

### Connection Declaration

Connections represent a connection established via a connector. The structure is as follows:

```
[ConnectorPackageName.]ConnectorName VariableName = new [ConnectorPackageName.]ConnectorName (ValueList[, map]);
```
Once a connection has been declared, actions can be invoked against that connection as follows:
```
[ConnectorPackageName.]ActionName (ConnectionVariableName, ValueList);
```
## Configuration Management

TODO!

Several Ballerina constructs such as actors and resources have configurable parameters. Examples include the URI of an HTTP endpoint and timeout values. These values MAY be set explicitly within the program using annotations but such values can be overridden from outside the program by applying appropriate property values. These values may be set via environment variables or other deployment management approaches.

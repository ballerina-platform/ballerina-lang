# Introducing Ballerina

Ballerina is a flexible, powerful, beautiful programming language designed for integration. Ballerina is:
- Simple
- Intuitive
- Visual
- Powerful
- Lightweight
- Cloud Native
- Container Native
- Fun

The conceptual model of Ballerina is that of a sequence diagram. Each participant in the integration (referred to as a `connector` in Ballerina) gets its own lifeline, and Ballerina defines a complete syntax and semantics for how the sequence diagram works and executes the desired integration.

Ballerina is not designed to be a general-purpose language. Instead, you should use Ballerina if you need to integrate a collection of network-connected systems such as HTTP endpoints, Web APIs, JMS services, and databases. The result of the integration can either be just that - an integration that runs once or repeatedly on a schedule - or it can be a reusable HTTP service that others can run.

This is an informal introduction to the Ballerina language.

## Introduction

Every Ballerina program has canonical representations both visually and textually.

### Concepts

- *Service*: A `service` is an HTTP web service described by a Swagger file. A service is the discrete unit of functionality that can be remotely accessed.
- *Resource*: A `resource` is a single request handler within a service. The resource concept is designed to be access protocol independent, but in the initial release of the language it is intended to work with HTTP.
- *Connector*: A `connector` represents a participant in the integration and is used to interact with an external system. Ballerina includes a set of standard connectors, and anyone can program additional connectors in Ballerina itself.
- *Action*: An `action` is an operation one can execute against a connector, i.e., a single interaction with a participant of the integration.
- *Worker*: A `worker` is a thread of execution that the integration developer programs as a lifeline.
- *Function*: A `function` is an operation that is executed by a worker.

![High Level Concepts](images/Figure1-1.png)


### Modularity & Versioning

Ballerina programs can be written in one or more files organized into packages. A package is represented by a directory.

A package defines a namespace. All symbols (e.g., service names, type names, and function names) defined in any file in the same package belong to that namespace. Only top level constructs marked `public` are visible outside a package.

Every symbol has a qualified name consisting of its package name and its own top level name. When written down in a program, qualified names are written as follows:

```
PackageName:SymbolName
```
Ballerina brings in versioning to the language. The details of this are still under development and will combine the versioning concepts from Maven, OSGi, and Java to a model that is native to the language.

Every top-level symbol has a version string associated with it (with a default value of "1.0.0"). Packages may define their version number in the Major.Minor.Patch format. Package import statements may indicate the version that they are importing in the Major.Minor format; patch version levels are not relevant to package importers.

In the initial release of Ballerina, the details of versioning are still under development. As such, any version string other than "1.0" for an import version will be rejected by the compiler. Note that there is currently no provision for declaring the version of a package.

## Structure of a Ballerina Program

A Ballerina file is structured as follows:
```
[package PackageName;]
[import PackageName [version ImportVersionNumber] [as Identifier];]*

(ServiceDefinition |
 FunctionDefinition |
 ConnectorDefinition |
 TypeDefinition |
 TypeConvertorDefinition |
 ConstantDefinition)+
```

**Note**: We follow the convention that terminals of the language (keywords) are lowercase words, whereas non-terminals are uppercase words. 

### Services & Resources

A `service` is defined as follows:
```
[ServiceAnnotations]
service ServiceName {
    ConnectorDeclaration;*
    VariableDeclaration;*
    ResourceDefinition;+
}
```
Services are singletons. As such all variables defined within a service scope are shared across all `resource` invocations.

Services may have the following annotations:
- TBD - someone will go thru the Swagger spec and propose a detailed list of annotations to be included at each level.

The structure of a `ResourceDefinition` used to define a `resource` is as follows:
```
[ResourceAnnotations]
resource ResourceName (Message VariableName[, ([ResourceParamAnnotations] TypeName VariableName)+]) {
    ConnectorDeclaration;*
    VariableDeclaration;*
    WorkerDeclaration;*
    Statement;+
}*
```

The visual representation of this (without the annotations) in a sequence diagram is as follows:

![Resources in a Service](images/bal-resource-skeleton.png)

m1 and m2 are messages that are passed by a client as input to the resource named resource-1 and resource-2, respectively. resource-1 will produce the message response1 as a result, and resource-2 will produce response2. To compute the response message, resource-1 relays message m1 to connector Connector-1 and will receive response1; similarly, resource-2 relays message m2 to connector Connector-2 and will receive response2.

### Functions

A `function` is defined as follows:

```
[FunctionAnnotations]
[public] function FunctionName (((TypeName VariableName)[(, TypeName VariableName)*])?)
        ((TypeName[(, TypeName)*])?) [throws exception] {
    ConnectorDeclaration;*
    VariableDeclaration;*
    WorkerDeclaration;*
    Statement;+
}
```

All functions are private to the package unless explicitly declared to be public with the `public` keyword. Functions may be invoked within the same package from a resource or a function in the same package without importing. Functions marked `public` can be invoked from another package after importing the package.

### Connectors & Actions

Connectors represent participants in the integration. A `connector` is defined as follows:
```
[ConnectorAnnotations]
connector ConnectorName ([ConnectorParamAnnotations]TypeName VariableName[(, TypeName VariableName)*]) {
    ConnectorDeclaration;*
    VariableDeclaration;*
    ActionDefinition;+
}
```

Note that `ConnectorAnnotations` are designed to help the editor provide a better user experience for connector users.

A `connector` defines a set of actions. Actions are operations that can be executed against a connector. The  structure of an `action` definition is as follows:

```
[ActionAnnotations]

action ActionName (ConnectorName VariableName[, ([ActionParamAnnotations] TypeName VariableName)+]) (TypeName*)
        [throws exception] {
    ConnectorDeclaration;*
    VariableDeclaration;*
    WorkerDeclaration;*
    Statement;+
}
```

Connectors are instantiated (by means of the `new` keyword) as follows:
```
[ConnectorPackageName:]ConnectorName ConnectorInstanceName = new [ConnectorPackageName:]ConnectorName (ValueList[, map]);
```
The newly created instance has the `ConnectorInstanceName` assigned. 

Once a connector of name `ConnectorInstanceName` has been instantiated, actions can be invoked against that connector as follows:
```
[ConnectorPackageName:]ConnectorName.ActionName (ConnectorInstanceName, ValueList);
```

### Workers

#### Defining & Declaring Workers
A `worker` is defined and declared as follows:
```
worker WorkerName (message m) {
    VariableDeclaration;*
    Statement;+
    [reply MessageName;]
}
```

#### Initiating the Worker

Workers initially come into existence when the enclosing entity (resource, function, or action)
becomes active. However, similar to a resource, the worker does not execute until it
has been sent a message.

A worker is triggered when a message is sent to the worker as follows by the enclosing entity:

```
MessageName -> WorkerName;
```

#### Waiting for Worker Completion

When the worker replies, the response message (if any) is received by the enclosing entity
from the worker as follows:
```
MessageName <- WorkerName;
```

#### Replying from a Worker

If the worker wishes to reply to the enclosing entity, it can do so using a `reply` statement.

### Types & Variables

Ballerina has variables of various types. The type system includes built-in primitive or value types, a
collection of built-in structured types, and array, record, and iterator type constructors. All variables
of primitive types are allocated on the stack, while all non-primitive types are allocated
on a heap using `new`.

The type system is illustrated in the following:

![Ballerina Type System](images/typesystem.png)

#### Declaring Variables

A `VariableDeclaration` has the following structure:

```
TypeName VariableName;
```

A `TypeName` can be one of the following built-in *primitive types*:
- boolean
- int
- long
- float
- double
- string

Primitive types do not have to be dynamically allocated as they are always allocated
on the stack.

A `TypeName` can also be one of the following built-in *non-primitive types*:
- message
- map
- exception

A `TypeName` can also be the name of a *user defined type*.

#### Constructed Types (User Defined Types)

##### Structured Types (Records)
User defined record types are defined using the `struct` keyword as follows:
```
[public] struct TypeName {
    TypeName VariableName;+
}
```
If a `struct` is marked `public`, it can be instantiated from another package.

##### Arrays
Arrays are defined using the array constructor `[]` as follows:
```
TypeName[]
```
All arrays are unbounded in length and support 0 based indexing.

##### Iterators
Iterators are defined using the iterator constructor `~` as follows:
```
TypeName~
```
Iterator typed values are navigated through using an `iterate` statement.

Iterators are currently only available for the built-in types xml and json. In the future we will allow developers to define their own iterators for their types.

#### XML & JSON Types

Ballerina has built-in support for XML elements, XML documents, and JSON documents. TypeName
can be any of the following:
- json\[\<json_schema_name\>\]
- xml\[<{XSD_namespace_name}type_name\>\]
- xmlDocument\[<{XSD_namespace_name}type_name\>\]

A variable of type `json` can hold any JSON document. The optional qualification of the TypeName
for a JSON document indicates the name of the JSON schema that the JSON value is assumed to
conform to.

A variable of type `xml` can hold any XML element. The optional qualification of the TypeName
for an XML document indicates the qualified type name of the XML Schema type that the XML
element is assumed to conform to.

A variable of type `xmlDocument` can hold any XML document, and the optional schema type is the type of the document element.

#### Allocating Variables

Primitive types do not have to be dynamically allocated as they are always allocated
on the stack. 

All non-primitive types, user-defined types, and array types have to be
allocated on the heap using `new` as follows:
```
new TypeName[(ValueList)]
```
The optional ValueList can be used to give initial values for the fields of any record type. The order
of values must correspond to the order of field declarations.

#### Default Values for Variables

Variables can be given values at time of declaration as follows:
```
TypeName VariableName = Value;
```

#### Literal Values

The following are examples of literal values for various types:
```
int age = 4;
double price = 4.0;
string name = "John";
xml address_xml = `<address><name>$name</name></address>`;
json address_json = `{"name" : "$name", "streetName" : "$street"}`;
map m = {"name" : "John", "age" : 34 };
int[] data = [1, 2, 3, 6, 10];
```

#### Type Coercion and Conversion

The built-in `float` and `double` follow the standard IEEE 754 specifications. The `int` and `long` types follow
the standard 32- and 64-bit integer arithmetic, respectively.

The following lossless type coercions are pre-defined in Ballerina:
- boolean -> int/long/float/double with values 0 or 1 for false or true, respectively
- int -> long/float/double
- long -> double
- float -> double

In addition to these built in type coercions, Ballerina allows one to define
arbitrary conversions from one non-primitive type to another non-primitive and have the language apply it automatically.

A `TypeConvertor` is defined as follows:
```
typeconverter TypeConverterName (TypeName VariableName) (TypeName) {
    VariableDeclaration;*
    Statement;+
}
```
If a TypeConvertor has been defined from Type1 to Type2, it will be invoked by the runtime upon
executing the following statement:
```
Type1 t1;
Type2 t2;

t2 = (Type2) t1;
```

That is, the registered type convertor is invoked by indicating the type cast as above. Note that while
the compiler can auto-detect the right convertor to apply, we have chosen to force the user to
request the appropriate convertor by applying a cast.

##### Built in Type Convertors

In addition to the built-in value type coercions, Ballerina also ships with a few pre-defined type
convertors to make development easier. The following predefined type convertors are declared in
the Ballerina package `ballerina.lang.convertors`:
- string/xml/json to message: creates a new message with the given string/xml/json as its payload
- down conversions for numeral types: int -> boolean (0 is false), long -> int/boolean, float -> int/boolean, double -> float/long/int/boolean,

Note that these must be triggered by indicating a type cast to the desired type.

### Statements

A Statement can be one of the following:
- assignment statement
- if statement
- iterate statement
- while statement
- break statement
- fork/join statement
- try/catch statement
- throw statement
- return statement
- reply statement
- worker initiation statement
- worker join statement
- action invocation statement
- comment statement

#### Assignment Statement

Assignment statements are encoded as follows:
```
VariableAccessor = Expression;
```
A `VariableAccessor` is one of:
- VariableName
- VariableAccessor'['ArrayIndex']'
- VariableAccessor'['MapIndex']'
- VariableAccessor.FieldName

#### If Statement

An `if` statement provides a way to perform conditional execution.
```
if (BooleanExpression) {
    Statement;*
}
[else if (BooleanExpression) {
    Statement;*
}]* [else {
    Statement;*
}]
```

#### Iterate Statement

An `iterate` statement provides a way to iterate through an iterator.
```
iterate (VariableType VariableName : Iterator) {
  Statement;+
}
```

#### While Statement

A `while` statement provides a way to execute a series of statements as long as a Boolean expression is met. 
```
while (BooleanExpression) {
    Statement;+
}
```

#### Break Statement

A `break` statement allows one to terminate the immediately enclosing loop.
This is only allowed within the `iterate` or `while` constructs.
```
break;
```

#### Fork/Join Statement

A `fork` statement allows one to replicate a message to any number of parallel
workers and have them independently operate on the copies of the message. The `join`
part of the `fork` statement allows one to define how the caller of `fork`
will wait for the parallel workers to complete.

```
fork (MessageName) {
  worker WorkerName (message VariableName) {
    VariableDeclaration;*
    Statement;+
    [reply MessageName;]
  }+       
} [join (JoinCondition) (message[] VariableName) {
  Statement;*
} timeout (Expression) (message[] VariableName) {
  Statement;*  
}]
```
Note that if the `join` clause is missing, it is equivalent to waiting for all workers to complete and ignorning the results.

The `JoinCondition` is one of the following:
- `any IntegerValue [(WorkerNameList)]`: wait for any k (i.e., the IntegerValue) of the given workers or any of the workers
- `all [(WorkerNameList)]`: wait for all given workers or all of the workers

where `WorkerNameList` is a list of comma-separated names of workers.

When the `JoinCondition` has been satisfied, the corresponding slots of the message array will be filled with the returned messages from the workers in the workers' lexical order. If the condition asks for up to some number of results to be available to satisfy the condition, it may be the case that more than that number are available by the time the statements within the join condition are executed. If a particular worker has completed but not sent a response message, or not yet completed, the corresponding message slot will be null.

The `timeout` clause allows one to specify a maximum time (in milliseconds) within which the join condition must be satisfied.


#### Exception Handling

Ballerina supports exception handling as a way to address unexpected scenarios in a Ballerina program. This is provided by the built-in `exception` type, the `try/catch` statement, and the `throw` statement. Furthermore, any function can indicate that it may throw an exception by saying `throws exception`.

The built-in `exception` type has three properties: its category (a string), its message (a string), and its properties (a map). These properties are manipulated using the functions defined in the `ballerina.lang.exception` package.

Note that there is only one built-in exception type, and all exceptions use this type with different values for the category property. All standard exception "types" are defined by category string constants in the `ballerina.lang.exception` package.

The syntax of a `try/catch` is as follows:
```
try {
    Statement;+
} catch (exception e) {
    Statement;+
}
```

The syntax of a `throw` statement is as follows:
```
throw Expression;
```

#### Return Statement

The syntax of a `return` statement is as follows:
```
return Expression*;
```

#### Reply Statement

The syntax of a `reply` statement is as follows:
```
reply Message?;
```

#### Comment Statement

Comments are quite different in Ballerina in comparison to other languages. Comments are only allowed as a statement - i.e., only inside a resource, action, or function.
Ballerina has designed structured mechanisms via annotations to document all Ballerina outer level constructs (services, resources, etc.), and comments only play the role of providing a comment about the logic of a resource, action, or function.

Any statement that starts with the characters `//` is a comment.

### Expressions
Similar to languages such as Java, Go, etc, Ballerina supports the following expressions: 
* mathamtical expressions `(x + y, x/y, etc.)`
* function calls `(foo(a,b))`
* action calls `(tweet(twitterActor, "hello"))`
* complex expressions `(foo(a,bar(c,d)))`

Please see the grammar file for more details. 

## Disabling Constructs from Execution

In traditional programming languages, developers use commenting as a technique to disable a block of code from executing. In Ballerina, we do not allow comments arbitrarily - we only allow comments as statements.

Ballerina instead allows the developer (either visually or textually) to mark any statement or function, action, connector, resource, or service to be disabled by prefixing it with the `!` character. Disabling a construct does not prevent the language parser, type checker, and other validations; it simply stops that construct from being executed at runtime.

## Configuration Management

TODO!

Several Ballerina constructs such as connectors and resources have configurable parameters. Examples include the URI of an HTTP endpoint and timeout values. These values MAY be set explicitly within the program using annotations, but such values can be overridden from outside the program by applying appropriate property values. These values may be set via environment variables or other deployment management approaches.

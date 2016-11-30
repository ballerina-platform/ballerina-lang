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

This is an informal introduction to the Ballerina language.

## Introduction

Every Ballerina program has both a textual representation and a canonical visual representation.

### Concepts

- *Service*: A `service` is an HTTP web service described by a Swagger. A service is the discrete unit of functionality that can be remotely accessed.
- *Resource*: A `resource` is a single request handler within a service. The resource concept is designed to be access protocol independent - but in the initial release of the language it is intended to work with HTTP.
- *Connector*: A `connector` represents a participant in the integration and is used to interact with an external system. Ballerina includes a set of standard connectors.
- *Connection*: A connection represents the instantiation of a `connector` with a particular configuration.
- *Action*: An `action` is an operation one can execute against a connection - i.e. a single interaction with a participant of the integration.
- *Function*: A `function` is an operation that is executed by a worker.
- *Worker*: A `worker` is a thread of execution that the integration developer programs as a lifeline.

![High Level Concepts](images/HighLevelConcepts.png)


### Modularity

Ballerina programs can be written in one or more files organized into packages. A package is represented by a directory.

A package defines a namespace. All symbols (e.g. service names, type names and function names) defined in any file in the same package belong to that namespace. Any symbol marked public is also visible to outside packages and can be accessed via the package qualified name of the symbol.

## Structure of a Ballerina Program

A Ballerina file is structured as follows:
```
[package PackageName;]
[import PackageName[ as Identifier];]*

(ServiceDefinition |
 FunctionDefinition |
 ConnectorDefinition |
 TypeDefinition |
 TypeConvertorDefinition |
 ConstantDefinition)+
```
Following is an example Ballerina program that shows the form of each construct. 
```
package org.example.weather; 
import balaerina.math

service WeatherService{
    WeatherConnector wc = new WeatherConnector( ... );
    resource WeatherInFResource(message message){
        float lat = xml.get(message.payload, "/lat");
        float lon = xml.get(message.payload, "/lon");
        float temperature = wc.getTemprature(new location(lat, lon));
        return `{"temperature":$temperature}`;
    }
 }

type location{
    int int lon; 
}

connector WeatherConnector{
    action getTemprature(location) (int) { ...}
    ...
}
    
function fromC2F(float temperature){
    return math.round(32 + temperature*5/9);
}
```

### Services & Resources

A `service` is defined as follows:
```
[ServiceAnnotations]
service ServiceName {
    ConnectionDeclaration;*
    VariableDeclaration;*
    ResourceDefinition;+
}
```
Services are singletons. As such all variables defined within a service scope are shared across all `resource` invocations.

Services may have the following annotations:
- TBD - someone will go thru the Swagger spec and propose a detailed list of annotations to be included at each level.

The structure of a ResourceDefinition is as follows:

```
[ResourceAnnotations]
resource ResourceName (Message VariableName[, ([ResourceParamAnnotations] TypeName VariableName)+]) {
    ConnectionDeclaration;*
    VariableDeclaration;*
    WorkerDeclaration;*
    Statement;+
}*
```

The visual representation of this (without the annotations) is as follows:

![bal-resource-skeleton.png]()

### Functions

The structure of a function is as follows:

```
[FunctionAnnotations]
[public] function FunctionName (((TypeName VariableName)[(, TypeName VariableName)*])?)
        ((TypeName[(, TypeName)*])?) [throws exception] {
    ConnectionDeclaration;*
    VariableDeclaration;*
    WorkerDeclaration;*
    Statement;+
}
```

All functions are private to the package unless explicitly declared to be public with the `public` keyword. Functions may be invoked within the same package from a resource or a function in the same package without importing. Functions marked `public` can be invoked from another package after importing the package.

### Connectors, Actions & Connections

A `connector` is defined as follows:
```
[ConnectorAnnotations]
connector ConnectorName ([ConnectorParamAnnotations]TypeName VariableName[(, TypeName VariableName)*]) {
    ConnectionDeclaration;*
    VariableDeclaration;*
    ActionDefinition;+
}
```

Note that ConnectorAnnotations are designed to help the editor provide a better user experience for connector users.

A `connector` defines a set of actions. Actions are operations (functions) that can be executed against a connector. The  structure of an `action` definition is as follows:

```
[ActionAnnotations]
action ActionName (ConnectorName VariableName[, ([ActionParamAnnotations] TypeName VariableName)+]) (TypeName*)
        [throws exception] {
    ConnectionDeclaration;*
    VariableDeclaration;*
    WorkerDeclaration;*
    Statement;+
}
```

Connections represent a connection established via a connector. The structure is as follows:

```
[ConnectorPackageName.]ConnectorName VariableName = new [ConnectorPackageName.]ConnectorName (ValueList[, map]);
```
Once a connection has been declared, actions can be invoked against that connection as follows:
```
[ConnectorPackageName.]ActionName (ConnectionVariableName, ValueList);
```

### Workers

#### Defining & Declaring Workers
Workers are defined and declared as follows:
```
worker WorkerName (message m) {
    ConnectionDeclaration;*
    VariableDeclaration;*
    Statement;+
    [reply MessageName;]
}
```

#### Initiating the Worker

Workers initially come into existence when the enclosing entity (resource, function or action)
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

Following code show a sample worker. 
```
worker AsyncCalculator (message m) {
    int x = xml.get(m, "x");
    int y = xml.get(m, "y");  
    int result = x + y;
    message m = new message(); 
    m.payload = `{"result": $result}`
    reply m
}

message m = new message(); 
m.payload = `{"x": 3, "y": 7}`
//trigger AsyncCalculator
m->AsyncCalculator
//AsyncCalculator will run in parallel to do_something()
do_something()
//wait for response from AsyncCalculator
response<-AsyncCalculator
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

#### Constructed Types

##### Records
User defined record types are defined using a TypeDefinition as follows:
```
[public] type TypeName {
    TypeName VariableName;+
}
```
If a record type is marked `public` then it may be instantiated from another package.

##### Arrays
Arrays may be defined using the array constructor `[]` as follows:
```
TypeName[]
```
All arrays are unbounded in length and support 0 based indexing.

##### Iterators
Iterators may be defined using the iterator constructor `~` as follows:
```
TypeName~
```
Iterators are values that can be navigated through using an `iterate` statement.


#### XML & JSON Types

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

#### Allocating Variables

Primitive types do not have to be dynamically allocated as they are always allocated
on the stack. For all non-primitive types, user defined types and array types have to be
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
xmlElement address_xml = `<address><name>$name</name></address>`;
json address_json = `{"name" : "$name", "streetName" : "$street"}`;
map m = {"name" : "John", "age" : 34 };
int[] data = [1, 2, 3, 6, 10];
```

#### Type Coercion and Conversion

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
    VariableDeclaration;*
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

### Statements

A Statement may be one of the following:
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
- comment statement

#### Assignment Statement

Assignment statements look like the following:
```
VariableAccessor = Expression;
```
A VariableAccessor is one of:
- VariableName
- VariableAccessor'['ArrayIndex']'
- VariableAccessor'['MapIndex']'
- VariableAccessor.FieldName

#### If Statement

Provides a way to perform conditional execution.
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

A fork statement allows one to replicate a message to any number of parallel
workers and have them independently operate on the copies of the message. The `join`
part of the `fork` statement allows one to define how the caller of `fork`
will wait for the parallel workers to complete.

```
fork (MessageName) {
  worker WorkerName (message VariableName) {
    ConnectionDeclaration;*
    VariableDeclaration;*
    Statement;+
    [reply MessageName;]
  }+       
} [join (JoinCondition) (message[] VariableName) {
  ConnectionDeclaration;*
  VariableDeclaration;*
  Statement;*
}]
```
The JoinCondition is one of the following:
- any IntegerValue[(, WorkerName)+]): wait for any k (IntegerValue) of the given workers or any of the workers
- all [WorkerNameList]: wait for all given workers or all of the workers

When the `JoinCondition` has been satisfied, the corresponding slots of the message array will be filled with the returned messages from the workers in the order the workers' lexical order. If the condition asks for up to some number of results to be available to satisfy the condition, it may be the case that more than that number are available by the time the statements within the join condition are executed. If a particular worker has completed but not sent a response message, or not yet completed, the corresponding message slot will be null.

Following is an Example. 
```
FlightService fs = new FlightService(...)
HotelService hs = new FlightService(...)
fork (msg) {
  worker checkFlightsWroker (message msg) {
    string to = xml.get(msg.payload, '/to')
    string from = xml.get(msg.payload, '/from')
    date address = new date(xml.get(msg.payload, '/date')
    return fs.query(`{"from":$from, "to":$to, "date":$date}`)
  },
  worker checkHotelsWroker (message msg) {
    string to = xml.get(msg.payload, '/to')
    string from = xml.get(msg.payload, '/from')
    date address = new date(xml.get(msg.payload, '/date')
    return hs.query(`{"from":$from, "to":$to, "date":$date}`)
  }       
} join all (message[] VariableName) {
  bookFlights(VariableName[0])
  bookHotels(VariableName[1])
}

```


#### Try/catch Statement

```
try {
    VariableDeclaration*
    Statement;+
} catch (exception e) {
    VariableDeclaration*
    Statement;+
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

Following is an Example. 
```
    try {
        response = http.sendPost (nyse_ep, m);
    } catch (exception e) {
        message.setHeader(m, HTTP.StatusCode, 500);// need to discuss
        json error = `{"error":"backend failed", "causedby":e.message}`;
        message.setPayload(m, error);
    }
```

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

#### Comment Statement

Comments are quite different in Ballerina in comparison to other languages. Comments are only allowed as a statement - i.e., only inside a resource, action or function.
Ballerina has designed, structured mechanisms via annotations to document all Ballerina outer level constructs (services, resources etc.) and comments only play the role of providing a comment about the logic of a resource, action or function.

Any statement that starts with the characters `//` is a comment.

### Expressions

## Disabling Constructs from Execution

In traditional programming languages, developers use commenting as a technique to disable a block of code from executing. In Ballerina, we do not allow comments arbitrarily - we only allow comments as statements.

Ballerina instead allows the developer (either visually or textually) to mark any statement or function, action, connector, resource or service to be disabled by prefixing it with the `!` character. Disabling a construct does not prevent the language parser, type checker and other validations but it simply stops that construct from being executed at runtime.

## Configuration Management

TODO!

Several Ballerina constructs such as actors and resources have configurable parameters. Examples include the URI of an HTTP endpoint and timeout values. These values MAY be set explicitly within the program using annotations but such values can be overridden from outside the program by applying appropriate property values. These values may be set via environment variables or other deployment management approaches.

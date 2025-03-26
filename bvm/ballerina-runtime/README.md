# The Ballerina Java Runtime
This module contains the implementation of the Ballerina runtime which is designed as the following packages.

#### io.ballerina.runtime.internal.\*
Since the internal package is restricted only to be used within the Ballerina runtime internals, Ballerina developers **should not** use any java classes from the internal package. These internal Java constructs can be often changed and will not be part of the release note.

#### io.ballerina.runtime.transactions.\*
This will only be exposed to the Ballerina transaction package and should not be used by external developers.

#### io.ballerina.runtime.observability.\*
This will only be exposed to the Ballerina observability package and should not be used by external developers.

#### io.ballerina.runtime.api.\*
Ballerina runtime exposes the `api` package to external developers to connect with the Ballerina runtime ecosystem. So external developers **should only** use these APIs. Any changes or improvements to the APIs will be notified with the release note.

## Ballerina Java Runtime API
Ballerina Java Interoperability enables Ballerina developers to call Java code from Ballerina as it runs on top of JVM.
Ballerina offers a set of Java APIs to developers to interactively work with Ballerina runtime constructs such as creating Ballerina Java values, calling Ballerina methods asynchronously, getting into Ballerina type systems, etc.

### Adding Ballerina Java Runtime Dependency
You can add Ballerina runtime dependency as below.

#### Maven

```xml
<dependency>
    <groupId>org.ballerinalang\</groupId>
    <artifactId>ballerina-runtime\</artifactId>
    <version>${ballerinaLangVersion}\</version>
</dependency>
```

#### Gradle

```Gradle 
implementation group: 'org.ballerinalang', name: 'ballerina-runtime', version: "${ballerinaLangVersion}"
```

**Important:** Always add Ballerina runtime as a compile time dependency. At runtime, it should use the Ballerina
runtime jar which is bundled with the running distribution. We should not create any fat jars which include the Ballerina runtime.
Those will cause unexpected issues due to mismatching runtime artifacts with the given distribution version.

Dependency versions can be found [here](https://github.com/ballerina-platform/ballerina-lang/packages/412940).

## Ballerina Java Runtime API
Ballerina runtime API will contain the following sub packages.

| **Package**                         | **Description**                                     |
|-------------------------------------|-----------------------------------------------------|
| io.ballerina.runtime.api            | Basic runtime constructs                            |
| io.ballerina.runtime.api.concurrent | Handle Ballerina-related asynchronous constructs    |
| io.ballerina.runtime.api.constants  | Runtime constants                                   |
| io.ballerina.runtime.api.creators   | APIs to create types, values, errors, etc.          |
| io.ballerina.runtime.api.flags      | Runtime flags for types and symbols                 |
| io.ballerina.runtime.api.repository | Runtime repository constructs for remote-management |
| io.ballerina.runtime.api.types      | Represent Ballerina Java types                      |
| io.ballerina.runtime.api.utils      | Utils methods                                       |
| io.ballerina.runtime.api.values     | Represent Ballerina Java values                     |

## Map Java types to Ballerina types
The following table summarizes how Java types are mapped to corresponding Ballerina types. This is applicable when mapping a return type of a Java method to a Ballerina type.

| **Java type**                            | **Ballerina type** | **Notes**                                              |
|------------------------------------------|--------------------|--------------------------------------------------------|
| Any reference type including “null type” | handle             |                                                        |
| boolean                                  | boolean            |                                                        |
| byte                                     | byte, int, float   | Widening conversion when byte -> int and byte -> float |
| short                                    | int, float         | Widening conversion                                    |
| char                                     | int, float         | Widening conversion                                    |
| int                                      | int, float         | Widening conversion                                    |
| long                                     | int, float         | Widening conversion when long -> float                 |
| float                                    | float              | Widening conversion                                    |
| double                                   | float              |                                                        |

## Map Ballerina types to Java types

The following table summarizes how Ballerina types are mapped to corresponding Java types. These rules are applicable when mapping a Ballerina function argument to a Java method/constructor parameter.

| **Ballerina type** | **Java type**                                    | **Notes**                                                              |
|--------------------|--------------------------------------------------|------------------------------------------------------------------------|
| handle             | Any reference type                               | As specified by the Java method/constructor signature                  |
| boolean            | boolean                                          |                                                                        |
| byte               | byte, short, char, int, long, float, double      | Widening conversion from byte -> short, char, int, long, float, double |
| int                | byte, char, short, int, long                     | Narrowing conversion when int -> byte, char, short, and int            |
| float              | byte, char, short, int, long, float, double      | Narrowing conversion when float -> byte, char, short, int, long, float |
| string             | io.ballerina.runtime.api.values.BString          |                                                                        |
| xml                | io.ballerina.runtime.api.values.BXml             |                                                                        |
| array              | io.ballerina.runtime.api.values.BArray           |                                                                        |
| tuple              | io.ballerina.runtime.api.values.BArray           |                                                                        |
| map                | io.ballerina.runtime.api.values.BMap             |                                                                        |
| table              | io.ballerina.runtime.api.values.BTable           |                                                                        |
| stream             | io.ballerina.runtime.api.values.BStream          |                                                                        |
| object             | io.ballerina.runtime.api.values.BObject          |                                                                        |
| future             | io.ballerina.runtime.api.values.BFuture          |                                                                        |
| function           | io.ballerina.runtime.api.values.BFunctionPointer |                                                                        |
| typedesc           | io.ballerina.runtime.api.values.BTypedesc        |                                                                        |
| error              | io.ballerina.runtime.api.values.BError           |                                                                        |

## Main API constructs

| **Construct**                                  | **Description**                                                                                                                                                                                                                                                                                                                                                                                  |
|------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| io.ballerina.runtime.api.Environment           | Developers can use this as the first argument of an interop method, Ballerina will inject an instance of `Environment` class when calling. That instance can be used to communicate with the currently executing Ballerina runtime. With `Environment` instance, you can get interop Ballerina function name, path parameters, strand id, strand metadata, current module, current runtime, etc. |
| io.ballerina.runtime.api.Module                | Represent Java runtime module.                                                                                                                                                                                                                                                                                                                                                                   |
| io.ballerina.runtime.api.types.PredefinedTypes | Contains predefined types.                                                                                                                                                                                                                                                                                                                                                                       |
| io.ballerina.runtime.api.Runtime               | An instance of the current runtime can be obtained through an `Environment` instance. This will contain APIs to call Ballerina object methods asynchronously.                                                                                                                                                                                                                                    |
| io.ballerina.runtime.api.types.TypeTags        | Contains runtime type tags.                                                                                                                                                                                                                                                                                                                                                                      |

## Create a Ballerina value

The **io.ballerina.runtime.api.creators.ValueCreator**  provides APIs to create Ballerina value instances. A Ballerina value can be created using one of the methods provided in the ValueCreator class as per the requirements.

For example, an array value can be created as follows,

```
BArray array = ValueCreator.createArrayValue(arrayType);
```

## Create a Ballerina type

`io.ballerina.runtime.api.creators.TypeCreator` class provides APIs to create Ballerina type instances. There are multiple methods available to create Ballerina type instances based on the requirements.

For example, a string type array can be created as follows.

```
ArrayType strArrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING)
```
Note: If we need to create a record type or object type defined in the module, we need to pass the type definition 
name and module to get the type instance. 

We can use other TypeCreator APIs to create our own record or object type. But record or object values created using those 
types will not work correctly in the Ballerina code since those type definitions are not defined in the module.

Those can only be used for Java unit tests.

## Calling Ballerina object methods and functions

Ballerina runtime exposes APIs to call Ballerina object methods and functions using Java. If the method is called with an available running strand, the method will be executed in the same strand. The caller can decide whether to execute the method concurrently or sequentially using the `StrandMetadata` argument.
The `isConcurrentSafe` property of the `StrandMetadata` argument should be set to `true` only if the caller guarantees that the mutable state is free of data races for the given method and arguments, allowing the method to run concurrently.

## Calling a Ballerina object method

Ballerina runtime exposes `callMethod` API to call a Ballerina object method using the `io.ballerina.runtime.api.Runtime` class.

The following code shows an example of calling an object method using Java API.

#### Ballerina

```ballerina
import ballerina/jballerina.java;

public class Person {

    public string name;
    public int age;
    
    function init(string name, int age) {
        self.name = name;
        self.age = age;
    }

    public function play(string sport = "cricket") returns string {
        return sport;
    }
        
    public function callPlayWithArgs(string s) returns string = @java:Method {
        'class: "org.ballerinalang.examples.Test"
    } external;
}

public function main() {
    Person p = new Person("John", 30);
    string sport = p.callPlayWithArgs("football");
}
```

#### Java

```java
class Test {
    public static BString callPlayWithArgs(Environment env, BObject object, BString bString) {
        return env.getRuntime().callMethod(object, "play", null, bString);
    }
}
```

## Calling a Ballerina function

Ballerina runtime exposes APIs to call a Ballerina function using Java. This can be done in two ways that are described below.

### Calling a Function using `io.ballerina.runtime.api.Runtime` class

Developers can call a Ballerina function using the `io.ballerina.runtime.api.Runtime` class. This class exposes the `callFunction` method to call a Ballerina function.

E.g.

#### Ballerina

The following Ballerina code defines a function in module `foo` from organization `testOrg`.

```ballerina

public function main() returns error? {
    boolean b = <boolean>callIsEven(2);
}

function isEven(int n) returns boolean {
    return n % 2 == 0;
}

public isolated function callIsEven(int n) returns any = @java:Method {
    'class: "org.ballerinalang.examples.Test",
    name: "callIsEven"
} external;
```

#### Java

```java

class Test {
    public static Object callIsEven(Environment env, long arg) {
        return env.getRuntime().callFunction(new Module("testOrg", "foo"), "isEven", null, arg);
    }
}
```

### Calling a Function Pointer

Developers can call a function through a function pointer which passes through an interop function. Runtime exposes the `call` method in `io.ballerina.runtime.api.values.BFunctionPointer` class.

E.g.

#### Ballerina

```ballerina
public function main() returns error? {
    function (int n) returns boolean f = isEven;
    any result = checkpanic invokeFunctionPointer(f, 2);
    boolean b = <boolean>result;
}

function isEven(int n) returns boolean {
    return n % 2 == 0;
}

public isolated function invokeFunctionPointer(function func, any|error... args) returns any|error = @java:Method {
    'class: "org.ballerinalang.examples.Test",
    name: "invokeFunctionPointer"
} external;
```

#### Java

```java
class Test {
    public static Object invokeFunctionPointer(Environment env, Object func, Object[] args) {
        BFunctionPointer function = (BFunctionPointer) func;
        return function.call(env.getRuntime(), args);
    }
}
```
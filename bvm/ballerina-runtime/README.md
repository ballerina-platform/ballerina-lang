# The Ballerina Java Runtime
This module contains the implementation of the Ballerina runtime which is designed as the following packages.

#### io.ballerina.runtime.internal.\*
Since the internal package is restricted only to be used with Ballerina runtime internals, ballerina developers **should not** use any java classes from the internal package. These internal java constructs can be often changed and will not be part of the release note.


#### io.ballerina.runtime.transactions.\*
This will be only exposed to the ballerina transaction package and should not be used by external developers.


#### io.ballerina.runtime.observability.\*
This will be only exposed to the ballerina observability package and should not be used by external developers.

#### io.ballerina.runtime.api.\*
Ballerina runtime expose api package to external developers to connect with Ballerina runtime ecosystem. So external developers **should only** use these APIs. Any changes or improvements to the APIs will be notified with the release note.

## Ballerina Java Runtime API
Ballerina Java Interoperability enables ballerina developers to call java code from ballerina as it runs on top of JVM. 
Ballerina offers a set of Java APIs to developers to interactively work with Ballerina runtime constructs such as create Ballerina Java values, call ballerina methods asynchronously, get into ballerina type systems etc.

### Adding Ballerina Java Runtime Dependency
You can add ballerina runtime dependency as below

#### Maven

``` xml
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

**Importan:** Always add ballerina runtime as a compile time dependency. At runtime it should use the ballerina 
runtime jar which is bundled with the running distribution. We should not create any fat jars including ballerina runtime. 
Those will cause unexpected issues due to mismatching runtime artifacts with the given distribution version.

Dependency versions can be found here.

<https://github.com/ballerina-platform/ballerina-lang/packages/412940>

## Ballerina Java Runtime API
Ballerina runtime API will contain the following sub packages.

|                                       |                                                  |
|---------------------------------------|--------------------------------------------------|
| **Package**                           | **Description**                                  |
| io.ballerina.runtime.api              | Basic runtime constructs                         |
| io.ballerina.runtime.api.async        | Handle ballerina asynchronous related constructs |
| io.ballerina.runtime.api.constants    | Runtime constants                                |
| io.ballerina.runtime.api.creators     | APIs to create types, values etc                 |
| io.ballerina.runtime.api.flags        | Runtime flags                                    |
| io.ballerina.runtime.api.launch       | Constructs for startup runtime                   |
| io.ballerina.runtime.api.types        | Represent ballerina Java types                   |
| io.ballerina.runtime.api.utils        | Utils methods                                    |
| io.ballerina.runtime.api.utils.values | Represent ballerina Java values                  |

## Map Java types to Ballerina types
The following table summarizes how Java types are mapped to corresponding Ballerina types. This is applicable when mapping a return type of a Java method to a Ballerina type.

|                                          |                    |                                                        |
|------------------------------------------|--------------------|--------------------------------------------------------|
| **Java type**                            | **Ballerina type** | **Notes**                                              |
| Any reference type including “null type” | handle             |                                                        |
| boolean                                  | boolean            |                                                        |
| byte                                     | byte, int, float   | widening conversion when byte -> int and byte -> float |
| short                                    | int, float         | widening conversion                                    |
| char                                     | int, float         | widening conversion                                    |
| int                                      | int, float         | widening conversion                                    |
| long                                     | int, float         | widening conversion when long -> float                 |
| float                                    | float              | widening conversion                                    |
| double                                   | float              |                                                        |

## Map Ballerina types to Java types

The following table summarizes how Ballerina types are mapped to corresponding Java types. These rules are applicable when mapping a Ballerina function argument to a Java method/constructor parameter.

|                    |                                                  |                                                                        |
|--------------------|--------------------------------------------------|------------------------------------------------------------------------|
| **Ballerina type** | **Java type**                                    | **Notes**                                                              |
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

## Jballerina main API constructs

|                                          |                                                                                                                                                                                                                                                                                                                                                                             |
|------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Construct**                            | **Description**                                                                                                                                                                                                                                                                                                                                                             |
| io.ballerina.runtime.api.Environment     | Developers can use this as the first argument of an interop method, Ballerina will inject an instance of Environment when calling. That instance can be used to communicate with the currently executing Ballerina runtime.With Environment you can get interop ballerina function name, path parameters, strand id, strand metadata, current module , current runtime etc. |
| io.ballerina.runtime.api.Future          | This will contain the future value once we call the ballerina method from API asynchrounously.                                                                                                                                                                                                                                                                              |
| io.ballerina.runtime.api.Module          | Represent Java runtime module.                                                                                                                                                                                                                                                                                                                                              |
| io.ballerina.runtime.api.PredefinedTypes | Contains Predefined types.                                                                                                                                                                                                                                                                                                                                                  |
| io.ballerina.runtime.api.Runtime         | You can get an instance of current runtime through an Environment instance. This will contain APIs to call ballerina object methods asynchronously.                                                                                                                                                                                                                         |
| io.ballerina.runtime.api.TypeTags        | Contains runtime type tags.                                                                                                                                                                                                                                                                                                                                                 |

## Create a Ballerina value

The **io.ballerina.runtime.api.creators.ValueCreator**  provides APIs to create Ballerina value instances. A Ballerina value can be created using one of the methods provided in the ValueCreator class as per the requirements.
For example, an array value can be created as follows,

```
BArray array = ValueCreator.createArrayValue(arrayType);
```

## Create a Ballerina type

`io.ballerina.runtime.api.creators.TypeCreator` class provides APIs to create Ballerina type instances. There are multiple methods available to create Ballerina type instances based on the requirements.

For example, a string array type can be created as follows,

```
ArrayType strArrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE\_STRING)
```

## Calling ballerina object method

Ballerina runtime has exposed APIs to call ballerina object method and ballerina function pointer using Java.

io.ballerina.runtime.api.Runtime class exposes two java APis to call object methods.

1. invokeMethodAsyncSequentially

Invoke Object method asynchronously and sequentially. This method will ensure that the object methods are invoked in the same thread where other object methods are executed. So, the methods will be executed sequentially per object level.

2. invokeMethodAsyncConcurrently

Invoke Object method asynchronously and concurrently. Caller needs to ensure that no data race is possible for the mutable state with a given object method and with arguments. So, the method can be concurrently run with different os threads.

Ex. call an isolated method using Java API.

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
    string sport = p.callPlay("football");
}
```

#### Java
```Java

class Test {
    public static BString callPlay(Environment env,BObject object,BString bString) {
        Future future=env.markAsync();
        env.getRuntime().invokeMethodAsyncConcurrently(object,"play","play",null,
            new Callback(){
                @Override
                public void notifySuccess(Object result){
                    future.complete(result);
                }
    
                @Override
                public void notifyFailure(BError error){
                    future.complete(error);
                }
            },null,PredefinedTypes.TYPE_STRING,bString,true);
            return null;
        }
}
```


**Note:** If caller can ensure that given object and object method is isolated and no data race is possible for the mutable state with given arguments, use @invokeMethodAsyncConcurrently
 otherwise @invokeMethodAsyncSequentially . We can decide the object method isolation if and only if both object.getType().isIsolated() and object.getType().isIsolated(methodName) returns true.

### Calling a Function Pointer

Developers can call a function through a function pointer which passes through an interop function. Runtime expose ‘asyncCall’ method in io.ballerina.runtime.api.values.BFunctionPointer class.

Ex.
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
    'class: "org.ballerinalang.examples",
    name: "invokeFunctionPointer"
} external;


```

#### Java
```java

class Test {
    public static Object invokeFunctionPointer(Object func, Object[] args) {
        BFunctionPointer function = (BFunctionPointer) func;
        List<Object> argList = new ArrayList<>();
        for (Object arg : args) {
            argList.add(arg);
            argList.add(true);  // Due to a limitation in the current api we need to pass true as every other 
                                // parameter value to handle default values.
        }
        return function.asyncCall(argList.toArray(), o -> o, METADATA);
    }
}

```



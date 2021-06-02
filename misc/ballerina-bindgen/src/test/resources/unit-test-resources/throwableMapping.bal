import ballerina/jballerina.java;
import ballerina/jballerina.java.arrays as jarrays;

# Ballerina class mapping for the Java `java.io.IOException` class.
@java:Binding {'class: "java.io.IOException"}
distinct class JIOException {

    *java:JObject;
    *JException;

    # The `handle` field that stores the reference to the `java.io.IOException` object.
    public handle jObj;

    # The init function of the Ballerina class mapping the `java.io.IOException` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string representation of the Ballerina class mapping the `java.io.IOException` Java class.
    #
    # + return - The `string` form of the Java object instance.
    function toString() returns string {
        return java:toString(self.jObj) ?: "null";
    }
    # The function that maps to the `addSuppressed` method of `java.io.IOException`.
    #
    # + arg0 - The `Throwable` value required to map with the Java method parameter.
    function addSuppressed(Throwable arg0) {
        java_io_IOException_addSuppressed(self.jObj, arg0.jObj);
    }

    # The function that maps to the `equals` method of `java.io.IOException`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function 'equals(Object arg0) returns boolean {
        return java_io_IOException_equals(self.jObj, arg0.jObj);
    }

    # The function that maps to the `fillInStackTrace` method of `java.io.IOException`.
    #
    # + return - The `Throwable` value returning from the Java mapping.
    function fillInStackTrace() returns Throwable {
        handle externalObj = java_io_IOException_fillInStackTrace(self.jObj);
        Throwable newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `getCause` method of `java.io.IOException`.
    #
    # + return - The `Throwable` value returning from the Java mapping.
    function getCause() returns Throwable {
        handle externalObj = java_io_IOException_getCause(self.jObj);
        Throwable newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `getClass` method of `java.io.IOException`.
    #
    # + return - The `Class` value returning from the Java mapping.
    function getClass() returns Class {
        handle externalObj = java_io_IOException_getClass(self.jObj);
        Class newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `getLocalizedMessage` method of `java.io.IOException`.
    #
    # + return - The `string` value returning from the Java mapping.
    function getLocalizedMessage() returns string? {
        return java:toString(java_io_IOException_getLocalizedMessage(self.jObj));
    }

    # The function that maps to the `getMessage` method of `java.io.IOException`.
    #
    # + return - The `string` value returning from the Java mapping.
    function getMessage() returns string? {
        return java:toString(java_io_IOException_getMessage(self.jObj));
    }

    # The function that maps to the `getStackTrace` method of `java.io.IOException`.
    #
    # + return - The `StackTraceElement[]` value returning from the Java mapping.
    function getStackTrace() returns StackTraceElement[]|error {
        handle externalObj = java_io_IOException_getStackTrace(self.jObj);
        StackTraceElement[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            StackTraceElement element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function that maps to the `getSuppressed` method of `java.io.IOException`.
    #
    # + return - The `Throwable[]` value returning from the Java mapping.
    function getSuppressed() returns Throwable[]|error {
        handle externalObj = java_io_IOException_getSuppressed(self.jObj);
        Throwable[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            Throwable element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function that maps to the `hashCode` method of `java.io.IOException`.
    #
    # + return - The `int` value returning from the Java mapping.
    function hashCode() returns int {
        return java_io_IOException_hashCode(self.jObj);
    }

    # The function that maps to the `initCause` method of `java.io.IOException`.
    #
    # + arg0 - The `Throwable` value required to map with the Java method parameter.
    # + return - The `Throwable` value returning from the Java mapping.
    function initCause(Throwable arg0) returns Throwable {
        handle externalObj = java_io_IOException_initCause(self.jObj, arg0.jObj);
        Throwable newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `notify` method of `java.io.IOException`.
    function notify() {
        java_io_IOException_notify(self.jObj);
    }

    # The function that maps to the `notifyAll` method of `java.io.IOException`.
    function notifyAll() {
        java_io_IOException_notifyAll(self.jObj);
    }

    # The function that maps to the `printStackTrace` method of `java.io.IOException`.
    function printStackTrace() {
        java_io_IOException_printStackTrace(self.jObj);
    }

    # The function that maps to the `printStackTrace` method of `java.io.IOException`.
    #
    # + arg0 - The `PrintStream` value required to map with the Java method parameter.
    function printStackTrace2(PrintStream arg0) {
        java_io_IOException_printStackTrace2(self.jObj, arg0.jObj);
    }

    # The function that maps to the `printStackTrace` method of `java.io.IOException`.
    #
    # + arg0 - The `PrintWriter` value required to map with the Java method parameter.
    function printStackTrace3(PrintWriter arg0) {
        java_io_IOException_printStackTrace3(self.jObj, arg0.jObj);
    }

    # The function that maps to the `setStackTrace` method of `java.io.IOException`.
    #
    # + arg0 - The `StackTraceElement[]` value required to map with the Java method parameter.
    # + return - The `error?` value returning from the Java mapping.
    function setStackTrace(StackTraceElement[] arg0) returns error? {
        java_io_IOException_setStackTrace(self.jObj, check jarrays:toHandle(arg0, "java.lang.StackTraceElement"));
    }

    # The function that maps to the `wait` method of `java.io.IOException`.
    #
    # + return - The `InterruptedException` value returning from the Java mapping.
    function 'wait() returns InterruptedException? {
        error|() externalObj = java_io_IOException_wait(self.jObj);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `java.io.IOException`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait2(int arg0) returns InterruptedException? {
        error|() externalObj = java_io_IOException_wait2(self.jObj, arg0);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `java.io.IOException`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait3(int arg0, int arg1) returns InterruptedException? {
        error|() externalObj = java_io_IOException_wait3(self.jObj, arg0, arg1);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

}

# The constructor function to generate an object of `java.io.IOException`.
#
# + return - The new `JIOException` class generated.
function newJIOException1() returns JIOException {
    handle externalObj = java_io_IOException_newJIOException1();
    JIOException newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `java.io.IOException`.
#
# + arg0 - The `string` value required to map with the Java constructor parameter.
# + return - The new `JIOException` class generated.
function newJIOException2(string arg0) returns JIOException {
    handle externalObj = java_io_IOException_newJIOException2(java:fromString(arg0));
    JIOException newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `java.io.IOException`.
#
# + arg0 - The `string` value required to map with the Java constructor parameter.
# + arg1 - The `Throwable` value required to map with the Java constructor parameter.
# + return - The new `JIOException` class generated.
function newJIOException3(string arg0, Throwable arg1) returns JIOException {
    handle externalObj = java_io_IOException_newJIOException3(java:fromString(arg0), arg1.jObj);
    JIOException newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `java.io.IOException`.
#
# + arg0 - The `Throwable` value required to map with the Java constructor parameter.
# + return - The new `JIOException` class generated.
function newJIOException4(Throwable arg0) returns JIOException {
    handle externalObj = java_io_IOException_newJIOException4(arg0.jObj);
    JIOException newObj = new (externalObj);
    return newObj;
}

function java_io_IOException_addSuppressed(handle receiver, handle arg0) = @java:Method {
    name: "addSuppressed",
    'class: "java.io.IOException",
    paramTypes: ["java.lang.Throwable"]
} external;

function java_io_IOException_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "java.io.IOException",
    paramTypes: ["java.lang.Object"]
} external;

function java_io_IOException_fillInStackTrace(handle receiver) returns handle = @java:Method {
    name: "fillInStackTrace",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_getCause(handle receiver) returns handle = @java:Method {
    name: "getCause",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_getClass(handle receiver) returns handle = @java:Method {
    name: "getClass",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_getLocalizedMessage(handle receiver) returns handle = @java:Method {
    name: "getLocalizedMessage",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_getMessage(handle receiver) returns handle = @java:Method {
    name: "getMessage",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_getStackTrace(handle receiver) returns handle = @java:Method {
    name: "getStackTrace",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_getSuppressed(handle receiver) returns handle = @java:Method {
    name: "getSuppressed",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_initCause(handle receiver, handle arg0) returns handle = @java:Method {
    name: "initCause",
    'class: "java.io.IOException",
    paramTypes: ["java.lang.Throwable"]
} external;

function java_io_IOException_notify(handle receiver) = @java:Method {
    name: "notify",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_notifyAll(handle receiver) = @java:Method {
    name: "notifyAll",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_printStackTrace(handle receiver) = @java:Method {
    name: "printStackTrace",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_printStackTrace2(handle receiver, handle arg0) = @java:Method {
    name: "printStackTrace",
    'class: "java.io.IOException",
    paramTypes: ["java.io.PrintStream"]
} external;

function java_io_IOException_printStackTrace3(handle receiver, handle arg0) = @java:Method {
    name: "printStackTrace",
    'class: "java.io.IOException",
    paramTypes: ["java.io.PrintWriter"]
} external;

function java_io_IOException_setStackTrace(handle receiver, handle arg0) = @java:Method {
    name: "setStackTrace",
    'class: "java.io.IOException",
    paramTypes: ["[Ljava.lang.StackTraceElement;"]
} external;

function java_io_IOException_wait(handle receiver) returns error? = @java:Method {
    name: "wait",
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_wait2(handle receiver, int arg0) returns error? = @java:Method {
    name: "wait",
    'class: "java.io.IOException",
    paramTypes: ["long"]
} external;

function java_io_IOException_wait3(handle receiver, int arg0, int arg1) returns error? = @java:Method {
    name: "wait",
    'class: "java.io.IOException",
    paramTypes: ["long", "int"]
} external;

function java_io_IOException_newJIOException1() returns handle = @java:Constructor {
    'class: "java.io.IOException",
    paramTypes: []
} external;

function java_io_IOException_newJIOException2(handle arg0) returns handle = @java:Constructor {
    'class: "java.io.IOException",
    paramTypes: ["java.lang.String"]
} external;

function java_io_IOException_newJIOException3(handle arg0, handle arg1) returns handle = @java:Constructor {
    'class: "java.io.IOException",
    paramTypes: ["java.lang.String", "java.lang.Throwable"]
} external;

function java_io_IOException_newJIOException4(handle arg0) returns handle = @java:Constructor {
    'class: "java.io.IOException",
    paramTypes: ["java.lang.Throwable"]
} external;

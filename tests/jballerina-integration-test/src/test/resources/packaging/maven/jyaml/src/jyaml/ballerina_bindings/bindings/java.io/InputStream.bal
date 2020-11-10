
import ballerina/java;

# Ballerina object mapping for Java abstract class `java/io/InputStream`.
#
# + _InputStream - The field that represents this Ballerina object, which is used for Java subtyping.
# + _Closeable - The field that represents the superclass object `Closeable`.
# + _AutoCloseable - The field that represents the superclass object `AutoCloseable`.
# + _Object - The field that represents the superclass object `Object`.
type InputStream object {

    *JObject;

    InputStreamT _InputStream = InputStreamT;
    CloseableT _Closeable = CloseableT;
    AutoCloseableT _AutoCloseable = AutoCloseableT;
    ObjectT _Object = ObjectT;

    # The init function of the Ballerina object mapping `java/io/InputStream` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string value of a Ballerina object mapping a Java class.
    #
    # + return - The `string` form of the object instance.
    function toString() returns string {
        return jObjToString(self.jObj);
    }

    # The function that maps to the `available` method of `java/io/InputStream`.
    # 
    # + return - The `int|IOException` value returning from the Java mapping.
    function available() returns int|IOException {
        int|error externalObj = java_io_InputStream_available(self.jObj);
        if (externalObj is error) {
            IOException e = IOException(message = externalObj.reason(), cause = externalObj);
            return e;
        } else { 
        return externalObj;
        }
    }

    # The function that maps to the `close` method of `java/io/InputStream`.
    # 
    # + return - The `error?` value returning from the Java mapping.
    function close() returns error? {
        error|() obj = java_io_InputStream_close(self.jObj);
        if (obj is error) {
            IOException e = IOException(message = obj.reason(), cause = obj);
            return e;
        }
    }

    # The function that maps to the `equals` method of `java/io/InputStream`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function 'equals(Object arg0) returns boolean {
        boolean externalObj = java_io_InputStream_equals(self.jObj, arg0.jObj);
        return externalObj;
    }

    # The function that maps to the `getClass` method of `java/io/InputStream`.
    # 
    # + return - The `Class` value returning from the Java mapping.
    function getClass() returns Class {
        handle externalObj = java_io_InputStream_getClass(self.jObj);
        Class obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `hashCode` method of `java/io/InputStream`.
    # 
    # + return - The `int` value returning from the Java mapping.
    function hashCode() returns int {
        int externalObj = java_io_InputStream_hashCode(self.jObj);
        return externalObj;
    }

    # The function that maps to the `mark` method of `java/io/InputStream`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    function mark(int arg0) {
        () obj = java_io_InputStream_mark(self.jObj, arg0);
    }

    # The function that maps to the `markSupported` method of `java/io/InputStream`.
    # 
    # + return - The `boolean` value returning from the Java mapping.
    function markSupported() returns boolean {
        boolean externalObj = java_io_InputStream_markSupported(self.jObj);
        return externalObj;
    }

    # The function that maps to the `notify` method of `java/io/InputStream`.
    function notify() {
        () obj = java_io_InputStream_notify(self.jObj);
    }

    # The function that maps to the `notifyAll` method of `java/io/InputStream`.
    function notifyAll() {
        () obj = java_io_InputStream_notifyAll(self.jObj);
    }

    # The function that maps to the `read` method of `java/io/InputStream`.
    # 
    # + return - The `int|IOException` value returning from the Java mapping.
    function read1() returns int|IOException {
        int|error externalObj = java_io_InputStream_read1(self.jObj);
        if (externalObj is error) {
            IOException e = IOException(message = externalObj.reason(), cause = externalObj);
            return e;
        } else { 
        return externalObj;
        }
    }

    # The function that maps to the `read` method of `java/io/InputStream`.
    #
    # + arg0 - The `byte[]` value required to map with the Java method parameter.
    # + return - The `int|IOException|error` value returning from the Java mapping.
    function read2(byte[] arg0) returns int|IOException|error {
        int|error externalObj = java_io_InputStream_read2(self.jObj, check toHandle(arg0, "byte"));
        if (externalObj is error) {
            IOException e = IOException(message = externalObj.reason(), cause = externalObj);
            return e;
        } else { 
        return externalObj;
        }
    }

    # The function that maps to the `read` method of `java/io/InputStream`.
    #
    # + arg0 - The `byte[]` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + arg2 - The `int` value required to map with the Java method parameter.
    # + return - The `int|IOException|error` value returning from the Java mapping.
    function read3(byte[] arg0, int arg1, int arg2) returns int|IOException|error {
        int|error externalObj = java_io_InputStream_read3(self.jObj, check toHandle(arg0, "byte"), arg1, arg2);
        if (externalObj is error) {
            IOException e = IOException(message = externalObj.reason(), cause = externalObj);
            return e;
        } else { 
        return externalObj;
        }
    }

    # The function that maps to the `reset` method of `java/io/InputStream`.
    # 
    # + return - The `error?` value returning from the Java mapping.
    function reset() returns error? {
        error|() obj = java_io_InputStream_reset(self.jObj);
        if (obj is error) {
            IOException e = IOException(message = obj.reason(), cause = obj);
            return e;
        }
    }

    # The function that maps to the `skip` method of `java/io/InputStream`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `int|IOException` value returning from the Java mapping.
    function skip(int arg0) returns int|IOException {
        int|error externalObj = java_io_InputStream_skip(self.jObj, arg0);
        if (externalObj is error) {
            IOException e = IOException(message = externalObj.reason(), cause = externalObj);
            return e;
        } else { 
        return externalObj;
        }
    }

    # The function that maps to the `wait` method of `java/io/InputStream`.
    # 
    # + return - The `error?` value returning from the Java mapping.
    function 'wait1() returns error? {
        error|() obj = java_io_InputStream_wait1(self.jObj);
        if (obj is error) {
            InterruptedException e = InterruptedException(message = obj.reason(), cause = obj);
            return e;
        }
    }

    # The function that maps to the `wait` method of `java/io/InputStream`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `error?` value returning from the Java mapping.
    function 'wait2(int arg0) returns error? {
        error|() obj = java_io_InputStream_wait2(self.jObj, arg0);
        if (obj is error) {
            InterruptedException e = InterruptedException(message = obj.reason(), cause = obj);
            return e;
        }
    }

    # The function that maps to the `wait` method of `java/io/InputStream`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `error?` value returning from the Java mapping.
    function 'wait3(int arg0, int arg1) returns error? {
        error|() obj = java_io_InputStream_wait3(self.jObj, arg0, arg1);
        if (obj is error) {
            InterruptedException e = InterruptedException(message = obj.reason(), cause = obj);
            return e;
        }
    }
};

# Constructor function to generate an object of type `InputStream` representing the `java/io/InputStream` Java class.
#
# + return - The new `InputStream` object generated.
function newInputStream1() returns InputStream {
    handle obj = java_io_InputStream_newInputStream1();
    InputStream _inputStream = new(obj);
    return _inputStream;
}

// External interop functions for mapping public constructors.

function java_io_InputStream_newInputStream1() returns handle = @java:Constructor {
    'class: "java.io.InputStream",
    paramTypes: []
} external;

// External interop functions for mapping public methods.

function java_io_InputStream_available(handle receiver) returns int|error = @java:Method {
    name: "available",
    'class: "java.io.InputStream",
    paramTypes: []
} external;

function java_io_InputStream_close(handle receiver) returns error? = @java:Method {
    name: "close",
    'class: "java.io.InputStream",
    paramTypes: []
} external;

function java_io_InputStream_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "java.io.InputStream",
    paramTypes: ["java.lang.Object"]
} external;

function java_io_InputStream_getClass(handle receiver) returns handle = @java:Method {
    name: "getClass",
    'class: "java.io.InputStream",
    paramTypes: []
} external;

function java_io_InputStream_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "java.io.InputStream",
    paramTypes: []
} external;

function java_io_InputStream_mark(handle receiver, int arg0) = @java:Method {
    name: "mark",
    'class: "java.io.InputStream",
    paramTypes: ["int"]
} external;

function java_io_InputStream_markSupported(handle receiver) returns boolean = @java:Method {
    name: "markSupported",
    'class: "java.io.InputStream",
    paramTypes: []
} external;

function java_io_InputStream_notify(handle receiver) = @java:Method {
    name: "notify",
    'class: "java.io.InputStream",
    paramTypes: []
} external;

function java_io_InputStream_notifyAll(handle receiver) = @java:Method {
    name: "notifyAll",
    'class: "java.io.InputStream",
    paramTypes: []
} external;

function java_io_InputStream_read1(handle receiver) returns int|error = @java:Method {
    name: "read",
    'class: "java.io.InputStream",
    paramTypes: []
} external;

function java_io_InputStream_read2(handle receiver, handle arg0) returns int|error = @java:Method {
    name: "read",
    'class: "java.io.InputStream",
    paramTypes: ["[B"]
} external;

function java_io_InputStream_read3(handle receiver, handle arg0, int arg1, int arg2) returns int|error = @java:Method {
    name: "read",
    'class: "java.io.InputStream",
    paramTypes: ["[B", "int", "int"]
} external;

function java_io_InputStream_reset(handle receiver) returns error? = @java:Method {
    name: "reset",
    'class: "java.io.InputStream",
    paramTypes: []
} external;

function java_io_InputStream_skip(handle receiver, int arg0) returns int|error = @java:Method {
    name: "skip",
    'class: "java.io.InputStream",
    paramTypes: ["long"]
} external;

function java_io_InputStream_wait1(handle receiver) returns error? = @java:Method {
    name: "wait",
    'class: "java.io.InputStream",
    paramTypes: []
} external;

function java_io_InputStream_wait2(handle receiver, int arg0) returns error? = @java:Method {
    name: "wait",
    'class: "java.io.InputStream",
    paramTypes: ["long"]
} external;

function java_io_InputStream_wait3(handle receiver, int arg0, int arg1) returns error? = @java:Method {
    name: "wait",
    'class: "java.io.InputStream",
    paramTypes: ["long", "int"]
} external;



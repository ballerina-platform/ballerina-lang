import ballerina/jballerina.java;
import ballerina/jballerina.java.arrays as jarrays;
import test.java.lang as javalang;
import test.java.nio.channels as javaniochannels;

# Ballerina class mapping for the Java `java.io.FileInputStream` class.
@java:Binding {'class: "java.io.FileInputStream"}
public distinct class FileInputStream {

    *java:JObject;
    *InputStream;

    # The `handle` field that stores the reference to the `java.io.FileInputStream` object.
    public handle jObj;

    # The init function of the Ballerina class mapping the `java.io.FileInputStream` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    public function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string representation of the Ballerina class mapping the `java.io.FileInputStream` Java class.
    #
    # + return - The `string` form of the Java object instance.
    public function toString() returns string {
        return java:toString(self.jObj) ?: "null";
    }
    # The function that maps to the `available` method of `java.io.FileInputStream`.
    #
    # + return - The `int` or the `IOException` value returning from the Java mapping.
    public function available() returns int|IOException {
        int|error externalObj = java_io_FileInputStream_available(self.jObj);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return externalObj;
        }
    }

    # The function that maps to the `close` method of `java.io.FileInputStream`.
    #
    # + return - The `IOException` value returning from the Java mapping.
    public function close() returns IOException? {
        error|() externalObj = java_io_FileInputStream_close(self.jObj);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `equals` method of `java.io.FileInputStream`.
    #
    # + arg0 - The `javalang:Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    public function 'equals(javalang:Object arg0) returns boolean {
        return java_io_FileInputStream_equals(self.jObj, arg0.jObj);
    }

    # The function that maps to the `getChannel` method of `java.io.FileInputStream`.
    #
    # + return - The `javaniochannels:FileChannel` value returning from the Java mapping.
    public function getChannel() returns javaniochannels:FileChannel {
        handle externalObj = java_io_FileInputStream_getChannel(self.jObj);
        javaniochannels:FileChannel newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `getClass` method of `java.io.FileInputStream`.
    #
    # + return - The `javalang:Class` value returning from the Java mapping.
    public function getClass() returns javalang:Class {
        handle externalObj = java_io_FileInputStream_getClass(self.jObj);
        javalang:Class newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `getFD` method of `java.io.FileInputStream`.
    #
    # + return - The `FileDescriptor` or the `IOException` value returning from the Java mapping.
    public function getFD() returns FileDescriptor|IOException {
        handle|error externalObj = java_io_FileInputStream_getFD(self.jObj);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            FileDescriptor newObj = new (externalObj);
            return newObj;
        }
    }

    # The function that maps to the `hashCode` method of `java.io.FileInputStream`.
    #
    # + return - The `int` value returning from the Java mapping.
    public function hashCode() returns int {
        return java_io_FileInputStream_hashCode(self.jObj);
    }

    # The function that maps to the `mark` method of `java.io.FileInputStream`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    public function mark(int arg0) {
        java_io_FileInputStream_mark(self.jObj, arg0);
    }

    # The function that maps to the `markSupported` method of `java.io.FileInputStream`.
    #
    # + return - The `boolean` value returning from the Java mapping.
    public function markSupported() returns boolean {
        return java_io_FileInputStream_markSupported(self.jObj);
    }

    # The function that maps to the `notify` method of `java.io.FileInputStream`.
    public function notify() {
        java_io_FileInputStream_notify(self.jObj);
    }

    # The function that maps to the `notifyAll` method of `java.io.FileInputStream`.
    public function notifyAll() {
        java_io_FileInputStream_notifyAll(self.jObj);
    }

    # The function that maps to the `read` method of `java.io.FileInputStream`.
    #
    # + return - The `int` or the `IOException` value returning from the Java mapping.
    public function read() returns int|IOException {
        int|error externalObj = java_io_FileInputStream_read(self.jObj);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return externalObj;
        }
    }

    # The function that maps to the `read` method of `java.io.FileInputStream`.
    #
    # + arg0 - The `byte[]` value required to map with the Java method parameter.
    # + return - The `int` or the `IOException` value returning from the Java mapping.
    public function read2(byte[] arg0) returns int|IOException|error {
        int|error externalObj = java_io_FileInputStream_read2(self.jObj, check jarrays:toHandle(arg0, "byte"));
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return externalObj;
        }
    }

    # The function that maps to the `read` method of `java.io.FileInputStream`.
    #
    # + arg0 - The `byte[]` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + arg2 - The `int` value required to map with the Java method parameter.
    # + return - The `int` or the `IOException` value returning from the Java mapping.
    public function read3(byte[] arg0, int arg1, int arg2) returns int|IOException|error {
        int|error externalObj = java_io_FileInputStream_read3(self.jObj, check jarrays:toHandle(arg0, "byte"), arg1, arg2);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return externalObj;
        }
    }

    # The function that maps to the `readAllBytes` method of `java.io.FileInputStream`.
    #
    # + return - The `byte[]` or the `IOException` value returning from the Java mapping.
    public function readAllBytes() returns byte[]|IOException|error {
        handle|error externalObj = java_io_FileInputStream_readAllBytes(self.jObj);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return <byte[]>check jarrays:fromHandle(externalObj, "byte");
        }
    }

    # The function that maps to the `readNBytes` method of `java.io.FileInputStream`.
    #
    # + arg0 - The `byte[]` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + arg2 - The `int` value required to map with the Java method parameter.
    # + return - The `int` or the `IOException` value returning from the Java mapping.
    public function readNBytes(byte[] arg0, int arg1, int arg2) returns int|IOException|error {
        int|error externalObj = java_io_FileInputStream_readNBytes(self.jObj, check jarrays:toHandle(arg0, "byte"), arg1, arg2);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return externalObj;
        }
    }

    # The function that maps to the `readNBytes` method of `java.io.FileInputStream`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `byte[]` or the `IOException` value returning from the Java mapping.
    public function readNBytes2(int arg0) returns byte[]|IOException|error {
        handle|error externalObj = java_io_FileInputStream_readNBytes2(self.jObj, arg0);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return <byte[]>check jarrays:fromHandle(externalObj, "byte");
        }
    }

    # The function that maps to the `reset` method of `java.io.FileInputStream`.
    #
    # + return - The `IOException` value returning from the Java mapping.
    public function reset() returns IOException? {
        error|() externalObj = java_io_FileInputStream_reset(self.jObj);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `skip` method of `java.io.FileInputStream`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `int` or the `IOException` value returning from the Java mapping.
    public function skip(int arg0) returns int|IOException {
        int|error externalObj = java_io_FileInputStream_skip(self.jObj, arg0);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return externalObj;
        }
    }

    # The function that maps to the `transferTo` method of `java.io.FileInputStream`.
    #
    # + arg0 - The `OutputStream` value required to map with the Java method parameter.
    # + return - The `int` or the `IOException` value returning from the Java mapping.
    public function transferTo(OutputStream arg0) returns int|IOException {
        int|error externalObj = java_io_FileInputStream_transferTo(self.jObj, arg0.jObj);
        if (externalObj is error) {
            IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return externalObj;
        }
    }

    # The function that maps to the `wait` method of `java.io.FileInputStream`.
    #
    # + return - The `javalang:InterruptedException` value returning from the Java mapping.
    public function 'wait() returns javalang:InterruptedException? {
        error|() externalObj = java_io_FileInputStream_wait(self.jObj);
        if (externalObj is error) {
            javalang:InterruptedException e = error javalang:InterruptedException(javalang:INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `java.io.FileInputStream`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `javalang:InterruptedException` value returning from the Java mapping.
    public function wait2(int arg0) returns javalang:InterruptedException? {
        error|() externalObj = java_io_FileInputStream_wait2(self.jObj, arg0);
        if (externalObj is error) {
            javalang:InterruptedException e = error javalang:InterruptedException(javalang:INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `java.io.FileInputStream`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `javalang:InterruptedException` value returning from the Java mapping.
    public function wait3(int arg0, int arg1) returns javalang:InterruptedException? {
        error|() externalObj = java_io_FileInputStream_wait3(self.jObj, arg0, arg1);
        if (externalObj is error) {
            javalang:InterruptedException e = error javalang:InterruptedException(javalang:INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

}

# The constructor function to generate an object of `java.io.FileInputStream`.
#
# + arg0 - The `File` value required to map with the Java constructor parameter.
# + return - The new `FileInputStream` class or `FileNotFoundException` error generated.
public function newFileInputStream1(File arg0) returns FileInputStream|FileNotFoundException {
    handle|error externalObj = java_io_FileInputStream_newFileInputStream1(arg0.jObj);
    if (externalObj is error) {
        FileNotFoundException e = error FileNotFoundException(FILENOTFOUNDEXCEPTION, externalObj, message = externalObj.message());
        return e;
    } else {
        FileInputStream newObj = new (externalObj);
        return newObj;
    }
}

# The constructor function to generate an object of `java.io.FileInputStream`.
#
# + arg0 - The `FileDescriptor` value required to map with the Java constructor parameter.
# + return - The new `FileInputStream` class generated.
public function newFileInputStream2(FileDescriptor arg0) returns FileInputStream {
    handle externalObj = java_io_FileInputStream_newFileInputStream2(arg0.jObj);
    FileInputStream newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `java.io.FileInputStream`.
#
# + arg0 - The `string` value required to map with the Java constructor parameter.
# + return - The new `FileInputStream` class or `FileNotFoundException` error generated.
public function newFileInputStream3(string arg0) returns FileInputStream|FileNotFoundException {
    handle|error externalObj = java_io_FileInputStream_newFileInputStream3(java:fromString(arg0));
    if (externalObj is error) {
        FileNotFoundException e = error FileNotFoundException(FILENOTFOUNDEXCEPTION, externalObj, message = externalObj.message());
        return e;
    } else {
        FileInputStream newObj = new (externalObj);
        return newObj;
    }
}

# The function that maps to the `nullInputStream` method of `java.io.FileInputStream`.
#
# + return - The `InputStream` value returning from the Java mapping.
public function FileInputStream_nullInputStream() returns InputStream {
    handle externalObj = java_io_FileInputStream_nullInputStream();
    InputStream newObj = new (externalObj);
    return newObj;
}

function java_io_FileInputStream_available(handle receiver) returns int|error = @java:Method {
    name: "available",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_close(handle receiver) returns error? = @java:Method {
    name: "close",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "java.io.FileInputStream",
    paramTypes: ["java.lang.Object"]
} external;

function java_io_FileInputStream_getChannel(handle receiver) returns handle = @java:Method {
    name: "getChannel",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_getClass(handle receiver) returns handle = @java:Method {
    name: "getClass",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_getFD(handle receiver) returns handle|error = @java:Method {
    name: "getFD",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_mark(handle receiver, int arg0) = @java:Method {
    name: "mark",
    'class: "java.io.FileInputStream",
    paramTypes: ["int"]
} external;

function java_io_FileInputStream_markSupported(handle receiver) returns boolean = @java:Method {
    name: "markSupported",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_notify(handle receiver) = @java:Method {
    name: "notify",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_notifyAll(handle receiver) = @java:Method {
    name: "notifyAll",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_nullInputStream() returns handle = @java:Method {
    name: "nullInputStream",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_read(handle receiver) returns int|error = @java:Method {
    name: "read",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_read2(handle receiver, handle arg0) returns int|error = @java:Method {
    name: "read",
    'class: "java.io.FileInputStream",
    paramTypes: ["[B"]
} external;

function java_io_FileInputStream_read3(handle receiver, handle arg0, int arg1, int arg2) returns int|error = @java:Method {
    name: "read",
    'class: "java.io.FileInputStream",
    paramTypes: ["[B", "int", "int"]
} external;

function java_io_FileInputStream_readAllBytes(handle receiver) returns handle|error = @java:Method {
    name: "readAllBytes",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_readNBytes(handle receiver, handle arg0, int arg1, int arg2) returns int|error = @java:Method {
    name: "readNBytes",
    'class: "java.io.FileInputStream",
    paramTypes: ["[B", "int", "int"]
} external;

function java_io_FileInputStream_readNBytes2(handle receiver, int arg0) returns handle|error = @java:Method {
    name: "readNBytes",
    'class: "java.io.FileInputStream",
    paramTypes: ["int"]
} external;

function java_io_FileInputStream_reset(handle receiver) returns error? = @java:Method {
    name: "reset",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_skip(handle receiver, int arg0) returns int|error = @java:Method {
    name: "skip",
    'class: "java.io.FileInputStream",
    paramTypes: ["long"]
} external;

function java_io_FileInputStream_transferTo(handle receiver, handle arg0) returns int|error = @java:Method {
    name: "transferTo",
    'class: "java.io.FileInputStream",
    paramTypes: ["java.io.OutputStream"]
} external;

function java_io_FileInputStream_wait(handle receiver) returns error? = @java:Method {
    name: "wait",
    'class: "java.io.FileInputStream",
    paramTypes: []
} external;

function java_io_FileInputStream_wait2(handle receiver, int arg0) returns error? = @java:Method {
    name: "wait",
    'class: "java.io.FileInputStream",
    paramTypes: ["long"]
} external;

function java_io_FileInputStream_wait3(handle receiver, int arg0, int arg1) returns error? = @java:Method {
    name: "wait",
    'class: "java.io.FileInputStream",
    paramTypes: ["long", "int"]
} external;

function java_io_FileInputStream_newFileInputStream1(handle arg0) returns handle|error = @java:Constructor {
    'class: "java.io.FileInputStream",
    paramTypes: ["java.io.File"]
} external;

function java_io_FileInputStream_newFileInputStream2(handle arg0) returns handle = @java:Constructor {
    'class: "java.io.FileInputStream",
    paramTypes: ["java.io.FileDescriptor"]
} external;

function java_io_FileInputStream_newFileInputStream3(handle arg0) returns handle|error = @java:Constructor {
    'class: "java.io.FileInputStream",
    paramTypes: ["java.lang.String"]
} external;

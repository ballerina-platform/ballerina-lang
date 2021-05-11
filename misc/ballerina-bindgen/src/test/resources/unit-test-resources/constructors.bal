import ballerina/jballerina.java;
import ballerina/jballerina.java.arrays as jarrays;

# Ballerina class mapping for the Java `org.ballerinalang.bindgen.ConstructorsTestResource` class.
@java:Binding {'class: "org.ballerinalang.bindgen.ConstructorsTestResource"}
distinct class ConstructorsTestResource {

    *java:JObject;
    *Object;

    # The `handle` field that stores the reference to the `org.ballerinalang.bindgen.ConstructorsTestResource` object.
    public handle jObj;

    # The init function of the Ballerina class mapping the `org.ballerinalang.bindgen.ConstructorsTestResource` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string representation of the Ballerina class mapping the `org.ballerinalang.bindgen.ConstructorsTestResource` Java class.
    #
    # + return - The `string` form of the Java object instance.
    function toString() returns string {
        return java:toString(self.jObj) ?: "null";
    }
    # The function that maps to the `equals` method of `org.ballerinalang.bindgen.ConstructorsTestResource`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function 'equals(Object arg0) returns boolean {
        return org_ballerinalang_bindgen_ConstructorsTestResource_equals(self.jObj, arg0.jObj);
    }

    # The function that maps to the `getClass` method of `org.ballerinalang.bindgen.ConstructorsTestResource`.
    #
    # + return - The `Class` value returning from the Java mapping.
    function getClass() returns Class {
        handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_getClass(self.jObj);
        Class newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `hashCode` method of `org.ballerinalang.bindgen.ConstructorsTestResource`.
    #
    # + return - The `int` value returning from the Java mapping.
    function hashCode() returns int {
        return org_ballerinalang_bindgen_ConstructorsTestResource_hashCode(self.jObj);
    }

    # The function that maps to the `notify` method of `org.ballerinalang.bindgen.ConstructorsTestResource`.
    function notify() {
        org_ballerinalang_bindgen_ConstructorsTestResource_notify(self.jObj);
    }

    # The function that maps to the `notifyAll` method of `org.ballerinalang.bindgen.ConstructorsTestResource`.
    function notifyAll() {
        org_ballerinalang_bindgen_ConstructorsTestResource_notifyAll(self.jObj);
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.ConstructorsTestResource`.
    #
    # + return - The `InterruptedException` value returning from the Java mapping.
    function 'wait() returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_wait(self.jObj);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.ConstructorsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait2(int arg0) returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_wait2(self.jObj, arg0);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.ConstructorsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait3(int arg0, int arg1) returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_wait3(self.jObj, arg0, arg1);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource1() returns ConstructorsTestResource {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource1();
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `boolean` value required to map with the Java constructor parameter.
# + arg1 - The `string` value required to map with the Java constructor parameter.
# + arg2 - The `StringBuffer` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource2(boolean arg0, string arg1, StringBuffer arg2) returns ConstructorsTestResource {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource2(arg0, java:fromString(arg1), arg2.jObj);
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `float` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class or `IOException` error generated.
function newConstructorsTestResource3(float arg0) returns ConstructorsTestResource|IOException {
    handle|error externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource3(arg0);
    if (externalObj is error) {
        IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
        return e;
    } else {
        ConstructorsTestResource newObj = new (externalObj);
        return newObj;
    }
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `File` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource4(File arg0) returns ConstructorsTestResource {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource4(arg0.jObj);
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `File` value required to map with the Java constructor parameter.
# + arg1 - The `boolean` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class or `InterruptedException` error generated.
function newConstructorsTestResource5(File arg0, boolean arg1) returns ConstructorsTestResource|InterruptedException {
    handle|error externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource5(arg0.jObj, arg1);
    if (externalObj is error) {
        InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
        return e;
    } else {
        ConstructorsTestResource newObj = new (externalObj);
        return newObj;
    }
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `File` value required to map with the Java constructor parameter.
# + arg1 - The `int` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource6(File arg0, int arg1) returns ConstructorsTestResource {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource6(arg0.jObj, arg1);
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `File` value required to map with the Java constructor parameter.
# + arg1 - The `StringBuffer` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource7(File arg0, StringBuffer arg1) returns ConstructorsTestResource {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource7(arg0.jObj, arg1.jObj);
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `int` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource8(int arg0) returns ConstructorsTestResource {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource8(arg0);
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `int[]` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource9(int[] arg0) returns ConstructorsTestResource|error {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource9(check jarrays:toHandle(arg0, "int"));
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `int[]` value required to map with the Java constructor parameter.
# + arg1 - The `string` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource10(int[] arg0, string arg1) returns ConstructorsTestResource|error {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource10(check jarrays:toHandle(arg0, "int"), java:fromString(arg1));
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `int` value required to map with the Java constructor parameter.
# + arg1 - The `int` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource11(int arg0, int arg1) returns ConstructorsTestResource {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource11(arg0, arg1);
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `Object[]` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource12(Object[] arg0) returns ConstructorsTestResource|error {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource12(check jarrays:toHandle(arg0, "java.lang.Object"));
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `Object[]` value required to map with the Java constructor parameter.
# + arg1 - The `boolean` value required to map with the Java constructor parameter.
# + arg2 - The `string[]` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource13(Object[] arg0, boolean arg1, string[] arg2) returns ConstructorsTestResource|error {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource13(check jarrays:toHandle(arg0, "java.lang.Object"), arg1, check jarrays:toHandle(arg2, "java.lang.String"));
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `string` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource14(string arg0) returns ConstructorsTestResource {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource14(java:fromString(arg0));
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `string[]` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource15(string[] arg0) returns ConstructorsTestResource|error {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource15(check jarrays:toHandle(arg0, "java.lang.String"));
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `string[]` value required to map with the Java constructor parameter.
# + arg1 - The `string[]` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource16(string[] arg0, string[] arg1) returns ConstructorsTestResource|error {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource16(check jarrays:toHandle(arg0, "java.lang.String"), check jarrays:toHandle(arg1, "java.lang.String"));
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `string` value required to map with the Java constructor parameter.
# + arg1 - The `byte` value required to map with the Java constructor parameter.
# + arg2 - The `int` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource17(string arg0, byte arg1, int arg2) returns ConstructorsTestResource {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource17(java:fromString(arg0), arg1, arg2);
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `string` value required to map with the Java constructor parameter.
# + arg1 - The `int` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class or `IOException` error generated.
function newConstructorsTestResource18(string arg0, int arg1) returns ConstructorsTestResource|IOException {
    handle|error externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource18(java:fromString(arg0), arg1);
    if (externalObj is error) {
        IOException e = error IOException(IOEXCEPTION, externalObj, message = externalObj.message());
        return e;
    } else {
        ConstructorsTestResource newObj = new (externalObj);
        return newObj;
    }
}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ConstructorsTestResource`.
#
# + arg0 - The `string` value required to map with the Java constructor parameter.
# + arg1 - The `string` value required to map with the Java constructor parameter.
# + return - The new `ConstructorsTestResource` class generated.
function newConstructorsTestResource19(string arg0, string arg1) returns ConstructorsTestResource {
    handle externalObj = org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource19(java:fromString(arg0), java:fromString(arg1));
    ConstructorsTestResource newObj = new (externalObj);
    return newObj;
}

function org_ballerinalang_bindgen_ConstructorsTestResource_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["java.lang.Object"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_getClass(handle receiver) returns handle = @java:Method {
    name: "getClass",
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_notify(handle receiver) = @java:Method {
    name: "notify",
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_notifyAll(handle receiver) = @java:Method {
    name: "notifyAll",
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_wait(handle receiver) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_wait2(handle receiver, int arg0) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["long"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_wait3(handle receiver, int arg0, int arg1) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["long", "int"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource1() returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource2(boolean arg0, handle arg1, handle arg2) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["boolean", "java.lang.String", "java.lang.StringBuffer"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource3(float arg0) returns handle|error = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["double"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource4(handle arg0) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["java.io.File"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource5(handle arg0, boolean arg1) returns handle|error = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["java.io.File", "boolean"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource6(handle arg0, int arg1) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["java.io.File", "char"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource7(handle arg0, handle arg1) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["java.io.File", "java.lang.StringBuffer"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource8(int arg0) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["int"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource9(handle arg0) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["[I"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource10(handle arg0, handle arg1) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["[I", "java.lang.String"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource11(int arg0, int arg1) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["int", "short"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource12(handle arg0) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["[Ljava.lang.Object;"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource13(handle arg0, boolean arg1, handle arg2) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["[Ljava.lang.Object;", "boolean", "[Ljava.lang.String;"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource14(handle arg0) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["java.lang.String"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource15(handle arg0) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["[Ljava.lang.String;"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource16(handle arg0, handle arg1) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["[Ljava.lang.String;", "[Ljava.lang.String;"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource17(handle arg0, byte arg1, int arg2) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["java.lang.String", "byte", "char"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource18(handle arg0, int arg1) returns handle|error = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["java.lang.String", "int"]
} external;

function org_ballerinalang_bindgen_ConstructorsTestResource_newConstructorsTestResource19(handle arg0, handle arg1) returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ConstructorsTestResource",
    paramTypes: ["java.lang.String", "java.lang.String"]
} external;

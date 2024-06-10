import ballerina/jballerina.java;

# Ballerina class mapping for the Java `org.ballerinalang.bindgen.AbstractTestResource` class.
@java:Binding {'class: "org.ballerinalang.bindgen.AbstractTestResource"}
distinct class AbstractTestResource {

    *java:JObject;
    *Object;

    # The `handle` field that stores the reference to the `org.ballerinalang.bindgen.AbstractTestResource` object.
    public handle jObj;

    # The init function of the Ballerina class mapping the `org.ballerinalang.bindgen.AbstractTestResource` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string representation of the Ballerina class mapping the `org.ballerinalang.bindgen.AbstractTestResource` Java class.
    #
    # + return - The `string` form of the Java object instance.
    function toString() returns string {
        return java:toString(self.jObj) ?: "";
    }
    # The function that maps to the `equals` method of `org.ballerinalang.bindgen.AbstractTestResource`.
    #
    # + arg0 - The `Object?` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function 'equals(Object? arg0) returns boolean {
        return org_ballerinalang_bindgen_AbstractTestResource_equals(self.jObj, arg0 is () ? java:createNull() : arg0.jObj);
    }

    # The function that maps to the `getClass` method of `org.ballerinalang.bindgen.AbstractTestResource`.
    #
    # + return - The `Class` value returning from the Java mapping.
    function getClass() returns Class {
        handle externalObj = org_ballerinalang_bindgen_AbstractTestResource_getClass(self.jObj);
        Class newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `hashCode` method of `org.ballerinalang.bindgen.AbstractTestResource`.
    #
    # + return - The `int` value returning from the Java mapping.
    function hashCode() returns int {
        return org_ballerinalang_bindgen_AbstractTestResource_hashCode(self.jObj);
    }

    # The function that maps to the `notify` method of `org.ballerinalang.bindgen.AbstractTestResource`.
    function notify() {
        org_ballerinalang_bindgen_AbstractTestResource_notify(self.jObj);
    }

    # The function that maps to the `notifyAll` method of `org.ballerinalang.bindgen.AbstractTestResource`.
    function notifyAll() {
        org_ballerinalang_bindgen_AbstractTestResource_notifyAll(self.jObj);
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.AbstractTestResource`.
    #
    # + return - The `InterruptedException` value returning from the Java mapping.
    function 'wait() returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_AbstractTestResource_wait(self.jObj);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.AbstractTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait2(int arg0) returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_AbstractTestResource_wait2(self.jObj, arg0);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.AbstractTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait3(int arg0, int arg1) returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_AbstractTestResource_wait3(self.jObj, arg0, arg1);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

}

function org_ballerinalang_bindgen_AbstractTestResource_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "org.ballerinalang.bindgen.AbstractTestResource",
    paramTypes: ["java.lang.Object"]
} external;

function org_ballerinalang_bindgen_AbstractTestResource_getClass(handle receiver) returns handle = @java:Method {
    name: "getClass",
    'class: "org.ballerinalang.bindgen.AbstractTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_AbstractTestResource_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "org.ballerinalang.bindgen.AbstractTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_AbstractTestResource_notify(handle receiver) = @java:Method {
    name: "notify",
    'class: "org.ballerinalang.bindgen.AbstractTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_AbstractTestResource_notifyAll(handle receiver) = @java:Method {
    name: "notifyAll",
    'class: "org.ballerinalang.bindgen.AbstractTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_AbstractTestResource_wait(handle receiver) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.AbstractTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_AbstractTestResource_wait2(handle receiver, int arg0) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.AbstractTestResource",
    paramTypes: ["long"]
} external;

function org_ballerinalang_bindgen_AbstractTestResource_wait3(handle receiver, int arg0, int arg1) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.AbstractTestResource",
    paramTypes: ["long", "int"]
} external;

import test.java.io as javaio;
import test.java.lang as javalang;

import ballerina/jballerina.java;
import ballerina/jballerina.java.arrays as jarrays;

# Ballerina class mapping for the Java `org.ballerinalang.bindgen.InterfaceTestResource` interface.
@java:Binding {'class: "org.ballerinalang.bindgen.InterfaceTestResource"}
public distinct class InterfaceTestResource {

    *java:JObject;

    # The `handle` field that stores the reference to the `org.ballerinalang.bindgen.InterfaceTestResource` object.
    public handle jObj;

    # The init function of the Ballerina class mapping the `org.ballerinalang.bindgen.InterfaceTestResource` Java interface.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    public function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string representation of the Ballerina class mapping the `org.ballerinalang.bindgen.InterfaceTestResource` Java interface.
    #
    # + return - The `string` form of the Java object instance.
    public function toString() returns string? {
        return java:toString(self.jObj);
    }

    # The function that maps to the `returnStringArray` method of `org.ballerinalang.bindgen.InterfaceTestResource`.
    #
    # + return - The `string?[]?` value returning from the Java mapping.
    public function returnStringArray() returns string?[]?|error {
        handle externalObj = org_ballerinalang_bindgen_InterfaceTestResource_returnStringArray(self.jObj);
        if java:isNull(externalObj) {
            return ();
        }
        return <string[]>check jarrays:fromHandle(externalObj, "string");
    }

    # The function that maps to the `testMethod` method of `org.ballerinalang.bindgen.InterfaceTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `int` value returning from the Java mapping.
    public function testMethod(int arg0) returns int {
        return org_ballerinalang_bindgen_InterfaceTestResource_testMethod(self.jObj, arg0);
    }

    # The function that maps to the `testMethodWithException` method of `org.ballerinalang.bindgen.InterfaceTestResource`.
    #
    # + arg0 - The `javalang:Object` value required to map with the Java method parameter.
    # + return - The `string?` or the `javaio:IOException` value returning from the Java mapping.
    public function testMethodWithException(javalang:Object arg0) returns string?|javaio:IOException {
        handle|error externalObj = org_ballerinalang_bindgen_InterfaceTestResource_testMethodWithException(self.jObj, arg0.jObj);
        if (externalObj is error) {
            javaio:IOException e = error javaio:IOException(javaio:IOEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return java:toString(externalObj);
        }
    }

}

# The function that retrieves the value of the public field `TEST_FIELD`.
#
# + return - The `int` value of the field.
public function InterfaceTestResource_getTEST_FIELD() returns int {
    return org_ballerinalang_bindgen_InterfaceTestResource_getTEST_FIELD();
}

function org_ballerinalang_bindgen_InterfaceTestResource_returnStringArray(handle receiver) returns handle = @java:Method {
    name: "returnStringArray",
    'class: "org.ballerinalang.bindgen.InterfaceTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_InterfaceTestResource_testMethod(handle receiver, int arg0) returns int = @java:Method {
    name: "testMethod",
    'class: "org.ballerinalang.bindgen.InterfaceTestResource",
    paramTypes: ["int"]
} external;

function org_ballerinalang_bindgen_InterfaceTestResource_testMethodWithException(handle receiver, handle arg0) returns handle|error = @java:Method {
    name: "testMethodWithException",
    'class: "org.ballerinalang.bindgen.InterfaceTestResource",
    paramTypes: ["java.lang.Object"]
} external;

function org_ballerinalang_bindgen_InterfaceTestResource_getTEST_FIELD() returns int = @java:FieldGet {
    name: "TEST_FIELD",
    'class: "org.ballerinalang.bindgen.InterfaceTestResource"
} external;

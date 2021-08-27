import ballerina/jballerina.java;
import test.java.lang as javalang;
import test.java.util as javautil;

# Ballerina class mapping for the Java `org.ballerinalang.bindgen.ModuleMappingTest` class.
@java:Binding {'class: "org.ballerinalang.bindgen.ModuleMappingTest"}
public distinct class ModuleMappingTest {

    *java:JObject;
    *javalang:Object;

    # The `handle` field that stores the reference to the `org.ballerinalang.bindgen.ModuleMappingTest` object.
    public handle jObj;

    # The init function of the Ballerina class mapping the `org.ballerinalang.bindgen.ModuleMappingTest` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    public function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string representation of the Ballerina class mapping the `org.ballerinalang.bindgen.ModuleMappingTest` Java class.
    #
    # + return - The `string` form of the Java object instance.
    public function toString() returns string {
        return java:toString(self.jObj) ?: "null";
    }
    # The function that maps to the `equals` method of `org.ballerinalang.bindgen.ModuleMappingTest`.
    #
    # + arg0 - The `javalang:Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    public function 'equals(javalang:Object arg0) returns boolean {
        return org_ballerinalang_bindgen_ModuleMappingTest_equals(self.jObj, arg0.jObj);
    }

    # The function that maps to the `getClass` method of `org.ballerinalang.bindgen.ModuleMappingTest`.
    #
    # + return - The `javalang:Class` value returning from the Java mapping.
    public function getClass() returns javalang:Class {
        handle externalObj = org_ballerinalang_bindgen_ModuleMappingTest_getClass(self.jObj);
        javalang:Class newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `hashCode` method of `org.ballerinalang.bindgen.ModuleMappingTest`.
    #
    # + return - The `int` value returning from the Java mapping.
    public function hashCode() returns int {
        return org_ballerinalang_bindgen_ModuleMappingTest_hashCode(self.jObj);
    }

    # The function that maps to the `notify` method of `org.ballerinalang.bindgen.ModuleMappingTest`.
    public function notify() {
        org_ballerinalang_bindgen_ModuleMappingTest_notify(self.jObj);
    }

    # The function that maps to the `notifyAll` method of `org.ballerinalang.bindgen.ModuleMappingTest`.
    public function notifyAll() {
        org_ballerinalang_bindgen_ModuleMappingTest_notifyAll(self.jObj);
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.ModuleMappingTest`.
    #
    # + return - The `javalang:InterruptedException` value returning from the Java mapping.
    public function 'wait() returns javalang:InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_ModuleMappingTest_wait(self.jObj);
        if (externalObj is error) {
            javalang:InterruptedException e = error javalang:InterruptedException(javalang:INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.ModuleMappingTest`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `javalang:InterruptedException` value returning from the Java mapping.
    public function wait2(int arg0) returns javalang:InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_ModuleMappingTest_wait2(self.jObj, arg0);
        if (externalObj is error) {
            javalang:InterruptedException e = error javalang:InterruptedException(javalang:INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.ModuleMappingTest`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `javalang:InterruptedException` value returning from the Java mapping.
    public function wait3(int arg0, int arg1) returns javalang:InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_ModuleMappingTest_wait3(self.jObj, arg0, arg1);
        if (externalObj is error) {
            javalang:InterruptedException e = error javalang:InterruptedException(javalang:INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

}

# The constructor function to generate an object of `org.ballerinalang.bindgen.ModuleMappingTest`.
#
# + return - The new `ModuleMappingTest` class generated.
public function newModuleMappingTest1() returns ModuleMappingTest {
    handle externalObj = org_ballerinalang_bindgen_ModuleMappingTest_newModuleMappingTest1();
    ModuleMappingTest newObj = new (externalObj);
    return newObj;
}

# The function that retrieves the value of the public field `BUILD_AWARE_ORDER`.
#
# + return - The `Comparator` value of the field.
public function ModuleMappingTest_getBUILD_AWARE_ORDER() returns javautil:Comparator {
    handle externalObj = org_ballerinalang_bindgen_ModuleMappingTest_getBUILD_AWARE_ORDER();
    javautil:Comparator newObj = new (externalObj);
    return newObj;
}

function org_ballerinalang_bindgen_ModuleMappingTest_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "org.ballerinalang.bindgen.ModuleMappingTest",
    paramTypes: ["java.lang.Object"]
} external;

function org_ballerinalang_bindgen_ModuleMappingTest_getClass(handle receiver) returns handle = @java:Method {
    name: "getClass",
    'class: "org.ballerinalang.bindgen.ModuleMappingTest",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ModuleMappingTest_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "org.ballerinalang.bindgen.ModuleMappingTest",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ModuleMappingTest_notify(handle receiver) = @java:Method {
    name: "notify",
    'class: "org.ballerinalang.bindgen.ModuleMappingTest",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ModuleMappingTest_notifyAll(handle receiver) = @java:Method {
    name: "notifyAll",
    'class: "org.ballerinalang.bindgen.ModuleMappingTest",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ModuleMappingTest_wait(handle receiver) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.ModuleMappingTest",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_ModuleMappingTest_wait2(handle receiver, int arg0) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.ModuleMappingTest",
    paramTypes: ["long"]
} external;

function org_ballerinalang_bindgen_ModuleMappingTest_wait3(handle receiver, int arg0, int arg1) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.ModuleMappingTest",
    paramTypes: ["long", "int"]
} external;

function org_ballerinalang_bindgen_ModuleMappingTest_getBUILD_AWARE_ORDER() returns handle = @java:FieldGet {
    name: "BUILD_AWARE_ORDER",
    'class: "org.ballerinalang.bindgen.ModuleMappingTest"
} external;

function org_ballerinalang_bindgen_ModuleMappingTest_newModuleMappingTest1() returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.ModuleMappingTest",
    paramTypes: []
} external;

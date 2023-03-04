import ballerina/jballerina.java;

# Ballerina class mapping for the Java `java.lang.Character$Subset` class.
@java:Binding {'class: "java.lang.Character$Subset"}
distinct class Subset {

    *java:JObject;
    *Object;

    # The `handle` field that stores the reference to the `java.lang.Character$Subset` object.
    public handle jObj;

    # The init function of the Ballerina class mapping the `java.lang.Character$Subset` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string representation of the Ballerina class mapping the `java.lang.Character$Subset` Java class.
    #
    # + return - The `string` form of the Java object instance.
    function toString() returns string {
        return java:toString(self.jObj) ?: "null";
    }
    # The function that maps to the `equals` method of `java.lang.Character$Subset`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function 'equals(Object arg0) returns boolean {
        return java_lang_Character_Subset_equals(self.jObj, arg0.jObj);
    }

    # The function that maps to the `getClass` method of `java.lang.Character$Subset`.
    #
    # + return - The `Class` value returning from the Java mapping.
    function getClass() returns Class {
        handle externalObj = java_lang_Character_Subset_getClass(self.jObj);
        Class newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `hashCode` method of `java.lang.Character$Subset`.
    #
    # + return - The `int` value returning from the Java mapping.
    function hashCode() returns int {
        return java_lang_Character_Subset_hashCode(self.jObj);
    }

    # The function that maps to the `notify` method of `java.lang.Character$Subset`.
    function notify() {
        java_lang_Character_Subset_notify(self.jObj);
    }

    # The function that maps to the `notifyAll` method of `java.lang.Character$Subset`.
    function notifyAll() {
        java_lang_Character_Subset_notifyAll(self.jObj);
    }

    # The function that maps to the `wait` method of `java.lang.Character$Subset`.
    #
    # + return - The `InterruptedException` value returning from the Java mapping.
    function 'wait() returns InterruptedException? {
        error|() externalObj = java_lang_Character_Subset_wait(self.jObj);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `java.lang.Character$Subset`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait2(int arg0) returns InterruptedException? {
        error|() externalObj = java_lang_Character_Subset_wait2(self.jObj, arg0);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `java.lang.Character$Subset`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait3(int arg0, int arg1) returns InterruptedException? {
        error|() externalObj = java_lang_Character_Subset_wait3(self.jObj, arg0, arg1);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

}

function java_lang_Character_Subset_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "java.lang.Character$Subset",
    paramTypes: ["java.lang.Object"]
} external;

function java_lang_Character_Subset_getClass(handle receiver) returns handle = @java:Method {
    name: "getClass",
    'class: "java.lang.Character$Subset",
    paramTypes: []
} external;

function java_lang_Character_Subset_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "java.lang.Character$Subset",
    paramTypes: []
} external;

function java_lang_Character_Subset_notify(handle receiver) = @java:Method {
    name: "notify",
    'class: "java.lang.Character$Subset",
    paramTypes: []
} external;

function java_lang_Character_Subset_notifyAll(handle receiver) = @java:Method {
    name: "notifyAll",
    'class: "java.lang.Character$Subset",
    paramTypes: []
} external;

function java_lang_Character_Subset_wait(handle receiver) returns error? = @java:Method {
    name: "wait",
    'class: "java.lang.Character$Subset",
    paramTypes: []
} external;

function java_lang_Character_Subset_wait2(handle receiver, int arg0) returns error? = @java:Method {
    name: "wait",
    'class: "java.lang.Character$Subset",
    paramTypes: ["long"]
} external;

function java_lang_Character_Subset_wait3(handle receiver, int arg0, int arg1) returns error? = @java:Method {
    name: "wait",
    'class: "java.lang.Character$Subset",
    paramTypes: ["long", "int"]
} external;

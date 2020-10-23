import ballerina/java;

type SType service object {
    resource string message;

    remote function foo(int i) returns int;
};

service class SClass {
    *SType;

    remote function foo(int i) returns int {
        return i + 100;
    }

    resource function get barPath() returns string {
        return "Hello";
    }

    function init() {
        self.message = "Hello";
    }
}

function testServiceObjectValue() {
    SType s = new SClass();
    assertEquality(s.message, "Hello");
    var x = callFoo(s);
    assertEquality(x, 101);
}


public function callFoo(service object {} s) returns int  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name:"callFoo"
} external;

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("AssertionError",
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
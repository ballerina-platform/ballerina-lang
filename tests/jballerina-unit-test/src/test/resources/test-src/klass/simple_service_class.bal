import ballerina/java;

type SType service object {
    string message;

    remote function foo(int i) returns int;
};

service class SClass {
    *SType;

    remote function foo(int i) returns int {
        return i + 100;
    }

    resource function get barPath() returns string {
        return self.message;
    }

    resource function get foo/path() returns string {
        return self.message + "foo";
    }

    resource function get .() returns string {
        return self.message + "dot";
    }

    function init() {
        self.message = "returned from `barPath`";
    }
}

function testServiceObjectValue() {
    SType s = new SClass();
    assertEquality(s.message, "returned from `barPath`");
    var x = wait callMethod(s, "$get$barPath");
    assertEquality(x, s.message);

    var y = wait callMethod(s, "$get$foo$path");
    assertEquality(y, s.message + "foo");

    var z = wait callMethod(s, "$get$.");
    assertEquality(z, s.message + "dot");
}


public function callMethod(service object {} s, string name) returns future<any|error>  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name:"callMethod"
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
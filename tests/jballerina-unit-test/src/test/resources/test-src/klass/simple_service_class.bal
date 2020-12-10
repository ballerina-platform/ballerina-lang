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

    resource function get foo/baz(string s) returns string {
        return s;
    }

    resource function get foo/[int i]() returns int {
        return i;
    }

    resource function get foo/[string s]/[string... r]() returns string {
        string result = s + ", ";
        foreach string rdash in r {
            result += rdash;
        }
        return result;
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

    // "$get$." is encorded into "$gen$$get$$0046"
    var z = wait callMethod(s, "$gen$$get$$0046");
    assertEquality(z, s.message + "dot");

    var rParamVal0 = wait callMethodWithParams(s, "$get$foo$*", [1]);
    assertEquality(rParamVal0, 1);

    any[] ar = ["hey", ["hello", " ", "world", "!"]];
    var rParamVal1 = wait callMethodWithParams(s, "$get$foo$*$**", ar);
    assertEquality(rParamVal1, "hey, hello world!");
}


public function callMethod(service object {} s, string name) returns future<any|error>  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name:"callMethod"
} external;

public function callMethodWithParams(service object {} s, string name, (any|error)[] ar) returns future<any|error>  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name:"callMethodWithParams"
} external;

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("AssertionError",
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

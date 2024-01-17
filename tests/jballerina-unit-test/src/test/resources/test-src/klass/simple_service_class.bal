import ballerina/jballerina.java;

type SType service object {
    string message;
    Person[] people;

    remote function foo(int i) returns int;
};

public class Person {
    final string name;
    final int age;

    function init(string name, int age) {
        self.name = name;
        self.age = age;
    }
}

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

    resource function get foo/zee(int q, int i = 0, string k = "", json j = {}) {

    }

    resource function get profile(string name) returns Person[] {
        return self.people.filter(p => p.name.includes(name));
    }

    function init() {
        self.message = "returned from `barPath`";
        self.people = [];
        self.people.push(new Person("John Doe", 10));
        self.people.push(new Person("Jane Doe", 20));
        self.people.push(new Person("Will Smith", 30));
        self.people.push(new Person("Steve Smith", 40));
    }
}

function testServiceObjectValue() {
    SType s = new SClass();
    assertEquality(s.message, "returned from `barPath`");
    var x = wait callMethod(s, "$get$barPath");
    assertEquality(x, s.message);

    var y = wait callMethod(s, "$get$foo$path");
    assertEquality(y, s.message + "foo");

    // "$get$." is encoded into "$gen$$get$&0046"
    var z = wait callMethod(s, "$gen$$get$&0046");
    assertEquality(z, s.message + "dot");

    var rParamVal0 = wait callMethodWithParams(s, "$get$foo$^", [1]);
    assertEquality(rParamVal0, 1);

    any[] ar = ["hey", ["hello", " ", "world", "!"]];
    var rParamVal1 = wait callMethodWithParams(s, "$get$foo$^$^^", ar);
    assertEquality(rParamVal1, "hey, hello world!");

    boolean[] paramDefaultability = <boolean[]> getParamDefaultability(s, "$get$foo$zee");
    boolean[] d = [false, true, true, true];
    assertEquality(paramDefaultability, d);

    var persons = wait callMethodWithParams(s, "$get$profile", ["Will Smith"]);
    if (persons is Person[]) {
        Person[] personArray = <Person[]> persons;
        assertEquality(personArray[0].age, 30);
    } else {
        panic error("AssertionError", message = "no profiles returned");
    }
}


public function callMethod(service object {} s, string name) returns future<any|error>  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name:"callMethod"
} external;

public function callMethodWithParams(service object {} s, string name, (any|error)[] ar) returns future<any|error>  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name:"callMethodWithParams"
} external;

public function getParamDefaultability(service object {} s, string name) returns boolean[]  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name:"getParamDefaultability"
} external;

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError",
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

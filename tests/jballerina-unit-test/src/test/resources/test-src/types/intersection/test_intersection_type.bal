// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

function testIntersectionTypes() {
    testIntersectionWithMemberTypeDefinedAfter();
    testIntersectionInUnion();
    testComplexIntersectionsInUnions();
    testTypeCheckingAgainstEffectiveType1();
    testTypeCheckingAgainstEffectiveType2();
}

type ReadOnlyFoo readonly & Foo;

function testIntersectionWithMemberTypeDefinedAfter() {
    ReadOnlyFoo rf = {i: 100, "j": "hello world"};
    Foo f = rf;
    assertTrue(f is Foo & readonly);
    assertTrue(f is ReadOnlyFoo);
    assertEquality(<Foo> {i: 100, "j": "hello world"}, f);

    var fn = function () {
        f.i = 200;
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality("cannot update 'readonly' field 'i' in record of type 'ReadOnlyFoo'", err.detail()["message"]);
}

type Foo record {
    int i;
};

function testIntersectionInUnion() {
    ReadOnlyFoo? a = ();
    Foo? & readonly b = {i: 100};

    ReadOnlyFoo? c = b;
    Foo? & readonly d = a;

    anydata ad1 = c;
    anydata ad2 = d;

    assertTrue(c.isReadOnly());
    assertTrue(d.isReadOnly());
    assertTrue(ad1.isReadOnly());
    assertTrue(ad2.isReadOnly());
    assertEquality(<Foo> {i: 100}, c);
    assertEquality((), d);
}

type InboundHandler object {
    public function canProcess(int req) returns boolean;
    boolean enabled;
};

type IntermediateInboundHandler object {
    *InboundHandler;
    int allow;
};

readonly class InboundHandlerImpl {
    *IntermediateInboundHandler;
    final int count;

    public function init(int allow, boolean enabled = false) {
        self.enabled = enabled;
        self.allow = allow;
        self.count = allow * 2;
    }

    public function canProcess(int req) returns boolean {
        return self.enabled && req < self.allow;
    }
}

type InboundHandlers InboundHandler[]|InboundHandler[][];

type Handlers record {|
    (InboundHandlers & readonly)? handlers;
|};

type NonImmutableHandlers record {|
    InboundHandlers|int? handlers?;
|};

function testComplexIntersectionsInUnions() {
    InboundHandler[] & readonly arr = [new InboundHandlerImpl(10, true), new InboundHandlerImpl(20)];

    Handlers h1 = {handlers: arr};
    NonImmutableHandlers h2 = h1;

    Handlers & readonly h3 = {handlers: arr};
    NonImmutableHandlers & readonly h4 = h3;

    assertTrue(h2?.handlers is InboundHandlers & readonly);
    assertTrue(h2?.handlers is readonly);

    InboundHandlers hds = <InboundHandlers> h2?.handlers;
    InboundHandler[] hdArray = <InboundHandler[]> hds;

    assertTrue(hdArray[0].canProcess(5));
    assertFalse(hdArray[0].canProcess(12));
    assertFalse(hdArray[1].canProcess(10));
}

function testTypeCheckingAgainstEffectiveType1() {
    readonly & json a = null;
    assertEquality((), a);

    map<json> & readonly b = {
        a: 1,
        b: "foo",
        c: null,
        d: {
            a1: null,
            b1: true
        }
    };
    assertEquality(1, b["a"]);
    assertEquality("foo", b["b"]);
    assertEquality((), b["c"]);

    assertTrue(b["d"] is map<json> & readonly);

    map<json> bd = <map<json> & readonly> b["d"];
    assertEquality((), bd["a1"]);
    assertTrue(bd["b1"]);
}

const BAR = "bar";

type Type "foo"|decimal|BAR;

function testTypeCheckingAgainstEffectiveType2() {
    Type[] & readonly farr = ["foo", 1.0, BAR, "bar"];
    assertEquality(4, farr.length());
    assertEquality("foo", farr[0]);
    assertEquality(1.0d, farr[1]);
    assertEquality(BAR, farr[2]);
    assertEquality("bar", farr[3]);
}
public type TableMetadata record {|
    isolated function () returns string query;
    isolated function (string) returns string queryOne;
|};

public isolated client class InMemoryClient {
    private final (isolated function () returns string) & readonly query;
    private final (isolated function (string) returns string) & readonly queryOne;

    public isolated function init(TableMetadata & readonly metadata) {
        self.query = metadata.query;
        self.queryOne = metadata.queryOne;
    }

    public isolated function runReadQuery() returns string {
        return self.query();
    }

    public isolated function runReadByKeyQuery(string key) returns string {
        return self.queryOne(key);
    }
}

function testIsolatedFunctionReadonlyIntersection() {
// This test is to test isolated qualifier in intersection type flags
    TableMetadata & readonly tableMetadata = {
        query: isolated function () returns string => "query",
        queryOne: isolated function (string key) returns string => key
    };

    InMemoryClient myClient = new InMemoryClient(tableMetadata);

    assertEquality(myClient.runReadQuery(), "query");
    assertEquality(myClient.runReadByKeyQuery("1"), "1");
}

function testIntersectionWithUnionEffectiveTypeAsAMemberOfAUnion() {
    (readonly & ([string]|int))? a = ["Ballerina"];
    assertEquality(<[string]>["Ballerina"], a);

    (readonly & ([string]|[int])|int)? b = ["Ballerina"];
    assertEquality(<[string]>["Ballerina"], b);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON, message = "expected '" + expectedValAsString + "', found '" +
                                                        actualValAsString + "'");
}

type Y readonly & record { int i; };

type Z record {
    Y y;
};

public function testReadOnlyIntersectionFieldInRecord() {
    readonly & record { int i; } y = {i: 10};
    Z z = {y};
    assertEquality(z.y, y);
}

type Atom readonly & object {
    function compare(Atom other);
};

readonly class AtomImpl {
    *Atom;

    function compare(Atom other) {
        if (self === other) {
            panic error("Same", message = "Both are the same");
        }
    }
}

function testRecursiveReadonlyIntersection() {
    Atom a = new AtomImpl();
    error? e = trap a.compare(a);
    assertError(e, "Same", "Both are the same");
}

function testRuntimeTypeNameOfIntersectionType() {
    any a = new AtomImpl();
    error|int r = trap (<int> a);
    assertError(r, "{ballerina}TypeCastError", "incompatible types: 'AtomImpl' cannot be cast to 'int'");
}

type Error error<record { string message; }>;

type ReadonlyTypeDef1 readonly;
type ReadonlyTypeDef2 readonly;

type ObjectType object {
    int atb1;
    function call() returns int;
};

type ReadonlyRecordIntersectionType1 ReadonlyTypeDef1 & record {|
    int a;
|};

type FooType record {|
    int a;
|};

type BarType record {|
    byte a;
|};

type ReadonlyRecordIntersectionType2 ReadonlyTypeDef1 & FooType;
type ReadonlyRecordIntersectionType3 FooType & ReadonlyTypeDef1;
type ReadonlyObjectIntersectionType1 ReadonlyTypeDef1 & ObjectType;
type ReadonlyObjectIntersectionType2 ObjectType & ReadonlyTypeDef1;
type IntersectionWithMultipleReadonlyType1 ReadonlyTypeDef1 & ReadonlyTypeDef2 & FooType;
type IntersectionWithMultipleReadonlyType2 ReadonlyTypeDef1 & FooType & ReadonlyTypeDef2;
type ReadonlyErrorIntersectionType ReadonlyTypeDef1 & error<FooType> & error<BarType>;

function testReadonlyIntersection() {
    ReadonlyRecordIntersectionType1 foo1 = {a: 1};
    assertTrue(<any>foo1 is readonly);

    ReadonlyRecordIntersectionType2 foo2 = {a: 1};
    assertTrue(<any>foo2 is readonly);

    ReadonlyRecordIntersectionType3 foo3 = {a: 1};
    assertTrue(<any>foo3 is readonly);

    ReadonlyObjectIntersectionType1 obj1 = object {
        int atb1 = 1;
        function call() returns int => 1;
    };
    assertTrue(<any>obj1 is readonly);

    ReadonlyObjectIntersectionType2 obj2 = object {
        int atb1 = 1;
        function call() returns int => 1;
    };
    assertTrue(<any>obj2 is readonly);

    IntersectionWithMultipleReadonlyType1 foo4 = {a: 1};
    assertTrue(<any>foo4 is readonly);

    IntersectionWithMultipleReadonlyType2 foo5 = {a: 1};
    assertTrue(<any>foo5 is readonly);

    ReadonlyErrorIntersectionType err = error("Error", a = 12);
    assertTrue(<any|error>err is readonly);
    assertTrue(<any>err.detail().a is byte);

    ReadonlyTypeDef1 & string value1 = "abc";
    assertTrue(<any>value1 is readonly);

    ReadonlyTypeDef1 & string[] value2 = ["1", "2", "3"];
    assertTrue(<any>value2 is readonly);
    assertTrue(<any>value2 is ReadonlyTypeDef1 & string[]);

    ReadonlyTypeDef1 & map<string> value3 = {"a": "1", "b": "2"};
    assertTrue(<any>value3 is readonly);
    assertTrue(<any>value3 is ReadonlyTypeDef1 & map<string>);

    ReadonlyTypeDef1 & ReadonlyTypeDef2 & map<string> value4 = {"a": "1", "b": "2"};
    assertTrue(<any>value4 is readonly);

    string[] value5 = ["1", "2", "3"];
    assertFalse(<any>value4 is ReadonlyTypeDef1 & string[]);
}

function assertError(any|error value, string errorMessage, string expDetailMessage) {
    if value is Error {
        if (value.message() != errorMessage) {
            panic error("Expected error message: " + errorMessage + " found: " + value.message());
        }

        if value.detail().message == expDetailMessage {
            return;
        }
        panic error("Expected error detail message: " + expDetailMessage + " found: " + value.detail().message);
    }
    panic error("Expected: Error, found: " + (typeof value).toString());
}

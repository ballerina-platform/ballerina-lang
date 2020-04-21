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

import ballerina/java;

// readonly-type-descriptor := readonly
// A shape belongs to the type readonly if its read-only bit is on.
//
// A value belonging to an inherently immutable basic type will always have its read-only bit on. These basic types are:
//
// - all simple types
//   - nil
//   - boolean
//   - int
//   - float
//   - decimal
// - string
// - error
// - function
// - service
// - typedesc
// - handle
//
// A value belonging to a selectively immutable basic type may have its read-only bit on. These basic types are:
//
// - xml
// - list
// - mapping
// - table

function testReadonlyType() {
    testSimpleAssignmentForInherentlyImmutableBasicTypes();
    testRuntimeIsTypeForInherentlyImmutableBasicTypes();
    testRuntimeIsTypeForSelectivelyImmutableBasicTypes();
    testRuntimeIsTypeForNeverImmutableBasicTypes();
}

function testSimpleAssignmentForInherentlyImmutableBasicTypes() {
    readonly a = ();
    assertTrue(a is ());

    readonly b = true;
    assertTrue(b);
    readonly c = false;
    assertFalse(c);

    readonly d = 123;
    assertTrue(d is int);
    assertEquality(123, d);

    readonly e = -124.0f;
    assertTrue(e is float);
    assertEquality(-124.0, e);

    readonly f = 34.23d;
    assertTrue(f is decimal);
    assertEquality(34.23d, f);

    readonly g = "str value";
    assertTrue(g is string);
    assertEquality("str value", g);

    error err = error("Reason", message = "error message");
    readonly h = err;
    assertTrue(h is error);
    error errorVal = <error> h;
    assertEquality("Reason", errorVal.reason());
    assertEquality("error message", errorVal.detail()?.message);

    error myError = AssertionError(message = "second error message");
    readonly i = myError;
    assertTrue(i is error);
    errorVal = <error> i;
    assertEquality(ASSERTION_ERROR_REASON, errorVal.reason());
    assertEquality("second error message", errorVal.detail()?.message);

    readonly j = assertTrue;
    assertTrue(j is function (any|error actual));
    function (any|error actual) trueFunc = <function (any|error actual)> j;
    trueFunc(true);

    service ser = service {
        resource function res() {

        }
    };
    readonly k = ser;
    assertTrue(k is service);
    assertEquality(ser, k);

    Employee employee = {name: "Jo"};
    typedesc<any> typeDesc = typeof Employee;
    readonly l = typeDesc;
    assertTrue(l is typedesc<any>);
    assertEquality(typeDesc, l);

    handle handleVal = java:fromString("val");
    readonly m = handleVal;
    assertTrue(m is handle);
    assertEquality(handleVal, m);
}

function testRuntimeIsTypeForInherentlyImmutableBasicTypes() {
    any a = ();
    assertTrue(a is readonly);

    any b = true;
    assertTrue(b is readonly);

    any c = false;
    assertTrue(c is readonly);

    any d = 123;
    assertTrue(d is readonly);

    any e = -124.0f;
    assertTrue(e is readonly);

    any f = 34.23d;
    assertTrue(f is readonly);

    any g = "str value";
    assertTrue(g is readonly);

    error err = error("Reason", message = "error message");
    any|error h = err;
    assertTrue(h is readonly);

    error myError = AssertionError(message = "second error message");
    any|error i = myError;
    assertTrue(i is readonly);

    any j = assertTrue;
    assertTrue(j is readonly);

    service ser = service {
        resource function res() {

        }
    };
    any k = ser;
    assertTrue(k is readonly);

    Employee employee = {name: "Jo"};
    typedesc<any> typeDesc = typeof Employee;
    any l = typeDesc;
    assertTrue(l is readonly);

    handle handleVal = java:fromString("val");
    any m = handleVal;
    assertTrue(m is readonly);
}

function testRuntimeIsTypeForSelectivelyImmutableBasicTypes() {
    xml a = xml `<foo><bar>Text</bar></foo>`;
    any b = a;
    any c = a.cloneReadOnly();
    assertFalse(b is readonly);
    assertTrue(c is readonly);

    int[] d = [1, 2];
    any e = d;
    any f = d.cloneReadOnly();
    assertFalse(e is readonly);
    assertTrue(f is readonly);

    [boolean, int] g = [true, 2];
    any h = g;
    any i = g.cloneReadOnly();
    assertFalse(h is readonly);
    assertTrue(i is readonly);

    map<string> j = {a: "a", b: "b"};
    any k = j;
    any l = j.cloneReadOnly();
    assertFalse(k is readonly);
    assertTrue(l is readonly);

    Employee m = {name: "Maryam"};
    any n = m;
    any o = m.cloneReadOnly();
    assertFalse(n is readonly);
    assertTrue(o is readonly);

    // TODO: table.
}

function testRuntimeIsTypeForNeverImmutableBasicTypes() {
    Student student = new;
    any a = student;
    assertFalse(a is readonly);

    int[] arr = [1, 2];
    any b = arr.toStream();
    assertFalse(b is readonly);
}

type AssertionError error<ASSERTION_ERROR_REASON>;

type Employee record {
    string name;
    string department = "finance";
};

type Student object {
    string name = "Maryam";

    function getName() returns string {
        return self.name;
    }
};

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

    panic AssertionError(message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

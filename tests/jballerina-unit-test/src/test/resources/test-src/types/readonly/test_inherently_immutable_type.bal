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
import ballerina/lang.'xml;

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
    assertEquality("Reason", errorVal.message());
    assertEquality("error message", errorVal.detail()["message"]);

    error myError = AssertionError(ASSERTION_ERROR_REASON, message = "second error message");
    readonly i = myError;
    assertTrue(i is error);
    errorVal = <error> i;
    assertEquality(ASSERTION_ERROR_REASON, errorVal.message());
    assertEquality("second error message", errorVal.detail()["message"]);

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

    'xml:Text xmlText = xml `xml text`;
    readonly n = xmlText;
    assertTrue(n is 'xml:Text);
    assertEquality(n, xml `xml text`);
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

    error myError = AssertionError(ASSERTION_ERROR_REASON, message = "second error message");
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

    'xml:Text xmlText = xml `xml text`;
    any n = xmlText;
    assertTrue(n is readonly);
}

function testRuntimeIsTypeForNeverImmutableBasicTypes() {
    Student student = new;
    any a = student;
    assertFalse(a is readonly);

    int[] arr = [1, 2];
    any b = arr.toStream();
    assertFalse(b is readonly);
}

type AssertionError distinct error;

type Employee record {
    string name;
    string department = "finance";
};

class Student {
    string name = "Maryam";

    function getName() returns string {
        return self.name;
    }
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

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

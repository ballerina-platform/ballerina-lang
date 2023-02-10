// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Foo record {|
    Bar x?;
|};

type Bar record {|
    *Foo;
|};

function testCyclicInclusionViaFieldWithTypeReferenceTypes() {
    Foo f = {};
    assertEquals(0, f.length());

    f.x = f;
    assertEquals(1, f.length());

    Foo g = {x: f};
    assertEquals(1, g.length());

    Foo? x = g.x;
    assertTrue(x is Foo);

    Foo fx = <Foo>x;
    assertEquals(1, fx.length());

    g = {x: {x: f}};
    assertEquals(1, g.length());

    x = g.x;
    assertTrue(x is Foo);

    fx = <Foo>x;
    assertEquals(1, fx.length());

    x = fx.x;
    assertTrue(x is Foo);
    assertTrue(x is Bar);

    fx = <Foo>x;
    assertEquals(1, fx.length());
}

public type ErrorRecord record {
    string code?;
    record {
        *ErrorRecord;
    } innerError?;
};

function testCyclicInclusionViaFieldWithAnonymousTypes() {
    ErrorRecord e = {code: "NotFound"};
    assertEquals(1, e.length());

    e.innerError = {};

    var nilableInnerError = e.innerError;
    assertTrue(nilableInnerError !is ());

    var innerError = <record {
        *ErrorRecord;
    }>nilableInnerError;
    assertEquals(0, innerError.length());

    e.innerError = {innerError: e};

    nilableInnerError = e.innerError;
    assertTrue(nilableInnerError !is ());

    innerError = <record {
        *ErrorRecord;
    }>nilableInnerError;
    assertEquals(1, innerError.length());
    // assertTrue(innerError === e); // https://github.com/ballerina-platform/ballerina-lang/issues/38039
}

function testRuntimeStringRepresentationForCyclicInclusionViaFieldWithAnonymousTypes() {
    var fn = function () {
        ErrorRecord e = {innerError: {}};
        var f = e.innerError;
        _ = <int><any>f;
    };

    error? err = trap fn();
    assertTrue(err is error);

    error e = <error>err;
    assertEquals("{ballerina}TypeCastError", e.message());
    assertEquals("incompatible types: 'record {| string code?; ... innerError?; anydata...; |}' cannot be cast to 'int'",
                 <string> checkpanic e.detail()["message"]);
}

function assertTrue(anydata actual) {
    assertEquals(true, actual);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}

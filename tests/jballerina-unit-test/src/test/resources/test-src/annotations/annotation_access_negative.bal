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

type Annot record {
    string foo;
    int bar?;
};

public annotation Annot v1 on type, class;
public annotation Annot v3 on function;

string strValue = "v1 value";

@v1 {
    foo: strValue,
    bar: 1
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testRecordTypeAnnotationReadonlyValueEdit()  {
    var fn = function() {
        typedesc<any> t = typeof a;
        Annot? annot = t.@v1;
        if (annot is Annot) {
            annot.foo = "EDITED";
        }
    };

    error? res = trap fn();
    assertEquality(true, res is error);

    error resError = <error> res;
    assertEquality("{ballerina/lang.map}InvalidUpdate", resError.message());
    assertEquality("Invalid update of record field: modification not allowed on readonly value",
                       resError.detail()["message"].toString());
}

@v1 {
    foo: strValue
}
class T2 {
    string name = "ballerina";
}

function testAnnotationOnObjectTypeReadonlyValueEdit() {
     var fn = function() {
        T2 c = new;
        typedesc<any> t = typeof c;
        Annot? annot = t.@v1;
        if (annot is Annot) {
            annot.foo = "EDITED";
        }
    };

    error? res = trap fn();
    assertEquality(true, res is error);

    error resError = <error> res;
    assertEquality("{ballerina/lang.map}InvalidUpdate", resError.message());
    assertEquality("Invalid update of record field: modification not allowed on readonly value",
                       resError.detail()["message"].toString());
}

@v3 {
    foo: "func",
    bar: 1
}
function funcWithAnnots() {}

function testAnnotationOnFunctionTypeReadonlyValueEdit() {
    var fn = function() {
        typedesc<any> t = typeof funcWithAnnots;
        Annot? annot = t.@v3;
        if (annot is Annot) {
            annot.foo = "EDITED";
        }
    };

    error? res = trap fn();
    assertTrue(res is error);

    error resError = <error> res;
    assertEquality("{ballerina/lang.map}InvalidUpdate", resError.message());
    assertEquality("Invalid update of record field: modification not allowed on readonly value",
                   resError.detail()["message"].toString());
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

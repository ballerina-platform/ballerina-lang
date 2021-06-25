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

// Detail types
type Detail record {
    string x;
};

type DetailTwo record {
    string x;
    string y;
};

type DetailThree record {
    int z;
};

// Error Types
type DetailFour map<string>;

type ErrorOne error<Detail>;

type ErrorTwo error<DetailTwo>;

type ErrorThree error<DetailThree>;

type ErrorFour error<record { string z;}>;

type ErrorFive error<DetailFour>;

type DistinctErrorOne distinct ErrorOne;

type DistinctErrorThree distinct ErrorThree;

//ErrorIntersections
type IntersectionError ErrorOne & ErrorTwo;

type IntersectionErrorTwo ErrorOne & ErrorTwo & ErrorThree;

type IntersectionErrorThree ErrorOne & ErrorFour;

type IntersectionErrorFour ErrorOne & ErrorFive;

type DistinctIntersectionError distinct ErrorOne & ErrorFive;

type IntersectionOfDistinctErrors distinct DistinctErrorOne & DistinctErrorThree;

type MyDetail record {|
    string s;
|};

type Other record {|
    string s;
|};

type MyError error<MyDetail> & error<Other>;

type MyDError distinct error<MyDetail>;
type MyOError distinct error<Other>;

type MyDistinctError MyDError & MyOError;

type RecordWithIntersectionReference record {
    IntersectionErrorThree err;
};

function testIntersectionForExistingDetail() {
    var err = error IntersectionError("message", x = "x", y = "y");
    assertEquality(err.detail().x, "x");
    assertEquality(err.detail().y, "y");
}

function testIntersectionForExisitingAndNewDetail() {
    var err = error IntersectionErrorTwo("message", x = "x", y = "y", z = 10);
    assertEquality(err.detail().x, "x");
    assertEquality(err.detail().y, "y");
    assertEquality(err.detail().z, 10);
}

function testIntersectionForAnonymousDetail() {
    var err = error IntersectionErrorThree("message", x = "x", z = "z");
    assertEquality(err.detail().x, "x");
    assertEquality(err.detail().z, "z");
}

function testIntersectionForDetailRecordAndDetailMap() {
    var err = error IntersectionErrorFour("message", x = "x");
    assertEquality(err.detail().x, "x");
}

function testDistinctIntersectionType() {
    var err = getDistinctError();
    assertEquality(err.detail().x, "x");
}

function getDistinctError() returns DistinctIntersectionError {
    var err = error DistinctIntersectionError("message", x = "x");
    return err;
}

function testIntersectionOfDistinctErrors() {
    var err = error IntersectionOfDistinctErrors("message", x = "x", z = 10);

    DistinctErrorOne errOne = err;
    assertEquality(errOne.detail().x, "x");

    DistinctErrorThree errThree = err;
    assertEquality(errThree.detail().z, 10);
}

public function testIntersectionAsFieldInRecord() {
    var err = error IntersectionErrorThree("message", x = "x", z = "z");
    RecordWithIntersectionReference errRec = {err};
    assertEquality(errRec.err, err);
}

public function testIntersectionAsFieldInAnonymousRecord() {
    var err = error IntersectionErrorThree("message", x = "x", z = "z");
    RecordWithIntersectionReference errRec = getAnonymousRecord(err);
    assertEquality(errRec.err, err);
}

type IntersectionOfInlineErrorOne DistinctErrorOne & error<map<string>>;
type IntersectionOfInlineErrorTwo DistinctErrorOne & error<Detail>;
type IntersectionOfInlineErrorThree DistinctErrorOne & error<DetailFour>;

type IntersectionOfDistinctAndInlineErrorOne distinct DistinctErrorOne & error<map<string>>;
type IntersectionOfDistinctAndInlineErrorTwo distinct DistinctErrorOne & error<Detail>;
type IntersectionOfDistinctAndInlineErrorThree distinct DistinctErrorOne & error<DetailFour>;

public function testIntersectionOfErrorWithInlineError() {
    IntersectionOfInlineErrorOne eOne = error IntersectionOfInlineErrorOne("eOne", x = "ex");
    assertEquality(eOne.message(), "eOne");
    assertEquality(eOne.detail().x, "ex");

    IntersectionOfInlineErrorTwo eTwo = error IntersectionOfInlineErrorTwo("eTwo", x = "ex");
    assertEquality(eTwo.message(), "eTwo");
    assertEquality(eTwo.detail().x, "ex");

    IntersectionOfInlineErrorThree eThree = error IntersectionOfInlineErrorThree("eThree", x = "ex");
    assertEquality(eThree.message(), "eThree");
    assertEquality(eThree.detail().x, "ex");

    IntersectionOfInlineErrorOne eOneDash = eThree;
    assertEquality(eOneDash.message(), "eThree");

    var dErrorOne = error IntersectionOfDistinctAndInlineErrorOne("eOne", x = "ex");
    assertEquality(dErrorOne.message(), "eOne");
    assertEquality(dErrorOne.detail().x, "ex");

    DistinctErrorOne dOneDash = dErrorOne;
    assertEquality(dOneDash.message(), "eOne");
    assertEquality(dOneDash.detail().x, "ex");

    DistinctErrorOne dErrorTwo = error IntersectionOfDistinctAndInlineErrorTwo("eTwo", x = "ex");
    assertEquality(dErrorTwo.message(), "eTwo");
    assertEquality(dErrorTwo.detail().x, "ex");

    DistinctErrorOne dErrorThree = error IntersectionOfDistinctAndInlineErrorThree("eThree", x = "ex");
    assertEquality(dErrorThree.message(), "eThree");
    assertEquality(dErrorThree.detail().x, "ex");
}

function getAnonymousRecord(IntersectionErrorThree err) returns record {IntersectionErrorThree err;} {
    record {IntersectionErrorThree err;} errRec = {err};
    return errRec;
}

type JsonParseDetail record {
    string s;
};

type JsonParseError error<JsonParseDetail> & distinct error;

function testAnonDistinctError() {
    error e = error JsonParseError("msg", s = "the ling info");
    if !(e is JsonParseError) {
        panic error("Assertion error");
    }
}

function testIntersectionOfSameSetOfErrorShapes() {
    MyDistinctError d = error("d", s = "s");

    MyDError de = d;
    assertEquality(de.message(), "d");
    assertEquality(de.detail().s, "s");

    MyOError oe = d;
    assertEquality(oe.message(), "d");
    assertEquality(oe.detail().s, "s");

    MyError m = error("m", s = "s");
    assertEquality(m.message(), "m");
    assertEquality(m.detail().s, "s");
}

type FreeError distinct error;

type E1 distinct FreeError;

type E2 FreeError & error<Detail>;

public function testDistinctErrorWithSameTypeIdsButDifferentTypes() {
    E1 x = error E1("");
    // E1 and E2 have matching type-ids.
    assertEquality(x is E2, false);
}

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

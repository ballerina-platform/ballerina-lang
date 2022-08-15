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

function testBasicDataTypeMaps() {
    map<int> mInt = {"a": 1, "b": 2};
    record {| int...; |} rInt = mInt;
    rInt["c"] = 3;
    assert(<record {| int...; |}>{"a": 1, "b": 2, "c": 3}, rInt);

    map<float> mFloat = {"a": 1.2, "b": 2.3};
    record {| float...; |} rFloat = mFloat;
    rFloat["c"] = 3.4;
    assert(<record {| float...; |}>{"a": 1.2, "b": 2.3, "c": 3.4}, rFloat);

    map<decimal> mDecimal = {"a": 1.2, "b": 2.3};
    record {| decimal...; |} rDecimal = mDecimal;
    rDecimal["c"] = 3.4;
    assert(<record {| decimal...; |}>{"a": 1.2, "b": 2.3, "c": 3.4}, rDecimal);

    map<boolean> mBoolean = {"a": true, "b": false};
    record {| boolean...; |} rBoolean = mBoolean;
    rBoolean["c"] = true;
    assert(<record {| boolean...; |}>{"a": true, "b": false, "c": true}, rBoolean);

    map<string> mString = {"a": "foo", "b": "bar"};
    record {| string...; |} rString = mString;
    rString["c"] = "baz";
    assert(<record {| string...; |}>{"a": "foo", "b": "bar", "c": "baz"}, rString);

    map<json> mJson = {"a": {"foo": "bar"}};
    record {| json...; |} rJson = mJson;
    rJson["b"] = {"baz": "qux"};
    assert(<record {| json...; |}>{"a": {"foo": "bar"}, "b": {"baz": "qux"}}, rJson);

    map<xml> mXml = {"a": xml `<book>The Lost World</book>`};
    record {| xml...; |} rXml = mXml;
    rXml["b"] = xml `<book>Sherlock Holmes</book>`;
    assert(<record {| xml...; |}>{"a": xml `<book>The Lost World</book>`, "b": xml `<book>Sherlock Holmes</book>`},
           rXml);
}

function testInclusiveRecordTypes() {
    map<int> mInt = {"a": 1, "b": 2};
    record {} rInt = mInt;
    rInt["c"] = 3;
    assert(<record {}>{"a": 1, "b": 2, "c": 3}, rInt);

    map<float> mFloat = {"a": 1.2, "b": 2.3};
    record {} rFloat = mFloat;
    rFloat["c"] = 3.4;
    assert(<record {}>{"a": 1.2, "b": 2.3, "c": 3.4}, rFloat);

    map<decimal> mDecimal = {"a": 1.2d, "b": 2.3d};
    record {} rDecimal = mDecimal;
    rDecimal["c"] = 3.4d;
    assert(<record {}>{"a": 1.2d, "b": 2.3d, "c": 3.4d}, rDecimal);

    map<boolean> mBoolean = {"a": true, "b": false};
    record {} rBoolean = mBoolean;
    rBoolean["c"] = true;
    assert(<record {}>{"a": true, "b": false, "c": true}, rBoolean);

    map<string> mString = {"a": "foo", "b": "bar"};
    record {} rString = mString;
    rString["c"] = "baz";
    assert(<record {}>{"a": "foo", "b": "bar", "c": "baz"}, rString);

    map<json> mJson = {"a": {"foo": "bar"}};
    record {} rJson = mJson;
    rJson["b"] = <json>{"baz": "qux"};
    assert(<record {}>{"a": {"foo": "bar"}, "b": {"baz": "qux"}}, rJson);

    map<xml> mXml = {"a": xml `<book>The Lost World</book>`};
    record {} rXml = mXml;
    rXml["b"] = xml `<book>Sherlock Holmes</book>`;
    assert(<record {}>{"a": xml `<book>The Lost World</book>`, "b": xml `<book>Sherlock Holmes</book>`},
           rXml);
}

function testInherentTypeViolationInInclusiveRecords() {
    map<decimal> mDecimal = {"a": 1.2, "b": 2.3};
    record {} rDecimal = mDecimal;
    rDecimal["c"] = 3.4; // this panics
}

function testInherentTypeViolationInExclusiveRecords() {
    map<int> mInt = {"a": 1, "b": 2};
    record {| (int|string)...; |} r = mInt;
    r["c"] = "foo"; // this panics
}

function testRecordsWithOptionalFields() {
    map<string> m = {"a":"aaa", "b":"bbb"};

    record {|
        string a?;
        string c?;
        string...;
    |} r = m;

    assert("aaa", r?.a);
    assert((), r?.c);

    r.a = "AA";
    r.c = "CC";

    assert(<record {| string a?; string c?; string...; |}>{"a": "AA", "b": "bbb", "c": "CC"}, r);
}

type Foo record {|
    int a;
    int...;
|};

type Bar record {|
    int a;
|};

function testSubtyping() {
    map<Bar> m1 = {"bb": {a: 10}};

    record {|
        Foo...;
    |} r1 = m1;

    Foo f1 = r1.get("bb");

    assert(<Foo>{a: 10}, f1);
    assert(<Bar>{a: 10}, f1);

    f1["cc"] = 20; // this panics
}

type Baz record {|
    *Bar;
    int b?;
    int...;
|};

type SuperTypeOfAll record {|
    int...;
|};

// Bar is a subtype of Foo
map<Bar> gM = {"bb": {a: 10}, "x": {a: 20}};

record {|
    SuperTypeOfAll x?;
    Foo...;
|} gR = gM;

function testComplexSubtyping() {
    // Fails since although the literal {a:10} is a valid value, its type is Bar.
    Baz b1 = <Baz>gR.get("x");
}

function testComplexSubtyping2() {
    record {| int c; |} rC = {c: 50};

    // Compile time allowed since rC's type is a subtype of SuperTypeOfAll. But fails at run time since gR is actually
    // a Bar map value.
    gR.x = rC;
}

type QuotedFieldNameWithEscapeChars record {|
    int 'd\.\ \@\#d;
|};

type QuotedSubFieldNameWithEscapeChars record {|
    QuotedFieldNameWithEscapeChars 'd\.\ \@\#d;
|};

function testQuotedFieldNamesWithEscapeCharacters() {
    json jsonMapping = {"d. @#d": 6};

    // cloneReadOnly
    json r = jsonMapping.cloneReadOnly();
    QuotedFieldNameWithEscapeChars newRec = <QuotedFieldNameWithEscapeChars>r;
    assert(6, newRec.'d\.\ \@\#d);

    // fromJsonWithType
    jsonMapping = {"d. @#d": 9};
    newRec = checkpanic jsonMapping.fromJsonWithType(QuotedFieldNameWithEscapeChars);
    assert(9, newRec.'d\.\ \@\#d);
    newRec.'d\.\ \@\#d = 7;
    assert(7, newRec.'d\.\ \@\#d);

    // cloneWithType
    jsonMapping = {"d. @#d": 8};
    newRec = checkpanic jsonMapping.cloneWithType(QuotedFieldNameWithEscapeChars);
    assert(8, newRec.'d\.\ \@\#d);
}

function testQuotedSubFieldNamesWithEscapeCharacters() {
    json jsonMapping = {"d. @#d": {"d. @#d": 7}};

    // cloneReadOnly
    json r = jsonMapping.cloneReadOnly();
    QuotedSubFieldNameWithEscapeChars newRec = <QuotedSubFieldNameWithEscapeChars>r;
    assert(7, newRec.'d\.\ \@\#d.'d\.\ \@\#d);

    // fromJsonWithType
    jsonMapping = {"d. @#d": {"d. @#d": 9}};
    newRec = checkpanic jsonMapping.fromJsonWithType(QuotedSubFieldNameWithEscapeChars);
    assert(9, newRec.'d\.\ \@\#d.'d\.\ \@\#d);
    newRec.'d\.\ \@\#d.'d\.\ \@\#d = 8;
    assert(8, newRec.'d\.\ \@\#d.'d\.\ \@\#d);

    // cloneWithType
    jsonMapping = {"d. @#d": {"d. @#d": 10}};
    newRec = checkpanic jsonMapping.cloneWithType(QuotedSubFieldNameWithEscapeChars);
    assert(10, newRec.'d\.\ \@\#d.'d\.\ \@\#d);
}

// Util functions

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string detail = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        panic error("{AssertionError}", message = detail);
    }
}

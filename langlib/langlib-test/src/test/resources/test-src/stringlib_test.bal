// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'string as strings;

string str = "Hello Ballerina!";
string str1 = "Hello Hello Ballerina!";

function testToLower() returns string {
    return str.toLowerAscii();
}

type EqualityOp "==" | "!=" | "===" | "!==";

type EqualityInsn record {|
        EqualityOp op;
|};

function testLength() {
    assertEquals(16, "Hello Ballerina!".length());

    EqualityOp tempFiniteType1 = "===";
    EqualityOp tempFiniteType2 = "!=";
    assertEquals(true, tempFiniteType1.length() != tempFiniteType2.length());

    int temp = lengthTestFunc({op : tempFiniteType1});
    assertEquals(3, temp);
}

function lengthTestFunc(EqualityInsn e) returns int {
    int length = 0;
    if ((e.op).length() == 3) {
        length = (e.op).length();
    }
    return length;
}

function testSubString() returns [string,string, string] {
    return [str.substring(6, 9), str.substring(6), strings:substring(str,6)];
}

function testIterator() {
    string str = "Foo Bar";
    string:Char[] expected = ["F", "o", "o", " ", "B", "a", "r"];
    int i = 0;

    object {
        public isolated function next() returns record {| string:Char value; |}?;
    } itr1 = str.iterator();
    record {| string:Char value; |}|() elem1 = itr1.next();
    while (elem1 is record {| string:Char value; |}) {
        assertEquals(expected[i], elem1.value);
        elem1 = itr1.next();
        i += 1;
    }
    assertEquals(7, i);

    i = 0;
    object {
        public isolated function next() returns record {| string value; |}?;
    } itr2 = str.iterator();
    record {| string value; |}|() elem2 = itr2.next();
    while (elem2 is record {| string:Char value; |}) {
        assertEquals(expected[i], elem2.value);
        elem2 = itr2.next();
        i+= 1;
    }
    assertEquals(7, i);
}

function testStartsWith() returns boolean {
    return strings:startsWith(str, "Hello");
}

function testConcat() returns string {
    return strings:concat("Hello ", "from ", "Ballerina");
}

function testIndexOf(string substr) returns int? {
    return str.indexOf(substr);
}

function testLastIndexOf() {
    int? i1 = str1.lastIndexOf("Hello");
    if (<int>i1 != 6) {
        error err = error("Index of the last occurrence of 'Hello' should equal 6");
        panic err;
    }

    int? i2 = str1.lastIndexOf("Invalid");
    if (i2 != ()) {
        error err = error("Index of the last occurrence of 'Invalid' should be nil");
        panic err;
    }
}

function testEndsWith(string substr) returns boolean {
    return str.endsWith(substr);
}

function testFromBytes() returns string|error {
    byte[] bytes = [72, 101, 108, 108, 111, 32, 66, 97, 108, 108, 101, 114, 105, 110, 97, 33];
    return strings:fromBytes(bytes);
}

function testJoin() returns string {
    string[] days = ["Sunday", "Monday", "Tuesday"];
    return strings:'join(", ", ...days);
}

function testCodePointCompare(string st1, string st2) returns int {
    return strings:codePointCompare(st1, st2);
}

function testGetCodepoint(string st, int index) returns int {
    return st.getCodePoint(index);
}

function testToCodepointInts(string st) returns int[] {
    return st.toCodePointInts();
}

function testFromCodePointInts(int[] ints) returns string|error {
    return strings:fromCodePointInts(ints);
}

function testSubstringOutRange() returns string {
    return "abcdef".substring(7, 9);
}

function testSubstring(string s, int si, int ei) returns error|string {
    error|string sub = trap s.substring(si, ei);
    return sub;
}

function testEqualsIgnoreCaseAscii() {
    [string, string, boolean][] data = [
                    ["aBCdeFg", "aBCdeFg", true],
                    ["aBCdeFg", "abcdefg", true],
                    ["aBCdeFg", "abcdefh", false],
                    ["Duල්Viන්", "duල්viන්", true],
                    ["Duන්Viල්", "duල්viන්", false]
                ];
    int i = 0;
    while (i < 5) {
        boolean result = strings:equalsIgnoreCaseAscii(data[i][0], data[i][1]);
        if (result != data[i][2]) {
            panic error("AssertionError", message = "Expected '" + data[i][2].toString() + "' but found '" + result.toString() + "'");
        }
        i = i + 1;
    }
}

function testIncludes() returns boolean {
    return strings:includes(str1, str, 6);
}

function testChainedStringFunctions() returns string {
    string foo = "foo1";
    foo = foo.concat("foo2").concat("foo3").concat("foo4");
    return foo;
}

function testLangLibCallOnStringSubTypes() {
    string a = "hello";
    string:Char b = "h";

    boolean b1 = a.startsWith("h");
    boolean b2 = b.startsWith("h");
    boolean b3 = a.startsWith("a");
    boolean b4 = b.startsWith("a");

    assertEquals(true, b1);
    assertEquals(true, b2);
    assertEquals(false, b3);
    assertEquals(false, b4);
}

type Strings "a"|"bc";

function testLangLibCallOnFiniteType() {
    Strings x = "bc";
    boolean b = x.startsWith("bc");
    assertEquals(true, b);
    assertEquals(false, x.startsWith("a"));
}

function testIteratorWithUnicodeChar(int codePoint, int[] expected) returns error? {
    string str = check string:fromCodePointInt(codePoint);
    int i = 0;
    foreach var ch in str {
        assertEquals(expected, ch.toCodePointInts());
    }
}

function testCharIterator(string stringValue) {
    string result = "";
    foreach var ch in stringValue {
        result = result + ch;
    }
    assertEquals(stringValue, result);
}

function concatNonBMP(string prefix, string expected) {
    string s = "👋world🤷!";
    assertEquals(expected, prefix + s);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquals(anydata expected, anydata actual) {
    if (expected == actual) {
        return;
    }

    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error(ASSERTION_ERROR_REASON, message = msg);
}

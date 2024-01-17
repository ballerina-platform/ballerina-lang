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

function testFromBytesInvalidValues() {

    byte[] bytes = [
        72,
        101,
        108,
        108,
        111,
        33,
        126,
        63,
        194,
        163,
        195,
        159,
        208,
        175,
        206,
        187,
        226,
        152,
        131,
        226,
        156,
        136,
        224,
        175,
        184,
        240,
        159,
        152,
        128,
        240,
        159,
        132,
        176,
        240,
        159,
        141,
        186
    ];
    string result = checkpanic strings:fromBytes(bytes);
    assertEquals("Hello!~?£ßЯλ☃✈௸😀🄰🍺", result);

    bytes = [237, 159, 191];
    result = checkpanic strings:fromBytes(bytes);
    assertEquals("퟿", result);

    bytes = [237, 160, 191];
    string|error negativeResult = strings:fromBytes(bytes);
    assertEquals(true, negativeResult is error);
    error err = <error>negativeResult;
    assertEquals("FailedToDecodeBytes", err.message());
    assertEquals("array contains invalid UTF-8 byte value", <string>checkpanic err.detail()["message"]);
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

const string constString1 = "abc";
const string constString2 = "123";
const string constString3 = "+1.21";
const string constString4 = "-3.121";
const string constString5 = " a a ";
const string constString6 = "#";

string & readonly roString1 = "--121";
string & readonly roString2 = "+-121";

string:Char char1 = "\u{0051}";
string:Char char2 = "\u{002b}";

string:Char & readonly roChar = "\u{002b}";

function getString() returns string {
    return "-987";
}

function getChar() returns string:Char {
    return "-";
}

function testPadStart() {
    string str = "123";
    string y1 = str.padStart(0, " ");
    assertEquals("123", y1);

    string y2 = str.padStart(3);
    assertEquals("123", y2);

    string y3 = str.padStart(4);
    assertEquals(" 123", y3);

    string y4 = str.padStart(5, " ");
    assertEquals("  123", y4);

    string y5 = str.padStart(5);
    assertEquals("  123", y5);

    string y6 = str.padStart(30, "-");
    assertEquals("---------------------------123", y6);

    string y7 = constString1.padStart(5, <string:Char> constString6);
    assertEquals("##abc", y7);

    string y8 = constString4.padStart(10);
    assertEquals("    -3.121", y8);

    string y9 = roString1.padStart(6, "0");
    assertEquals("0--121", y9);

    string y10 = char1.padStart(5, char2);
    assertEquals("++++Q", y10);

    string y11 = getString().padStart(5, getChar());
    assertEquals("--987", y11);

    string y12 = getChar().padStart(5, getChar());
    assertEquals("-----", y12);

    string y13 = roChar.padStart(5, roChar);
    assertEquals("+++++", y13);

    string y14 = roChar.padStart(5);
    assertEquals("    +", y14);

    string y15 = getString().padStart(5);
    assertEquals(" -987", y15);

    string y16 = getChar().padStart(5);
    assertEquals("    -", y16);

    string y17 = "\u{0043}\u{00f3}".padStart(30, "\u{0048}");
    assertEquals("HHHHHHHHHHHHHHHHHHHHHHHHHHHHCó", y17);

    string y18 = "\u{00ee}".padStart(30, "\u{00c8}");
    assertEquals("ÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈî", y18);

    string y19 = "\u{00c3}".padStart(30);
    assertEquals("                             Ã", y19);

    string y20 = "ලලල".padStart(6, "ස");
    assertEquals("සසසලලල", y20);

    string y21 = "සිංහල".padStart(30);
    assertEquals("                         සිංහල", y21);

    string y27 = constString3.padStart(0xa);
    assertEquals("     +1.21", y27);

    string y28 = constString3.padStart(0x1a);
    assertEquals("                     +1.21", y28);

    string y29 = constString3.padStart(-0xaaffa);
    assertEquals("+1.21", y29);

    string y30 = constString3.padStart(-1213);
    assertEquals("+1.21", y30);

    int:Signed32 i1 = 6;
    string y31 = roString2.padStart(i1, "0");
    assertEquals("0+-121", y31);

    int:Unsigned32 i2 = 6;
    string y32 = roString2.padStart(i2, "0");
    assertEquals("0+-121", y32);

    byte i3 = 6;
    string y33 = "+ABC".padStart(i3, "0");
    assertEquals("00+ABC", y33);

    int:Signed8 i4 = 6;
    string y34 = " +ABC".padStart(i4);
    assertEquals("  +ABC", y34);

    int:Unsigned8 i5 = 6;
    string y35 = " -ABC".padStart(i5);
    assertEquals("  -ABC", y35);

    string y36 = "".padStart(-3);
    assertEquals("", y36);

    string y37 = "".padStart(0);
    assertEquals("", y37);

    string y38 = "".padStart(3);
    assertEquals("   ", y38);
}

function testPadEnd() {
    string str = "123";
    string y1 = str.padEnd(0, " ");
    assertEquals("123", y1);

    string y2 = str.padEnd(3);
    assertEquals("123", y2);

    string y3 = str.padEnd(4);
    assertEquals("123 ", y3);

    string y4 = str.padEnd(5, " ");
    assertEquals("123  ", y4);

    string y5 = str.padEnd(5);
    assertEquals("123  ", y5);

    string y6 = str.padEnd(30, "-");
    assertEquals("123---------------------------", y6);

    string y7 = constString2.padEnd(5, <string:Char> constString6);
    assertEquals("123##", y7);

    string y8 = constString3.padEnd(10);
    assertEquals("+1.21     ", y8);

    string y9 = roString2.padEnd(6, "0");
    assertEquals("+-1210", y9);

    string y10 = char1.padEnd(5, char2);
    assertEquals("Q++++", y10);

    string y11 = getString().padEnd(5, getChar());
    assertEquals("-987-", y11);

    string y12 = getChar().padEnd(5, getChar());
    assertEquals("-----", y12);

    string y13 = roChar.padEnd(5, roChar);
    assertEquals("+++++", y13);

    string y14 = roChar.padEnd(5);
    assertEquals("+    ", y14);

    string y15 = getString().padEnd(5);
    assertEquals("-987 ", y15);

    string y16 = getChar().padEnd(5);
    assertEquals("-    ", y16);

    string y17 = "\u{0043}\u{00f3}".padEnd(30, "\u{0048}");
    assertEquals("CóHHHHHHHHHHHHHHHHHHHHHHHHHHHH", y17);

    string y18 = "\u{00ee}".padEnd(30, "\u{00c8}");
    assertEquals("îÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈ", y18);

    string y19 = "\u{00c3}".padEnd(30);
    assertEquals("Ã                             ", y19);

    string y20 = "සිංහල".padEnd(30, "ල");
    assertEquals("සිංහලලලලලලලලලලලලලලලලලලලලලලලලලල", y20);

    string y21 = "සිංහල".padEnd(30);
    assertEquals("සිංහල                         ", y21);

    string y27 = constString3.padEnd(0xa);
    assertEquals("+1.21     ", y27);

    string y28 = constString3.padEnd(0x1a);
    assertEquals("+1.21                     ", y28);

    string y29 = constString3.padEnd(-0xaaffa);
    assertEquals("+1.21", y29);

    string y30 = constString3.padEnd(-1213);
    assertEquals("+1.21", y30);

    int:Signed32 i1 = 6;
    string y31 = roString2.padEnd(i1, "0");
    assertEquals("+-1210", y31);

    int:Unsigned32 i2 = 6;
    string y32 = roString2.padEnd(i2, "0");
    assertEquals("+-1210", y32);

    byte i3 = 6;
    string y33 = "+ABC".padEnd(i3, "0");
    assertEquals("+ABC00", y33);

    int:Signed8 i4 = 6;
    string y34 = " +ABC".padEnd(i4);
    assertEquals(" +ABC ", y34);

    int:Unsigned8 i5 = 6;
    string y35 = " -ABC".padEnd(i5);
    assertEquals(" -ABC ", y35);

    string y36 = "".padEnd(-3);
    assertEquals("", y36);

    string y37 = "".padEnd(0);
    assertEquals("", y37);

    string y38 = "".padEnd(3);
    assertEquals("   ", y38);
}

function testPadZero() {
    string str = "123";
    string y1 = str.padZero(0, " ");
    assertEquals("123", y1);

    string y2 = str.padZero(3);
    assertEquals("123", y2);

    string y3 = str.padZero(4);
    assertEquals("0123", y3);

    string y4 = str.padZero(5, " ");
    assertEquals("  123", y4);

    string y5 = str.padZero(5);
    assertEquals("00123", y5);

    string str2 = "+123";
    string str3 = "--123";
    string y6 = str2.padZero(5);
    assertEquals("+0123", y6);
    string y7 = str3.padZero(6);
    assertEquals("-0-123", y7);
    string y8 = str2.padZero(5, " ");
    assertEquals("+ 123", y8);
    string y9 = str3.padZero(6, "+");
    assertEquals("-+-123", y9);
    string y10 = str2.padZero(5);
    assertEquals("+0123", y10);

    string y11 = "-123".padZero(30, "+");
    assertEquals("-++++++++++++++++++++++++++123", y11);

    string y12 = constString2.padZero(5, <string:Char> constString6);
    assertEquals("##123", y12);

    string y13 = constString3.padZero(10);
    assertEquals("+000001.21", y13);

    string y14 = roString2.padZero(6, "0");
    assertEquals("+0-121", y14);

    string y15 = char1.padZero(5, char2);
    assertEquals("++++Q", y15);

    string y16 = getString().padZero(5, getChar());
    assertEquals("--987", y16);

    string y17 = getChar().padZero(5, getChar());
    assertEquals("-----", y17);

    string y18 = roChar.padZero(5, roChar);
    assertEquals("+++++", y18);

    string y19 = roChar.padZero(5);
    assertEquals("+0000", y19);

    string y20 = getString().padZero(5);
    assertEquals("-0987", y20);

    string y21 = getChar().padZero(5);
    assertEquals("-0000", y21);

    string y22 = "\u{0043}\u{00f3}".padZero(30, "\u{0048}");
    assertEquals("HHHHHHHHHHHHHHHHHHHHHHHHHHHHCó", y22);

    string y23 = "\u{00ee}".padZero(30, "\u{00c8}");
    assertEquals("ÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈî", y23);

    string y24 = "\u{00c3}".padZero(30);
    assertEquals("00000000000000000000000000000Ã", y24);

    string y25 = "සිංහල".padZero(30, "ල");
    assertEquals("ලලලලලලලලලලලලලලලලලලලලලලලලලසිංහල", y25);

    string y26 = "සිංහල".padZero(30);
    assertEquals("0000000000000000000000000සිංහල", y26);

    string y27 = constString3.padZero(0xa);
    assertEquals("+000001.21", y27);

    string y28 = constString3.padZero(0x1a);
    assertEquals("+0000000000000000000001.21", y28);

    string y29 = constString3.padZero(-0xaaffa);
    assertEquals("+1.21", y29);

    string y30 = constString3.padZero(-1213);
    assertEquals("+1.21", y30);

    int:Signed32 i1 = 6;
    string y31 = roString2.padZero(i1, "0");
    assertEquals("+0-121", y31);

    int:Unsigned32 i2 = 6;
    string y32 = roString2.padZero(i2, "0");
    assertEquals("+0-121", y32);

    byte i3 = 6;
    string y33 = "+ABC".padZero(i3, "0");
    assertEquals("+00ABC", y33);

    int:Signed8 i4 = 6;
    string y34 = " +ABC".padZero(i4);
    assertEquals("0 +ABC", y34);

    int:Unsigned8 i5 = 6;
    string y35 = " -ABC".padZero(i5);
    assertEquals("0 -ABC", y35);

    string y36 = "".padZero(-3);
    assertEquals("", y36);

    string y37 = "".padZero(0);
    assertEquals("", y37);

    string y38 = "".padZero(3);
    assertEquals("000", y38);
}

function testMatches() {
    string stringToMatch1 = "This Should Match";
    string:RegExp regex1 = re `Th.*ch`;
    boolean result1 = stringToMatch1.matches(regex1);
    assertTrue(result1);

    string:RegExp regex2 = re `Should`;
    boolean result2 = stringToMatch1.matches(regex2);
    assertFalse(result2);

    string stringToMatch3 = "abc1";
    string:RegExp regex3 = re `([a-z0-9]{5,})`;
    boolean result3 = stringToMatch3.matches(regex3);
    assertFalse(result3);

    string stringToMatch4 = "abc12";
    boolean result4 = stringToMatch4.matches(regex3);
    assertTrue(result4);

    string stringToMatch5 = "abc123";
    boolean result5 = stringToMatch5.matches(regex3);
    assertTrue(result5);
}

function testIncludesMatch() {
    string stringToMatch1 = "This Should Match";
    string:RegExp regex1 = re `Th.*ch`;
    boolean result1 = stringToMatch1.includesMatch(regex1);
    assertTrue(result1);

    string:RegExp regex2 = re `Should`;
    boolean result2 = stringToMatch1.includesMatch(regex2);
    assertTrue(result2);

    string stringToMatch3 = "abc1";
    string:RegExp regex3 = re `([a-z0-9]{5,})`;
    boolean result3 = stringToMatch3.includesMatch(regex3);
    assertFalse(result3);

    string stringToMatch4 = "Betty Botter bought some butter but she said the butter’s bitter.";
    string:RegExp regex4 = re `[bB].tt[a-z]*`;
    boolean result4 = stringToMatch4.includesMatch(regex4);
    assertTrue(result4);

    string stringToMatch5 = "HelloWorld";
    string:RegExp regex5 = re `[0-9]`;
    boolean result5 = stringToMatch5.includesMatch(regex5);
    assertFalse(result5);

    string stringToMatch6 = "An apple is nice";
    string:RegExp regex6 = re `apple|orange|pear|banana|kiwi`;
    boolean result6 = stringToMatch6.includesMatch(regex6);
    assertTrue(result6);
}

function testFromBytesAsync() {
    foreach int i in 0 ... 15 {
        callFromBytesAsync();
    }
}

isolated function callFromBytesAsync() {
    worker w1 {
        future<string|error> bytesFuture = start callFromBytes1();
        string|error str = wait bytesFuture;
        assertTrue(str is string);
        if (str is string) {
            assertEquals(str, "Hello!~?£ßЯλ☃✈௸😀🄰🍺");
        }
    }
    worker w2 {
        future<string|error> bytesFuture = start callFromBytes2();
        string|error str = wait bytesFuture;
        assertTrue(str is string);
        if (str is string) {
            assertEquals(str, "Hello Ballerina!");
        }
    }
    _ = wait {w1, w2};
}

isolated function callFromBytes1() returns string|error {
    byte[] bytes = [
        72,
        101,
        108,
        108,
        111,
        33,
        126,
        63,
        194,
        163,
        195,
        159,
        208,
        175,
        206,
        187,
        226,
        152,
        131,
        226,
        156,
        136,
        224,
        175,
        184,
        240,
        159,
        152,
        128,
        240,
        159,
        132,
        176,
        240,
        159,
        141,
        186
    ];
    return check string:fromBytes(bytes);
}

isolated function callFromBytes2() returns string|error {
    byte[] bytes = [
        72,
        101,
        108,
        108,
        111,
        32,
        66,
        97,
        108,
        108,
        101,
        114,
        105,
        110,
        97,
        33
    ];
    return checkpanic string:fromBytes(bytes);
}

function testEqualsIgnoreCaseAsciiAsync() {
    foreach int i in 0 ... 15 {
        callEqualsIgnoreCaseAsciiAsync();
    }
}

isolated function callEqualsIgnoreCaseAsciiAsync() {
    worker w1 {
        future<boolean|error> equalsFuture = start callEqualsIgnoreCaseAscii1();
        boolean|error result = wait equalsFuture;
        assertTrue(result is boolean);
        if (result is boolean) {
            assertTrue(result);
        }
    }
    worker w2 {
        future<boolean|error> equalsFuture = start callEqualsIgnoreCaseAscii2();
        boolean|error result = wait equalsFuture;
        assertTrue(result is boolean);
        if (result is boolean) {
            assertTrue(result);
        }
    }
    _ = wait {w1, w2};
}

isolated function callEqualsIgnoreCaseAscii1() returns boolean {
    return string:equalsIgnoreCaseAscii("aBCdeFg", "aBCdeFg");
}

isolated function callEqualsIgnoreCaseAscii2() returns boolean {
    return string:equalsIgnoreCaseAscii("Duල්Viන්", "Duල්Viන්");
}

const ASSERTION_ERROR_REASON = "AssertionError";

isolated function assertEquals(anydata expected, anydata actual) {
    if (expected == actual) {
        return;
    }

    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error(ASSERTION_ERROR_REASON, message = msg);
}

isolated function assertTrue(anydata actual) {
    assertEquals(true, actual);
}

function assertFalse(anydata actual) {
    assertEquals(false, actual);
}

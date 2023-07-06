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

function concatBMP() returns string {
    string prefix = "red ";
    string s = "apple";
    return prefix + s;
}

function nonBMPLength() returns (int) {
    string smiley = "h😀llo";
    return smiley.length();
}

function recordStringValue() returns int {
    string smiley = "h😀llo";
    record {| string myField; |} r = {myField: smiley};
    boolean containsKey = r.hasKey("myField");
    if (!containsKey) {
        return -1;
    }
    return r.myField.length();
}

function testError() returns int {
    string smiley = "h🤷llo";
    error err = error(smiley);
    return err.message().length();
}

function testArrayStore() returns int {
    string[] arr = [];
    string[][] arr2 = [["h🤷llo", "h🤷llo", "h🤷llo"], ["h🤷llo", "h🤷llo", "h🤷llo"]];
    arr[0] = "h🤷llo";
    return arr[0].length() + arr2[0][1].length();
}

function testStringIndexAccess() {
    string hello = "hello👋";
    string val = hello[5];
    assert(val.length(), 1);

    string str1 = "ABCDEF";
    string:Char testChar1 = str1[1];
    assert(testChar1 is string && testChar1 is string:Char, true);

    testChar1 = str1[1][0][0];
    assert(testChar1 is string && testChar1 is string:Char, true);

    string str2 = indexAccessTestFunc(str1[0]);
    assert(str2[1] is string && str2[1] is string:Char, true);
}

function indexAccessTestFunc(string:Char character) returns string {
    assert(character, "A");
    return character + "M";
}

function testStringIndexAccessException() {
    string hello = "hello👋";
    string val = hello[6];
}

function anyToStringCasting() returns int {
    any a = "hello👋";
    string k = <string> a;
    return k.length();
}

function anydataToStringCast() returns int {
    anydata a = "hello👋";
    string k = <string> a;
    return k.length();
}

function assert(anydata actual, anydata expected) {
    if (expected == actual) {
        return;
    }
    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error(reason);
}

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

import ballerina/lang.'xml;
import ballerina/lang.'string as strings;

const ASSERTION_ERR_REASON = "AssertionError";

function assignToString() {
    string s1 = xml `test1`;
    string s2 = <'xml:Text> xml `test2`;
    'xml:Text x1 = <'xml:Text> xml `test3`;
    string s3 = x1;
    string s4 = <string> xml `test4`;
    string str1 = "test5";
    xml x2 = xml `${str1}`;
    string s5 = <'xml:Text> x2;
    string[] s6 = ["test", "hello", xml `my`, "world"];
    map<string> s7 = {};
    s7["firstElement"] = xml `hello`;
    s7[xml `secondElement`] = "my";
    s7[xml `thirdElement`] = "world";

    if (s1 == "test1" && s2 == "test2" && s3 == "test3" && s4 == "test4" && s5 == "test5" && s6[2] == "my" &&
        s7.get("firstElement") == "hello" && s7.get("secondElement") == "my" && s7.get("thirdElement") == "world") {
        return;
    }
    panic error(ASSERTION_ERR_REASON, message = "expected 'true', found 'false'");
}

function foo(string s1, string s2, string s3) returns string {
    return s1 + s2 + s3;
}

function passToFunction() {
    string s1 = foo("hello ", "hello ", xml `world`);
    string s2 = foo("hello ", xml `my `, "world");
    string s3 = foo(xml `hello `, "my ", "friend");

    if (s1 == "hello hello world" && s2 == "hello my world" && s3 == "hello my friend") {
        return;
    }
    panic error(ASSERTION_ERR_REASON, message = "expected 'true', found 'false'");
}

function invokeLangLibMethods() {
    'xml:Text t = xml `hello world`;

    if (t.indexOf("l", 1) == 2 && t.getCodePoint(6) == 119 && t.substring(2, 6) == "llo " &&
        strings:codePointCompare(getXMLText(), "y world") == -1 && !getXMLText().startsWith("he")) {
        return;
    }
    panic error(ASSERTION_ERR_REASON, message = "expected 'true', found 'false'");
}

function getXMLText() returns 'xml:Text {
    return xml `my world`;
}

function testXMLTestToStringConversion() {
    assignToString();
    passToFunction();
    invokeLangLibMethods();
}

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

import ballerina/lang.regexp;

const string:RegExp regExpr1 = re `World`;
const regExpr2 = re `[0-9]`;
const string:RegExp regExpr3 = re `[bB].tt[a-z]*`;
const string:RegExp regExpr4 = re `.`;
const string:RegExp regExpr5 = re `GFG`;
const regExpr6 = re `(GFG)(FGF)`;
const regExpr7 = re `Ba[a-z ]+`;
const string:RegExp regExpr8 = re `This`;
const map<string:RegExp> regExpr9 = {"R1": re `This`, "R2": re `Ba[a-z ]+`};

function testFind() {
    string str1 = "HelloWorld";
    regexp:Span? res1 = regExpr1.find(str1);
    assertTrue(res1 is regexp:Span);
    regexp:Span resultSpan1 = <regexp:Span>res1;
    assertEquality(5, resultSpan1.startIndex);
    assertEquality(10, resultSpan1.endIndex);
    assertEquality("World", resultSpan1.substring());

    regexp:Span? resultSpan2 = regExpr2.find(str1);
    assertTrue(resultSpan2 is ());

    regexp:Span? res2  = regExpr3.find("Betty Botter bought some butter but she said the butter’s bitter.", 5);
    assertTrue(res2 is regexp:Span);
    regexp:Span resultSpan3 = <regexp:Span>res2;
    assertEquality(6, resultSpan3.startIndex);
    assertEquality(12, resultSpan3.endIndex);
    assertEquality("Botter", resultSpan3.substring());
}

function testFindGroups() {
    string str1 = "HelloWorld";
    regexp:Groups? res1 = regExpr1.findGroups(str1);
    assertTrue(res1 is regexp:Groups);
    regexp:Groups resultGroups1 = <regexp:Groups>res1;
    assertEquality(1, resultGroups1.length());
    regexp:Span resultSpan1 = <regexp:Span>resultGroups1[0];
    assertEquality(5, resultSpan1.startIndex);
    assertEquality(10, resultSpan1.endIndex);
    assertEquality("World", resultSpan1.substring());

    string str2 = "Betty Botter bought some butter but she said the butter’s bitter.";
    regexp:Groups? res2  = regExpr3.findGroups(str2);
    assertTrue(res2 is regexp:Groups);
    regexp:Groups resultGroups2 = <regexp:Groups>res2;
    assertEquality(5, resultGroups2.length());

    regexp:Span resultSpan2 = <regexp:Span>resultGroups2[1];
    assertEquality(6, resultSpan2.startIndex);
    assertEquality(12, resultSpan2.endIndex);
    assertEquality("Botter", resultSpan2.substring());

    string str3 = "A B\nC\tD\rE";
    regexp:Groups? res3 = regExpr4.findGroups(str3);
    assertTrue(res3 is regexp:Groups);
    regexp:Groups resultGroups3 = <regexp:Groups>res3;
    assertEquality(7, resultGroups3.length());

    regexp:Span resultSpan3 = <regexp:Span>resultGroups3[0];
    assertEquality(0, resultSpan3.startIndex);
    assertEquality(1, resultSpan3.endIndex);
    assertEquality("A", resultSpan3.substring());
}

function testFindAll() {
    string str1 = "GFGFGFGFGFGFGFGFGFG";
    regexp:Span[]? res1 = regExpr5.findAll(str1);
    assertTrue(res1 is regexp:Span[]);
    regexp:Span[] resultSpanArr1 = <regexp:Span[]>res1;
    assertEquality(5, resultSpanArr1.length());

    regexp:Span resultSpan1_1 = resultSpanArr1[0];
    assertEquality(0, resultSpan1_1.startIndex);
    assertEquality(3, resultSpan1_1.endIndex);
    assertEquality("GFG", resultSpan1_1.substring());

    regexp:Span resultSpan1_2 = resultSpanArr1[1];
    assertEquality(4, resultSpan1_2.startIndex);
    assertEquality(7, resultSpan1_2.endIndex);
    assertEquality("GFG", resultSpan1_2.substring());
}

function testFindAllGroups() {
    string str1 = "GFGFGFGFGFGFGFGFGF";
    regexp:Groups[]? res1 = regExpr6.findAllGroups(str1);
    assertTrue(res1 is regexp:Groups[]);
    regexp:Groups[] groupsArr1 = <regexp:Groups[]>res1;
    assertEquality(3, groupsArr1.length());

    regexp:Groups groups1_1 = groupsArr1[0];
    regexp:Span resultSpan1_1_1 = groups1_1[0];
    assertEquality(0, resultSpan1_1_1.startIndex);
    assertEquality(3, resultSpan1_1_1.endIndex);
    assertEquality("GFG", resultSpan1_1_1.substring());

    regexp:Span? resultSpanOrNil1_1_2 = groups1_1[1];
    assertTrue(resultSpanOrNil1_1_2 is regexp:Span);
    regexp:Span resultSpan1_1_2 = <regexp:Span>resultSpanOrNil1_1_2;
    assertEquality(3, resultSpan1_1_2.startIndex);
    assertEquality(6, resultSpan1_1_2.endIndex);
    assertEquality("FGF", resultSpan1_1_2.substring());
}

function testMatchAt() {
    string str1 = "Ballerina is great";
    regexp:Span? res1 = regExpr7.matchAt(str1);
    assertTrue(res1 is regexp:Span);
    regexp:Span resultSpan1 = <regexp:Span>res1;
    assertEquality(0, resultSpan1.startIndex);
    assertEquality(18, resultSpan1.endIndex);
    assertEquality(str1, resultSpan1.substring());
}

function testReplace() {
    string str1 = "ReplaceThisThisTextThis";
    string replacement1 = " ";
    string result1 = regExpr8.replace(str1, replacement1);
    assertEquality("Replace ThisTextThis", result1);

    string replacement2 = "# ";
    string result2 = regExpr8.replace(str1, replacement2);
    assertEquality("Replace# ThisTextThis", result2);
}

function testConstMap1() {
    string str1 = "ReplaceThisThisTextThis";
    string replacement1 = " ";
    string result1 = (<string:RegExp> regExpr9["R1"]).replace(str1, replacement1);
    assertEquality("Replace ThisTextThis", result1);

    string replacement2 = "# ";
    string result2 = (<string:RegExp> regExpr9["R1"]).replace(str1, replacement2);
    assertEquality("Replace# ThisTextThis", result2);
}

function testConstMap2() {
    string str1 = "Ballerina is great";
    regexp:Span? res1 = (<string:RegExp> regExpr9["R2"]).matchAt(str1);
    assertTrue(res1 is regexp:Span);
    regexp:Span resultSpan1 = <regexp:Span>res1;
    assertEquality(0, resultSpan1.startIndex);
    assertEquality(18, resultSpan1.endIndex);
    assertEquality(str1, resultSpan1.substring());
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

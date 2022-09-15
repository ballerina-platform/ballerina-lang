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
import ballerina/jballerina.java;

function testFind() {
    string str1 = "HelloWorld";
    var regExpr1 = re `World`;
    regexp:Span? res1 = regExpr1.find(str1);
    assertTrue(res1 is regexp:Span);
    regexp:Span resultSpan1 = <regexp:Span>res1;
    assertEquality(5, resultSpan1.startIndex);
    assertEquality(10, resultSpan1.endIndex);
    assertEquality("World", resultSpan1.substring());

    string str2 = "GFGFGFGFGFGFGFGFGFG";
    var regExpr2 = re `GFG`;
    regexp:Span? res2 = regExpr2.find(str2);
    assertTrue(res2 is regexp:Span);
    regexp:Span resultSpan2 = <regexp:Span>res2;
    assertEquality(0, resultSpan2.startIndex);
    assertEquality(3, resultSpan2.endIndex);
    assertEquality("GFG", resultSpan2.substring());

    regexp:Span? res3 = regExpr2.find(str2, 3);
    assertTrue(res3 is regexp:Span);
    regexp:Span resultSpan3 = <regexp:Span>res3;
    assertEquality(4, resultSpan3.startIndex);
    assertEquality(7, resultSpan3.endIndex);
    assertEquality("GFG", resultSpan3.substring());

    var regExpr4 = re `[0-9]`;
    regexp:Span? res4 = regExpr4.find(str1);
    assertTrue(res4 is ());
}

function testFindGroups() {
    string str1 = "HelloWorld";
    var regExpr1 = re `World`;
    regexp:Groups? res1 = regExpr1.findGroups(str1);
    assertTrue(res1 is regexp:Groups);
    regexp:Groups resultGroups1 = <regexp:Groups>res1;
    assertEquality(1, resultGroups1.length());
    regexp:Span resultSpan1 = <regexp:Span>resultGroups1[0];
    assertEquality(5, resultSpan1.startIndex);
    assertEquality(10, resultSpan1.endIndex);
    assertEquality("World", resultSpan1.substring());

    string str2 = "GFGFGFGFGFGFGFGFGFG";
    var regExpr2 = re `GFG`;
    regexp:Groups? res2 = regExpr2.findGroups(str2);
    assertTrue(res2 is regexp:Groups);
    regexp:Groups resultGroups2 = <regexp:Groups>res2;
    assertEquality(5, resultGroups2.length());

    regexp:Span resultSpan2_1 = <regexp:Span>resultGroups2[0];
    assertEquality(0, resultSpan2_1.startIndex);
    assertEquality(3, resultSpan2_1.endIndex);
    assertEquality("GFG", resultSpan2_1.substring());

    regexp:Span resultSpan2_2 = <regexp:Span>resultGroups2[1];
    assertEquality(4, resultSpan2_2.startIndex);
    assertEquality(7, resultSpan2_2.endIndex);
    assertEquality("GFG", resultSpan2_2.substring());

    regexp:Span resultSpan2_3 = <regexp:Span>resultGroups2[2];
    assertEquality(8, resultSpan2_3.startIndex);
    assertEquality(11, resultSpan2_3.endIndex);
    assertEquality("GFG", resultSpan2_3.substring());

    regexp:Span resultSpan2_4 = <regexp:Span>resultGroups2[3];
    assertEquality(12, resultSpan2_4.startIndex);
    assertEquality(15, resultSpan2_4.endIndex);
    assertEquality("GFG", resultSpan2_4.substring());

    regexp:Span resultSpan2_5 = <regexp:Span>resultGroups2[4];
    assertEquality(16, resultSpan2_5.startIndex);
    assertEquality(19, resultSpan2_5.endIndex);
    assertEquality("GFG", resultSpan2_5.substring());

    regexp:Groups? res3 = regExpr2.findGroups(str2, 15);
    assertTrue(res3 is regexp:Groups);
    regexp:Groups resultGroups3 = <regexp:Groups>res3;
    assertEquality(1, resultGroups3.length());
    regexp:Span resultSpan3 = <regexp:Span>resultGroups3[0];
    assertEquality(16, resultSpan3.startIndex);
    assertEquality(19, resultSpan3.endIndex);
    assertEquality("GFG", resultSpan3.substring());

    var regExpr3 = re `(GFG)(FGF)`;
    regexp:Groups? res4 = regExpr3.findGroups(str2);
    assertTrue(res4 is regexp:Groups);
    regexp:Groups resultGroups4 = <regexp:Groups>res4;
    assertEquality(3, resultGroups4.length());

    regexp:Span resultSpan3_1 = <regexp:Span>resultGroups4[0];
    assertEquality(0, resultSpan3_1.startIndex);
    assertEquality(6, resultSpan3_1.endIndex);
    assertEquality("GFGFGF", resultSpan3_1.substring());

    regexp:Span resultSpan3_2 = <regexp:Span>resultGroups4[1];
    assertEquality(6, resultSpan3_2.startIndex);
    assertEquality(12, resultSpan3_2.endIndex);
    assertEquality("GFGFGF", resultSpan3_2.substring());

    regexp:Span resultSpan3_3 = <regexp:Span>resultGroups4[2];
    assertEquality(12, resultSpan3_3.startIndex);
    assertEquality(18, resultSpan3_3.endIndex);
    assertEquality("GFGFGF", resultSpan3_3.substring());
}

function testFindAll() {
    string str1 = "GFGFGFGFGFGFGFGFGFG";
    var regExpr1 = re `GFG`;
    regexp:Span[]? res1 = regExpr1.findAll(str1);
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

    regexp:Span resultSpan1_3 = resultSpanArr1[2];
    assertEquality(8, resultSpan1_3.startIndex);
    assertEquality(11, resultSpan1_3.endIndex);
    assertEquality("GFG", resultSpan1_3.substring());

    regexp:Span resultSpan1_4 = resultSpanArr1[3];
    assertEquality(12, resultSpan1_4.startIndex);
    assertEquality(15, resultSpan1_4.endIndex);
    assertEquality("GFG", resultSpan1_4.substring());

    regexp:Span resultSpan1_5 = resultSpanArr1[4];
    assertEquality(16, resultSpan1_5.startIndex);
    assertEquality(19, resultSpan1_5.endIndex);
    assertEquality("GFG", resultSpan1_5.substring());
}

function testFindAllGroups() {
    string str2 = "GFGFGFGFGFGFGFGFGF";
    var regExpr2 = re `(GFG)(FGF)`;
    var res2 = regExpr2.findGroups(str2);

//    string str5 = "100000100011";
//    var regExpr4 = re `0+`;
//    var result5 = regExpr4.findAllGroups(str5);
    print(res2);
}

function testMatchAt() {
    string str1 = "HelloWorld";
    var regExpr1 = re `World`;
    regexp:Span? res1 = regExpr1.matchAt(str1);
    assertTrue(res1 is ());

    regexp:Span? res2 = regExpr1.matchAt(str1, 5);
    assertTrue(res2 is regexp:Span);
    regexp:Span resultSpan1 = <regexp:Span>res2;
    assertEquality(5, resultSpan1.startIndex);
    assertEquality(10, resultSpan1.endIndex);
    assertEquality("World", resultSpan1.substring());

    string str2 = "Ballerina is great";
    var regExpr2 = re `Ba[a-z ]+`;
    regexp:Span? res3 = regExpr2.matchAt(str2);
    assertTrue(res3 is regexp:Span);
    regexp:Span resultSpan2 = <regexp:Span>res3;
    assertEquality(0, resultSpan2.startIndex);
    assertEquality(18, resultSpan2.endIndex);
    assertEquality(str2, resultSpan2.substring());
}

function testMatchGroupsAt() {
    var regExpr1 = re `([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9]{1,3}))?`;
    string str1 = "time: 14:35:59";

    regexp:Groups? res1 = regExpr1.matchGroupsAt(str1);
    assertTrue(res1 is ());

    regexp:Groups? res2 = regExpr1.matchGroupsAt(str1, 6);
    assertTrue(res2 is regexp:Groups);
    regexp:Groups resultGroups1 = <regexp:Groups>res2;
    regexp:Span resultSpan1_1 = <regexp:Span>resultGroups1[0];
    assertEquality(6, resultSpan1_1.startIndex);
    assertEquality(8, resultSpan1_1.endIndex);
    assertEquality("14", resultSpan1_1.substring());

    regexp:Span resultSpan1_2 = <regexp:Span>resultGroups1[1];
    assertEquality(9, resultSpan1_2.startIndex);
    assertEquality(11, resultSpan1_2.endIndex);
    assertEquality("35", resultSpan1_2.substring());

    regexp:Span resultSpan1_3 = <regexp:Span>resultGroups1[2];
    assertEquality(12, resultSpan1_3.startIndex);
    assertEquality(14, resultSpan1_3.endIndex);
    assertEquality("59", resultSpan1_3.substring());

     var regExpr2 = re `([0-9]+)×([0-9]+)`;
     string str2 = "1440×900";

    regexp:Groups? res3 = regExpr2.matchGroupsAt(str2);
    assertTrue(res3 is regexp:Groups);
    regexp:Groups resultGroups2 = <regexp:Groups>res3;
    regexp:Span resultSpan2_1 = <regexp:Span>resultGroups2[0];
    assertEquality(0, resultSpan2_1.startIndex);
    assertEquality(4, resultSpan2_1.endIndex);
    assertEquality("1440", resultSpan2_1.substring());

    regexp:Span resultSpan2_2 = <regexp:Span>resultGroups2[1];
    assertEquality(5, resultSpan2_2.startIndex);
    assertEquality(8, resultSpan2_2.endIndex);
    assertEquality("900", resultSpan2_2.substring());
}

function testIsFullMatch() {
    var regExpr1 = re `(?i:[a-z]+)`;
    string str1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    boolean isFullMatch1 = regExpr1.isFullMatch(str1);
    assertTrue(isFullMatch1);

    var regExpr2 = re `\\d\\d\\d`;
    string str2 = "a123";
    boolean isFullMatch2 = regExpr2.isFullMatch(str2);
    assertFalse(isFullMatch2);

    string str3 = "This Should Match";
    var regExpr3 = re `Th.*ch`;
    boolean isFullMatch3 = regExpr3.isFullMatch(str3);
    assertTrue(isFullMatch3);
}

function testFullMatchGroups() {
    var regExpr1 = re `([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9]{1,3}))?`;
    string str1 = "time: 14:35:59";

    regexp:Groups? res1 = regExpr1.fullMatchGroups(str1);
    assertTrue(res1 is ());

    string str2 = "14:35:59";
    regexp:Groups? res2 = regExpr1.matchGroupsAt(str2);
    assertTrue(res2 is regexp:Groups);
    regexp:Groups resultGroups1 = <regexp:Groups>res2;
    regexp:Span resultSpan1_1 = <regexp:Span>resultGroups1[0];
    assertEquality(0, resultSpan1_1.startIndex);
    assertEquality(2, resultSpan1_1.endIndex);
    assertEquality("14", resultSpan1_1.substring());

    regexp:Span resultSpan1_2 = <regexp:Span>resultGroups1[1];
    assertEquality(3, resultSpan1_2.startIndex);
    assertEquality(5, resultSpan1_2.endIndex);
    assertEquality("35", resultSpan1_2.substring());

    regexp:Span resultSpan1_3 = <regexp:Span>resultGroups1[2];
    assertEquality(6, resultSpan1_3.startIndex);
    assertEquality(8, resultSpan1_3.endIndex);
    assertEquality("59", resultSpan1_3.substring());

    var regExpr2 = re `([0-9]+)×([0-9]+)`;
    string str3 = "1440×900";

    regexp:Groups? res3 = regExpr2.fullMatchGroups(str3);
    assertTrue(res3 is regexp:Groups);
    regexp:Groups resultGroups2 = <regexp:Groups>res3;
    regexp:Span resultSpan2_1 = <regexp:Span>resultGroups2[0];
    assertEquality(0, resultSpan2_1.startIndex);
    assertEquality(4, resultSpan2_1.endIndex);
    assertEquality("1440", resultSpan2_1.substring());

    regexp:Span resultSpan2_2 = <regexp:Span>resultGroups2[1];
    assertEquality(5, resultSpan2_2.startIndex);
    assertEquality(8, resultSpan2_2.endIndex);
    assertEquality("900", resultSpan2_2.substring());
}

function testReplace() {
    string str1 = "ReplaceThisThisTextThis";
    var regExpr1 = re `This`;
    string replacement1 = " ";
    string result1 = regExpr1.replace(str1, replacement1);
    assertEquality("Replace ThisTextThis", result1);

    string replacement2 = "# ";
    string result2 = regExpr1.replace(str1, replacement2);
    assertEquality("Replace# ThisTextThis", result2);

    string str3 = "10010011";
    var regExpr3 = re `0+`;
    string replacement3 = "*";
    string result3 = regExpr3.replace(str3, replacement3);
    assertEquality("1*10011", result3);

    string str4 = "100100011";
    var regExpr4 = re `0+`;
    string replacement4 = "";
    string result4 = regExpr4.replace(str4, replacement4);
    assertEquality("1100011", result4);

    string str5 = "100000100011";
    string result5 = regExpr4.replace(str5, replacementFunctionForReplace);
    assertEquality("16100011", result5);

    string str6 = "100100011";
    var regExpr5 = re `0+`;
    string result6 = regExpr5.replace(str6, replacementFunctionForReplace, 4);
    assertEquality("1001711", result6);
}

isolated function replacementFunctionForReplace(regexp:Groups groups) returns string {
    return groups[0].endIndex.toString();
}

function testReplaceAll() {
    string str1 = "ReplaceTTTGGGThis";
    var regExpr1 = re `T.*G`;
    string replacement1 = " ";
    string result1 = regExpr1.replaceAll(str1, replacement1);
    assertEquality("Replace This", result1);

    string str2 = "100100011";
    var regExpr2 = re `0+`;
    string replacement2 = "*";
    string result2 = regExpr2.replaceAll(str2, replacement2);
    assertEquality("1*1*11", result2);

    //non matching
    string str3 = "100100011";
    var regExpr3 = re `95`;
    string replacement3 = "*";
    string result3 = regExpr3.replaceAll(str3, replacement3);
    assertEquality(str3, result3);

    string str4 = "100100011";
    var regExpr4 = re `0+`;
    string replacement4 = "";
    string result4 = regExpr4.replaceAll(str4, replacement4);
    assertEquality("1111", result4);

    string str5 = "100000100011";
    string result5 = regExpr4.replaceAll(str5, replacementFunctionForReplaceAll);
    assertEquality("121211", result5);
}

isolated function replacementFunctionForReplaceAll(regexp:Groups groups) returns string {
    return groups.length().toString();
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

public function print(any|error value) = @java:Method {
    'class: "org.ballerinalang.langlib.test.LangLibRegexpTest",
    name: "print"
} external;

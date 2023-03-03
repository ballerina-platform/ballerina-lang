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
    regexp:Span? resultSpan4 = regExpr4.find(str1);
    assertTrue(resultSpan4 is ());

    string str3 = "Betty Botter bought some butter but she said the butter’s bitter.";
    var regExpr5 = re `[bB].tt[a-z]*`;
    regexp:Span? res4  = regExpr5.find(str3);
    assertTrue(res4 is regexp:Span);
    regexp:Span resultSpan5 = <regexp:Span>res4;
    assertEquality(0, resultSpan5.startIndex);
    assertEquality(5, resultSpan5.endIndex);
    assertEquality("Betty", resultSpan5.substring());

    regexp:Span? res5  = regExpr5.find(str3, 5);
    assertTrue(res5 is regexp:Span);
    regexp:Span resultSpan6 = <regexp:Span>res5;
    assertEquality(6, resultSpan6.startIndex);
    assertEquality(12, resultSpan6.endIndex);
    assertEquality("Botter", resultSpan6.substring());
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
    assertEquality(1, resultGroups2.length());

    regexp:Span resultSpan2_1 = <regexp:Span>resultGroups2[0];
    assertEquality(0, resultSpan2_1.startIndex);
    assertEquality(3, resultSpan2_1.endIndex);
    assertEquality("GFG", resultSpan2_1.substring());

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
    assertEquality(0, resultSpan3_2.startIndex);
    assertEquality(3, resultSpan3_2.endIndex);
    assertEquality("GFG", resultSpan3_2.substring());

    regexp:Span resultSpan3_3 = <regexp:Span>resultGroups4[2];
    assertEquality(3, resultSpan3_3.startIndex);
    assertEquality(6, resultSpan3_3.endIndex);
    assertEquality("FGF", resultSpan3_3.substring());

    string str3 = "Betty Botter bought some butter but she said the butter’s bitter.";
    var regExpr4 = re `[bB].tt[a-z]*`;
    regexp:Groups? res5  = regExpr4.findGroups(str3);
    assertTrue(res5 is regexp:Groups);
    regexp:Groups resultGroups5 = <regexp:Groups>res5;
    assertEquality(1, resultGroups5.length());

    regexp:Span resultSpan5_1 = <regexp:Span>resultGroups5[0];
    assertEquality(0, resultSpan5_1.startIndex);
    assertEquality(5, resultSpan5_1.endIndex);
    assertEquality("Betty", resultSpan5_1.substring());

    string str4 = "ABC&&DEF";
    var regExpr5 = re `[C&&D]`;
    regexp:Groups? res6 = regExpr5.findGroups(str4);
    assertTrue(res6 is regexp:Groups);
    regexp:Groups resultGroups6 = <regexp:Groups>res6;
    assertEquality(1, resultGroups6.length());

    regexp:Span resultSpan6_1 = <regexp:Span>resultGroups6[0];
    assertEquality(2, resultSpan6_1.startIndex);
    assertEquality(3, resultSpan6_1.endIndex);
    assertEquality("C", resultSpan6_1.substring());

    string str5 = "A B\nC\tD\rE";
    var regExpr6 = re `.`;
    regexp:Groups? res7 = regExpr6.findGroups(str5);
    assertTrue(res7 is regexp:Groups);
    regexp:Groups resultGroups7 = <regexp:Groups>res7;
    assertEquality(1, resultGroups7.length());

    regexp:Span resultSpan7_1 = <regexp:Span>resultGroups7[0];
    assertEquality(0, resultSpan7_1.startIndex);
    assertEquality(1, resultSpan7_1.endIndex);
    assertEquality("A", resultSpan7_1.substring());

    string str6 = "A B\nC\tD\rE";
    var regExpr7 = re `\s`;
    regexp:Groups? res8 = regExpr7.findGroups(str6);
    assertTrue(res8 is regexp:Groups);
    regexp:Groups resultGroups8 = <regexp:Groups>res8;
    assertEquality(1, resultGroups8.length());

    regexp:Span resultSpan8_1 = <regexp:Span>resultGroups8[0];
    assertEquality(1, resultSpan8_1.startIndex);
    assertEquality(2, resultSpan8_1.endIndex);
    assertEquality(" ", resultSpan8_1.substring());

    var regExpr8 = re `\S`;
    regexp:Groups? res9 = regExpr8.findGroups(str6);
    assertTrue(res9 is regexp:Groups);
    regexp:Groups resultGroups9 = <regexp:Groups>res9;
    assertEquality(1, resultGroups9.length());

    regexp:Span resultSpan9_1 = <regexp:Span>resultGroups9[0];
    assertEquality(0, resultSpan9_1.startIndex);
    assertEquality(1, resultSpan9_1.endIndex);
    assertEquality("A", resultSpan9_1.substring());
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
    string str1 = "GFGFGFGFGFGFGFGFGF";
    var regExpr1 = re `(GFG)(FGF)`;
    regexp:Groups[] groupsArr1 = regExpr1.findAllGroups(str1);
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

    regexp:Groups groups1_2 = groupsArr1[1];
    regexp:Span resultSpan1_2_1 = groups1_2[0];
    assertEquality(6, resultSpan1_2_1.startIndex);
    assertEquality(9, resultSpan1_2_1.endIndex);
    assertEquality("GFG", resultSpan1_2_1.substring());

    regexp:Span? resultSpanOrNil1_2_2 = groups1_2[1];
    assertTrue(resultSpanOrNil1_2_2 is regexp:Span);
    regexp:Span resultSpan1_2_2 = <regexp:Span>resultSpanOrNil1_2_2;
    assertEquality(9, resultSpan1_2_2.startIndex);
    assertEquality(12, resultSpan1_2_2.endIndex);
    assertEquality("FGF", resultSpan1_2_2.substring());

    regexp:Groups groups1_3= groupsArr1[2];
    regexp:Span resultSpan1_3_1 = groups1_3[0];
    assertEquality(12, resultSpan1_3_1.startIndex);
    assertEquality(15, resultSpan1_3_1.endIndex);
    assertEquality("GFG", resultSpan1_3_1.substring());

    regexp:Span? resultSpanOrNil1_3_2 = groups1_3[1];
    assertTrue(resultSpanOrNil1_3_2 is regexp:Span);
    regexp:Span resultSpan1_3_2 = <regexp:Span>resultSpanOrNil1_3_2;
    assertEquality(15, resultSpan1_3_2.startIndex);
    assertEquality(18, resultSpan1_3_2.endIndex);
    assertEquality("FGF", resultSpan1_3_2.substring());

    string str2 = "abab";
    string:RegExp regExpr2 = re `((a)(b))`;

    regexp:Groups[] groupsArr2 = regExpr2.findAllGroups(str2);
    assertEquality(2, groupsArr2.length());

    regexp:Groups groups2_1 = groupsArr2[0];
    regexp:Span? resultSpanOrNil2_1_1 = groups2_1[0];
    assertTrue(resultSpanOrNil2_1_1 is regexp:Span);
    regexp:Span resultSpan2_1_1 = <regexp:Span> resultSpanOrNil2_1_1;
    assertEquality(0, resultSpan2_1_1.startIndex);
    assertEquality(2, resultSpan2_1_1.endIndex);
    assertEquality("ab", resultSpan2_1_1.substring());

    regexp:Span? resultSpanOrNil2_1_2 = groups2_1[1];
    assertTrue(resultSpanOrNil2_1_2 is regexp:Span);
    regexp:Span resultSpan2_1_2 = <regexp:Span> resultSpanOrNil2_1_2;
    assertEquality(0, resultSpan2_1_2.startIndex);
    assertEquality(1, resultSpan2_1_2.endIndex);
    assertEquality("a", resultSpan2_1_2.substring());

    regexp:Span? resultSpanOrNil2_1_3 = groups2_1[2];
    assertTrue(resultSpanOrNil2_1_3 is regexp:Span);
    regexp:Span resultSpan2_1_3 = <regexp:Span> resultSpanOrNil2_1_3;
    assertEquality(1, resultSpan2_1_3.startIndex);
    assertEquality(2, resultSpan2_1_3.endIndex);
    assertEquality("b", resultSpan2_1_3.substring());

    regexp:Groups groups2_2 = groupsArr2[1];
    regexp:Span? resultSpanOrNil2_2_1 = groups2_2[0];
    assertTrue(resultSpanOrNil2_2_1 is regexp:Span);
    regexp:Span resultSpan2_2_1 = <regexp:Span> resultSpanOrNil2_2_1;
    assertEquality(2, resultSpan2_2_1.startIndex);
    assertEquality(4, resultSpan2_2_1.endIndex);
    assertEquality("ab", resultSpan2_2_1.substring());

    regexp:Span? resultSpanOrNil2_2_2 = groups2_2[1];
    assertTrue(resultSpanOrNil2_2_2 is regexp:Span);
    regexp:Span resultSpan2_2_2 = <regexp:Span> resultSpanOrNil2_2_2;
    assertEquality(2, resultSpan2_2_2.startIndex);
    assertEquality(3, resultSpan2_2_2.endIndex);
    assertEquality("a", resultSpan2_2_2.substring());

    regexp:Span? resultSpanOrNil2_2_3 = groups2_2[2];
    assertTrue(resultSpanOrNil2_2_3 is regexp:Span);
    regexp:Span resultSpan2_2_3 = <regexp:Span> resultSpanOrNil2_2_3;
    assertEquality(3, resultSpan2_2_3.startIndex);
    assertEquality(4, resultSpan2_2_3.endIndex);
    assertEquality("b", resultSpan2_2_3.substring());

    string:RegExp regExpr3 = re `(a|b)`;
    regexp:Groups[] groupsArr3 = regExpr3.findAllGroups(str2);
    assertEquality(4, groupsArr3.length());

    regexp:Groups groups3_1 = groupsArr3[0];
    regexp:Span? resultSpanOrNil3_1_1 = groups3_1[0];
    assertTrue(resultSpanOrNil3_1_1 is regexp:Span);
    regexp:Span resultSpan3_1_1 = <regexp:Span> resultSpanOrNil3_1_1;
    assertEquality(0, resultSpan3_1_1.startIndex);
    assertEquality(1, resultSpan3_1_1.endIndex);
    assertEquality("a", resultSpan3_1_1.substring());

    regexp:Groups groups3_2 = groupsArr3[1];
    regexp:Span? resultSpanOrNil3_2_1 = groups3_2[0];
    assertTrue(resultSpanOrNil3_2_1 is regexp:Span);
    regexp:Span resultSpan3_2_1 = <regexp:Span> resultSpanOrNil3_2_1;
    assertEquality(1, resultSpan3_2_1.startIndex);
    assertEquality(2, resultSpan3_2_1.endIndex);
    assertEquality("b", resultSpan3_2_1.substring());

    regexp:Groups groups3_3 = groupsArr3[2];
    regexp:Span? resultSpanOrNil3_3_1 = groups3_3[0];
    assertTrue(resultSpanOrNil3_3_1 is regexp:Span);
    regexp:Span resultSpan3_3_1 = <regexp:Span> resultSpanOrNil3_3_1;
    assertEquality(2, resultSpan3_3_1.startIndex);
    assertEquality(3, resultSpan3_3_1.endIndex);
    assertEquality("a", resultSpan3_3_1.substring());

    regexp:Groups groups3_4 = groupsArr3[3];
    regexp:Span? resultSpanOrNil3_4_1 = groups3_4[0];
    assertTrue(resultSpanOrNil3_4_1 is regexp:Span);
    regexp:Span resultSpan3_4_1 = <regexp:Span> resultSpanOrNil3_4_1;
    assertEquality(3, resultSpan3_4_1.startIndex);
    assertEquality(4, resultSpan3_4_1.endIndex);
    assertEquality("b", resultSpan3_4_1.substring());

    string:RegExp regExpr4 = re `a|b`;
    regexp:Groups[] groupsArr4 = regExpr4.findAllGroups(str2);
    assertEquality(4, groupsArr4.length());

    regexp:Groups groups4_1 = groupsArr4[0];
    regexp:Span? resultSpanOrNil4_1_1 = groups4_1[0];
    assertTrue(resultSpanOrNil4_1_1 is regexp:Span);
    regexp:Span resultSpan4_1_1 = <regexp:Span> resultSpanOrNil4_1_1;
    assertEquality(0, resultSpan4_1_1.startIndex);
    assertEquality(1, resultSpan4_1_1.endIndex);
    assertEquality("a", resultSpan4_1_1.substring());

    string:RegExp regExpr5 = re `(c|d)`;
    regexp:Groups[] groupsArr5 = regExpr5.findAllGroups(str2);
    assertEquality(0, groupsArr5.length());

    string:RegExp regExpr6 = re `((c)(d))`;
    regexp:Groups[] groupsArr6 = regExpr6.findAllGroups(str2);
    assertEquality(0, groupsArr6.length());
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
    var regExpr3 = re `A|Th.*ch|^`;
    boolean isFullMatch3 = regExpr3.isFullMatch(str3);
    assertTrue(isFullMatch3);

    var regExpr4 = re `A|Th.*ch|^&`;
    boolean isFullMatch4 = regExpr4.isFullMatch(str3);
    assertTrue(isFullMatch4);
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
    string result11 = regExpr1.replaceAll(str1, replacement1);
    assertEquality("Replace This", result11);
    string result12 = regExpr1.replaceAll(str1, replacement1, 2);
    assertEquality("Replace This", result12);

    string str2 = "100100011";
    var regExpr2 = re `0+`;
    string replacement2 = "*";
    string result21 = regExpr2.replaceAll(str2, replacement2);
    assertEquality("1*1*11", result21);
    string result22 = regExpr2.replaceAll(str2, replacement2, 3);
    assertEquality("1001*11", result22);

    //non matching
    string str3 = "100100011";
    var regExpr3 = re `95`;
    string replacement3 = "*";
    string result31 = regExpr3.replaceAll(str3, replacement3);
    assertEquality(str3, result31);
    string result32 = regExpr3.replaceAll(str3, replacement3, 7);
    assertEquality(str3, result32);

    string str4 = "100100011";
    var regExpr4 = re `0+`;
    string replacement4 = "";
    string result41 = regExpr4.replaceAll(str4, replacement4);
    assertEquality("1111", result41);
    string result42 = regExpr4.replaceAll(str4, replacement4, 3);
    assertEquality("100111", result42);

    string str5 = "100000100011";
    string result51 = regExpr4.replaceAll(str5, replacementFunctionForReplaceAll);
    assertEquality("151311", result51);
    string result52 = regExpr4.replaceAll(str5, replacementFunctionForReplaceAll, 6);
    assertEquality("1000001311", result52);
}

isolated function replacementFunctionForReplaceAll(regexp:Groups groups) returns string {
    return groups[0].substring().length().toString();
}

function testFromString() {
    string:RegExp|error x1 = regexp:fromString("AB+C*D{1,4}");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `AB+C*D{1,4}` == x1);
    }

    x1 = regexp:fromString("A?B+C*?D{1,4}");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `A?B+C*?D{1,4}` == x1);
    }

    x1 = regexp:fromString("A\\sB\\WC\\Dd\\\\");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `A\sB\WC\Dd\\` == x1);
    }

    x1 = regexp:fromString("\\s{1}\\p{sc=Braille}*");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `\s{1}\p{sc=Braille}*` == x1);
    }

    x1 = regexp:fromString("AB+\\p{gc=Lu}{1,}");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `AB+\p{gc=Lu}{1,}` == x1);
    }

    x1 = regexp:fromString("A\\p{Lu}??B+\\W\\(+?C*D{1,4}?");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `A\p{Lu}??B+\W\(+?C*D{1,4}?` == x1);
    }

    x1 = regexp:fromString("\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*` == x1);
    }

    x1 = regexp:fromString("[\\r\\n\\^]");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[\r\n\^]` == x1);
    }

    x1 = regexp:fromString("[A\\sB\\WC\\Dd\\\\]");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[A\sB\WC\Dd\\]` == x1);
    }

    x1 = regexp:fromString("[AB\\p{gc=Lu}]+?");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[AB\p{gc=Lu}]+?` == x1);
    }

    x1 = regexp:fromString("[\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA\\)]??");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA\)]??` == x1);
    }

    x1 = regexp:fromString("[A\\sA-GB\\WC\\DJ-Kd\\\\]*");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[A\sA-GB\WC\DJ-Kd\\]*` == x1);
    }

    x1 = regexp:fromString("[\\sA-F\\p{sc=Braille}K-Mabc-d\\--]");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[\sA-F\p{sc=Braille}K-Mabc-d\--]` == x1);
    }

    x1 = regexp:fromString("[A-B\\p{gc=Lu}\\s-\\SAD\\s-\\w]");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[A-B\p{gc=Lu}\s-\SAD\s-\w]` == x1);
    }

    x1 = regexp:fromString("[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?` == x1);
    }

    x1 = regexp:fromString("(?:ABC)");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?:ABC)` == x1);
    }

    x1 = regexp:fromString("(?i:ABC)");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?i:ABC)` == x1);
    }

    x1 = regexp:fromString("(?i-m:AB+C*)");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?i-m:AB+C*)` == x1);
    }

    x1 = regexp:fromString("(?im-sx:AB+C*D{1})");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?im-sx:AB+C*D{1})` == x1);
    }

    x1 = regexp:fromString("(?:AB+C*D{1,})");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?:AB+C*D{1,})` == x1);
    }

    x1 = regexp:fromString("(?imxs:AB+C*D{1,4})");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?imxs:AB+C*D{1,4})` == x1);
    }

    x1 = regexp:fromString("(?imx-s:A?B+C*?D{1,4})");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?imx-s:A?B+C*?D{1,4})` == x1);
    }

    x1 = regexp:fromString("(?imx-s:A?B+C*?D{1,4})");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?imx-s:A?B+C*?D{1,4})` == x1);
    }

    x1 = regexp:fromString("(?:\\*\\d)");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?:\*\d)` == x1);
    }

    x1 = regexp:fromString("(?i-s:\\s{1}\\p{sc=Braille}*)");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?i-s:\s{1}\p{sc=Braille}*)` == x1);
    }

    x1 = regexp:fromString("(?ims-x:\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*)");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?ims-x:\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*)` == x1);
    }

    x1 = regexp:fromString("(?isxm:[AB\\p{gc=Lu}]+?)");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?isxm:[AB\p{gc=Lu}]+?)` == x1);
    }

    x1 = regexp:fromString("(?isx-m:[A\\p{Lu}B\\W\\(CD]*)");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?isx-m:[A\p{Lu}B\W\(CD]*)` == x1);
    }

    x1 = regexp:fromString("([^\\r\\n\\^a-z])");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `([^\r\n\^a-z])` == x1);
    }

    x1 = regexp:fromString("(?im-sx:[A\\sA-GB\\WC\\DJ-Kd\\\\]*)");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?im-sx:[A\sA-GB\WC\DJ-Kd\\]*)` == x1);
    }

    x1 = regexp:fromString("(?i-s:[\\sA-F\\p{sc=Braille}K-Mabc-d\\--])");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?i-s:[\sA-F\p{sc=Braille}K-Mabc-d\--])` == x1);
    }

    x1 = regexp:fromString("(?i-sxm:[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?)");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?i-sxm:[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?)` == x1);
    }

    x1 = regexp:fromString("(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef` == x1);
    }

    x1 = regexp:fromString("(z)((a+)?(b+)?(c))*");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(z)((a+)?(b+)?(c))*` == x1);
    }

    x1 = regexp:fromString("(?i-m:[0-9])");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?i-m:[0-9])` == x1);
    }

    x1 = regexp:fromString("^^^^^^^robot$$$$");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `^^^^^^^robot$$$$` == x1);
    }

    x1 = regexp:fromString("cx{0,93}c");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `cx{0,93}c` == x1);
    }

    x1 = regexp:fromString("x*y+$");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `x*y+$` == x1);
    }

    x1 = regexp:fromString("[\\d]*[\\s]*bc.");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[\d]*[\s]*bc.` == x1);
    }

    x1 = regexp:fromString("X?y?z?");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `X?y?z?` == x1);
    }

    x1 = regexp:fromString("\\??\\??\\??\\??\\??");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `\??\??\??\??\??` == x1);
    }

    x1 = regexp:fromString(".?.?.?.?.?.?.?");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `.?.?.?.?.?.?.?` == x1);
    }

    x1 = regexp:fromString("bc..[\\d]*[\\s]*");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `bc..[\d]*[\s]*` == x1);
    }

    x1 = regexp:fromString("\\\\u123");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `\\u123` == x1);
    }
}

function testFromStringNegative() {
    string:RegExp|error x1 = regexp:fromString("AB+^*");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character '*'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("AB\\hCD");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character '\\h'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("AB\\pCD");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character '\\p'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("AB\\uCD");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character '\\u'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("AB\\u{001CD");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character '\\u{001CD'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("AB\\p{sc=Lu");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Missing '}' character", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("[^abc");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Missing ']' character", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("(abc");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Missing ')' character", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("(ab^*)");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character '*'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("\\p{");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid end of characters", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("\\p{sc=^}");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character '^'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("\\p{sc=L^}");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character 'L^'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("\\p{gc=");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid end of characters", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("[");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Missing ']' character", <string> checkpanic x1.detail()["message"]);
    }
}

function testSplit() {
    string str1 = "abc cde efg";
    string[] arrExpected1 = ["abc", "cde", "efg"];
    var regExpr1 = re ` `;
    string[] resArr1 = regExpr1.split(str1);
    assertEquality(3, resArr1.length());
    assertEquality(arrExpected1, resArr1);

    string str2 = "abc,cde,efg";
    var regExpr2 = re `,`;
    string[] resArr2 = regExpr2.split(str2);
    assertEquality(3, resArr2.length());
    assertEquality(arrExpected1, resArr2);

    string str3 = "amal,,kamal,,nimal,,sunimal,";
    string[] arrExpected2 = ["amal", "kamal", "nimal", "sunimal,"];
    string[] resArr3 = re `,,`.split(str3);
    assertEquality(4, resArr3.length());
    assertEquality(arrExpected2, resArr3);

    string[] arrExpected4 = [str3];
    string[] resArr4 = re ` `.split(str3);
    assertEquality(1, resArr4.length());
    assertEquality(arrExpected4, resArr4);

    string str5 = "ballerina@geeks@wso2";
    string[] arrExpected5 = ["ballerina", "geeks", "wso2"];
    string[] resArr5 = re `@`.split(str5);
    assertEquality(3, resArr5.length());
    assertEquality(arrExpected5, resArr5);

    string str6 = "ballerina.geeks.wso2";
    string[] arrExpected6 = [str6];
    string[] resArr6 = re `.`.split(str6);
    assertEquality(1, resArr6.length());
    assertEquality(arrExpected6, resArr6);

    string str7 = "zzzzayyyybxxxxawwww";
    string[] arrExpected7 = ["zzzz", "yyyy", "xxxx", "wwww"];
    string[] resArr7 = re `[a-c]`.split(str7);
    assertEquality(4, resArr7.length());
    assertEquality(arrExpected7, resArr7);

    string str8 = "apple|9|1.88;2.78|0#10";
    string[] resArr8 = re `[|;#]`.split(str8);
    string[] arrExpected8 = ["apple", "9", "1.88", "2.78", "0", "10"];
    assertEquality(6, resArr8.length());
    assertEquality(arrExpected8, resArr8);

    string str9 = "1 2   3 4  5";
    string[] resArr9 = re `\s+`.split(str9);
    string[] arrExpected9 = ["1", "2", "3", "4", "5"];
    assertEquality(5, resArr9.length());
    assertEquality(arrExpected9, resArr9);
}

function testLangLibFuncWithNamedArgExpr() {
    regexp:Span? res1 = regexp:find(re = re `World`, str = "HelloWorld");
    assertTrue(res1 is regexp:Span);
    regexp:Span resultSpan1 = <regexp:Span>res1;
    assertEquality(5, resultSpan1.startIndex);
    assertEquality(10, resultSpan1.endIndex);
    assertEquality("World", resultSpan1.substring());

    regexp:Groups? res2 = regexp:findGroups(re = re `World`, str = "HelloWorld");
    assertTrue(res2 is regexp:Groups);
    regexp:Groups resultGroups1 = <regexp:Groups>res2;
    assertEquality(1, resultGroups1.length());
    resultSpan1 = <regexp:Span>resultGroups1[0];
    assertEquality(5, resultSpan1.startIndex);
    assertEquality(10, resultSpan1.endIndex);
    assertEquality("World", resultSpan1.substring());

    regexp:Span[]? res3 = regexp:findAll(re = re `GFG`, str = "GFGFGFGFGFGFGFGFGFG");
    assertTrue(res3 is regexp:Span[]);
    regexp:Span[] resultSpanArr1 = <regexp:Span[]>res3;
    assertEquality(5, resultSpanArr1.length());

    regexp:Span? res4 = regexp:matchAt(re = re `World`, str = "HelloWorld");
    assertTrue(res4 is ());

    regexp:Groups? res5 = regexp:matchGroupsAt(
        re = re `([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9]{1,3}))?`,
        str = "time: 14:35:59", startIndex = 6);
    assertTrue(res5 is regexp:Groups);
    regexp:Groups resultGroups2 = <regexp:Groups>res5;
    regexp:Span resultSpan2_1 = <regexp:Span>resultGroups2[0];
    assertEquality(6, resultSpan2_1.startIndex);
    assertEquality(8, resultSpan2_1.endIndex);
    assertEquality("14", resultSpan2_1.substring());

    boolean isFullMatch1 = regexp:isFullMatch(re = re `(?i:[a-z]+)`, str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    assertTrue(isFullMatch1);

    regexp:Groups? res6 = regexp:fullMatchGroups(re = re `([0-9]+)×([0-9]+)`, str = "1440×900");
    assertTrue(res6 is regexp:Groups);
    regexp:Groups resultGroups3 = <regexp:Groups>res6;
    regexp:Span resultSpan3_1 = <regexp:Span>resultGroups3[0];
    assertEquality(0, resultSpan3_1.startIndex);
    assertEquality(4, resultSpan3_1.endIndex);
    assertEquality("1440", resultSpan3_1.substring());

    string result1 = regexp:replaceAll(re = re `T.*G`, str = "ReplaceTTTGGGThis", replacement = " ");
    assertEquality("Replace This", result1);

    string result2 = regexp:replace(re = re `This`, str = "ReplaceThisThisTextThis", replacement = " ");
    assertEquality("Replace ThisTextThis", result2);

    regexp:Groups[]? res7 = regexp:findAllGroups(re = re `(GFG)(FGF)`, str = "GFGFGFGFGFGFGFGFGF");
    assertTrue(res7 is regexp:Groups[]);
    regexp:Groups[] groupsArr1 = <regexp:Groups[]>res7;
    assertEquality(3, groupsArr1.length());

    string:RegExp|error x1 = regexp:fromString(str = "AB+C*D{1,4}");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `AB+C*D{1,4}` == x1);
    }
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

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

string moduleLevelPattern = "\\p{";

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

    var regExpr6 = re `(?im-sx:H*ere?!?)`;
    regexp:Span? res6  = regExpr6.find("match here", 5);
    assertTrue(res6 is regexp:Span);
    regexp:Span resultSpan7 = <regexp:Span>res6;
    assertEquality(6, resultSpan7.startIndex);
    assertEquality(10, resultSpan7.endIndex);
    assertEquality("here", resultSpan7.substring());

    var regExpr7 = re `${"3*"}`;
    regexp:Span? res71  = regExpr7.find("1233333333", 5);
    assertTrue(res71 is regexp:Span);
    regexp:Span resultSpan81 = <regexp:Span>res71;
    assertEquality(5, resultSpan81.startIndex);
    assertEquality(10, resultSpan81.endIndex);
    assertEquality("33333", resultSpan81.substring());

    regexp:Span? res72  = regExpr7.find("");
    assertTrue(res72 is regexp:Span);
    regexp:Span resultSpan82 = <regexp:Span>res72;
    assertEquality(0, resultSpan82.startIndex);
    assertEquality(0, resultSpan82.endIndex);
    assertEquality("", resultSpan82.substring());

    var regExpr8 = re `${""}`;
    regexp:Span? res8  = regExpr8.find("empty match");
    assertTrue(res8 is regexp:Span);
    regexp:Span resultSpan9 = <regexp:Span>res8;
    assertEquality(0, resultSpan9.startIndex);
    assertEquality(0, resultSpan9.endIndex);
    assertEquality("", resultSpan9.substring());

    var regExpr9 = re `.*`;
    regexp:Span? res9  = regExpr9.find("string", 0);
    assertTrue(res9 is regexp:Span);
    regexp:Span resultSpan10 = <regexp:Span>res9;
    assertEquality(0, resultSpan10.startIndex);
    assertEquality(6, resultSpan10.endIndex);
    assertEquality("string", resultSpan10.substring());

    var regExpr10 = re `\p{sc=Greek}\P{Lu}`;
    regexp:Span? res10  = regExpr10.find("ThisisGreekCharacterWithNumberΑ1", 28);
    assertTrue(res10 is regexp:Span);
    regexp:Span resultSpan11 = <regexp:Span>res10;
    assertEquality(30, resultSpan11.startIndex);
    assertEquality(32, resultSpan11.endIndex);
    assertEquality("Α1", resultSpan11.substring());

    var regExpr11 = re `.`;
    regexp:Span? res11  = regExpr11.find("", 0);
    assertTrue(res11 is ());

    string str12 = "🔜 #RayoBarça 🔵🔴 https://t.co/iHDUx7EmFJ";
    var regExpr12 = re `\p{S}`;
    regexp:Span? result12 = regExpr12.find(str12, 12);
    assertTrue(result12 is regexp:Span);
    regexp:Span resultSpan12 = <regexp:Span>result12;
    assertEquality(13, resultSpan12.startIndex);
    assertEquality(14, resultSpan12.endIndex);
    assertEquality("🔵", resultSpan12.substring());

    regexp:Span? result13 = re `🔵🔴`.find(str12);
    assertTrue(result13 is regexp:Span);
    regexp:Span resultSpan13 = <regexp:Span>result13;
    assertEquality(13, resultSpan13.startIndex);
    assertEquality(15, resultSpan13.endIndex);
    assertEquality("🔵🔴", resultSpan13.substring());
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

    string str7 = "ABC";
    var regExpr9 = re `()`;
    regexp:Groups? res10 = regExpr9.findGroups(str7);
    assertTrue(res10 is regexp:Groups);
    regexp:Groups resultGroups10 = <regexp:Groups>res10;
    assertEquality(2, resultGroups10.length());

    regexp:Span resultSpan10_1 = <regexp:Span>resultGroups10[0];
    assertEquality(0, resultSpan10_1.startIndex);
    assertEquality(0, resultSpan10_1.endIndex);
    assertEquality("", resultSpan10_1.substring());

    regexp:Span resultSpan10_2 = <regexp:Span>resultGroups10[1];
    assertEquality(0, resultSpan10_2.startIndex);
    assertEquality(0, resultSpan10_2.endIndex);
    assertEquality("", resultSpan10_2.substring());

    string str8 = "ABC";
    var regExpr10 = re `${""}`;
    regexp:Groups? res11 = regExpr10.findGroups(str8);
    assertTrue(res11 is regexp:Groups);
    regexp:Groups resultGroups11 = <regexp:Groups>res11;
    assertEquality(1, resultGroups11.length());

    regexp:Span resultSpan11_1 = <regexp:Span>resultGroups11[0];
    assertEquality(0, resultSpan11_1.startIndex);
    assertEquality(0, resultSpan11_1.endIndex);
    assertEquality("", resultSpan11_1.substring());

    string str9 = "ABC";
    var regExpr11 = re `(.*)`;
    regexp:Groups? res12 = regExpr11.findGroups(str9);
    assertTrue(res12 is regexp:Groups);
    regexp:Groups resultGroups12 = <regexp:Groups>res12;
    assertEquality(2, resultGroups12.length());

    regexp:Span resultSpan12_1 = <regexp:Span>resultGroups12[0];
    assertEquality(0, resultSpan12_1.startIndex);
    assertEquality(3, resultSpan12_1.endIndex);
    assertEquality("ABC", resultSpan12_1.substring());

    regexp:Span resultSpan12_2 = <regexp:Span>resultGroups12[1];
    assertEquality(0, resultSpan12_2.startIndex);
    assertEquality(3, resultSpan12_2.endIndex);
    assertEquality("ABC", resultSpan12_2.substring());

    string str10 = "ABC";
    var regExpr12 = re `.*`;
    regexp:Groups? res13 = regExpr12.findGroups(str10);
    assertTrue(res13 is regexp:Groups);
    regexp:Groups resultGroups13 = <regexp:Groups>res13;
    assertEquality(1, resultGroups13.length());

    regexp:Span resultSpan13_1 = <regexp:Span>resultGroups13[0];
    assertEquality(0, resultSpan13_1.startIndex);
    assertEquality(3, resultSpan13_1.endIndex);
    assertEquality("ABC", resultSpan13_1.substring());

    string str11 = "";
    var regExpr13 = re `(.*)`;
    regexp:Groups? res14 = regExpr13.findGroups(str11);
    assertTrue(res14 is regexp:Groups);
    regexp:Groups resultGroups14 = <regexp:Groups>res14;
    assertEquality(2, resultGroups14.length());

    regexp:Span resultSpan14_0 = <regexp:Span>resultGroups14[0];
    assertEquality(0, resultSpan14_0.startIndex);
    assertEquality(0, resultSpan14_0.endIndex);
    assertEquality("", resultSpan14_0.substring());

    regexp:Span resultSpan14_1 = <regexp:Span>resultGroups14[0];
    assertEquality(0, resultSpan14_1.startIndex);
    assertEquality(0, resultSpan14_1.endIndex);
    assertEquality("", resultSpan14_1.substring());

    var regExpr14 = re `\p{S}`;
    string str12 = "🔜 #RayoBarça 🔵🔴 https://t.co/iHDUx7EmFJ";
    regexp:Groups? res15 = regExpr14.findGroups(str12, 1);
    assertTrue(res15 is regexp:Groups);
    regexp:Groups resultGroups15 = <regexp:Groups>res15;
    regexp:Span resultSpan15_0 = <regexp:Span>resultGroups15[0];
    assertEquality(13, resultSpan15_0.startIndex);
    assertEquality(14, resultSpan15_0.endIndex);
    assertEquality("🔵", resultSpan15_0.substring());
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

    string str2 = "ABC";
    var regExpr2 = re `.*`;
    regexp:Span[]? res2 = regExpr2.findAll(str2);
    assertTrue(res2 is regexp:Span[]);
    regexp:Span[] resultSpanArr2 = <regexp:Span[]>res2;
    assertEquality(2, resultSpanArr2.length());

    regexp:Span resultSpan2_1 = resultSpanArr2[0];
    assertEquality(0, resultSpan2_1.startIndex);
    assertEquality(3, resultSpan2_1.endIndex);
    assertEquality("ABC", resultSpan2_1.substring());

    regexp:Span resultSpan2_2 = resultSpanArr2[1];
    assertEquality(3, resultSpan2_2.startIndex);
    assertEquality(3, resultSpan2_2.endIndex);
    assertEquality("", resultSpan2_2.substring());

    string str3 = "ABC";
    var regExpr3 = re `${""}`;
    regexp:Span[]? res3 = regExpr3.findAll(str3, 2);
    assertTrue(res3 is regexp:Span[]);
    regexp:Span[] resultSpanArr3 = <regexp:Span[]>res3;
    assertEquality(2, resultSpanArr3.length());

    regexp:Span resultSpan3_3 = resultSpanArr3[0];
    assertEquality(2, resultSpan3_3.startIndex);
    assertEquality(2, resultSpan3_3.endIndex);
    assertEquality("", resultSpan3_3.substring());

    regexp:Span resultSpan3_4 = resultSpanArr3[1];
    assertEquality(3, resultSpan3_4.startIndex);
    assertEquality(3, resultSpan3_4.endIndex);
    assertEquality("", resultSpan3_4.substring());

    string str4 = "ABC";
    var regExpr4 = re `()`;
    regexp:Span[]? res4 = regExpr4.findAll(str4, 2);
    assertTrue(res3 is regexp:Span[]);
    regexp:Span[] resultSpanArr4 = <regexp:Span[]>res4;
    assertEquality(2, resultSpanArr3.length());

    regexp:Span resultSpan4_1 = resultSpanArr4[0];
    assertEquality(2, resultSpan4_1.startIndex);
    assertEquality(2, resultSpan4_1.endIndex);
    assertEquality("", resultSpan4_1.substring());

    regexp:Span resultSpan4_2 = resultSpanArr4[1];
    assertEquality(3, resultSpan4_2.startIndex);
    assertEquality(3, resultSpan4_2.endIndex);
    assertEquality("", resultSpan4_2.substring());

    string str5 = "";
    var regExpr5 = re `ABC*`;
    regexp:Span[]? res5 = regExpr5.findAll(str5);
    assertTrue(res5 is regexp:Span[]);
    regexp:Span[] resultSpanArr5 = <regexp:Span[]>res5;
    assertEquality(0, resultSpanArr5.length());

    string str6 = "ABC";
    var regExpr6 = re `ABC*`;
    regexp:Span[]? res6 = regExpr6.findAll(str6);
    assertTrue(res6 is regexp:Span[]);
    regexp:Span[] resultSpanArr6 = <regexp:Span[]>res6;
    assertEquality(1, resultSpanArr6.length());

    regexp:Span resultSpan6_1 = resultSpanArr6[0];
    assertEquality(0, resultSpan6_1.startIndex);
    assertEquality(3, resultSpan6_1.endIndex);
    assertEquality("ABC", resultSpan6_1.substring());

    string str7 = "ABC";
    var regExpr7 = re `(ABC)*`;
    regexp:Span[]? res7 = regExpr7.findAll(str7);
    assertTrue(res7 is regexp:Span[]);
    regexp:Span[] resultSpanArr7 = <regexp:Span[]>res7;
    assertEquality(2, resultSpanArr7.length());

    regexp:Span resultSpan7_1 = resultSpanArr7[0];
    assertEquality(0, resultSpan7_1.startIndex);
    assertEquality(3, resultSpan7_1.endIndex);
    assertEquality("ABC", resultSpan7_1.substring());

    regexp:Span resultSpan7_2 = resultSpanArr7[1];
    assertEquality(3, resultSpan7_2.startIndex);
    assertEquality(3, resultSpan7_2.endIndex);
    assertEquality("", resultSpan7_2.substring());

    string str8 = "ABC";
    var regExpr8 = re `ABC`;
    regexp:Span[]? res8 = regExpr8.findAll(str8);
    assertTrue(res8 is regexp:Span[]);
    regexp:Span[] resultSpanArr8 = <regexp:Span[]>res8;
    assertEquality(1, resultSpanArr8.length());

    regexp:Span resultSpan8_1 = resultSpanArr8[0];
    assertEquality(0, resultSpan8_1.startIndex);
    assertEquality(3, resultSpan8_1.endIndex);
    assertEquality("ABC", resultSpan8_1.substring());

    var regExpr9 = re `\p{S}`;
    string str9 = "🔜 #RayoBarça 🔵🔴 https://t.co/iHDUx7EmFJ";
    regexp:Span[]? res9 = regExpr9.findAll(str9, 14);
    assertTrue(res9 is regexp:Span[]);
    regexp:Span[] resultSpanArr9 = <regexp:Span[]>res9;
    assertEquality(1, resultSpanArr8.length());

    regexp:Span resultSpan9_1 = resultSpanArr9[0];
    assertEquality(14, resultSpan9_1.startIndex);
    assertEquality(15, resultSpan9_1.endIndex);
    assertEquality("🔴", resultSpan9_1.substring());
}

function testFindAllGroups() {
    string str1 = "GFGFGFGFGFGFGFGFGF";
    var regExpr1 = re `(GFG)(FGF)`;
    regexp:Groups[] groupsArr1 = regExpr1.findAllGroups(str1);
    assertEquality(3, groupsArr1.length());

    regexp:Groups groups1_1 = groupsArr1[0];
    regexp:Span resultSpan1_1_0 = groups1_1[0];
    assertEquality(0, resultSpan1_1_0.startIndex);
    assertEquality(6, resultSpan1_1_0.endIndex);
    assertEquality("GFGFGF", resultSpan1_1_0.substring());

    regexp:Span? resultSpanOrNil1_1_1 = groups1_1[1];
    assertTrue(resultSpanOrNil1_1_1 is regexp:Span);
    regexp:Span resultSpan1_1_1 = <regexp:Span> resultSpanOrNil1_1_1;
    assertEquality(0, resultSpan1_1_1.startIndex);
    assertEquality(3, resultSpan1_1_1.endIndex);
    assertEquality("GFG", resultSpan1_1_1.substring());

    regexp:Span? resultSpanOrNil1_1_2 = groups1_1[2];
    assertTrue(resultSpanOrNil1_1_2 is regexp:Span);
    regexp:Span resultSpan1_1_2 = <regexp:Span>resultSpanOrNil1_1_2;
    assertEquality(3, resultSpan1_1_2.startIndex);
    assertEquality(6, resultSpan1_1_2.endIndex);
    assertEquality("FGF", resultSpan1_1_2.substring());

    regexp:Groups groups1_2 = groupsArr1[1];
    regexp:Span resultSpan1_2_0 = groups1_2[0];
    assertEquality(6, resultSpan1_2_0.startIndex);
    assertEquality(12, resultSpan1_2_0.endIndex);
    assertEquality("GFGFGF", resultSpan1_2_0.substring());

    regexp:Span? resultSpanOrNil1_2_1 = groups1_2[1];
    assertTrue(resultSpanOrNil1_2_1 is regexp:Span);
    regexp:Span resultSpan1_2_1 = <regexp:Span>resultSpanOrNil1_2_1;
    assertEquality(6, resultSpan1_2_1.startIndex);
    assertEquality(9, resultSpan1_2_1.endIndex);
    assertEquality("GFG", resultSpan1_2_1.substring());

    regexp:Span? resultSpanOrNil1_2_2 = groups1_2[2];
    assertTrue(resultSpanOrNil1_2_2 is regexp:Span);
    regexp:Span resultSpan1_2_2 = <regexp:Span>resultSpanOrNil1_2_2;
    assertEquality(9, resultSpan1_2_2.startIndex);
    assertEquality(12, resultSpan1_2_2.endIndex);
    assertEquality("FGF", resultSpan1_2_2.substring());

    regexp:Groups groups1_3 = groupsArr1[2];
    regexp:Span resultSpan1_3_0 = groups1_3[0];
    assertEquality(12, resultSpan1_3_0.startIndex);
    assertEquality(18, resultSpan1_3_0.endIndex);
    assertEquality("GFGFGF", resultSpan1_3_0.substring());

    regexp:Span? resultSpanOrNil1_3_1 = groups1_3[1];
    assertTrue(resultSpanOrNil1_3_1 is regexp:Span);
    regexp:Span resultSpan1_3_1 = <regexp:Span>resultSpanOrNil1_3_1;
    assertEquality(12, resultSpan1_3_1.startIndex);
    assertEquality(15, resultSpan1_3_1.endIndex);
    assertEquality("GFG", resultSpan1_3_1.substring());

    regexp:Span? resultSpanOrNil1_3_2 = groups1_3[2];
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
    regexp:Span? resultSpanOrNil2_1_0 = groups2_1[0];
    assertTrue(resultSpanOrNil2_1_0 is regexp:Span);
    regexp:Span resultSpan2_1_0 = <regexp:Span> resultSpanOrNil2_1_0;
    assertEquality(0, resultSpan2_1_0.startIndex);
    assertEquality(2, resultSpan2_1_0.endIndex);
    assertEquality("ab", resultSpan2_1_0.substring());

    regexp:Span? resultSpanOrNil2_1_1 = groups2_1[1];
    assertTrue(resultSpanOrNil2_1_1 is regexp:Span);
    regexp:Span resultSpan2_1_1 = <regexp:Span> resultSpanOrNil2_1_1;
    assertEquality(0, resultSpan2_1_1.startIndex);
    assertEquality(2, resultSpan2_1_1.endIndex);
    assertEquality("ab", resultSpan2_1_1.substring());

    regexp:Span? resultSpanOrNil2_1_2 = groups2_1[2];
    assertTrue(resultSpanOrNil2_1_2 is regexp:Span);
    regexp:Span resultSpan2_1_2 = <regexp:Span> resultSpanOrNil2_1_2;
    assertEquality(0, resultSpan2_1_2.startIndex);
    assertEquality(1, resultSpan2_1_2.endIndex);
    assertEquality("a", resultSpan2_1_2.substring());

    regexp:Span? resultSpanOrNil2_1_3 = groups2_1[3];
    assertTrue(resultSpanOrNil2_1_3 is regexp:Span);
    regexp:Span resultSpan2_1_3 = <regexp:Span> resultSpanOrNil2_1_3;
    assertEquality(1, resultSpan2_1_3.startIndex);
    assertEquality(2, resultSpan2_1_3.endIndex);
    assertEquality("b", resultSpan2_1_3.substring());

    regexp:Groups groups2_2 = groupsArr2[1];
    regexp:Span? resultSpanOrNil2_2_0 = groups2_2[0];
    assertTrue(resultSpanOrNil2_2_0 is regexp:Span);
    regexp:Span resultSpan2_2_0 = <regexp:Span> resultSpanOrNil2_2_0;
    assertEquality(2, resultSpan2_2_0.startIndex);
    assertEquality(4, resultSpan2_2_0.endIndex);
    assertEquality("ab", resultSpan2_2_0.substring());

    regexp:Span? resultSpanOrNil2_2_1 = groups2_2[1];
    assertTrue(resultSpanOrNil2_2_1 is regexp:Span);
    regexp:Span resultSpan2_2_1 = <regexp:Span> resultSpanOrNil2_2_1;
    assertEquality(2, resultSpan2_2_1.startIndex);
    assertEquality(4, resultSpan2_2_1.endIndex);
    assertEquality("ab", resultSpan2_2_1.substring());

    regexp:Span? resultSpanOrNil2_2_2 = groups2_2[2];
    assertTrue(resultSpanOrNil2_2_2 is regexp:Span);
    regexp:Span resultSpan2_2_2 = <regexp:Span> resultSpanOrNil2_2_2;
    assertEquality(2, resultSpan2_2_2.startIndex);
    assertEquality(3, resultSpan2_2_2.endIndex);
    assertEquality("a", resultSpan2_2_2.substring());

    regexp:Span? resultSpanOrNil2_2_3 = groups2_2[3];
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

    string:RegExp regExpr7 = re `${""}`;
    regexp:Groups[] groupsArr7 = regExpr7.findAllGroups("string", startIndex = 5);
    assertEquality(2, groupsArr7.length());

    regexp:Groups groups7_1 = groupsArr7[0];
    regexp:Span? resultSpanOrNil7_1_1 = groups7_1[0];
    assertTrue(resultSpanOrNil7_1_1 is regexp:Span);
    regexp:Span resultSpan7_1_1 = <regexp:Span> resultSpanOrNil7_1_1;
    assertEquality(5, resultSpan7_1_1.startIndex);
    assertEquality(5, resultSpan7_1_1.endIndex);
    assertEquality("", resultSpan7_1_1.substring());

    regexp:Groups groups7_2 = groupsArr7[1];
    regexp:Span? resultSpanOrNil7_2_1 = groups7_2[0];
    assertTrue(resultSpanOrNil7_2_1 is regexp:Span);
    regexp:Span resultSpan7_2_1 = <regexp:Span> resultSpanOrNil7_2_1;
    assertEquality(6, resultSpan7_2_1.startIndex);
    assertEquality(6, resultSpan7_2_1.endIndex);
    assertEquality("", resultSpan7_2_1.substring());

    string:RegExp regExpr8 = re `()`;
    regexp:Groups[] groupsArr8 = regExpr8.findAllGroups("A", startIndex = 0);
    assertEquality(2, groupsArr8.length());

    regexp:Groups groups8_1 = groupsArr8[0];
    regexp:Span? resultSpanOrNil8_1_1 = groups8_1[0];
    assertTrue(resultSpanOrNil8_1_1 is regexp:Span);
    regexp:Span resultSpan8_1_1 = <regexp:Span> resultSpanOrNil8_1_1;
    assertEquality(0, resultSpan8_1_1.startIndex);
    assertEquality(0, resultSpan8_1_1.endIndex);
    assertEquality("", resultSpan8_1_1.substring());

    regexp:Groups groups8_2 = groupsArr8[1];
    regexp:Span? resultSpanOrNil8_2_1 = groups8_2[0];
    assertTrue(resultSpanOrNil8_2_1 is regexp:Span);
    regexp:Span resultSpan8_2_1 = <regexp:Span> resultSpanOrNil8_2_1;
    assertEquality(1, resultSpan8_2_1.startIndex);
    assertEquality(1, resultSpan8_2_1.endIndex);
    assertEquality("", resultSpan8_2_1.substring());

    string:RegExp regExpr9 = re `(.*)`;
    regexp:Groups[] groupsArr9 = regExpr9.findAllGroups("");
    assertEquality(1, groupsArr9.length());

    string:RegExp regExpr10 = re `(.*)`;
    regexp:Groups[] groupsArr10 = regExpr10.findAllGroups("ABCD", startIndex = 0);
    assertEquality(2, groupsArr10.length());

    regexp:Groups groups10_1 = groupsArr10[0];
    regexp:Span? resultSpanOrNil10_1_1 = groups10_1[0];
    assertTrue(resultSpanOrNil10_1_1 is regexp:Span);
    regexp:Span resultSpan10_1_1 = <regexp:Span> resultSpanOrNil10_1_1;
    assertEquality(0, resultSpan10_1_1.startIndex);
    assertEquality(4, resultSpan10_1_1.endIndex);
    assertEquality("ABCD", resultSpan10_1_1.substring());

    regexp:Groups groups10_2 = groupsArr10[1];
    regexp:Span? resultSpanOrNil10_2_1 = groups10_2[0];
    assertTrue(resultSpanOrNil10_2_1 is regexp:Span);
    regexp:Span resultSpan10_2_1 = <regexp:Span> resultSpanOrNil10_2_1;
    assertEquality(4, resultSpan10_2_1.startIndex);
    assertEquality(4, resultSpan10_2_1.endIndex);
    assertEquality("", resultSpan10_2_1.substring());

    string:RegExp regExpr11 = re `${"(.)"}`;
    regexp:Groups[] groupsArr11 = regExpr11.findAllGroups("ABCD", startIndex = 2);
    assertEquality(2, groupsArr11.length());

    regexp:Groups groups11_1 = groupsArr11[0];
    regexp:Span? resultSpanOrNil11_1_1 = groups11_1[0];
    assertTrue(resultSpanOrNil11_1_1 is regexp:Span);
    regexp:Span resultSpan11_1_1 = <regexp:Span> resultSpanOrNil11_1_1;
    assertEquality(2, resultSpan11_1_1.startIndex);
    assertEquality(3, resultSpan11_1_1.endIndex);
    assertEquality("C", resultSpan11_1_1.substring());

    regexp:Groups groups11_2 = groupsArr11[1];
    regexp:Span? resultSpanOrNil11_2_1 = groups11_2[0];
    assertTrue(resultSpanOrNil11_2_1 is regexp:Span);
    regexp:Span resultSpan11_2_1 = <regexp:Span> resultSpanOrNil11_2_1;
    assertEquality(3, resultSpan11_2_1.startIndex);
    assertEquality(4, resultSpan11_2_1.endIndex);
    assertEquality("D", resultSpan11_2_1.substring());

    string:RegExp regExpr12 = re `(\p{S})`;
    string str12 = "🔜 #RayoBarça 🔵🔴 https://t.co/iHDUx7EmFJ";
    regexp:Groups[] groupsArr12 = regExpr12.findAllGroups(str12, 1);
    assertEquality(2, groupsArr12.length());

    regexp:Groups groups12_1 = groupsArr12[0];
    regexp:Span? resultSpanOrNil12_1_1 = groups12_1[0];
    assertTrue(resultSpanOrNil12_1_1 is regexp:Span);
    regexp:Span resultSpan12_1_1 = <regexp:Span> resultSpanOrNil12_1_1;
    assertEquality(13, resultSpan12_1_1.startIndex);
    assertEquality(14, resultSpan12_1_1.endIndex);
    assertEquality("🔵", resultSpan12_1_1.substring());

    regexp:Groups groups12_2 = groupsArr12[1];
    regexp:Span? resultSpanOrNil12_2_1 = groups12_2[0];
    assertTrue(resultSpanOrNil12_2_1 is regexp:Span);
    regexp:Span resultSpan12_2_1 = <regexp:Span> resultSpanOrNil12_2_1;
    assertEquality(14, resultSpan12_2_1.startIndex);
    assertEquality(15, resultSpan12_2_1.endIndex);
    assertEquality("🔴", resultSpan12_2_1.substring());
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

    string str3 = "";
    var regExpr3 = re `.`;
    regexp:Span? res4 = regExpr3.matchAt(str3);
    assertTrue(res4 is ());

    string str4 = "AB";
    var regExpr4 = re `.*`;
    regexp:Span? res5 = regExpr4.matchAt(str4);
    assertTrue(res5 is regexp:Span);
    regexp:Span resultSpan3 = <regexp:Span>res5;
    assertEquality(0, resultSpan3.startIndex);
    assertEquality(2, resultSpan3.endIndex);
    assertEquality("AB", resultSpan3.substring());

    string str5 = "ABC";
    var regExpr5 = re `ABC`;
    regexp:Span? res6 = regExpr5.matchAt(str5, 1);
    assertTrue(res6 is ());

    string str6 = "ABC";
    var regExpr6 = re `((A*)BC)`;
    regexp:Span? res7 = regExpr6.matchAt(str6, 1);
    assertTrue(res7 is regexp:Span);
    regexp:Span resultSpan4 = <regexp:Span>res7;
    assertEquality(1, resultSpan4.startIndex);
    assertEquality(3, resultSpan4.endIndex);
    assertEquality("BC", resultSpan4.substring());

    string str7 = "ABC";
    var regExpr7 = re `${""}`;
    regexp:Span? res8 = regExpr7.matchAt(str7, 1);
    assertTrue(res8 is ());

    string str8 = "🔜🔵🔴";
    var regExpr8 = re `\p{S}+`;
    regexp:Span? result8 = regExpr8.matchAt(str8, 1);
    assertTrue(result8 is regexp:Span);
    regexp:Span resultSpan8 = <regexp:Span>result8;
    assertEquality(1, resultSpan8.startIndex);
    assertEquality(3, resultSpan8.endIndex);
    assertEquality("🔵🔴", resultSpan8.substring());
}

function testMatchGroupsAt() {
    var regExpr1 = re `([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9]{1,3}))?`;
    string str1 = "time: 14:35:59";

    regexp:Groups? res1 = regExpr1.matchGroupsAt(str1);
    assertTrue(res1 is ());

    regexp:Groups? res2 = regExpr1.matchGroupsAt(str1, 6);
    assertTrue(res2 is regexp:Groups);

    regexp:Groups resultGroups1 = <regexp:Groups>res2;
    regexp:Span resultSpan1_0 = <regexp:Span>resultGroups1[0];
    assertEquality(6, resultSpan1_0.startIndex);
    assertEquality(14, resultSpan1_0.endIndex);
    assertEquality("14:35:59", resultSpan1_0.substring());

    regexp:Span resultSpan1_1 = <regexp:Span>resultGroups1[1];
    assertEquality(6, resultSpan1_1.startIndex);
    assertEquality(8, resultSpan1_1.endIndex);
    assertEquality("14", resultSpan1_1.substring());

    regexp:Span resultSpan1_2 = <regexp:Span>resultGroups1[2];
    assertEquality(9, resultSpan1_2.startIndex);
    assertEquality(11, resultSpan1_2.endIndex);
    assertEquality("35", resultSpan1_2.substring());

    regexp:Span resultSpan1_3 = <regexp:Span>resultGroups1[3];
    assertEquality(12, resultSpan1_3.startIndex);
    assertEquality(14, resultSpan1_3.endIndex);
    assertEquality("59", resultSpan1_3.substring());

    var regExpr2 = re `([0-9]+)×([0-9]+)`;
    string str2 = "1440×900";

    regexp:Groups? res3 = regExpr2.matchGroupsAt(str2);
    assertTrue(res3 is regexp:Groups);
    regexp:Groups resultGroups2 = <regexp:Groups>res3;
    regexp:Span resultSpan2_0 = <regexp:Span>resultGroups2[0];
    assertEquality(0, resultSpan2_0.startIndex);
    assertEquality(8, resultSpan2_0.endIndex);
    assertEquality("1440×900", resultSpan2_0.substring());

    regexp:Span resultSpan2_1 = <regexp:Span>resultGroups2[1];
    assertEquality(0, resultSpan2_1.startIndex);
    assertEquality(4, resultSpan2_1.endIndex);
    assertEquality("1440", resultSpan2_1.substring());

    regexp:Span resultSpan2_2 = <regexp:Span>resultGroups2[2];
    assertEquality(5, resultSpan2_2.startIndex);
    assertEquality(8, resultSpan2_2.endIndex);
    assertEquality("900", resultSpan2_2.substring());

    string str3 = "";
    var regExpr3 = re `(.)`;
    regexp:Groups? res4 = regExpr3.matchGroupsAt(str3);
    assertTrue(res4 is ());

    string str4 = "AB";
    var regExpr4_1 = re `.*`;
    regexp:Groups? res5_1 = regExpr4_1.matchGroupsAt(str4);
    assertTrue(res5_1 is regexp:Groups);
    regexp:Groups resultGroups3_1 = <regexp:Groups>res5_1;
    assertEquality(resultGroups3_1.length(), 1);

    regexp:Span resultSpan3_1 = <regexp:Span>resultGroups3_1[0];
    assertEquality(0, resultSpan3_1.startIndex);
    assertEquality(2, resultSpan3_1.endIndex);
    assertEquality("AB", resultSpan3_1.substring());

    var regExpr4_2 = re `(.*)`;
    regexp:Groups? res5_2 = regExpr4_2.matchGroupsAt(str4);
    assertTrue(res5_2 is regexp:Groups);
    regexp:Groups resultGroups3_2 = <regexp:Groups>res5_2;
    assertEquality(resultGroups3_2.length(), 2);

    regexp:Span resultSpan3_2_0 = <regexp:Span>resultGroups3_2[0];
    assertEquality(0, resultSpan3_2_0.startIndex);
    assertEquality(2, resultSpan3_2_0.endIndex);
    assertEquality("AB", resultSpan3_2_0.substring());

    regexp:Span resultSpan3_2_1 = <regexp:Span>resultGroups3_2[0];
    assertEquality(0, resultSpan3_2_1.startIndex);
    assertEquality(2, resultSpan3_2_1.endIndex);
    assertEquality("AB", resultSpan3_2_1.substring());

    string str5 = "ABC";
    var regExpr5 = re `(ABC)`;
    regexp:Groups? res6 = regExpr5.matchGroupsAt(str5, 1);
    assertTrue(res6 is ());

    string str6 = "ABC";
    var regExpr6 = re `((A*)BC)`;
    regexp:Groups? res7 = regExpr6.matchGroupsAt(str6, 1);
    assertTrue(res7 is regexp:Groups);
    regexp:Groups resultGroups4 = <regexp:Groups>res7;
    assertEquality(resultGroups4.length(), 3);

    regexp:Span resultSpan4_0 = <regexp:Span>resultGroups4[0];
    assertEquality(1, resultSpan4_0.startIndex);
    assertEquality(3, resultSpan4_0.endIndex);
    assertEquality("BC", resultSpan4_0.substring());

    regexp:Span resultSpan4_1 = <regexp:Span>resultGroups4[0];
    assertEquality(1, resultSpan4_1.startIndex);
    assertEquality(3, resultSpan4_1.endIndex);
    assertEquality("BC", resultSpan4_1.substring());

    regexp:Span resultSpan4_2 = <regexp:Span>resultGroups4[1];
    assertEquality(1, resultSpan4_2.startIndex);
    assertEquality(3, resultSpan4_2.endIndex);
    assertEquality("BC", resultSpan4_2.substring());

    string str7 = "ABC";
    var regExpr7 = re `${""}`;
    regexp:Groups? res8 = regExpr7.matchGroupsAt(str7, 1);
    assertTrue(res8 is ());

   string str8 = "";
    var regExpr8 = re `${""}`;
    regexp:Groups? res9 = regExpr8.matchGroupsAt(str8);
    assertTrue(res9 is regexp:Groups);
    regexp:Groups resultGroups5 = <regexp:Groups>res9;
    assertEquality(resultGroups5.length(), 1);

    regexp:Span resultSpan5 = <regexp:Span>resultGroups5[0];
    assertEquality(0, resultSpan5.startIndex);
    assertEquality(0, resultSpan5.endIndex);
    assertEquality("", resultSpan5.substring());

    string str9 = "ABC";
    var regExpr9 = re `ABC`;
    regexp:Groups? res10 = regExpr9.matchGroupsAt(str9);
    assertTrue(res10 is regexp:Groups);
    regexp:Groups resultGroups6 = <regexp:Groups>res10;
    assertEquality(resultGroups6.length(), 1);

    regexp:Span resultSpan6 = <regexp:Span>resultGroups6[0];
    assertEquality(0, resultSpan6.startIndex);
    assertEquality(3, resultSpan6.endIndex);
    assertEquality("ABC", resultSpan6.substring());

    string str10 = " ABC";
    var regExpr10 = re `(ABC)`;
    regexp:Groups? res11 = regExpr10.matchGroupsAt(str10, 1);
    assertTrue(res11 is regexp:Groups);
    regexp:Groups resultGroups7 = <regexp:Groups>res11;
    assertEquality(resultGroups7.length(), 2);

    regexp:Span resultSpan7_0 = <regexp:Span>resultGroups7[0];
    assertEquality(1, resultSpan7_0.startIndex);
    assertEquality(4, resultSpan7_0.endIndex);
    assertEquality("ABC", resultSpan7_0.substring());

    regexp:Span resultSpan7_1 = <regexp:Span>resultGroups7[0];
    assertEquality(1, resultSpan7_1.startIndex);
    assertEquality(4, resultSpan7_1.endIndex);
    assertEquality("ABC", resultSpan7_1.substring());
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

    var regExpr5 = re `${""}`;
    boolean isFullMatch5 = regExpr5.isFullMatch("");
    assertTrue(isFullMatch5);

    var regExpr6 = re `${""}`;
    boolean isFullMatch6 = regExpr6.isFullMatch("ABC");
    assertFalse(isFullMatch6);

    var regExpr7 = re `(ABC)`;
    boolean isFullMatch7 = regExpr7.isFullMatch("");
    assertFalse(isFullMatch7);

    var regExpr8 = re `.*`;
    boolean isFullMatch8 = regExpr8.isFullMatch("");
    assertTrue(isFullMatch8);

    var regExpr9 = re `.`;
    boolean isFullMatch9 = regExpr9.isFullMatch("");
    assertFalse(isFullMatch9);

    var regExpr10 = re `${""}`;
    boolean isFullMatch10 = regExpr10.isFullMatch("");
    assertTrue(isFullMatch10);

    var regExpr11 = re `a|`;
    boolean isFullMatch11 = regExpr11.isFullMatch("");
    assertTrue(isFullMatch11);
}

function testFullMatchGroups() {
    var regExpr1 = re `([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9]{1,3}))?`;
    string str1 = "time: 14:35:59";

    regexp:Groups? res1 = regExpr1.fullMatchGroups(str1);
    assertTrue(res1 is ());

    string str2 = "14:35:59";
    regexp:Groups? res2 = regExpr1.fullMatchGroups(str2);
    assertTrue(res2 is regexp:Groups);
    regexp:Groups resultGroups1 = <regexp:Groups>res2;
    regexp:Span resultSpan1_0 = <regexp:Span>resultGroups1[0];
    assertEquality(0, resultSpan1_0.startIndex);
    assertEquality(8, resultSpan1_0.endIndex);
    assertEquality("14:35:59", resultSpan1_0.substring());

    regexp:Span resultSpan1_1 = <regexp:Span>resultGroups1[1];
    assertEquality(0, resultSpan1_1.startIndex);
    assertEquality(2, resultSpan1_1.endIndex);
    assertEquality("14", resultSpan1_1.substring());

    regexp:Span resultSpan1_2 = <regexp:Span>resultGroups1[2];
    assertEquality(3, resultSpan1_2.startIndex);
    assertEquality(5, resultSpan1_2.endIndex);
    assertEquality("35", resultSpan1_2.substring());

    regexp:Span resultSpan1_3 = <regexp:Span>resultGroups1[3];
    assertEquality(6, resultSpan1_3.startIndex);
    assertEquality(8, resultSpan1_3.endIndex);
    assertEquality("59", resultSpan1_3.substring());

    var regExpr2 = re `([0-9]+)×([0-9]+)`;
    string str3 = "1440×900";

    regexp:Groups? res3 = regExpr2.fullMatchGroups(str3);
    assertTrue(res3 is regexp:Groups);
    regexp:Groups resultGroups2 = <regexp:Groups>res3;
    regexp:Span resultSpan2_0 = <regexp:Span>resultGroups2[0];
    assertEquality(0, resultSpan2_0.startIndex);
    assertEquality(8, resultSpan2_0.endIndex);
    assertEquality("1440×900", resultSpan2_0.substring());

    regexp:Span resultSpan2_1 = <regexp:Span>resultGroups2[1];
    assertEquality(0, resultSpan2_1.startIndex);
    assertEquality(4, resultSpan2_1.endIndex);
    assertEquality("1440", resultSpan2_1.substring());

    regexp:Span resultSpan2_2 = <regexp:Span>resultGroups2[2];
    assertEquality(5, resultSpan2_2.startIndex);
    assertEquality(8, resultSpan2_2.endIndex);
    assertEquality("900", resultSpan2_2.substring());

    var regExpr3 = re `${""}`;
    regexp:Groups? res4 = regExpr3.fullMatchGroups("");
    regexp:Groups resultGroups3 = <regexp:Groups>res4;
    regexp:Span resultSpan3_0 = <regexp:Span>resultGroups3[0];
    assertEquality(0, resultSpan3_0.startIndex);
    assertEquality(0, resultSpan3_0.endIndex);
    assertEquality("", resultSpan3_0.substring());

    var regExpr4 = re `()`;
    regexp:Groups? res5 = regExpr4.fullMatchGroups("");

    assertTrue(res5 is regexp:Groups);
    regexp:Groups resultGroups4 = <regexp:Groups>res5;
    assertEquality(2, resultGroups4.length());

    regexp:Span resultSpan4_0 = <regexp:Span>resultGroups4[0];
    assertEquality(0, resultSpan4_0.startIndex);
    assertEquality(0, resultSpan4_0.endIndex);
    assertEquality("", resultSpan4_0.substring());

    regexp:Span resultSpan4_1 = <regexp:Span>resultGroups4[1];
    assertEquality(0, resultSpan4_1.startIndex);
    assertEquality(0, resultSpan4_1.endIndex);
    assertEquality("", resultSpan4_1.substring());

    var regExpr5 = re `(.)`;
    regexp:Groups? res6 = regExpr5.fullMatchGroups("");
    assertTrue(res6 is ());

    var regExpr6 = re `A`;
    regexp:Groups? res7 = regExpr6.fullMatchGroups("A");
    assertTrue(res7 is regexp:Groups);
    regexp:Groups resultGroups5 = <regexp:Groups>res7;
    assertEquality(1, resultGroups5.length());
    regexp:Span resultSpan5 = <regexp:Span>resultGroups5[0];
    assertEquality(0, resultSpan5.startIndex);
    assertEquality(1, resultSpan5.endIndex);
    assertEquality("A", resultSpan5.substring());

    var regExpr7 = re `(A)`;
    regexp:Groups? res8 = regExpr7.fullMatchGroups("A");
    assertTrue(res8 is regexp:Groups);
    regexp:Groups resultGroups6 = <regexp:Groups>res8;
    assertEquality(2, resultGroups6.length());

    regexp:Span resultSpan6_0 = <regexp:Span>resultGroups6[0];
    assertEquality(0, resultSpan6_0.startIndex);
    assertEquality(1, resultSpan6_0.endIndex);
    assertEquality("A", resultSpan6_0.substring());

    regexp:Span resultSpan6_1 = <regexp:Span>resultGroups6[1];
    assertEquality(0, resultSpan6_1.startIndex);
    assertEquality(1, resultSpan6_1.endIndex);
    assertEquality("A", resultSpan6_1.substring());

    var regExpr8 = re `(.*)`;
    regexp:Groups? res9 = regExpr8.fullMatchGroups("A");
    assertTrue(res9 is regexp:Groups);
    regexp:Groups resultGroups7 = <regexp:Groups>res9;
    assertEquality(2, resultGroups7.length());

    regexp:Span resultSpan7_0 = <regexp:Span>resultGroups7[0];
    assertEquality(0, resultSpan7_0.startIndex);
    assertEquality(1, resultSpan7_0.endIndex);
    assertEquality("A", resultSpan7_0.substring());

    regexp:Span resultSpan7_1 = <regexp:Span>resultGroups7[1];
    assertEquality(0, resultSpan7_1.startIndex);
    assertEquality(1, resultSpan7_1.endIndex);
    assertEquality("A", resultSpan7_1.substring());

    var regExpr9 = re `(A*B)|(AB)|(D*A*E*B*R*)`;
    regexp:Groups? res10 = regExpr9.fullMatchGroups("AB");
    assertTrue(res9 is regexp:Groups);
    regexp:Groups resultGroups8 = <regexp:Groups>res10;
    assertEquality(2, resultGroups8.length());

    regexp:Span resultSpan8_0 = <regexp:Span>resultGroups8[0];
    assertEquality(0, resultSpan8_0.startIndex);
    assertEquality(2, resultSpan8_0.endIndex);
    assertEquality("AB", resultSpan8_0.substring());

    regexp:Span resultSpan8_1 = <regexp:Span>resultGroups8[1];
    assertEquality(0, resultSpan8_1.startIndex);
    assertEquality(2, resultSpan8_1.endIndex);
    assertEquality("AB", resultSpan8_1.substring());

    var regExpr10 = re `(A*)(B)|(AB)|(D*A*E*B*R*)`;
    regexp:Groups? res11 = regExpr10.fullMatchGroups("AB");
    assertTrue(res10 is regexp:Groups);
    regexp:Groups resultGroups9 = <regexp:Groups>res11;
    assertEquality(3, resultGroups9.length());

    regexp:Span resultSpan9_0 = <regexp:Span>resultGroups9[0];
    assertEquality(0, resultSpan9_0.startIndex);
    assertEquality(2, resultSpan9_0.endIndex);
    assertEquality("AB", resultSpan9_0.substring());

    regexp:Span resultSpan9_1 = <regexp:Span>resultGroups9[1];
    assertEquality(0, resultSpan9_1.startIndex);
    assertEquality(1, resultSpan9_1.endIndex);
    assertEquality("A", resultSpan9_1.substring());

    regexp:Span resultSpan9_2 = <regexp:Span>resultGroups9[2];
    assertEquality(1, resultSpan9_2.startIndex);
    assertEquality(2, resultSpan9_2.endIndex);
    assertEquality("B", resultSpan9_2.substring());
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

    string str7 = "";
    var regExpr6 = re `${""}`;
    string result7 = regExpr6.replace(str7, replacementFunctionForReplace);
    assertEquality("0", result7);

    string str8 = "";
    var regExpr7 = re `${""}`;
    string result8 = regExpr7.replace(str8, "A");
    assertEquality("A", result8);

    string str9 = "B";
    var regExpr8 = re `${""}`;
    string result9 = regExpr8.replace(str9, "A");
    assertEquality("AB", result9);

    string str10 = "";
    var regExpr9 = re `(AB)`;
    string result10 = regExpr9.replace(str10, "D");
    assertEquality("", result10);

    string str11 = "EABC";
    var regExpr10 = re `A|B|C`;
    string result11_1 = regExpr10.replace(str11, "D", 1);
    assertEquality("EDBC", result11_1);
    string result11_2 = regExpr10.replace(str11, replacementFunctionForReplace, 1);
    assertEquality("E2BC", result11_2);

    string str12 = "DDAAAABBCC";
    var regExpr11 = re `B*|A*|C*`;
    string result12_1 = regExpr11.replace(str12, "E", 3);
    assertEquality("DDAEAAABBCC", result12_1);
    string result12_2 = regExpr11.replace(str12, replacementFunctionForReplace, 3);
    assertEquality("DDA3AAABBCC", result12_2);

    string str13 = "";
    var regExpr12 = re `()`;
    string result13 = regExpr12.replace(str13, "A");
    assertEquality("A", result13);

    string str14 = "ABCD";
    var regExpr13 = re `()`;
    string result14 = regExpr13.replace(str14, "E");
    assertEquality("EABCD", result14);

    string str15 = "ABCD";
    var regExpr14 = re `.*`;
    string result15 = regExpr14.replace(str15, replacementFunctionForReplace, 1);
    assertEquality("A4", result15);

    string str16 = "ABCD";
    var regExpr15 = re `BCD`;
    string result16 = regExpr15.replace(str16, replacementFunctionForReplace, 1);
    assertEquality("A4", result16);

    string str17 = "AB";
    var regExpr16_1 = re `A|AB|AK`;
    string result17_1 = regExpr16_1.replace(str17, "Z");
    assertEquality("ZB", result17_1);
    var regExpr16_2 = re `AB|A|AK`;
    string result17_2 = regExpr16_2.replace(str17, "Z");
    assertEquality("Z", result17_2);

    string str18 = "🔜 #RayoBarça 🔵🔴 https://t.co/iHDUx7EmFJ";
    var regExpr17 = re `\p{S}`;
    string result17 = regExpr17.replace(str18, replacementFunctionForReplace);
    assertEquality("1 #RayoBarça 🔵🔴 https://t.co/iHDUx7EmFJ", result17);

    string result18 = regExpr17.replace(str18, replacementFunctionForReplace, 4);
    assertEquality("🔜 #RayoBarça 14🔴 https://t.co/iHDUx7EmFJ", result18);

    string result19 = regExpr17.replace(str18, "");
    assertEquality(" #RayoBarça 🔵🔴 https://t.co/iHDUx7EmFJ", result19);

    string result20 = regExpr17.replace(str18, "", 3);
    assertEquality("🔜 #RayoBarça 🔴 https://t.co/iHDUx7EmFJ", result20);
}

isolated function replacementFunctionForReplace(regexp:Groups groups) returns string {
    return groups[0].endIndex.toString();
}

function testReplaceAll() {
    string str1 = "ReplaceTTTGGGThis";
    var regExpr1 = re `T.*G`;
    string replacement1 = " ";
    string result1_1 = regExpr1.replaceAll(str1, replacement1);
    assertEquality("Replace This", result1_1);
    string result1_2 = regExpr1.replaceAll(str1, replacement1, 2);
    assertEquality("Replace This", result1_2);

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

    string str6 = "ABCD";
    var regExpr5 = re `A|B|C|D|R`;
    string result6 = regExpr5.replaceAll(str6, replacementFunctionForReplace);
    assertEquality("1234", result6);

    string str7 = "";
    var regExpr6 = re `${""}`;
    string result7 = regExpr6.replaceAll(str7, replacementFunctionForReplace);
    assertEquality("0", result7);

    string str8 = "";
    var regExpr7 = re `${""}`;
    string result8 = regExpr7.replaceAll(str8, "A");
    assertEquality("A", result8);

    string str9 = "B";
    var regExpr8 = re `${""}`;
    string result9 = regExpr8.replaceAll(str9, "A");
    assertEquality("ABA", result9);

    string str10 = "";
    var regExpr9 = re `(AB)`;
    string result10 = regExpr9.replaceAll(str10, "D");
    assertEquality("", result10);

    string str11 = "EABC";
    var regExpr10 = re `A|B|C`;
    string result11_1 = regExpr10.replaceAll(str11, "D", 1);
    assertEquality("EDDD", result11_1);
    string result11_2 = regExpr10.replaceAll(str11, replacementFunctionForReplace, 2);
    assertEquality("EA34", result11_2);

    string str12 = "DDAAAABBCC";
    var regExpr11 = re `B*|A*|C*`;
    string result12_1 = regExpr11.replaceAll(str12, "E", 3);
    assertEquality("DDAEAEAEAEECECE", result12_1);
    string result12_2 = regExpr11.replaceAll(str12, replacementFunctionForReplace, 3);
    assertEquality("DDA3A4A5A88C9C10", result12_2);

    string str13 = "";
    var regExpr12 = re `()`;
    string result13 = regExpr12.replaceAll(str13, "A");
    assertEquality("A", result13);

    string str14 = "ABCD";
    var regExpr13 = re `()`;
    string result14 = regExpr13.replaceAll(str14, "E");
    assertEquality("EAEBECEDE", result14);

    string str15 = "ABCD";
    var regExpr14 = re `.*`;
    string result15 = regExpr14.replaceAll(str15, replacementFunctionForReplace, 1);
    assertEquality("A44", result15);

    string str16 = "ABCD";
    var regExpr15 = re `BCD`;
    string result16 = regExpr15.replaceAll(str16, replacementFunctionForReplace, 1);
    assertEquality("A4", result16);

    string str17 = "AB";
    var regExpr16_1 = re `A|AB|AK`;
    string result17_1 = regExpr16_1.replaceAll(str17, "Z");
    assertEquality("ZB", result17_1);
    var regExpr16_2 = re `AB|A|AK`;
    string result17_2 = regExpr16_2.replaceAll(str17, "Z");
    assertEquality("Z", result17_2);

    var regExpr18 = re `\p{S}`;
    string str18 = "🔜 #RayoBarça 🔵🔴 https://t.co/iHDUx7EmFJ";
    string result18_1 = regExpr18.replaceAll(str18, "");
    assertEquality(" #RayoBarça  https://t.co/iHDUx7EmFJ", result18_1);
    string result18_2 = regExpr18.replaceAll(str18, "", 12);
    assertEquality("🔜 #RayoBarça  https://t.co/iHDUx7EmFJ", result18_2);

    var regExpr19 = re `(\d{2})/(\d{2})/(\d{4})`;
    string str19 = "04/05/2023, 05/05/2023";
    string result19 = regExpr19.replaceAll(str19, updateDateFormat);
    assertEquality("2023-05-04, 2023-05-05", result19);

    string result20_1 = regExpr18.replaceAll(str18, "🔴");
    assertEquality("🔴 #RayoBarça 🔴🔴 https://t.co/iHDUx7EmFJ", result20_1);
    string result20_2 = regExpr18.replaceAll(str18, "🔴", 12);
    assertEquality("🔜 #RayoBarça 🔴🔴 https://t.co/iHDUx7EmFJ", result20_2);
}

isolated function replacementFunctionForReplaceAll(regexp:Groups groups) returns string {
    return groups[0].substring().length().toString();
}

isolated function updateDateFormat(regexp:Groups groups) returns string {
    if groups.length() != 4 {
        return (<regexp:Span>groups[0]).substring();
    }
    string year = (<regexp:Span>groups[3]).substring();
    string month = (<regexp:Span>groups[2]).substring();
    string day = (<regexp:Span>groups[1]).substring();
    return string `${year}-${month}-${day}`;
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

    x1 = regexp:fromString("");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `${""}` == x1);
    }

    x1 = regexp:fromString(string `${""}`);
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `${""}` == x1);
    }

    x1 = regexp:fromString(string `${"`"}`);
    assertTrue(x1 is string:RegExp);

    x1 = regexp:fromString("[[ab\\]]");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[[ab\]]` == x1);
    }

    x1 = regexp:fromString("A&B\\s|&D");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `A&B\s|&D` == x1);
    }
        
    x1 = regexp:fromString("\\p{Ll}|\\p{Mn}|\\p{Nd}|\\p{Sm}|\\p{Pc}|\\p{Cc}|\\p{Zs}");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `\p{Ll}|\p{Mn}|\p{Nd}|\p{Sm}|\p{Pc}|\p{Cc}|\p{Zs}` == x1);
    }
    
    x1 = regexp:fromString("\\d{2}-\\d{2}-\\d{4}");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `\d{2}-\d{2}-\d{4}` == x1);
    }
    
    x1 = regexp:fromString("\\d{2}:\\d{2}:\\d{4}");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `\d{2}:\d{2}:\d{4}` == x1);
    }
    
    x1 = regexp:fromString("(?:\\d{2}/\\d{2}/\\d{4})");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `(?:\d{2}/\d{2}/\d{4})` == x1);
    }
    
    x1 = regexp:fromString("abc|de|");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `abc|de|` == x1);
    }
    
    x1 = regexp:fromString("abc|de\\|");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `abc|de\|` == x1);
    }
    
    x1 = regexp:fromString("[\\d{2}-\\d{2}-\\d{4}]");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `[\d{2}-\d{2}-\d{4}]` == x1);
    }
}

function testFromStringNegative() {
    string:RegExp|error x1 = regexp:fromString("AB+^*");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing backslash before '*' token in 'AB+^*'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("AB\\hCD");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: invalid character 'h' after backslash in 'AB\\hCD'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("AB\\pCD");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing open brace '{' token in 'AB\\pCD'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("AB\\uCD");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: invalid character 'u' after backslash in 'AB\\uCD'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("AB\\u{001CD");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing close brace '}' token in 'AB\\u{001CD'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("AB\\p{sc=Lu");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing close brace '}' token in 'AB\\p{sc=Lu'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("[^abc");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing close bracket ']' token in '[^abc'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("(abc");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing close parenthesis ')' token in '(abc'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("(ab^*)");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing backslash before '*' token in '(ab^*)'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("\\p{");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: invalid end character in '\\p{'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("\\p{sc=^}");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: invalid Unicode property value '^' in '\\p{sc=^}'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("\\p{sc=L^}");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: invalid Unicode property value 'L^' in '\\p{sc=L^}'", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("\\p{gc=");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: invalid end character in '\\p{gc='", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString("[");
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing close bracket ']' token in '['", 
            <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString(string `${"["}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing close bracket ']' token in '['", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString(string `${"[[]]"}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing backslash before ']' token in '[[]]'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString(string `${"(?im-gi:ABC)"}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: invalid flag 'g' in '(?im-gi:ABC)'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString(string `${"(?im-si:ABC)"}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: duplicate flag 'i' in '(?im-si:ABC)'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString(string `${"?:ABC"}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: missing backslash before '?' token in '?:ABC'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString(string `${"\\b"}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: invalid character 'b' after backslash in '\\b'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString(string `${"[]"}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: empty character class disallowed in '[]'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString(string `[z-a]`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: invalid character class range 'z'-'a' in '[z-a]'", <string> checkpanic x1.detail()["message"]);
    }
}

function testModuleLevelPatterns() {
    string:RegExp|error x1 = regexp:fromString(moduleLevelPattern);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: invalid end character in '\\p{'", 
            <string> checkpanic x1.detail()["message"]);
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
    string[] arrExpected6 = ["ballerina", "geeks", "wso2"];
    string[] resArr6 = re `\.`.split(str6);
    assertEquality(3, resArr6.length());
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

    string str10 = "1 2   3 4  5";
    string[] resArr10 = re `${""}`.split(str10);
    string[] arrExpected10 = ["1", " ", "2", " ", " ", " ", "3", " ", "4", " ", " ", "5", ""];
    assertEquality(13, resArr10.length());
    assertEquality(arrExpected10, resArr10);

    string str11 = "1 2   3 4  5";
    string[] resArr11 = re `()`.split(str11);
    string[] arrExpected11 = ["1", " ", "2", " ", " ", " ", "3", " ", "4", " ", " ", "5", ""];
    assertEquality(13, resArr11.length());
    assertEquality(arrExpected11, resArr11);

    string str12 = "";
    string[] resArr12 = re `${""}`.split(str12);
    string[] arrExpected12 = [""];
    assertEquality(1, resArr12.length());
    assertEquality(arrExpected12, resArr12);

    string str13 = "";
    string[] resArr13 = re `()`.split(str13);
    string[] arrExpected13 = [""];
    assertEquality(1, resArr13.length());
    assertEquality(arrExpected13, resArr13);

    string str14 = "";
    string[] resArr14 = re `[a-z]`.split(str14);
    string[] arrExpected14 = [""];
    assertEquality(1, resArr14.length());
    assertEquality(arrExpected14, resArr14);

    string str15 = "abcd";
    string[] resArr15 = re `[a-z]`.split(str15);
    string[] arrExpected15 = ["", "", "", "", ""];
    assertEquality(5, resArr15.length());
    assertEquality(arrExpected15, resArr15);

    string str16 = ",";
    string[] resArr16 = re `,`.split(str16);
    string[] arrExpected16 = ["", ""];
    assertEquality(2, resArr16.length());
    assertEquality(arrExpected16, resArr16);

    string str17 = "ab123";
    string[] resArr17 = re `[a-z]`.split(str17);
    string[] arrExpected17 = ["", "", "123"];
    assertEquality(3, resArr17.length());
    assertEquality(arrExpected17, resArr17);

    string str18 = "a\\b";
    string[] resArr18 = re `\\`.split(str18);
    string[] arrExpected18 = ["a", "b"];
    assertEquality(2, resArr18.length());
    assertEquality(arrExpected18, resArr18);

    string str19 = "a123";
    string[] resArr19 = re `a`.split(str19);
    string[] arrExpected19 = ["", "123"];
    assertEquality(2, resArr19.length());
    assertEquality(arrExpected19, resArr19);

    string str20 = "123a";
    string[] resArr20 = re `a`.split(str20);
    string[] arrExpected20 = ["123", ""];
    assertEquality(2, resArr20.length());
    assertEquality(arrExpected20, resArr20);

    string str21 = "abab";
    string[] resArr21 = re `[ab]`.split(str21);
    string[] arrExpected21 = ["", "", "", "", ""];
    assertEquality(5, resArr21.length());
    assertEquality(arrExpected21, resArr21);
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
    assertEquality(14, resultSpan2_1.endIndex);
    assertEquality("14:35:59", resultSpan2_1.substring());

    regexp:Span resultSpan2_2 = <regexp:Span>resultGroups2[1];
    assertEquality(6, resultSpan2_2.startIndex);
    assertEquality(8, resultSpan2_2.endIndex);
    assertEquality("14", resultSpan2_2.substring());

    boolean isFullMatch1 = regexp:isFullMatch(re = re `(?i:[a-z]+)`, str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    assertTrue(isFullMatch1);

    regexp:Groups? res6 = regexp:fullMatchGroups(re = re `([0-9]+)×([0-9]+)`, str = "1440×900");
    assertTrue(res6 is regexp:Groups);
    regexp:Groups resultGroups3 = <regexp:Groups>res6;
    regexp:Span resultSpan3_1 = <regexp:Span>resultGroups3[0];
    assertEquality(0, resultSpan3_1.startIndex);
    assertEquality(8, resultSpan3_1.endIndex);
    assertEquality("1440×900", resultSpan3_1.substring());

    regexp:Span resultSpan3_2 = <regexp:Span>resultGroups3[1];
    assertEquality(0, resultSpan3_2.startIndex);
    assertEquality(4, resultSpan3_2.endIndex);
    assertEquality("1440", resultSpan3_2.substring());

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

function testEmptyRegexpFind() {
    // find
    regexp:Span? resA1 = regexp:find(re = re `World`, str = "");
    assertTrue(resA1 is ());
    regexp:Span? resA2 = regexp:find(re = re `${""}`, str = "");
    assertTrue(resA2 is regexp:Span);
    regexp:Span? resA3 = regexp:find(re = re `${""}`, str = "HelloWorld");
    assertTrue(resA3 is regexp:Span);
    string regexStrA = "";
    regexp:Span? resA4 = regexp:find(re = re `${regexStrA}`, str = "HelloWorld");
    assertTrue(resA4 is regexp:Span);
    regexp:Span? resA5 = regexp:find(re = re `${regexStrA}`, str = "");
    assertTrue(resA5 is regexp:Span);
    regexp:Span? resA6 = regexp:find(re = re `(.*)`, str = "");
    assertTrue(resA6 is regexp:Span);

    // find all
    regexp:Span[] resB1 = regexp:findAll(re `(\w+ing)`, "");
    assertEquality(0, resB1.length());
    regexp:Span[] resB2 = regexp:findAll(re `${""}`, "");
    assertEquality(1, resB2.length());
    regexp:Span[] resB3 = regexp:findAll(re `${""}`, "There once was a king who liked to sing");
    assertEquality(40, resB3.length());

    // find groups
    regexp:Groups? resC1 = regexp:findGroups(re `(\w+tt\w+)`, "");
    assertTrue(resC1 is ());
    regexp:Groups? resC2 = regexp:findGroups(re `${""}`, "");
    assertTrue(resC2 is regexp:Groups);

    // find all groups
    regexp:Groups[] resD1 = regexp:findAllGroups(re `(\w+ble)`, "");
    assertEquality(0, resD1.length());
    regexp:Groups[] resD2 = regexp:findAllGroups(re `${""}`, "");
    assertEquality(1, resD2.length());

    // full match groups
    regexp:Groups? resE1 = regexp:fullMatchGroups(re = re `${""}`, str = "HelloWorld");
    assertTrue(resE1 is ());
    regexp:Groups? resE2 = regexp:fullMatchGroups(re = re `${""}`, str = "");
    assertTrue(resE2 is regexp:Groups);
}

function testEmptyRegexpMatch() {
    // matchAt
    string regexStrA = "";
    regexp:Span? resA1 = regexp:matchAt(re = re `${regexStrA}`, str = "HelloWorld");
    assertTrue(resA1 is ());
    regexp:Span? resA2 = regexp:matchAt(re = re `${regexStrA}`, str = "HelloWorld", startIndex = 4);
    assertTrue(resA2 is ());
}

public function testRegexpFromString() returns error? {
    regexp:RegExp regex1 = check regexp:fromString("[a-z]");
    regexp:Span? res1 = regexp:find(regex1, "TLearn/ Ballerina^ in");
    assertTrue(res1 is regexp:Span);

    //Need to be addressed in https://github.com/ballerina-platform/ballerina-lang/issues/39686
    //regexp:RegExp regex2 = check regexp:fromString("^[^a-zA-Z0-9]");
    //regexp:Span? res2 = regexp:find(regex2, "*TLearn/ Ballerina^ in");
    //assertTrue(res2 is regexp:Span);
}

function testRuntimeRegexpParser() {
    string:RegExp _ = re `${"(?-:)"}`;
    anydata|error result = regexp:fromString("(?-:)");
}

function testTranslatingDiffNodesInCharClass() {
    string:RegExp pattern1 = re `[A-Z\d]`;
    regexp:Span? res1 = pattern1.find("ABC2");
    assertTrue(res1 is regexp:Span);
    assertEquality("A", (<regexp:Span>res1).substring());

    string:RegExp pattern2 = re `[A-Z*]`;
    regexp:Span? res2 = pattern2.find("ABC2");
    assertTrue(res2 is regexp:Span);
    assertEquality("A", (<regexp:Span>res2).substring());

    string:RegExp pattern3 = re `[A-Za-z]`;
    regexp:Span[] res3 = pattern3.findAll("ABCabc");
    string concatRes = "";
    foreach regexp:Span span in res3 {
        concatRes += span.substring();
    }
    assertEquality("ABCabc", concatRes);

    string:RegExp pattern4 = re `[A-Z\d\s]`;
    regexp:Span? res4 = pattern4.find("ABC2 ");
    assertTrue(res4 is regexp:Span);
    assertEquality("A", (<regexp:Span>res4).substring());

    string:RegExp pattern5 = re `[\d\s.]`;
    regexp:Span? res5 = pattern5.find("ABC2 ");
    assertTrue(res5 is regexp:Span);
    assertEquality("2", (<regexp:Span>res5).substring());

    string:RegExp pattern6 = re `[a-z\d]`;
    regexp:Span? res6 = pattern6.find("ABC2 a");
    assertTrue(res6 is regexp:Span);
    assertEquality("2", (<regexp:Span>res6).substring());

    string:RegExp pattern7 = re `[\sa-z]`;
    regexp:Span? res7 = pattern7.find("ABC2 a");
    assertTrue(res7 is regexp:Span);
    assertEquality(" ", (<regexp:Span>res7).substring());

    string:RegExp pattern8 = re `[\--\\]`;
    regexp:Span? res8 = pattern8.find("ABC2 a");
    assertTrue(res8 is regexp:Span);
    assertEquality("A", (<regexp:Span>res8).substring());
}

function testRegexpInterpolation() {
    string pattern1 = "^ABC";
    string:RegExp reg1 = re `${pattern1}`;
    assertTrue(reg1.toString() == pattern1);
    
    string pattern2 = "ABC$";
    string:RegExp reg2 = re `${pattern2}`;
    assertTrue(reg2.toString() == pattern2);
    
    string pattern3 = "x|y|Z|^Ab$";
    string:RegExp reg3 = re `${pattern3}`;
    assertTrue(reg3.toString() == pattern3);
    
    string pattern4 = "A*|B+|AB?|A{2,4}|A{2,}|A{2}";
    string:RegExp reg4 = re `${pattern4}`;
    assertTrue(reg4.toString() == pattern4);
    
    string pattern5 = "A{2,4}?|AB??|A*?|A+?";
    string:RegExp reg5 = re `${pattern5}`;
    assertTrue(reg5.toString() == pattern5);
    
    string pattern6 = "A.B+C{1,3}?";
    string:RegExp reg6 = re `${pattern6}`;
    assertTrue(reg6.toString() == pattern6);
    
    string pattern7 = "A.*?";
    string:RegExp reg7 = re `${pattern7}`;
    assertTrue(reg7.toString() == pattern7);
    
    string pattern8 = "[^A-Z\\w]";
    string:RegExp reg8 = re `${pattern8}`;
    assertTrue(reg8.toString() == pattern8);
    
    string pattern9 = "[^\\u{1234}]|[\\u{5C}]";
    string:RegExp reg9 = re `${pattern9}`;
    assertTrue(reg9.toString() == pattern9);
    
    string pattern10 = "[^\\r]|[\\n]|[\\t]";
    string:RegExp reg10 = re `${pattern10}`;
    assertTrue(reg10.toString() == pattern10);
    
    string pattern11 = "[^\\^]|[\\*][\\[]";
    string:RegExp reg11 = re `${pattern11}`;
    assertTrue(reg11.toString() == pattern11);
    
    string pattern12 = "[^\\p{sc=Latin}]|[\\p{gc=Lu}]|[\\P{Lu}]";
    string:RegExp reg12 = re `${pattern12}`;
    assertTrue(reg12.toString() == pattern12);
    
    string pattern13 = "[\\w\\d\\s\\S\\D\\W]";
    string:RegExp reg13 = re `${pattern13}`;
    assertTrue(reg13.toString() == pattern13);
    
    string pattern14 = "[\\-]";
    string:RegExp reg14 = re `${pattern14}`;
    assertTrue(reg14.toString() == pattern14);
    
    string pattern15 = "[^\\nd-hM-N\\tQ-T]|";
    string:RegExp reg15 = re `${pattern15}`;
    assertTrue(reg15.toString() == pattern15);
    
    string pattern16 = "(D*)|(A.*?)|(?m:AAD*)|(?ms-ix:[^ABC]{1245,3212})";
    string:RegExp reg16 = re `${pattern16}`;
    assertTrue(reg16.toString() == pattern16);
    
    string pattern17 = "^\\P{Ll}+\\)$";
    string:RegExp reg17 = re `${pattern17}`;
    assertTrue(reg17.toString() == pattern17);
}

function testRegexpInterpolationNegative() {
    string pattern1 = "(ABC)+\\k";
    string:RegExp|error reg1 = trap re `${pattern1}`;
    assertTrue(reg1 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg1).message());
    assertEquality("invalid character 'k' after backslash in insertion substring '(ABC)+\\k'", 
        <string>checkpanic (<error>reg1).detail()["message"]);

    string pattern2 = "\\p";
    string:RegExp|error reg2 = trap re `${pattern2}`;
    assertTrue(reg2 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg2).message());
    assertEquality("missing open brace '{' token in insertion substring '\\p'", 
        <string>checkpanic (<error>reg2).detail()["message"]);

    string pattern3 = "\\p{gx=}";
    string:RegExp|error reg3 = trap re `${pattern3}`;
    assertTrue(reg3 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg3).message());
    assertEquality("invalid Unicode general category value 'gx=' in insertion substring '\\p{gx=}'", 
        <string>checkpanic (<error>reg3).detail()["message"]);

    string pattern4 = "\\p{sk=}";
    string:RegExp|error reg4 = trap re `${pattern4}`;
    assertTrue(reg4 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg4).message());
    assertEquality("invalid Unicode general category value 'sk=' in insertion substring '\\p{sk=}'", 
        <string>checkpanic (<error>reg4).detail()["message"]);

    string pattern5 = "\\p{gc=Z}";
    string:RegExp|error reg5 = trap re `${pattern5}`;
    assertTrue(reg5 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg5).message());
    assertEquality("invalid Unicode general category value 'Z' in insertion substring '\\p{gc=Z}'", 
        <string>checkpanic (<error>reg5).detail()["message"]);

    string pattern6 = "\\p{Lz}";
    string:RegExp|error reg6 = trap re `${pattern6}`;
    assertTrue(reg6 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg6).message());
    assertEquality("invalid Unicode general category value 'L' in insertion substring '\\p{Lz}'", 
        <string>checkpanic (<error>reg6).detail()["message"]);

    string pattern7 = "\\p{Ll";
    string:RegExp|error reg7 = trap re `${pattern7}`;
    assertTrue(reg7 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg7).message());
    assertEquality("missing close brace '}' token in insertion substring '\\p{Ll'", 
        <string>checkpanic (<error>reg7).detail()["message"]);

    string pattern8 = "ABC{2";
    string:RegExp|error reg8 = trap re `${pattern8}`;
    assertTrue(reg8 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg8).message());
    assertEquality("missing close brace '}' token in insertion substring 'ABC{2'", 
        <string>checkpanic (<error>reg8).detail()["message"]);

    string pattern9 = "ABC{2,";
    string:RegExp|error reg9 = trap re `${pattern9}`;
    assertTrue(reg9 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg9).message());
    assertEquality("missing close brace '}' token in insertion substring 'ABC{2,'", 
        <string>checkpanic (<error>reg9).detail()["message"]);

    string pattern10 = "ABC{2,3";
    string:RegExp|error reg10 = trap re `${pattern10}`;
    assertTrue(reg10 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg10).message());
    assertEquality("missing close brace '}' token in insertion substring 'ABC{2,3'", 
        <string>checkpanic (<error>reg10).detail()["message"]);

    string pattern11 = "[z-a]";
    string:RegExp|error reg11 = trap re `${pattern11}`;
    assertTrue(reg11 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg11).message());
    assertEquality("invalid character class range 'z'-'a' in insertion substring '[z-a]'", 
        <string>checkpanic (<error>reg11).detail()["message"]);

    string pattern12 = "[ab-kZ-A]";
    string:RegExp|error reg12 = trap re `${pattern12}`;
    assertTrue(reg12 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg12).message());
    assertEquality("invalid character class range 'Z'-'A' in insertion substring '[ab-kZ-A]'", 
        <string>checkpanic (<error>reg12).detail()["message"]);

    string pattern13 = "\\p{sc=Lati*}";
    string:RegExp|error reg13 = trap re `${pattern13}`;
    assertTrue(reg13 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg13).message());
    assertEquality("invalid Unicode property value 'Lati*' in insertion substring '\\p{sc=Lati*}'", 
        <string>checkpanic (<error>reg13).detail()["message"]);

    string pattern14 = "[a-z";
    string:RegExp|error reg14 = trap re `${pattern14}`;
    assertTrue(reg14 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg14).message());
    assertEquality("missing close bracket ']' token in insertion substring '[a-z'", 
        <string>checkpanic (<error>reg14).detail()["message"]);

    string pattern15 = "(?ik-m:abc)";
    string:RegExp|error reg15 = trap re `${pattern15}`;
    assertTrue(reg15 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg15).message());
    assertEquality("invalid flag 'k' in insertion substring '(?ik-m:abc)'", 
        <string>checkpanic (<error>reg15).detail()["message"]);

    string pattern16 = "(?im-si:abc)";
    string:RegExp|error reg16 = trap re `${pattern16}`;
    assertTrue(reg16 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg16).message());
    assertEquality("duplicate flag 'i' in insertion substring '(?im-si:abc)'", 
        <string>checkpanic (<error>reg16).detail()["message"]);

    string pattern17 = "(?im-s:abc";
    string:RegExp|error reg17 = trap re `${pattern17}`;
    assertTrue(reg17 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg17).message());
    assertEquality("missing close parenthesis ')' token in insertion substring '(?im-s:abc'", 
        <string>checkpanic (<error>reg17).detail()["message"]);
    
    string pattern18 = "\\p";
    string:RegExp|error reg18 = trap re `${pattern18}`;
    assertTrue(reg18 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg18).message());
    assertEquality("missing open brace '{' token in insertion substring '\\p'", 
        <string>checkpanic (<error>reg18).detail()["message"]);
        
    string pattern19 = "\\p{Ll";
    string:RegExp|error reg19 = trap re `${pattern19}`;
    assertTrue(reg19 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg19).message());
    assertEquality("missing close brace '}' token in insertion substring '\\p{Ll'", 
        <string>checkpanic (<error>reg19).detail()["message"]);
        
    string pattern20 = "abc\\w[a-!]";
    string:RegExp|error reg20 = trap re `${pattern20}`;
    assertTrue(reg20 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg20).message());
    assertEquality("invalid character class range 'a'-'!' in insertion substring 'abc\\w[a-!]'", 
        <string>checkpanic (<error>reg20).detail()["message"]);
        
    string pattern21 = "abc\\w[a--]";
    string:RegExp|error reg21 = trap re `${pattern21}`;
    assertTrue(reg21 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg21).message());
    assertEquality("invalid character class range 'a'-'-' in insertion substring 'abc\\w[a--]'", 
        <string>checkpanic (<error>reg21).detail()["message"]);
        
    string pattern22 = "abc[a-z[A-Z]]";
    string:RegExp|error reg22 = trap re `${pattern22}`;
    assertTrue(reg22 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg22).message());
    assertEquality("missing backslash before ']' token in insertion substring 'abc[a-z[A-Z]]'", 
        <string>checkpanic (<error>reg22).detail()["message"]);
    
    string pattern23 = "abc[a-zZ-A]";
    string:RegExp|error reg23 = trap re `${pattern23}`;
    assertTrue(reg23 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg23).message());
    assertEquality("invalid character class range 'Z'-'A' in insertion substring 'abc[a-zZ-A]'", 
        <string>checkpanic (<error>reg23).detail()["message"]);
        
    string pattern24 = "\\p{Mz}";
    string:RegExp|error reg24 = trap re `${pattern24}`;
    assertTrue(reg24 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg24).message());
    assertEquality("invalid Unicode general category value 'M' in insertion substring '\\p{Mz}'", 
        <string>checkpanic (<error>reg24).detail()["message"]);
    
    string pattern25 = "\\p{Nz}";
    string:RegExp|error reg25 = trap re `${pattern25}`;
    assertTrue(reg25 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg25).message());
    assertEquality("invalid Unicode general category value 'N' in insertion substring '\\p{Nz}'", 
        <string>checkpanic (<error>reg25).detail()["message"]);
    
    string pattern26 = "\\p{Pz}";
    string:RegExp|error reg26 = trap re `${pattern26}`;
    assertTrue(reg26 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg26).message());
    assertEquality("invalid Unicode general category value 'P' in insertion substring '\\p{Pz}'", 
        <string>checkpanic (<error>reg26).detail()["message"]);
    
    string pattern27 = "\\p{Sz}";
    string:RegExp|error reg27 = trap re `${pattern27}`;
    assertTrue(reg27 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg27).message());
    assertEquality("invalid Unicode general category value 'S' in insertion substring '\\p{Sz}'", 
        <string>checkpanic (<error>reg27).detail()["message"]);
    
    string pattern28 = "\\p{Zz}";
    string:RegExp|error reg28 = trap re `${pattern28}`;
    assertTrue(reg28 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg28).message());
    assertEquality("invalid Unicode general category value 'Z' in insertion substring '\\p{Zz}'", 
        <string>checkpanic (<error>reg28).detail()["message"]);
        
    string pattern29 = "\\p{Cz}";
    string:RegExp|error reg29 = trap re `${pattern29}`;
    assertTrue(reg29 is error);
    assertEquality("{ballerina}RegularExpressionParsingError", (<error>reg29).message());
    assertEquality("invalid Unicode general category value 'C' in insertion substring '\\p{Cz}'", 
        <string>checkpanic (<error>reg29).detail()["message"]);          
}

function testCharClassesWithMultipleRangesAndAtoms() returns error? {
    string:RegExp pattern1 = check regexp:fromString("[a-zA-Z0-9]");
    assertTrue(pattern1 is string:RegExp);
    assertTrue(re `[a-zA-Z0-9]` == <string:RegExp>pattern1);
    regexp:Span? res1 = (<string:RegExp>pattern1).find("ABC");
    assertTrue(res1 is regexp:Span);
    assertEquality("A", (<regexp:Span>res1).substring());

    string:RegExp pattern2 = check regexp:fromString("[abc-mzABC-FGH012-5]");
    assertTrue(pattern2 is string:RegExp);
    assertTrue(re `[abc-mzABC-FGH012-5]` == <string:RegExp>pattern2);
    regexp:Span? res2 = (<string:RegExp>pattern2).find("abc");
    assertTrue(res2 is regexp:Span);
    assertEquality("a", (<regexp:Span>res2).substring());

    string:RegExp pattern3 = check regexp:fromString("[a-z\\d\\s]");
    assertTrue(pattern3 is string:RegExp);
    assertTrue(re `[a-z\d\s]` == <string:RegExp>pattern3);
    regexp:Span? res3 = (<string:RegExp>pattern3).find("abc");
    assertTrue(res3 is regexp:Span);
    assertEquality("a", (<regexp:Span>res3).substring());

    string:RegExp pattern4 = check regexp:fromString("[\\w-\\d\\s]");
    assertTrue(pattern4 is string:RegExp);
    assertTrue(re `[\w-\d\s]` == <string:RegExp>pattern4);
    regexp:Span? res4 = (<string:RegExp>pattern4).find("abc");
    assertTrue(res4 is regexp:Span);
    assertEquality("a", (<regexp:Span>res4).substring());

    string:RegExp pattern5 = check regexp:fromString("[ABC-MZ.a-z]");
    assertTrue(pattern5 is string:RegExp);
    assertTrue(re `[ABC-MZ.a-z]` == <string:RegExp>pattern5);
    regexp:Span? res5 = (<string:RegExp>pattern5).find("abc");
    assertTrue(res5 is regexp:Span);
    assertEquality("a", (<regexp:Span>res5).substring());

    string:RegExp pattern6 = check regexp:fromString("[^a-zA-Z*]");
    assertTrue(pattern6 is string:RegExp);
    assertTrue(re `[^a-zA-Z*]` == <string:RegExp>pattern6);
    regexp:Span? res6 = (<string:RegExp>pattern6).find("123");
    assertTrue(res6 is regexp:Span);
    assertEquality("1", (<regexp:Span>res6).substring());

    string:RegExp pattern7 = check regexp:fromString("[a-z.\\s\\w]");
    assertTrue(pattern7 is string:RegExp);
    assertTrue(re `[a-z.\s\w]` == <string:RegExp>pattern7);
    regexp:Span? res7 = (<string:RegExp>pattern7).find("abc");
    assertTrue(res7 is regexp:Span);
    assertEquality("a", (<regexp:Span>res7).substring());
    
    string:RegExp pattern8 = check regexp:fromString("[\\s]");
    assertTrue(pattern8 is string:RegExp);
    assertTrue(re `[\s]` == <string:RegExp>pattern8);
    regexp:Span? res8 = (<string:RegExp>pattern8).find("abc ");
    assertTrue(res8 is regexp:Span);
    assertEquality(" ", (<regexp:Span>res8).substring());
    
    string:RegExp pattern9 = check regexp:fromString("[\\p{Ll}]");
    assertTrue(pattern9 is string:RegExp);
    assertTrue(re `[\p{Ll}]` == <string:RegExp>pattern9);
    regexp:Span? res9 = (<string:RegExp>pattern9).find("Abc BCa");
    assertTrue(res9 is regexp:Span);
    assertEquality("b", (<regexp:Span>res9).substring());
}

function testRegexpWithUnicodeChars() {
    string startChar = string `\u{0d9c}`;
    string endChar = string `\u{1F310}`;
    regexp:Span? resA1 = regexp:find(re = re `\u{1F30D}`, str = "😀 Hello, 🌍! ❤️");
    assertTrue(resA1 is regexp:Span);
    regexp:Span? resA2 = regexp:find(re `\u{1F30D}`, "🌍😀 Hello! ❤️", 5);
    assertTrue(resA2 is ());
    regexp:Span? resA3 = regexp:find(re `\u{1F30D}`, "😀 Hello 🌍! ❤️", 6);
    assertTrue(resA3 is regexp:Span);
    regexp:Span? resA4 = regexp:find(re `\u{1F31F}`, "😀 Hello, 🌍! ❤️");
    assertTrue(resA4 is ());
    regexp:Span? resA5 = regexp:find(re `\u{0d9c}`, "පරිගණක 10");
    assertTrue(resA5 is regexp:Span);
    regexp:Span? resA6 = regexp:find(re `[${startChar}${endChar}]`, "😀 Hello, 🌏! ❤️");
    assertTrue(resA6 is regexp:Span);
    regexp:Span? resA7 = regexp:find(re `[\u{1F600}\u{1F310}]`, "😀 Hello, 😀🌏! ❤️");
    assertTrue(resA7 is regexp:Span);
    regexp:Span? resA8 = regexp:find(re `[\u{0d9c}-\u{1F310}]`, "😀 Hello, 🌏! ❤️");
    assertTrue(resA8 is regexp:Span);
    // Invalid code point range
    regexp:Span? resA9 = regexp:find(re `\u{FF3AD}`, "😀 Hello, 🌍! ❤️");
    assertTrue(resA9 is ());

    regexp:Span[] resB1 = regexp:findAll(re `\u{1F30D}`, "😀 Hello, 🌍! ❤️");
    assertTrue(resB1.length() == 1);
    assertTrue(resB1[0].startIndex == 9);
    assertTrue(resB1[0].endIndex == 10);
    regexp:Span[] resB2 = regexp:findAll(re `\u{1F30D}`, "🌍😀 Hello! ❤️", 5);
    assertTrue(resB2.length() == 0);
    regexp:Span[] resB3 = regexp:findAll(re `\u{1F30D}`, "😀 Hello 🌍! 🌍❤️", 6);
    assertTrue(resB3.length() == 2);
    assertTrue(resB3[0].startIndex == 8);
    assertTrue(resB3[0].endIndex == 9);
    assertTrue(resB3[1].startIndex == 11);
    assertTrue(resB3[1].endIndex == 12);
    regexp:Span[] resB4 = regexp:findAll(re `\u{1F31F}`, "😀 Hello, 🌍! ❤️");
    assertTrue(resB4.length() == 0);
    regexp:Span[] resB5 = regexp:findAll(re `\u{0d9c}`, "පරිගණක 10");
    assertTrue(resB5.length() == 1);
    regexp:Span[] resB6 = regexp:findAll(re `[\u{1F600}\u{1F310}]`, "😀 Hello, 🌏! ❤️");
    assertTrue(resB6.length() == 1);
    regexp:Span[] resB7 = regexp:findAll(re `[${startChar}${endChar}]`, "😀 Hello,! ❤️");
    assertTrue(resB7.length() == 1);
    regexp:Span[] resB8 = regexp:findAll(re `[\u{1F30C}-\u{1F30F}]`, "Hello, 🌏! ❤️");
    assertTrue(resB8.length() == 1);
    // Invalid code point range
    regexp:Span[] resB9 = regexp:findAll(re `\u{FF3AD}`, "😀 Hello, 🌍! ❤️");
    assertTrue(resB9.length() == 0);

    regexp:Groups? resC1 = regexp:findGroups(re `o\u{1F30D}`, "😀 Hello🌍! ❤️o🌍");
    assertTrue(resC1 is regexp:Groups);
    regexp:Groups? resC2 = regexp:findGroups(re `l\u{1F30D}`, "😀 Hello🌍! ❤️o🌍");
    assertTrue(resC2 is ());

    regexp:Groups[] resD1 = regexp:findAllGroups(re `o\u{1F30D}`, "😀 Hello🌍! ❤️o🌍");
    assertTrue(resD1.length() == 2);
    assertTrue(resD1[0][0].startIndex == 6);
    assertTrue(resD1[0][0].endIndex == 8);
    assertTrue(resD1[1][0].startIndex == 11);
    assertTrue(resD1[1][0].endIndex == 13);
    regexp:Groups[] resD2 = regexp:findAllGroups(re `o\u{1F301}`, "😀 Hello🌍! ❤️o🌍");
    assertTrue(resD2.length() == 0);

    boolean resE1 = regexp:isFullMatch(re `.*\u{1F30D}.*`, "😀 Hello, 🌍!");
    assertTrue(resE1);
    boolean resE2 = regexp:isFullMatch(re `.*\u{1F310}.*`, "😀 Hello, 🌍!");
    assertFalse(resE2);

    regexp:Groups? resF1 = regexp:fullMatchGroups(re `.*o\u{1F30D}.*`, "😀 Hello🌍! ❤️o🌍");
    assertTrue(resF1 is regexp:Groups);
    regexp:Groups? resF2 = regexp:fullMatchGroups(re `.*o\u{1F301}.*`, "😀 Hello🌍! ❤️o🌍");
    assertTrue(resF2 is ());

    string resG1 = regexp:replace(re `o\u{1F30D}`, "😀 Hello🌍! ❤️o🌍", "\u{1F3D6}");
    assertEquality("😀 Hell🏖! ❤️o🌍", resG1);
    string resG2 = regexp:replace(re `o\u{1F30D}`, "😀 Hello🌍! ❤️o🌍", "==");
    assertEquality("😀 Hell==! ❤️o🌍", resG2);
    string resG3 = regexp:replace(re `\u{1F30D}`, "😀 Hello🌍! ❤️o🌍", "\u{1F3D6}", 8);
    assertEquality("😀 Hello🌍! ❤️o🏖", resG3);

    string resH1 = regexp:replaceAll(re `\u{1F30D}`, "😀 Hello🌍! ❤️o🌍", "\u{1F3D6}");
    assertEquality("😀 Hello🏖! ❤️o🏖", resH1);
    string resH2 = regexp:replaceAll(re `\u{1F30D}`, "😀 Hello🌍! ❤️o🌍", "==");
    assertEquality("😀 Hello==! ❤️o==", resH2);
    string resH3 = regexp:replaceAll(re `\u{1F30D}`, "😀 Hello🌍! ❤️o🌍+🌍!!", "\u{1F3D6}", 8);
    assertEquality("😀 Hello🌍! ❤️o🏖+🏖!!", resH3);

    string[] resI1 = regexp:split(re `\u{1F30D}`, "😀 Hello🌍Our🌍Earth");
    assertEquality("😀 Hello", resI1[0]);
    string[] resI2 = regexp:split(re `\u{1F310}`, "😀 Hello🌍Our🌍Earth");
    assertEquality("😀 Hello🌍Our🌍Earth", resI2[0]);

    regexp:Span? resJ1 = regexp:matchAt(re `\u{1F30D}.*`, "🌍😀 Hello🌍! ❤️o🌍");
    assertTrue(resJ1 is regexp:Span);
    regexp:Span? resJ2 = regexp:matchAt(re `\u{1F30D}.*`, "😀 Hello🌍! ❤️o🌍", 7);
    assertTrue(resJ2 is regexp:Span);
    regexp:Span? resJ3 = regexp:matchAt(re `\u{1F30D}.*`, "😀 Hello🌍! ❤️o🌍");
    assertTrue(resJ3 is ());
    regexp:Span? resJ4 = regexp:matchAt(re `\u{1F310}`, "😀 Hello🌍! ❤️o🌍");
    assertTrue(resJ4 is ());
    regexp:Span? resJ5 = regexp:matchAt(re `\u{1F30D}`, "😀 Hello🌍! ❤️o", 10);
    assertTrue(resJ5 is ());
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

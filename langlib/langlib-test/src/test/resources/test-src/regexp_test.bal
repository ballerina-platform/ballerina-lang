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

   //  regexp:Span? res72  = regExpr7.find("");
   //  assertTrue(res72 is regexp:Span);
   //  regexp:Span resultSpan82 = <regexp:Span>res72;
   //  assertEquality(0, resultSpan82.startIndex);
   //  assertEquality(0, resultSpan82.endIndex);
   //  assertEquality("", resultSpan82.substring());
    
    var regExpr8 = re ``;
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

   //  var regExpr11 = re `.`;
   //  regexp:Span? res11  = regExpr11.find("", 0);
   //  assertTrue(res11 is regexp:Span);
   //  regexp:Span resultSpan12 = <regexp:Span>res11;
   //  assertEquality(0, resultSpan12.startIndex);
   //  assertEquality(6, resultSpan12.endIndex);
   //  assertEquality("string", resultSpan12.substring());
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
    var regExpr10 = re ``;
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

   //  string str11 = "";
   //  var regExpr13 = re `(.*)`;
   //  regexp:Groups? res14 = regExpr13.findGroups(str11);
   //  assertTrue(res14 is regexp:Groups);
   //  regexp:Groups resultGroups14 = <regexp:Groups>res14;
   //  assertEquality(0, resultGroups14.length());

   //  regexp:Span resultSpan14_1 = <regexp:Span>resultGroups14[0];
   //  assertEquality(0, resultSpan14_1.startIndex);
   //  assertEquality(3, resultSpan14_1.endIndex);
   //  assertEquality("", resultSpan14_1.substring());
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
    var regExpr3 = re ``;
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

   //  string str5 = "";
   //  var regExpr5 = re `ABC*`;
   //  regexp:Span[]? res5 = regExpr5.findAll(str5);
   //  assertTrue(res5 is regexp:Span[]);
   //  regexp:Span[] resultSpanArr5 = <regexp:Span[]>res5;
   //  assertEquality(0, resultSpanArr5.length());

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

    string:RegExp regExpr7 = re ``;
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

   //  string:RegExp regExpr9 = re `(.*)`;
   //  regexp:Groups[] groupsArr9 = regExpr9.findAllGroups("");
   //  assertEquality(1, groupsArr9.length());

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
    var regExpr7 = re ``;
    regexp:Span? res8 = regExpr7.matchAt(str7, 1);
    assertTrue(res8 is ());
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
    assertEquality(resultGroups3_2.length(), 1);

    regexp:Span resultSpan3_2 = <regexp:Span>resultGroups3_2[0];
    assertEquality(0, resultSpan3_2.startIndex);
    assertEquality(2, resultSpan3_2.endIndex);
    assertEquality("AB", resultSpan3_2.substring());

    string str5 = "ABC";
    var regExpr5 = re `(ABC)`;
    regexp:Groups? res6 = regExpr5.matchGroupsAt(str5, 1);
    assertTrue(res6 is ());

    string str6 = "ABC";
    var regExpr6 = re `((A*)BC)`;
    regexp:Groups? res7 = regExpr6.matchGroupsAt(str6, 1);
    assertTrue(res7 is regexp:Groups);
    regexp:Groups resultGroups4 = <regexp:Groups>res7;
    assertEquality(resultGroups4.length(), 2);

    regexp:Span resultSpan4_1 = <regexp:Span>resultGroups4[0];
    assertEquality(1, resultSpan4_1.startIndex);
    assertEquality(3, resultSpan4_1.endIndex);
    assertEquality("BC", resultSpan4_1.substring());

    regexp:Span resultSpan4_2 = <regexp:Span>resultGroups4[1];
    assertEquality(1, resultSpan4_2.startIndex);
    assertEquality(1, resultSpan4_2.endIndex);
    assertEquality("", resultSpan4_2.substring());

    string str7 = "ABC";
    var regExpr7 = re ``;
    regexp:Groups? res8 = regExpr7.matchGroupsAt(str7, 1);
    assertTrue(res8 is ());

   string str8 = "";
    var regExpr8 = re ``;
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
    assertEquality(resultGroups7.length(), 1);

    regexp:Span resultSpan7 = <regexp:Span>resultGroups7[0];
    assertEquality(1, resultSpan7.startIndex);
    assertEquality(4, resultSpan7.endIndex);
    assertEquality("ABC", resultSpan7.substring());
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

    var regExpr5 = re ``;
    boolean isFullMatch5 = regExpr5.isFullMatch("");
    assertTrue(isFullMatch5);

    var regExpr6 = re ``;
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

   //  var regExpr3 = re ``;
   //  regexp:Groups? res4 = regExpr3.fullMatchGroups("");
   //  assertTrue(res4 is ());

    var regExpr4 = re `()`;
    regexp:Groups? res5 = regExpr4.fullMatchGroups("");

    assertTrue(res5 is regexp:Groups);
    regexp:Groups resultGroups3 = <regexp:Groups>res5;
    assertEquality(1, resultGroups3.length());
    regexp:Span resultSpan3 = <regexp:Span>resultGroups3[0];
    assertEquality(0, resultSpan3.startIndex);
    assertEquality(0, resultSpan3.endIndex);
    assertEquality("", resultSpan3.substring());

    var regExpr5 = re `(.)`;
    regexp:Groups? res6 = regExpr5.fullMatchGroups("");
    assertTrue(res6 is ());

    var regExpr6 = re `A`;
    regexp:Groups? res7 = regExpr6.fullMatchGroups("A");
    assertTrue(res7 is regexp:Groups);
    regexp:Groups resultGroups4 = <regexp:Groups>res7;
    assertEquality(1, resultGroups4.length());
    regexp:Span resultSpan4 = <regexp:Span>resultGroups4[0];
    assertEquality(0, resultSpan4.startIndex);
    assertEquality(1, resultSpan4.endIndex);
    assertEquality("A", resultSpan4.substring());

    var regExpr7 = re `(A)`;
    regexp:Groups? res8 = regExpr7.fullMatchGroups("A");
    assertTrue(res8 is regexp:Groups);
    regexp:Groups resultGroups5 = <regexp:Groups>res8;
    assertEquality(1, resultGroups5.length());
    regexp:Span resultSpan5 = <regexp:Span>resultGroups5[0];
    assertEquality(0, resultSpan5.startIndex);
    assertEquality(1, resultSpan5.endIndex);
    assertEquality("A", resultSpan5.substring());

    var regExpr8 = re `(.*)`;
    regexp:Groups? res9 = regExpr8.fullMatchGroups("A");
    assertTrue(res9 is regexp:Groups);
    regexp:Groups resultGroups6 = <regexp:Groups>res9;
    assertEquality(1, resultGroups6.length());
    regexp:Span resultSpan6 = <regexp:Span>resultGroups6[0];
    assertEquality(0, resultSpan6.startIndex);
    assertEquality(1, resultSpan6.endIndex);
    assertEquality("A", resultSpan6.substring());

    var regExpr9 = re `(A*B)|(AB)|(D*A*E*B*R*)`;
    regexp:Groups? res10 = regExpr9.fullMatchGroups("AB");
    assertTrue(res9 is regexp:Groups);
    regexp:Groups resultGroups7 = <regexp:Groups>res10;
    assertEquality(1, resultGroups7.length());
    regexp:Span resultSpan7 = <regexp:Span>resultGroups6[0];
    assertEquality(0, resultSpan7.startIndex);
    assertEquality(1, resultSpan7.endIndex);
    assertEquality("A", resultSpan7.substring());
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

   //  string str7 = "";
   //  var regExpr6 = re ``;
   //  string result7 = regExpr6.replace(str7, replacementFunctionForReplace);
   //  assertEquality("", result7);

   //  string str8 = "";
   //  var regExpr7 = re ``;
   //  string result8 = regExpr7.replace(str8, "A");
   //  assertEquality("", result8);

   //  string str9 = "B";
   //  var regExpr8 = re ``;
   //  string result9 = regExpr8.replace(str9, "A");
   //  assertEquality("B", result9);

   //  string str10 = "";
   //  var regExpr9 = re `(AB)`;
   //  string result10 = regExpr9.replace(str10, "D");
   //  assertEquality("", result10);

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

   //  string str13 = "";
   //  var regExpr12 = re `()`;
   //  string result9 = regExpr12.replace(str13, "A");
   //  assertEquality("", result9);

    string str14 = "ABCD";
    var regExpr13 = re `()`;
    string result10 = regExpr13.replace(str14, "E");
    assertEquality("EABCD", result10);

    string str15 = "ABCD";
    var regExpr14 = re `.*`;
    string result11 = regExpr14.replace(str15, replacementFunctionForReplace, 1);
    assertEquality("A4", result11);

    string str16 = "ABCD";
    var regExpr15 = re `BCD`;
    string result12 = regExpr15.replace(str16, replacementFunctionForReplace, 1);
    assertEquality("A4", result12);

    string str17 = "AB";
    var regExpr16_1 = re `A|AB|AK`;
    string result13_1 = regExpr16_1.replace(str17, "Z");
    assertEquality("ZB", result13_1);
    var regExpr16_2 = re `AB|A|AK`;
    string result13_2 = regExpr16_2.replace(str17, "Z");
    assertEquality("Z", result13_2);
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

   //  string str7 = "";
   //  var regExpr6 = re ``;
   //  string result7 = regExpr6.replaceAll(str7, replacementFunctionForReplace);
   //  assertEquality("", result7);

   //  string str8 = "";
   //  var regExpr7 = re ``;
   //  string result8 = regExpr7.replaceAll(str8, "A");
   //  assertEquality("", result8);

   //  string str9 = "B";
   //  var regExpr8 = re ``;
   //  string result9 = regExpr8.replaceAll(str9, "A");
   //  assertEquality("B", result9);

   //  string str10 = "";
   //  var regExpr9 = re `(AB)`;
   //  string result10 = regExpr9.replaceAll(str10, "D");
   //  assertEquality("", result10);

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

   //  string str13 = "";
   //  var regExpr12 = re `()`;
   //  string result9 = regExpr12.replaceAll(str13, "A");
   //  assertEquality("", result9);

    string str14 = "ABCD";
    var regExpr13 = re `()`;
    string result10 = regExpr13.replaceAll(str14, "E");
    assertEquality("EAEBECEDE", result10);

    string str15 = "ABCD";
    var regExpr14 = re `.*`;
    string result11 = regExpr14.replaceAll(str15, replacementFunctionForReplace, 1);
    assertEquality("A44", result11);

    string str16 = "ABCD";
    var regExpr15 = re `BCD`;
    string result12 = regExpr15.replaceAll(str16, replacementFunctionForReplace, 1);
    assertEquality("A4", result12);

    string str17 = "AB";
    var regExpr16_1 = re `A|AB|AK`;
    string result13_1 = regExpr16_1.replaceAll(str17, "Z");
    assertEquality("ZB", result13_1);
    var regExpr16_2 = re `AB|A|AK`;
    string result13_2 = regExpr16_2.replaceAll(str17, "Z");
    assertEquality("Z", result13_2);
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

    x1 = regexp:fromString("");
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `` == x1);
    }

    x1 = regexp:fromString(string `${""}`);
    assertTrue(x1 is string:RegExp);
    if (x1 is string:RegExp) {
       assertTrue(re `` == x1);
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

    x1 = regexp:fromString(string `${"["}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Missing ']' character", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString(string `${"[[]]"}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character ']'", <string> checkpanic x1.detail()["message"]);
    }

   //  x1 = regexp:fromString(string `${"(?im-gi:ABC)"}`);
   //  assertTrue(x1 is error);
   //  if (x1 is error) {
   //      assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
   //      assertEquality("Failed to parse regular expression: Invalid character ']'", <string> checkpanic x1.detail()["message"]);
   //  }

   //  x1 = regexp:fromString(string `${"(?im-si:ABC)"}`);
   //  assertTrue(x1 is error);
   //  if (x1 is error) {
   //      assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
   //      assertEquality("Failed to parse regular expression: Invalid character ']'", <string> checkpanic x1.detail()["message"]);
   //  }


    x1 = regexp:fromString(string `${"?:ABC"}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character '?'", <string> checkpanic x1.detail()["message"]);
    }

    x1 = regexp:fromString(string `${"\\b"}`);
    assertTrue(x1 is error);
    if (x1 is error) {
        assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
        assertEquality("Failed to parse regular expression: Invalid character '\\b'", <string> checkpanic x1.detail()["message"]);
    }

   //  x1 = regexp:fromString(string `${"[]"}`);
   //  assertTrue(x1 is error);
   //  if (x1 is error) {
   //      assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
   //      assertEquality("Failed to parse regular expression: Invalid character '\\b'", <string> checkpanic x1.detail()["message"]);
   //  }

   //  x1 = regexp:fromString(string `[z-a]`);
   //  assertTrue(x1 is error);
   //  if (x1 is error) {
   //      assertEquality("{ballerina/lang.regexp}RegularExpressionParsingError", x1.message());
   //      assertEquality("Failed to parse regular expression: Invalid character '\\'", <string> checkpanic x1.detail()["message"]);
   //  }
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
    string[] resArr10 = re ``.split(str10);
    string[] arrExpected10 = ["1"," ","2"," "," "," ","3"," ","4"," "," ","5"];
    assertEquality(12, resArr10.length());
    assertEquality(arrExpected10, resArr10);

    string str11 = "1 2   3 4  5";
    string[] resArr11 = re `()`.split(str11);
    string[] arrExpected11 = ["1"," ","2"," "," "," ","3"," ","4"," "," ","5"];
    assertEquality(12, resArr11.length());
    assertEquality(arrExpected11, resArr11);

   //  string str12 = "";
   //  string[] resArr12 = re ``.split(str12);
   //  string[] arrExpected12 = [""];
   //  assertEquality(1, resArr12.length());
   //  assertEquality(arrExpected12, resArr12);

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

   //  string str15 = "abcd";
   //  string[] resArr15 = re `[a-z]`.split(str15);
   //  string[] arrExpected15 = ["abcd"];
   //  assertEquality(1, resArr15.length());
   //  assertEquality(arrExpected15, resArr15);

   //  string str16 = ",";
   //  string[] resArr16 = re `,`.split(str16);
   //  string[] arrExpected16 = ["abcd"];
   //  assertEquality(1, resArr16.length());
   //  assertEquality(arrExpected16, resArr16);

   //  string str17 = "ab123";
   //  string[] resArr17 = re `[a-z]`.split(str17);
   //  string[] arrExpected17 = ["", "", "123"];
   //  assertEquality(3, resArr17.length());
   //  assertEquality(arrExpected17, resArr17);

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

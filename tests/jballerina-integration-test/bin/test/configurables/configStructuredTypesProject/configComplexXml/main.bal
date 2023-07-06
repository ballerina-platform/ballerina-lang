// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;
import testOrg/configLib.util;

type Album record {
    xml details;
};

// Test simple xml type
configurable xml xmlVar1 = ?;
configurable xml:Comment xmlVar2 = ?;
configurable xml:Element xmlVar3 = ?;
configurable xml:ProcessingInstruction xmlVar4 = ?;
configurable xml:Text xmlVar5 = ?;

// Test structured xml type
configurable xml[] xmlArr = ?;
configurable map<xml> xmlMap = ?;
configurable Album xmlRecord = ?;
configurable table<map<xml>> xmlTable1 = ?;
configurable table<Album> xmlTable2 = ?;

// Test xml unions
configurable xml:Element|int|float xmlUnion1 = ?;
configurable xml|Album xmlUnion2 = ?;
configurable xml:ProcessingInstruction|xml:Element xmlUnion3 = ?;

public function main() {
    testSimpleXmlType();
    testStructuredXmlType();
    testUnionXmlType();
    util:print("Tests passed");
}

function testSimpleXmlType() {
    test:assertEquals(xmlVar1.toString(), "<book><name>Sherlock Holmes</name><author>Arthur Conan Doyle" + 
    "</author></book>");
    test:assertEquals(xmlVar2.toString(), "<!--I am a comment-->");
    test:assertEquals(xmlVar3.toString(), "<greet>Hello!</greet>");
    test:assertEquals(xmlVar4.toString(), "<?xml-stylesheet href=\"mystyle.css\" type=\"text/css\"?>");
    test:assertEquals(xmlVar5.toString(), "xml text");
}

function testStructuredXmlType() {
    test:assertEquals(xmlArr.toString(), "[`<!--I am a comment-->`,`<?target data?>`]");
    test:assertEquals(xmlMap.toString(), "{\"title\":`<title>This is a title!</title>`," + 
    "\"comment\":`<!--I am a comment-->`}");
    test:assertEquals(xmlRecord.toString(), "{\"details\":`<TITLE>Hide your heart</TITLE>" +
    "<ARTIST>Bonnie Tylor</ARTIST><COUNTRY>UK</COUNTRY><YEAR>1988</YEAR>`}");
    test:assertEquals(xmlTable1.toString(), "[{\"title\":`<title>This is a title!</title>`," +
    "\"comment\":`<!--I am a comment-->`},{\"title\":`<greet>Hello!</greet>`,\"comment\":`<!--comment-->`}]");
    test:assertEquals(xmlTable2.toString(), "[{\"details\":`<TITLE>Black angel</TITLE>" +
    "<ARTIST>Savage Rose</ARTIST><COUNTRY>EU</COUNTRY><YEAR>1995</YEAR>`,\"price\":10.9}," +
    "{\"details\":`<TITLE>For the good times</TITLE><ARTIST>Kenny Rogers</ARTIST><COUNTRY>UK</COUNTRY>" +
    "<YEAR>1995</YEAR>`}]");
}

function testUnionXmlType() {
    test:assertEquals(xmlUnion1.toString(), "<book><name>Harry Potter and the sorcerer's stone</name></book>");
    test:assertEquals(xmlUnion2.toString(), "<greet>Hello!</greet>");
    test:assertEquals(xmlUnion3.toString(), "<?xml-stylesheet href=\"mystyle.css\" type=\"text/css\"?>");
}

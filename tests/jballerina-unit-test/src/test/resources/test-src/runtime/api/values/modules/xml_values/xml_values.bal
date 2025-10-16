// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
import ballerina/jballerina.java;

public function validateAPI() {
    xml xmlVal = getXMLValueFromString1();
    test:assertEquals(xmlVal.toString(), "<book>The Lost World</book>");
    test:assertEquals(xmlVal.data(), "The Lost World");

    xmlVal = getXMLValueFromString2();
    test:assertEquals(xmlVal.toString(), "<reservationID>12345678901234567890123456789012345678901234567890" +
    "12345678901234567890123456789012345678901234567890aaaaaa</reservationID>");
    test:assertEquals(xmlVal.data(), "1234567890123456789012345678901234567890123456789012345678901234567890" +
    "123456789012345678901234567890aaaaaa");

    xmlVal = getXMLValueFromInputStream1();
    test:assertEquals(xmlVal.toString(), "<book>The Lost World</book>");
    test:assertEquals(xmlVal.data(), "The Lost World");

    xmlVal = getXMLValueFromInputStream2();

    test:assertEquals(xmlVal.children().length(), 5);
    test:assertEquals(xmlVal.children()[0].toString(), "\n");

    test:assertTrue(xmlVal.children()[1] is xml:Element);
    xml:Element child1 = <xml:Element>xmlVal.children()[1];
    test:assertEquals(child1.toString(), "<reservationID>123456789012345678901234567890123456789012345678901234567890" +
    "123456789012345678901234567890123456789exceeding100chars</reservationID>");
    test:assertEquals(child1.getName(), "reservationID");
    test:assertEquals(child1.children().length(), 1);
    test:assertTrue(child1.children()[0] is xml:Text);

    xml:Text text = <xml:Text>child1.children()[0];
    string xmlString = "12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
    "1234567890123456789exceeding100chars";
    test:assertEquals(text.data(), xmlString);

    test:assertEquals(xmlVal.children()[2].toString(), "\n    ");

    test:assertTrue(xmlVal.children()[3] is xml:Element);
    xml:Element child2 = <xml:Element>xmlVal.children()[3];
    test:assertEquals(child2.getName(), "confirmationID");
    test:assertEquals(child2.children().length(), 1);
    test:assertTrue(child2.children()[0] is xml:Text);
    test:assertEquals(child2.children()[0].data(), "RPFABE");

    test:assertEquals(xmlVal.children()[4].toString(), "\n");
}

function getXMLValueFromString1() returns xml = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getXMLValueFromString2() returns xml = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getXMLValueFromInputStream1() returns xml = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getXMLValueFromInputStream2() returns xml = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

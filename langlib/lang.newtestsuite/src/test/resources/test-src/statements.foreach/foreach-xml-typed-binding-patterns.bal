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

import ballerina/lang.test as test;

string output = "";

string expectedXml1 = "0:<p:person xmlns:p=\"foo\" xmlns:q=\"bar\">\n" +
            "        <p:name>bob</p:name>\n" +
            "        <p:address>\n" +
            "            <p:city>NY</p:city>\n" +
            "            <q:country>US</q:country>\n" +
            "        </p:address>\n" +
            "        <q:ID>1131313</q:ID>\n" +
            "    </p:person> ";

string expectedXml2 = "0:<p:name xmlns:p=\"foo\">bob</p:name> " +
            "1:<p:address xmlns:p=\"foo\">\n" +
            "            <p:city>NY</p:city>\n" +
            "            <q:country xmlns:q=\"bar\">US</q:country>\n" +
            "        </p:address> 2:<q:ID xmlns:q=\"bar\">1131313</q:ID> ";

xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
        <p:name>bob</p:name>
        <p:address>
            <p:city>NY</p:city>
            <q:country>US</q:country>
        </p:address>
        <q:ID>1131313</q:ID>
    </p:person>`;

function concatIntXml(int i, xml x) {
    output = output + i.toString() + ":" + x.toString() + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testXmlWithRootWithSimpleVariableWithoutType() {
    output = "";

    int i = 0;
    foreach var v in xdata {
        if v is xml {
            concatIntXml(i, v);
            i += 1;
        }
    }
    test:assertEquals(output, expectedXml1);
}

function testXmlWithRootWithSimpleVariableWithType() {
    output = "";

    int i = 0;
    foreach xml|string v in xdata {
        if v is xml {
            concatIntXml(i, v);
            i += 1;
        }
    }
    test:assertEquals(output, expectedXml1);
}

// ---------------------------------------------------------------------------------------------------------------------

function testXmlInnerElementsWithSimpleVariableWithoutType() {
    output = "";

    int i = 0;
    foreach var v in (xdata/*).elements() {
        if v is xml {
            concatIntXml(i, v);
            i += 1;
        }
    }
    test:assertEquals(output, expectedXml2);
}

function testXmlInnerElementsWithSimpleVariableWithType() {
    output = "";

    int i = 0;
    foreach xml|string v in (xdata/*).elements() {
        if v is xml {
            concatIntXml(i, v);
            i += 1;
        }
    }
    test:assertEquals(output, expectedXml2);
}

// ---------------------------------------------------------------------------------------------------------------------

function testEmptyXmlIteration() {
    output = "";

    xml data = xml `ABC`;

    int i = 0;
    foreach var v in data/* {
        if v is xml {
            concatIntXml(i, v);
            i += 1;
        }
    }
    test:assertEquals(output, "");
}

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

string payload = "<p:person xmlns:p=\"foo\" xmlns:q=\"bar\">\n" +
                             "        <p:name>bob</p:name>\n" +
                             "        <p:address>\n" +
                             "            <p:city>NY</p:city>\n" +
                             "            <q:country>US</q:country>\n" +
                             "        </p:address>\n" +
                             "        <q:ID>1131313</q:ID>\n" +
                             "    </p:person> ";

xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
        <p:name>bob</p:name>
        <p:address>
            <p:city>NY</p:city>
            <q:country>US</q:country>
        </p:address>
        <q:ID>1131313</q:ID>
    </p:person>`;

function concatString (string value) {
    output = output + value + " ";
}

function concatIntString (int i, string v) {
    output = output + i.toString() + ":" + v + " ";
}

function testXMLWithArityOne() {
    output = "";
    foreach var x in xdata {
        if x is xml {
            concatString(x.toString());
        }
    }
    test:assertEquals(output, payload);
}

function testXMLWithArityTwo() {
    output = "";
    int i = 0;
    foreach var x in xdata {
        if x is xml {
            concatIntString(i, x.toString());
            i += 1;
        }
    }
    test:assertEquals(output, "0:" + payload);
}

function testXMLWithArityChildren() {
    string payload = "0:<p:name xmlns:p=\"foo\">bob</p:name> 1:<p:address xmlns:p=\"foo\">\n" +
                    "            <p:city>NY</p:city>\n" +
                    "            <q:country xmlns:q=\"bar\">US</q:country>\n" +
                    "        </p:address> 2:<q:ID xmlns:q=\"bar\">1131313</q:ID> ";
    output = "";
    int i = 0;
    foreach var x in (xdata/*).elements() {
        if x is xml {
            concatIntString(i, x.toString());
            i += 1;
        }
    }
    test:assertEquals(output, payload);
}

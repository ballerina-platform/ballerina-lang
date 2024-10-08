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

xmlns "foo" as ns;
xmlns "bar" as k;

function testXMLFilterExpr() {
    xml x1 = xml `<ns:root></ns:root>`;
    xml x2 = x1.<*>; // get all elements
    x2 = x1.<ns:*>;
    x2 = getXML().<ns:root>;
    x2 = x1.<ns:other>;
    x2 = x1.<other>;
    x2 = x1.<k:*>;
    x2 = getXML().<ns:*|k:*>;
}

function testXMLStepExpr() {
    xml x1 = xml `<ns:root><ns:child></ns:child></ns:root>`;
    xml x2 = x1/<ns:child>;
    x2 = x1/*;
    x2 = x1/<*>;
    x2 = x1/**/<ns:child>;
    x2 = x1/<ns:child>[indx];
    x2 = x1/**/<ns:child|k:child>;
    string s = x1/**/<ns:child>.toString();
}

// utils

int indx = 0;

function getXML() returns xml => xml `<greet>Hello World!</greet>`;

function testXmlStepExprWithExtension() {
    xml x1 = xml `<item><ns:name>T-shirt</ns:name></item><item>text<name>Watch</name></item>`; 
    xml x2 = x1/*.get(indx);
    x2 = x1/<ns:name>.<ns:item>;
    x2 = x1/**/<name>.<ns:item|name>[indx];
}

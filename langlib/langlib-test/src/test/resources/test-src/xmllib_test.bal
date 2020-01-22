// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'xml as xmllib;

xml catalog = xml `<CATALOG>
                       <CD>
                           <TITLE>Empire Burlesque</TITLE>
                           <ARTIST>Bob Dylan</ARTIST>
                       </CD>
                       <CD>
                           <TITLE>Hide your heart</TITLE>
                           <ARTIST>Bonnie Tyler</ARTIST>
                       </CD>
                       <CD>
                           <TITLE>Greatest Hits</TITLE>
                           <ARTIST>Dolly Parton</ARTIST>
                       </CD>
                   </CATALOG>`;

function testLength(xml x) returns int {
    return x.length();
}

// data provider function
function getXML() returns xml[] {
    xml[] data = [];

    data[data.length()] = catalog;
    data[data.length()] = catalog["CD"][0];
    data[data.length()] = xml `Hello World!`;

    return data;
}

function getNameOfElement() returns string {
    xml elem = xml `<elem>elem</elem>`;
    return elem.getName();
}

function getNameOfElementNegative() returns string {
    xml seq = xml `<elem>a</elem>` + xml `<elem>a</elem>`;
    return seq.getName();
}

function testSetElementName() returns xml {
    xml elem = xml `<elem attr="attr1">content</elem>`;
    elem.setName("el2");
    return elem;
}

function testSetElementNameNegative() returns xml {
    xml elem = xml `<elem attr="attr1">content</elem>`;
    xml elemN = xml `<elemN></elemN>`;
    xml seq = elem + elemN;
    seq.setName("el2");
    return seq;
}

function testGetChildren() returns xml {
    return catalog.getChildren().elements()[0].getChildren().elements()[0];
}

function testGetChildrenNegative() returns xml {
    return catalog.getChildren().getChildren();
}

function testSetChildren() returns xml {
    xml child = xml `<e>child</e>`;
    catalog.getChildren().elements()[0].setChildren(child);
    return catalog.getChildren().elements()[0];
}

function testSetChildrenNegative() {
    xml child = xml `<e>child</e>`;
    catalog.getChildren().setChildren(child);
}

function testGetAttributes() returns map<string> {
    xml elem = xml `<elem attr="attr1" attr2="attr2">content</elem>`;
    return elem.getAttributes();
}

function testGetAttributesNegative() returns map<string> {
    xml elem = xml `<elem attr="attr1">content</elem>`;
    xml elemN = xml `<elemN></elemN>`;
    xml seq = elem + elemN;
    return seq.getAttributes();
}

function testGetContent() returns [string, string, string] {
    xml t = xml `hello world`;
    xml pi = xml `<?pi-node type="cont"?>`;
    xml comment = xml `<!-- this is a comment text -->`;
    return [t.getContent(), pi.getContent(), comment.getContent()];
}

function testGetContentNegative() returns string {
    xml t = xml `<elm>cont</elm>`;
    return t.getContent();
}

function testForEach() returns xml {
    xml a = xml `<elm></elm>`;
    xml r = a.getChildren();
    foreach var x in catalog.* {
        if (x is xml) {
            r += x;
        }
    }
    return r;
}

function testSlice() returns [xml, xml, xml] {
    xml elemL = xml `<elemL>content</elemL>`;
    xml elemN = xml `<elemN>content</elemN>`;
    xml elemM = xml `<elemM>content</elemM>`;
    xml elem = elemL + elemN + elemM;
    return [elem.slice(0, 2), elem.slice(1), xmllib:slice(elem, 1)];
}

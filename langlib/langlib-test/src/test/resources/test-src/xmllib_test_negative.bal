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

import ballerina/lang.'xml as xmllib;

function getNameOfElementNegative() returns string {
    xml seq = xmllib:concat(xml `<elem>a</elem>`, xml `<elem>a</elem>`);
    return seq.getName();
}

function testSetElementNameNegative() returns xml {
    xml elem = xml `<elem attr="attr1">content</elem>`;
    xml elemN = xml `<elemN></elemN>`;
    xml seq = xmllib:concat(elem, elemN);
    seq.setName("el2");
    return seq;
}

function testGetChildrenNegative() returns xml {
     xml elem = xml `<elem attr="attr1">content</elem>`;
     xml elemN = xml `<elemN></elemN>`;
     xml seq = xmllib:concat(elem, elemN);
     return seq.getChildren();
}

function testSetChildrenNegative() {
    xml child = xml `<e>child</e>`;
    xml elem = xml `<elem attr="attr1">content</elem>`;
    xml elemN = xml `<elemN></elemN>`;
    xml seq = xmllib:concat(elem, elemN);
    seq.setChildren(child);
}

function testGetAttributesNegative() returns map<string> {
    xml elem = xml `<elem attr="attr1">content</elem>`;
    xml elemN = xml `<elemN></elemN>`;
    xml seq = xmllib:concat(elem, elemN);
    return seq.getAttributes();
}

function testGetTargetNegative() {
   xml elm = xml `<elm>e</elm>`;
   _ = elm.getTarget();
}

function testGetContentNegative() returns string {
    xmllib:Element element = <xmllib:Element> xml `<elem attr="attr1">content</elem>`;
    return element.getContent();
}

xml theXml = xml `<book>the book</book>`;
xml bitOfText = xml `bit of text\u2702\u2705`;
xml compositeXml = theXml + bitOfText;

function testGetDescendantsFromSeq() returns xml {
   xml x4 = compositeXml.getDescendants();
   return x4;
}

function tesXMLStrip() returns xml {
   xml<'xml:Element> x21 = xml `<foo><bar/><?foo?>text1 text2<!--Com1--> <bar/></foo><foo><?foo?>text1<!--Com2--></foo>`;
   xml<'xml:Element> x22 = x21.strip();
}

type attributesRecord record {
    string x;
};

function testCreateElement() {
    xml children1 = xml `<ele>hello</ele><ele>world</ele>`;

    record {string x;} attributes1 = {
                x: "https://ballerina.io"
    };

    attributesRecord attributes2 = {x:"https://ballerina.io"};

    map<string> attributes3 = {
        "href" : "https://ballerina.io"
    };

    xml:Element r1 = 'xml:createElement("elem", attributes1, children1);
    xml:Element r2 = 'xml:createElement("elem", attributes2, children1);
    xml:Element r3 = 'xml:createElement("elem", attributes3, "<ele>hello</ele>");
    xml:Element r4 = 'xml:createElement(xml `<ele/>`, attributes3, children1);
}

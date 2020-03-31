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

import ballerina/lang.'xml;
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

function testFromString() returns xml|error {
    string s = catalog.toString();
    xml x = <xml> 'xml:fromString(s);
    return x["CD"]["TITLE"];
}

function emptyConcatCall() returns xml {
    return 'xml:concat();
}

function testConcat() returns xml {
    xml x = xml `<hello>xml content</hello>`;
    return 'xml:concat(x, <xml> testFromString(), "hello from String");
}

function testIsElement() returns [boolean, boolean, boolean] {
    xml x1 = 'xml:concat();
    boolean b1 = x1 is 'xml:Element;

    boolean b2 = false;
    xml x2 = catalog;
    if(x2 is 'xml:Element) {
        if(x2.getName() == "CATALOG") {
            b2 = true;
        }
    }

    boolean b3 = testConcat() is 'xml:Element;
    return [b1, b2, b3];
}

function testXmlPI() returns [boolean, boolean] {
    xml pi = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    return [pi is 'xml:ProcessingInstruction,
        emptyConcatCall() is 'xml:ProcessingInstruction];
}

function testXmlIsComment() returns [boolean, boolean] {
    xml cmnt = xml `<!-- hello from comment -->`;
    return [cmnt is 'xml:Comment,
        emptyConcatCall() is 'xml:Comment];
}

function testXmlIsText() returns [boolean, boolean] {
    xml text = xml `hello text`;
    return [text is 'xml:Text,
        emptyConcatCall() is 'xml:Text];
}

function getNameOfElement() returns string {
    'xml:Element element = <'xml:Element> xml `<elem>elem</elem>`;
    return element.getName();
}

function testSetElementName() returns xml {
    'xml:Element element = <'xml:Element> xml `<elem attr="attr1">content</elem>`;
    element.setName("el2");
    return element;
}

function testGetChildren() returns xml {
    'xml:Element cat = <'xml:Element> catalog;
    xml ch1  = cat.getChildren().strip()[0];
    'xml:Element ch1e = <'xml:Element> ch1;
    return ch1e.getChildren().strip();
}

function testSetChildren() returns xml {
    xml child = xml `<e>child</e>`;
    'xml:Element catElm = <'xml:Element> catalog;
    xml ch1 = catElm.getChildren().strip()[0];
    'xml:Element ch1em = <'xml:Element> ch1;
    ch1em.setChildren(child);
    return catElm.getChildren().strip()[0];
}

function testGetAttributes() returns map<string> {
    'xml:Element elem = <'xml:Element> xml `<elem attr="attr1" attr2="attr2">content</elem>`;
    return elem.getAttributes();
}

function testGetTarget() returns string {
    xml x = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    'xml:ProcessingInstruction pi = <'xml:ProcessingInstruction> x;
    return pi.getTarget();
}

function testGetContent() returns [string, string, string] {
    'xml:Text t = <'xml:Text> xml `hello world`;
    'xml:ProcessingInstruction pi = <'xml:ProcessingInstruction> xml `<?pi-node type="cont"?>`;
    'xml:Comment comment = <'xml:Comment> xml `<!-- this is a comment text -->`;
    return [t.getContent(), pi.getContent(), comment.getContent()];
}

function testCreateElement() returns [xml, xml, xml] {
    xml t = xml `hello world`;
    'xml:Element r1 = 'xml:createElement("elem", t);
    'xml:Element r2 = 'xml:createElement("elem");

    return [r1, r1.getChildren(), r2.getChildren()];
}

function testCreateProcessingInstruction() returns xml {
    return 'xml:createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");
}

function testCreateComment() returns xml {
    return 'xml:createComment("This text should be wraped in xml comment");
}

function testForEach() returns xml {
    xml r = 'xml:concat();
    foreach var x in catalog/* {
        if (x is xml) {
            if (x is 'xml:Element) {
                r = 'xml:concat(r, x);
            }
        }
    }
    return r;
}

function testSlice() returns [xml, xml, xml] {
    'xml:Element elemL = <'xml:Element> xml `<elemL>content</elemL>`;
    'xml:Element elemN = <'xml:Element> xml `<elemN>content</elemN>`;
    'xml:Element elemM = <'xml:Element> xml `<elemM>content</elemM>`;
    xml elem = 'xml:concat(elemL, elemN, elemM);
    return [elem.slice(0, 2), elem.slice(1), 'xml:slice(elem, 1)];
}

function testXMLCycleError() returns [error|xml, error|xml] {
     return [trap testXMLCycleErrorInner(), trap testXMLCycleInnerNonError()];
}

function testXMLCycleErrorInner() returns xml {
    'xml:Element cat = <'xml:Element> catalog.clone();
    'xml:Element fc = <'xml:Element> cat.getChildren().strip()[0];
    fc.setChildren(cat);
    return cat;
}

function testXMLCycleInnerNonError() returns xml {
    'xml:Element cat = <'xml:Element> catalog.clone();
    var cds = cat.getChildren().strip();
    'xml:Element fc = <'xml:Element> cds[0];
    fc.setChildren(cds[1]);
    return cat;
}

function testXMLCycleDueToChildrenOfChildren() returns xml|error {
    'xml:Element cat = <'xml:Element> catalog.clone();
    'xml:Element subRoot = <'xml:Element> xml `<subRoot></subRoot>`;
    subRoot.setChildren(cat);
    var cds = cat.getChildren().strip();
    'xml:Element fc = <'xml:Element> cds[0];
    error? er = trap fc.setChildren(subRoot);
    check trap fc.setChildren(subRoot);
    return cat;
}

function testGet() returns [xml|error, xml|error, xml|error, xml|error, xml|error] {
    var e = 'xml:createElement("elem");
    xml|error e1 = trap e.get(0);
    xml|error e2 = trap e.get(3);

    var c = 'xml:createComment("Comment content");
    xml|error c1 = trap c.get(0);

    var p = 'xml:createProcessingInstruction("PITarget", "VAL-0");
    var s = 'xml:concat(e, c, p);
    xml|error item = trap s.get(2);
    xml|error item2 = trap s.get(-1);

    return [e1, e2, c1, item, item2];
}

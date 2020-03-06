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

function testFromString() returns xml|error {
    string s = catalog.toString();
    xml x = <xml> xmllib:fromString(s);
    return x["CD"]["TITLE"];
}

function emptyConcatCall() returns xml {
    return xmllib:concat();
}

function testConcat() returns xml {
    xml x = xml `<hello>xml content</hello>`;
    return xmllib:concat(x, <xml> testFromString(), "hello from String");
}

function testIsElement() returns [boolean, boolean, boolean] {
    return [xmllib:concat().isElement(), catalog.isElement(), testConcat().isElement()];
}

function testXmlPI() returns [boolean, boolean, boolean] {
    xml pi = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    return [pi.isProcessingInstruction(),
        xmllib:isProcessingInstruction(pi),
        emptyConcatCall().isProcessingInstruction()];
}

function testXmlIsComment() returns [boolean, boolean, boolean] {
    xml cmnt = xml `<!-- hello from comment -->`;
    return [cmnt.isComment(),
        xmllib:isComment(cmnt),
        emptyConcatCall().isComment()];
}

function testXmlIsText() returns [boolean, boolean, boolean] {
    xml text = xml `hello text`;
    return [text.isText(),
        xmllib:isText(text),
        emptyConcatCall().isComment()];
}

function getNameOfElement() returns string {
    xml elem = xml `<elem>elem</elem>`;
    return elem.getName();
}

function getNameOfElementNegative() returns string {
    xml seq = xmllib:concat(xml `<elem>a</elem>`, xml `<elem>a</elem>`);
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
    xml seq = xmllib:concat(elem, elemN);
    seq.setName("el2");
    return seq;
}

function testGetChildren() returns xml {
    return catalog.getChildren().strip()[0].getChildren().strip();
}

function testGetChildrenNegative() returns xml {
    return catalog.getChildren().strip().getChildren();
}

function testSetChildren() returns xml {
    xml child = xml `<e>child</e>`;
    catalog.getChildren().strip()[0].setChildren(child);
    return catalog.getChildren().strip()[0];
}

function testSetChildrenNegative() {
    xml child = xml `<e>child</e>`;
    catalog.getChildren().strip().setChildren(child);
}

function testGetAttributes() returns map<string> {
    xml elem = xml `<elem attr="attr1" attr2="attr2">content</elem>`;
    return elem.getAttributes();
}

function testGetAttributesNegative() returns map<string> {
    xml elem = xml `<elem attr="attr1">content</elem>`;
    xml elemN = xml `<elemN></elemN>`;
    xml seq = xmllib:concat(elem, elemN);
    return seq.getAttributes();
}

function testGetTarget() returns string {
    xml pi = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    return pi.getTarget();
}

function testGetTargetNegative() {
   xml elm = xml `<elm>e</elm>`;
   _ = elm.getTarget();
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

function testCreateElement() returns [xml, xml, xml] {
    xml t = xml `hello world`;
    xml r1 = xmllib:createElement("elem", t);
    xml r2 = xmllib:createElement("elem");

    return [r1, r1.getChildren(), r2.getChildren()];
}

function testCreateProcessingInstruction() returns xml {
    return xmllib:createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");
}

function testCreateComment() returns xml {
    return xmllib:createComment("This text should be wraped in xml comment");
}

function testCopingComment() returns xml {
    xml bookComment = xml `<!--some comment-->`;

    // Makes a copy of an XML element.
    xml x = bookComment.copy();
    return x;
}

function testForEach() returns xml {
    xml r = xmllib:concat();
    foreach var x in catalog/* {
        if (x is xml) {
            if (x.isElement()) {
                r += x;
            }
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

function testXMLCycleError() returns [error|xml, error|xml] {
     return [trap testXMLCycleErrorInner(), trap testXMLCycleInnerNonError()];
}

function testXMLCycleErrorInner() returns xml {
    xml cat = catalog.clone();
    cat.getChildren().strip()[0].setChildren(cat);
    return cat;
}

function testXMLCycleInnerNonError() returns xml {
    xml cat = catalog.clone();
    var cds = cat.getChildren().strip();
    cds[0].setChildren(cds[1]);
    return cat;
}

function testXMLCycleDueToChildrenOfChildren() returns xml|error {
    xml cat = catalog.clone();
    xml subRoot = xml `<subRoot></subRoot>`;
    subRoot.setChildren(cat);
    var cds = cat.getChildren().strip();
    error? er = trap cds[0].setChildren(subRoot);
    check trap cds[0].setChildren(subRoot);
    return cat;
}

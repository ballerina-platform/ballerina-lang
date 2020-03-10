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
    xml x1 = xmllib:concat();
    boolean b1 = x1 is xmllib:Element;

    boolean b2 = false;
    xml x2 = catalog;
    if(x2 is xmllib:Element) {
        if(x2.getName() == "CATALOG") {
            b2 = true;
        }
    }

    boolean b3 = testConcat() is xmllib:Element;
    return [b1, b2, b3];
}

function testXmlPI() returns [boolean, boolean] {
    xml pi = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    return [pi is xmllib:ProcessingInstruction,
        emptyConcatCall() is xmllib:ProcessingInstruction];
}

function testXmlIsComment() returns [boolean, boolean] {
    xml cmnt = xml `<!-- hello from comment -->`;
    return [cmnt is xmllib:Comment,
        emptyConcatCall() is xmllib:Comment];
}

function testXmlIsText() returns [boolean, boolean] {
    xml text = xml `hello text`;
    return [text is xmllib:Text,
        emptyConcatCall() is xmllib:Text];
}

function getNameOfElement() returns string {
    xmllib:Element element = <xmllib:Element> xml `<elem>elem</elem>`;
    return element.getName();
}

function testSetElementName() returns xml {
    xmllib:Element element = <xmllib:Element> xml `<elem attr="attr1">content</elem>`;
    element.setName("el2");
    return element;
}

function testGetChildren() returns xml {
    xmllib:Element cat = <xmllib:Element> catalog;
    xml ch1  = cat.getChildren().strip()[0];
    xmllib:Element ch1e = <xmllib:Element> ch1;
    return ch1e.getChildren().strip();
}

function testSetChildren() returns xml {
    xml child = xml `<e>child</e>`;
    xmllib:Element catElm = <xmllib:Element> catalog;
    xml ch1 = catElm.getChildren().strip()[0];
    xmllib:Element ch1em = <xmllib:Element> ch1;
    ch1em.setChildren(child);
    return catElm.getChildren().strip()[0];
}

function testGetAttributes() returns map<string> {
    xmllib:Element elem = <xmllib:Element> xml `<elem attr="attr1" attr2="attr2">content</elem>`;
    return elem.getAttributes();
}

function testGetTarget() returns string {
    xml x = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    xmllib:ProcessingInstruction pi = <xmllib:ProcessingInstruction> x;
    return pi.getTarget();
}

function testGetContent() returns [string, string, string] {
    xmllib:Text t = <xmllib:Text> xml `hello world`;
    xmllib:ProcessingInstruction pi = <xmllib:ProcessingInstruction> xml `<?pi-node type="cont"?>`;
    xmllib:Comment comment = <xmllib:Comment> xml `<!-- this is a comment text -->`;
    return [t.getContent(), pi.getContent(), comment.getContent()];
}

function testCreateElement() returns [xml, xml, xml] {
    xml t = xml `hello world`;
    xmllib:Element r1 = xmllib:createElement("elem", t);
    xmllib:Element r2 = xmllib:createElement("elem");

    return [r1, r1.getChildren(), r2.getChildren()];
}

function testCreateProcessingInstruction() returns xml {
    return xmllib:createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");
}

function testCreateComment() returns xml {
    return xmllib:createComment("This text should be wraped in xml comment");
}

//function testForEach() returns xml {
//    xml r = xmllib:concat();
//    foreach var x in catalog/* {
//        if (x is xml) {
//            if (x.isElement()) {
//                r += x;
//            }
//        }
//    }
//    return r;
//}

//function testSlice() returns [xml, xml, xml] {
//    xmllib:Element elemL = <xmllib:Element> xml `<elemL>content</elemL>`;
//    xmllib:Element elemN = <xmllib:Element> xml `<elemN>content</elemN>`;
//    xmllib:Element elemM = <xmllib:Element> xml `<elemM>content</elemM>`;
//    xml elem = xmllib:concat(elemL, elemN, elemM);
//    return [elem.slice(0, 2), elem.slice(1), xmllib:slice(elem, 1)];
//}
//
//function testXMLCycleError() returns [error|xml, error|xml] {
//     return [trap testXMLCycleErrorInner(), trap testXMLCycleInnerNonError()];
//}
//
//function testXMLCycleErrorInner() returns xml {
//    xml cat = catalog.clone();
//    cat.getChildren().strip()[0].setChildren(cat);
//    return cat;
//}
//
//function testXMLCycleInnerNonError() returns xml {
//    xml cat = catalog.clone();
//    var cds = cat.getChildren().strip();
//    cds[0].setChildren(cds[1]);
//    return cat;
//}
//
//function testXMLCycleDueToChildrenOfChildren() returns xml|error {
//    xml cat = catalog.clone();
//    xml subRoot = xml `<subRoot></subRoot>`;
//    subRoot.setChildren(cat);
//    var cds = cat.getChildren().strip();
//    error? er = trap cds[0].setChildren(subRoot);
//    check trap cds[0].setChildren(subRoot);
//    return cat;
//}
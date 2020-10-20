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
import ballerina/lang.'int as langint;

'xml:Element catalog = xml `<CATALOG>
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
    data[data.length()] = catalog/<CD>[0];
    data[data.length()] = xml `Hello World!`;

    return data;
}

function testFromString() returns xml|error {
    string s = catalog.toString();
    xml x = <xml> 'xml:fromString(s);
    return x/<CD>/<TITLE>;
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
    'xml:Element element = xml `<elem>elem</elem>`;
    return element.getName();
}

function testSetElementName() returns xml {
    'xml:Element element = xml `<elem attr="attr1">content</elem>`;
    element.setName("el2");
    return element;
}

function testGetChildren() returns xml {
    xml ch1  = catalog.getChildren().strip()[0];
    'xml:Element ch1e = <'xml:Element> ch1;
    return ch1e.getChildren().strip();
}

function testSetChildren() returns xml {
    xml child = xml `<e>child</e>`;
    xml ch1 = catalog.getChildren().strip()[0];
    'xml:Element ch1em = <'xml:Element> ch1;
    ch1em.setChildren(child);
    return catalog.getChildren().strip()[0];
}

function testGetAttributes() returns map<string> {
    'xml:Element elem = xml `<elem attr="attr1" attr2="attr2">content</elem>`;
    return elem.getAttributes();
}

function testGetTarget() returns string {
    'xml:ProcessingInstruction pi = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    return pi.getTarget();
}

function testGetContent() returns [string, string, string] {
    'xml:Text t = <'xml:Text> xml `hello world`;
    'xml:ProcessingInstruction pi = <'xml:ProcessingInstruction> xml `<?pi-node type="cont"?>`;
    'xml:Comment comment = xml `<!-- this is a comment text -->`;
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

function testCreateText() {
    'xml:Text text1 = 'xml:createText("This is xml text");
    'xml:Text text2 = 'xml:createText("");
    'xml:Text text3 = 'xml:createText("T");
    'xml:Text text4 = 'xml:createText("Thisisxmltext");
    'xml:Text text5 = 'xml:createText("XML\ntext");

    assert(<string>text1, "This is xml text");
    assert(<string>text2, "");
    assert(<string>text3, "T");
    assert(<string>text4, "Thisisxmltext");
    assert(<string>text5, "XML\ntext");
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
    'xml:Element elemL = xml `<elemL>content</elemL>`;
    'xml:Element elemN = xml `<elemN>content</elemN>`;
    'xml:Element elemM = xml `<elemM>content</elemM>`;
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

xml bookstore = xml `<bookstore><book category="cooking">
                            <title lang="en">Everyday Italian</title>
                            <author>Giada De Laurentiis</author>
                            <year>1990</year>
                            <price>30.00</price>
                        </book>
                        <book category="children">
                            <title lang="en">Harry Potter</title>
                            <author>J. K. Rowling</author>
                            <year>2005</year>
                            <price>29.99</price>
                        </book>
                        <book category="web" cover="paperback">
                            <title lang="en">Learning XML</title>
                            <author>Erik T. Ray</author>
                            <year>2020</year>
                            <price>39.95</price>
                        </book>
                    </bookstore>`;

function testAsyncFpArgsWithXmls() returns [int, xml] {

    int sum = 0;
    ((bookstore/*).elements()).forEach(function (xml x) {
      int value =   <int>langint:fromString((x/<year>/*).toString()) ;
      future<int> f1 = start getRandomNumber(value);
      int result = wait f1;
      sum = sum + result;
    });

    var filter = ((bookstore/*).elements()).filter(function (xml x) returns boolean {
      int value =   <int>langint:fromString((x/<year>/*).toString()) ;
      future<int> f1 = start getRandomNumber(value);
      int result = wait f1;
      return result > 2000;
    });

    var filter2 = (filter).map(function (xml x) returns xml {
      int value =   <int>langint:fromString((x/<year>/*).toString()) ;
      future<int> f1 = start getRandomNumber(value);
      int result = wait f1;
      return xml `<year>${result}</year>`;
    });
    return [sum, filter];
}

function getRandomNumber(int i) returns int {
    return i + 2;
}

function testChildren() {
     xml brands = xml `<Brands><!-- Comment --><Apple>IPhone</Apple><Samsung>Galaxy</Samsung><OP>OP7</OP></Brands>`;

     xml p = brands.children(); // equivalent to getChildren()
     assert(p.length(), 4);
     assert(p.toString(), "<!-- Comment --><Apple>IPhone</Apple><Samsung>Galaxy</Samsung><OP>OP7</OP>");

     xml seq = brands/*;
     xml q = seq.children();
     assert(q.length(), 3);
     assert(q.toString(), "IPhoneGalaxyOP7");
}

function testElements() {
    xml presidents = xml `<Leaders>
                            <!-- This is a comment -->
                            <US>Obama</US>
                            <US>Trump</US>
                            <RUS>Putin</RUS>
                          </Leaders>`;
    xml seq = presidents/*;

    xml y = seq.elements();
    assert(y.length(), 3);
    assert(y.toString(), "<US>Obama</US><US>Trump</US><RUS>Putin</RUS>");

    xml z = seq.elements("RUS");
    assert(z.length(), 1);
    assert(z.toString(), "<RUS>Putin</RUS>");
}

function testElementsNS() {
    xmlns "foo" as ns;
    xml presidents = xml `<Leaders>
                            <!-- This is a comment -->
                            <ns:US>Obama</ns:US>
                            <US>Trump</US>
                            <RUS>Putin</RUS>
                          </Leaders>`;
    xml seq = presidents/*;

    xml usNs = seq.elements("{foo}US");
    assert(usNs.length(), 1);
    assert(usNs.toString(), "<ns:US xmlns:ns=\"foo\">Obama</ns:US>");

    xml usNoNs = seq.elements("US");
    assert(usNoNs.length(), 1);
    assert(usNoNs.toString(), "<US>Trump</US>");
}

function testElementChildren() {
    xml letter = xml `<note>
                        <to>Tove</to>
                        <to>Irshad</to>
                        <!-- This is a comment -->
                        <from>Jani</from>
                        <body>Don't forget me this weekend!</body>
                      </note>`;

    xml p = letter.elementChildren();
    xml q = letter.elementChildren("to");

    assert(p.length(), 4);
    assert(p.toString(), "<to>Tove</to><to>Irshad</to><from>Jani</from><body>Don't forget me this weekend!</body>");
    assert(q.length(), 2);
    assert(q.toString(), "<to>Tove</to><to>Irshad</to>");

    xml seq = 'xml:concat(letter, letter);
    xml y = seq.elementChildren();
    xml z = seq.elementChildren("to");

    assert(y.length(), 8);
    assert(y.toString(), "<to>Tove</to><to>Irshad</to><from>Jani</from><body>Don't forget me this weekend!</body>" +
                         "<to>Tove</to><to>Irshad</to><from>Jani</from><body>Don't forget me this weekend!</body>");
    assert(z.length(), 4);
    assert(z.toString(), "<to>Tove</to><to>Irshad</to><to>Tove</to><to>Irshad</to>");
}

function testElementChildrenNS() {
    xmlns "foo" as ns;
    xml letter = xml `<note>
                            <ns:to>Tove</ns:to>
                            <to>Irshad</to>
                            <!-- This is a comment -->
                            <from>Jani</from>
                            <body>Don't forget me this weekend!</body>
                          </note>`;
    xml seq = 'xml:concat(letter, letter);

    xml toNs = seq.elementChildren("{foo}to");
    assert(toNs.length(), 2);
    assert(toNs.toString(), "<ns:to xmlns:ns=\"foo\">Tove</ns:to><ns:to xmlns:ns=\"foo\">Tove</ns:to>");

    xml toNoNs = seq.elementChildren("to");
    assert(toNoNs.length(), 2);
    assert(toNoNs.toString(), "<to>Irshad</to><to>Irshad</to>");
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}




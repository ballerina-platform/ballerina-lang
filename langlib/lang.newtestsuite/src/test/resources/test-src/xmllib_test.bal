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

import ballerina/lang.'xml;
import ballerina/lang.'int as langint;
import ballerina/lang.test as test;

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

function testLength() {
    xml[] data = getXML();
    test:assertEquals(data[0].length(), 1);
    test:assertEquals(data[1].length(), 1);
    test:assertEquals(data[2].length(), 1);
}

// data provider function
function getXML() returns xml[] {
    xml[] data = [];

    data[data.length()] = catalog;
    data[data.length()] = catalog/<CD>[0];
    data[data.length()] = xml `Hello World!`;

    return data;
}


function fromString() returns xml|error {
    string s = catalog.toString();
    xml x = <xml> 'xml:fromString(s);
    return x/<CD>/<TITLE>;
}

function testFromString(){
    xml|error x = fromString();
    test:assertEquals((<xml> x).toString(), "<TITLE>Empire Burlesque</TITLE><TITLE>Hide your heart</TITLE><TITLE>Greatest Hits</TITLE>");
}

function emptyConcatCall() returns xml {
    xml x = 'xml:concat();
    test:assertEquals(x.length(), 0);
    return x;
}

function concat() returns xml {
    xml x = xml `<hello>xml content</hello>`;
    return 'xml:concat(x, <xml> fromString(), "hello from String");
}

function testConcat(){
    xml x = concat();
    //test:assertEquals(x.length(), 5);
    test:assertEquals(x.toString(),
                        "<hello>xml content</hello><TITLE>Empire Burlesque</TITLE><TITLE>Hide your heart</TITLE>" +
                                "<TITLE>Greatest Hits</TITLE>hello from String");
}

function testIsElement() {
    xml x1 = 'xml:concat();
    boolean b1 = x1 is 'xml:Element;

    boolean b2 = false;
    xml x2 = catalog;
    if(x2 is 'xml:Element) {
        if(x2.getName() == "CATALOG") {
            b2 = true;
        }
    }

    boolean b3 = concat() is 'xml:Element;
    test:assertFalse(b1);
    test:assertTrue(b2);
    test:assertFalse(b3);
}

function testXmlPI() {
    xml pi = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    test:assertTrue(pi is 'xml:ProcessingInstruction);
    test:assertFalse(emptyConcatCall() is 'xml:ProcessingInstruction);
}

function testXmlIsComment() {
    xml cmnt = xml `<!-- hello from comment -->`;
    test:assertTrue(cmnt is 'xml:Comment);
    test:assertFalse(emptyConcatCall() is 'xml:Comment);
}

function testXmlIsText() {
    xml text = xml `hello text`;
    test:assertTrue(text is 'xml:Text);
    test:assertTrue(emptyConcatCall() is 'xml:Text);
}

function testGetNameOfElement() returns string {
    'xml:Element element = xml `<elem>elem</elem>`;
    return element.getName();
}

function testSetElementName() {
    'xml:Element element = xml `<elem attr="attr1">content</elem>`;
    element.setName("el2");
    test:assertEquals(element.toString(), "<el2 attr=\"attr1\">content</el2>");
}

function testGetChildren() {
    xml ch1  = catalog.getChildren().strip()[0];
    'xml:Element ch1e = <'xml:Element> ch1;
    xml x = ch1e.getChildren().strip();
    test:assertEquals(x.toString(), "<TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST>");
}

function testSetChildren() {
    xml child = xml `<e>child</e>`;
    xml ch1 = catalog.getChildren().strip()[0];
    'xml:Element ch1em = <'xml:Element> ch1;
    ch1em.setChildren(child);
    xml x = catalog.getChildren().strip()[0];
    test:assertEquals(x.toString(), "<CD><e>child</e></CD>");
}

function testGetAttributes() {
    'xml:Element elem = xml `<elem attr="attr1" attr2="attr2">content</elem>`;
    map<string> mp = elem.getAttributes();
    test:assertEquals(mp, {"attr":"attr1", "attr2":"attr2"});
}

function testGetTarget() returns string {
    'xml:ProcessingInstruction pi = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    return pi.getTarget();
}

function testGetContent() {
    'xml:Text t = <'xml:Text> xml `hello world`;
    'xml:ProcessingInstruction pi = <'xml:ProcessingInstruction> xml `<?pi-node type="cont"?>`;
    'xml:Comment comment = xml `<!-- this is a comment text -->`;
    test:assertEquals(t.getContent(), "hello world");
    test:assertEquals(pi.getContent(), "type=\"cont\"");
    test:assertEquals(comment.getContent(), " this is a comment text ");
}

function testCreateElement() {
    xml t = xml `hello world`;
    'xml:Element r1 = 'xml:createElement("elem", t);
    'xml:Element r2 = 'xml:createElement("elem");
    test:assertEquals(r1.toString(), "<elem>hello world</elem>");
    test:assertEquals(r1.getChildren().toString(), "hello world");
    test:assertEquals(r2.getChildren().toString(), "");
}

function testCreateProcessingInstruction() {
    xml x = 'xml:createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");
    test:assertEquals(x.toString(), "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>");
}

function testCreateComment() {
    xml x = 'xml:createComment("This text should be wraped in xml comment");
    test:assertEquals(x.toString(), "<!--This text should be wraped in xml comment-->");
}

function testForEach() {
    xml r = 'xml:concat();
    foreach var x in catalog/* {
        if (x is xml) {
            if (x is 'xml:Element) {
                r = 'xml:concat(r, x);
            }
        }
    }
    int x = r.length();
    //test:assertEquals(x, 3);
}

function testSlice() {
    'xml:Element elemL = xml `<elemL>content</elemL>`;
    'xml:Element elemN = xml `<elemN>content</elemN>`;
    'xml:Element elemM = xml `<elemM>content</elemM>`;
    xml elem = 'xml:concat(elemL, elemN, elemM);
    test:assertEquals(elem.slice(0, 2).toString(), "<elemL>content</elemL><elemN>content</elemN>");
    test:assertEquals(elem.slice(1).toString(), "<elemN>content</elemN><elemM>content</elemM>");
    test:assertEquals('xml:slice(elem, 1).toString(), "<elemN>content</elemN><elemM>content</elemM>");
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

function xMLCycleDueToChildrenOfChildren() returns xml|error {
    'xml:Element cat = <'xml:Element> catalog.clone();
    'xml:Element subRoot = <'xml:Element> xml `<subRoot></subRoot>`;
    subRoot.setChildren(cat);
    var cds = cat.getChildren().strip();
    'xml:Element fc = <'xml:Element> cds[0];
    error? er = trap fc.setChildren(subRoot);
    check trap fc.setChildren(subRoot);
    return cat;
}

function testXMLCycleDueToChildrenOfChildren(){
    xml|error x = xMLCycleDueToChildrenOfChildren();
    test:assertError(x);
    test:assertEquals(x.toString(),
                         "error {ballerina/lang.xml}XMLOperationError message=Failed to set children to xml element: Cycle detected");
}

function testGet() {
    var e = 'xml:createElement("elem");
    xml|error e1 = trap e.get(0);
    xml|error e2 = trap e.get(3);

    var c = 'xml:createComment("Comment content");
    xml|error c1 = trap c.get(0);

    var p = 'xml:createProcessingInstruction("PITarget", "VAL-0");
    var s = 'xml:concat(e, c, p);
    xml|error item = trap s.get(2);
    xml|error item2 = trap s.get(-1);

    test:assertEquals(e1.toString(), "<elem/>");
    test:assertError(e2);
    test:assertEquals(e2.toString(), "error xml sequence index out of range. Length: '1' requested: '3'");
    test:assertEquals(c1.toString(), "<!--Comment content-->");
    test:assertEquals(item.toString(), "<?PITarget VAL-0?>");
    test:assertError(item2);
    test:assertEquals(item2.toString(), "error xml sequence index out of range. Length: '3' requested: '-1'");
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
     test:assertEquals(p.length(), 4);
     test:assertEquals(p.toString(), "<!-- Comment --><Apple>IPhone</Apple><Samsung>Galaxy</Samsung><OP>OP7</OP>");

     xml seq = brands/*;
     xml q = seq.children();
     test:assertEquals(q.length(), 3);
     test:assertEquals(q.toString(), "IPhoneGalaxyOP7");
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
    test:assertEquals(y.length(), 3);
    test:assertEquals(y.toString(), "<US>Obama</US><US>Trump</US><RUS>Putin</RUS>");

    xml z = seq.elements("RUS");
    test:assertEquals(z.length(), 1);
    test:assertEquals(z.toString(), "<RUS>Putin</RUS>");
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
    test:assertEquals(usNs.length(), 1);
    test:assertEquals(usNs.toString(), "<ns:US xmlns:ns=\"foo\">Obama</ns:US>");

    xml usNoNs = seq.elements("US");
    test:assertEquals(usNoNs.length(), 1);
    test:assertEquals(usNoNs.toString(), "<US>Trump</US>");
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

    test:assertEquals(p.length(), 4);
    test:assertEquals(p.toString(), "<to>Tove</to><to>Irshad</to><from>Jani</from><body>Don't forget me this weekend!</body>");
    test:assertEquals(q.length(), 2);
    test:assertEquals(q.toString(), "<to>Tove</to><to>Irshad</to>");

    xml seq = 'xml:concat(letter, letter);
    xml y = seq.elementChildren();
    xml z = seq.elementChildren("to");

    test:assertEquals(y.length(), 8);
    test:assertEquals(y.toString(), "<to>Tove</to><to>Irshad</to><from>Jani</from><body>Don't forget me this weekend!</body>" +
                         "<to>Tove</to><to>Irshad</to><from>Jani</from><body>Don't forget me this weekend!</body>");
    test:assertEquals(z.length(), 4);
    test:assertEquals(z.toString(), "<to>Tove</to><to>Irshad</to><to>Tove</to><to>Irshad</to>");
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
    test:assertEquals(toNs.length(), 2);
    test:assertEquals(toNs.toString(), "<ns:to xmlns:ns=\"foo\">Tove</ns:to><ns:to xmlns:ns=\"foo\">Tove</ns:to>");

    xml toNoNs = seq.elementChildren("to");
    test:assertEquals(toNoNs.length(), 2);
    test:assertEquals(toNoNs.toString(), "<to>Irshad</to><to>Irshad</to>");
}

function testXMLFunctionalCtor() {
    'xml:Element x = 'xml:Element("hello");
    'xml:Element y = xml `<hello></hello>`;
    test:assertEquals(y, x);
}

function testXMLFunctionalConstructorWithAttributes() {
    'xml:Element x = 'xml:Element("element", { "attr": "attrVal", "two": "Val2"});
    'xml:Element y = xml `<element attr="attrVal" two="Val2"/>`;
    test:assertEquals(y, x);
}

function testXMLFunctionalConstructorWithAChild() {
    'xml:Element x = 'xml:Element("element", {attr: "hello"}, 'xml:Element("child"));
    'xml:Element y = xml `<element attr="hello"><child/></element>`;
    test:assertEquals(y, x);
}

function testXMLCommentCtor() {
    'xml:Comment x = 'xml:Comment("comment content");
    'xml:Comment y = xml `<!--comment content-->`;
    test:assertEquals(y, x);
}

function testXMLPICtor() {
    'xml:ProcessingInstruction x = 'xml:ProcessingInstruction("DONOT", "print this");
    'xml:ProcessingInstruction y = xml `<?DONOT print this?>`;
    test:assertEquals(y, x);
}

function testXMLTextCtor() {
    'xml:Text x = 'xml:Text("this is a charactor sequence");
    'xml:Text y = xml `this is a charactor sequence`;
    test:assertEquals(y, x);
}

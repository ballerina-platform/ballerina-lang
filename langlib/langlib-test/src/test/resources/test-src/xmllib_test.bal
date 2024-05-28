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
import ballerina/lang.test;

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
    xml x = <xml> checkpanic 'xml:fromString(s);
    return x/<CD>/<TITLE>;
}

function emptyConcatCall() returns xml {
    return 'xml:concat();
}

function testConcat() returns xml {
    xml x = xml `<hello>xml content</hello>`;
    return 'xml:concat(x, <xml> checkpanic testFromString(), "hello from String");
}

function testConcatWithXMLSequence() {
    string a = "string one";
    string b = "string two";
    'xml:Element catalogClone = catalog.clone();

    xml c = 'xml:concat(catalog, a, b);
    assertEquals(c.length(), 2);
    assertEquals(catalog, catalogClone);

    xml d = 'xml:concat();
    foreach var x in catalog/<CD> {
        d = 'xml:concat(d, x);
    }
    assertEquals(d.length(), 3);

    xml e = 'xml:concat(c, d);
    assertEquals(e.length(), 5);
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

function testSetElementName() returns [xml, xml] {
    'xml:Element element1 = xml `<elem attr="attr1">content</elem>`;
    'xml:Element element2 = xml `<e/>`;
    element1.setName("el2");
    element2.setName("{http://www.ballerina-schema.io/schema}Elem");
    return [element1, element2];
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

function testGetContent() returns [string, string] {
    'xml:Text t = <'xml:Text> xml `hello world`;
    'xml:ProcessingInstruction pi = <'xml:ProcessingInstruction> xml `<?pi-node type="cont"?>`;
    'xml:Comment comment = xml `<!-- this is a comment text -->`;
    return [pi.getContent(), comment.getContent()];
}

type attributesMap map<string>;

function testCreateElement() {
    xml children1 = xml `<foo>hello</foo><foo>world</foo>`;
    xml:Text children2 = xml `hello`;
    xml:Comment children3 = xml `<!--Comment1-->`;
    xml:Element children4 = xml `<foo>hello world</foo>`;
    xml:ProcessingInstruction children5 = xml `<?foo?>`;

    map<string> attributes1 = {
        "href" : "https://ballerina.io"
    };

    map<string> attributes2 = {
        "href" : "https://ballerina.io",
        "src" : "test.jpg"
    };

    attributesMap attributes3 = {"href":"https://ballerina.io"};

    xml:Element r1 = 'xml:createElement("elem", attributes1, children1);
    xml:Element r2 = 'xml:createElement("elem", attributes2, children1);
    xml:Element r3 = 'xml:createElement("elem");
    xml:Element r4 = 'xml:createElement("elem", attributes1);
    xml:Element r5 = 'xml:createElement("elem", {}, children1);
    xml:Element r6 = 'xml:createElement("elem", {}, xml ``);
    xml:Element r7 = 'xml:createElement("elem", {"href" : "https://ballerina.io"}, xml `hello world`);
    xml:Element r8 = 'xml:createElement("elem", attributes1, children5);
    xml:Element r9 = 'xml:createElement("elem", attributes1, children2);
    xml:Element r11 = 'xml:createElement("elem", attributes1, children3);
    xml:Element r12 = 'xml:createElement("elem", attributes1, children4);
    xml:Element r10 = 'xml:createElement("elem", attributes3, children1);

    assertEquals(r1.toString(), "<elem href=\"https://ballerina.io\"><foo>hello</foo><foo>world</foo></elem>");
    assertEquals(r2.toString(), "<elem href=\"https://ballerina.io\" src=\"test.jpg\"><foo>hello</foo><foo>world</foo></elem>");
    assertEquals(r3.toString(), "<elem/>");
    assertEquals(r4.toString(), "<elem href=\"https://ballerina.io\"/>");
    assertEquals(r5.toString(), "<elem><foo>hello</foo><foo>world</foo></elem>");
    assertEquals(r6.toString(), "<elem/>");
    assertEquals(r7.toString(), "<elem href=\"https://ballerina.io\">hello world</elem>");
    assertEquals(r8.toString(), "<elem href=\"https://ballerina.io\"><?foo?></elem>");
    assertEquals(r9.toString(), "<elem href=\"https://ballerina.io\">hello</elem>");
    assertEquals(r10.toString(), "<elem href=\"https://ballerina.io\"><foo>hello</foo><foo>world</foo></elem>");
    assertEquals(r11.toString(), "<elem href=\"https://ballerina.io\"><!--Comment1--></elem>");
    assertEquals(r12.toString(), "<elem href=\"https://ballerina.io\"><foo>hello world</foo></elem>");
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

    assertEquals(text1.toString(), "This is xml text");
    assertEquals(text2.toString(), "");
    assertEquals(text3.toString(), "T");
    assertEquals(text4.toString(), "Thisisxmltext");
    assertEquals(text5.toString(), "XML\ntext");
}

function testForEach() {
    xml r = 'xml:concat();
    foreach var x in catalog/* {
        r = 'xml:concat(r, x);
    }
    assertEquals(r.length(), 7);
}

function testSlice() returns [xml, xml, xml] {
    'xml:Element elemL = xml `<elemL>content</elemL>`;
    'xml:Element elemN = xml `<elemN>content</elemN>`;
    'xml:Element elemM = xml `<elemM>content</elemM>`;
    xml elem = 'xml:concat(elemL, elemN, elemM);
    return [elem.slice(0, 2), elem.slice(1), 'xml:slice(elem, 1)];
}

function testXMLStrip() {
    xml<'xml:Element> x1 = xml `<foo><bar/><?foo?>text1 <!--Com1--> <bar/></foo><foo><?foo?>text1<!--Com2--></foo>`;
    xml x2 = x1.strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?>text1 <!--Com1--> <bar/></foo><foo><?foo?>text1<!--Com2--></foo>");
    xml x21 = x1.cloneReadOnly();
    x2 = x21.strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?>text1 <!--Com1--> <bar/></foo><foo><?foo?>text1<!--Com2--></foo>");
    x2 = (x1.cloneReadOnly()).strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?>text1 <!--Com1--> <bar/></foo><foo><?foo?>text1<!--Com2--></foo>");

    'xml:Element x7 = xml `<foo><bar/><?foo?>text1 text2<!--Comment--><bar/>  </foo>`;
    x2 = x7.strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?>text1 text2<!--Comment--><bar/>  </foo>");
    x21 = x7.cloneReadOnly();
    x2 = x21.strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?>text1 text2<!--Comment--><bar/>  </foo>");
    x2 = (x7.cloneReadOnly()).strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?>text1 text2<!--Comment--><bar/>  </foo>");
    x21 = x7.cloneReadOnly();
    'xml:Element x6 = <'xml:Element> x21.strip();
    assertEquals(x6.toString(), "<foo><bar/><?foo?>text1 text2<!--Comment--><bar/>  </foo>");

    string a = "interpolation";
    xml<'xml:Text> x10 = xml `text1 text2 ${a}`;
    x2 = x10.strip();
    assertEquals(x2.toString(), "text1 text2 interpolation");
    x21 = x10.cloneReadOnly();
    x2 = x21.strip();
    assertEquals(x2.toString(), "text1 text2 interpolation");
    x2 = (x10.cloneReadOnly()).strip();
    assertEquals(x2.toString(), "text1 text2 interpolation");

    'xml:Text x11 = xml `text1 text2`;
    x2 = x11.strip();
    assertEquals(x2.toString(), "text1 text2");
    x21 = x11.cloneReadOnly();
    x2 = x21.strip();
    assertEquals(x2.toString(), "text1 text2");
    x2 = (x11.cloneReadOnly()).strip();
    assertEquals(x2.toString(), "text1 text2");

    xml<'xml:Comment> x3 = xml `<!--Comment1--><!--Comment2-->`;
    x2 = x3.strip();
    assertEquals(x2.toString(), "");
    x21 = x3.cloneReadOnly();
    x2 = x21.strip();
    assertEquals(x2.toString(), "");
    x2 = (x3.cloneReadOnly()).strip();
    assertEquals(x2.toString(), "");

    'xml:Comment x8 = xml `<!--Comment1-->`;
    x2 = x8.strip();
    assertEquals(x2.toString(), "");
    x21 = x8.cloneReadOnly();
    x2 = x21.strip();
    assertEquals(x2.toString(), "");
    x2 = (x8.cloneReadOnly()).strip();
    assertEquals(x2.toString(), "");

    xml<'xml:ProcessingInstruction> x4 = xml `<?foo?><?fu?>`;
    x2 = x4.strip();
    assertEquals(x2.toString(), "");
    x21 = x4.cloneReadOnly();
    x2 = x21.strip();
    assertEquals(x2.toString(), "");
    x2 = (x4.cloneReadOnly()).strip();
    assertEquals(x2.toString(), "");

    'xml:ProcessingInstruction x9 = xml `<?foo?>`;
    x2 = x9.strip();
    assertEquals(x2.toString(), "");
    x21 = x9.cloneReadOnly();
    x2 = x21.strip();
    assertEquals(x2.toString(), "");
    x2 = (x9.cloneReadOnly()).strip();
    assertEquals(x2.toString(), "");

    xml x5 = xml `<foo><bar/><?foo?></foo><?foo?>text1 text2<!--Com1--> <foo><bar/></foo>`;
    x2 = x5.strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?></foo>text1 text2 <foo><bar/></foo>");
    x21 = x5.cloneReadOnly();
    x2 = x21.strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?></foo>text1 text2 <foo><bar/></foo>");
    x2 = (x5.cloneReadOnly()).strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?></foo>text1 text2 <foo><bar/></foo>");

    readonly & xml x22 = xml `<foo><bar/><?foo?></foo><?foo?>text1 text2<!--Com1--> <foo><bar/></foo>`;
    x21 = x22;
    x2 = x21.strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?></foo>text1 text2 <foo><bar/></foo>");
    x2 = x22.strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?></foo>text1 text2 <foo><bar/></foo>");

    readonly & xml<'xml:Element> x12 = xml `<foo>fa<?foo?>text1 <!--Com1--> <bar/></foo><foo>text1<!--Com2--></foo>`;
    x21 = x12;
    x2 = x21.strip();
    assertEquals(x2.toString(), "<foo>fa<?foo?>text1 <!--Com1--> <bar/></foo><foo>text1<!--Com2--></foo>");
    x2 = x12.strip();
    assertEquals(x2.toString(), "<foo>fa<?foo?>text1 <!--Com1--> <bar/></foo><foo>text1<!--Com2--></foo>");

    readonly & 'xml:Element x13 = xml `<foo><bar/><?foo?>text1 text2<!--Comment--><bar/>  </foo>`;
    x21 = x13;
    x2 = x21.strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?>text1 text2<!--Comment--><bar/>  </foo>");
    x2 = x13.strip();
    assertEquals(x2.toString(), "<foo><bar/><?foo?>text1 text2<!--Comment--><bar/>  </foo>");

    x21 = x13;
    'xml:Element x14 = <'xml:Element> x21.strip();
    assertEquals(x14.toString(), "<foo><bar/><?foo?>text1 text2<!--Comment--><bar/>  </foo>");

    readonly & xml<'xml:Text> x15 = xml `text1 text2 ${a}`;
    x21 = x15;
    x2 = x21.strip();
    assertEquals(x2.toString(), "text1 text2 interpolation");
    x2 = x15.strip();
    assertEquals(x2.toString(), "text1 text2 interpolation");

    readonly & 'xml:Text x16 = xml `text1 text2`;
    x21 = x16;
    x2 = x21.strip();
    assertEquals(x2.toString(), "text1 text2");
    x2 = x16.strip();
    assertEquals(x2.toString(), "text1 text2");

    readonly & xml<'xml:Comment> x17 = xml `<!--Comment1--><!--Comment2-->`;
    x21 = x17;
    x2 = x21.strip();
    assertEquals(x2.toString(), "");
    x2 = x17.strip();
    assertEquals(x2.toString(), "");

    readonly & 'xml:Comment x18 = xml `<!--Comment1-->`;
    x21 = x18;
    x2 = x21.strip();
    assertEquals(x2.toString(), "");
    x2 = x18.strip();
    assertEquals(x2.toString(), "");

    readonly & xml<'xml:ProcessingInstruction> x19 = xml `<?foo?><?fu?>`;
    x21 = x19;
    x2 = x21.strip();
    assertEquals(x2.toString(), "");
    x2 = x19.strip();
    assertEquals(x2.toString(), "");

    readonly & 'xml:ProcessingInstruction x20 = xml `<?foo?>`;
    x21 = x20;
    x2 = x21.strip();
    assertEquals(x2.toString(), "");
    x2 = x20.strip();
    assertEquals(x2.toString(), "");
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
    check setChildren(fc, subRoot);
    return cat;
}

function setChildren('xml:Element fc, 'xml:Element subRoot) returns error? {
    return trap fc.setChildren(subRoot);
}

function testSetChildrenFunction() {
    string url = "https://ballerina.io";
    xml:Element xml1 = xml `<ele href="${url}"><foo>temp</foo></ele>`;
    xml:setChildren(xml1, "Ballerina");
    assertEquals(xml1.toString(), "<ele href=\"https://ballerina.io\">Ballerina</ele>");

    xml1 = xml `<ele href="${url}"><foo>${url}</foo></ele>`;
    xml:setChildren(xml1, "Ballerina");
    assertEquals(xml1.toString(), "<ele href=\"https://ballerina.io\">Ballerina</ele>");

    xml1 = xml `<ele href="${url}"><foo>temp value</foo></ele>`;
    xml:setChildren(xml1, url);
    assertEquals(xml1.toString(), "<ele href=\"https://ballerina.io\">https://ballerina.io</ele>");

    xml1 = xml `<ele><foo>temp value</foo></ele>`;
    xml:setChildren(xml1, string `this is ${url}`);
    assertEquals(xml1.toString(), "<ele>this is https://ballerina.io</ele>");

    xml1 = xml `<ele href="${url}"></ele>`;
    xml:setChildren(xml1, "Ballerina");
    assertEquals(xml1.toString(), "<ele href=\"https://ballerina.io\">Ballerina</ele>");
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
      int value = <int> checkpanic langint:fromString((x/<year>/*).toString()) ;
      future<int> f1 = start getRandomNumber(value);
      int result = checkpanic wait f1;
      sum = sum + result;
    });

    var filter = ((bookstore/*).elements()).filter(function (xml x) returns boolean {
      int value =   <int> checkpanic langint:fromString((x/<year>/*).toString()) ;
      future<int> f1 = start getRandomNumber(value);
      int result = checkpanic wait f1;
      return result > 2000;
    });

    var filter2 = (filter).map(function (xml x) returns xml {
      int value =   <int> checkpanic langint:fromString((x/<year>/*).toString()) ;
      future<int> f1 = start getRandomNumber(value);
      int result = checkpanic wait f1;
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
     assertEquals(p.length(), 4);
     assertEquals(p.toString(), "<!-- Comment --><Apple>IPhone</Apple><Samsung>Galaxy</Samsung><OP>OP7</OP>");

     xml seq = brands/*;
     xml q = seq.children();
     assertEquals(q.length(), 3);
     assertEquals(q.toString(), "IPhoneGalaxyOP7");
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
    assertEquals(y.length(), 3);
    assertEquals(y.toString(), "<US>Obama</US><US>Trump</US><RUS>Putin</RUS>");

    xml z = seq.elements("RUS");
    assertEquals(z.length(), 1);
    assertEquals(z.toString(), "<RUS>Putin</RUS>");
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
    assertEquals(usNs.length(), 1);
    assertEquals(usNs.toString(), "<ns:US xmlns:ns=\"foo\">Obama</ns:US>");

    xml usNoNs = seq.elements("US");
    assertEquals(usNoNs.length(), 1);
    assertEquals(usNoNs.toString(), "<US>Trump</US>");
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

    assertEquals(p.length(), 4);
    assertEquals(p.toString(), "<to>Tove</to><to>Irshad</to><from>Jani</from><body>Don't forget me this weekend!</body>");
    assertEquals(q.length(), 2);
    assertEquals(q.toString(), "<to>Tove</to><to>Irshad</to>");

    xml seq = 'xml:concat(letter, letter);
    xml y = seq.elementChildren();
    xml z = seq.elementChildren("to");

    assertEquals(y.length(), 8);
    assertEquals(y.toString(), "<to>Tove</to><to>Irshad</to><from>Jani</from><body>Don't forget me this weekend!</body>" +
                         "<to>Tove</to><to>Irshad</to><from>Jani</from><body>Don't forget me this weekend!</body>");
    assertEquals(z.length(), 4);
    assertEquals(z.toString(), "<to>Tove</to><to>Irshad</to><to>Tove</to><to>Irshad</to>");
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
    assertEquals(toNs.length(), 2);
    assertEquals(toNs.toString(), "<ns:to xmlns:ns=\"foo\">Tove</ns:to><ns:to xmlns:ns=\"foo\">Tove</ns:to>");

    xml toNoNs = seq.elementChildren("to");
    assertEquals(toNoNs.length(), 2);
    assertEquals(toNoNs.toString(), "<to>Irshad</to><to>Irshad</to>");
}

function testXMLIteratorInvocation() {
    xml a = xml `<!--first-->`;
    xml<'xml:Comment> seq1 = <xml<'xml:Comment>> a.concat(xml `<!--second-->`);

    object {
        public isolated function next() returns record {| 'xml:Comment value; |}?;
    } iter1 = seq1.iterator();

    assertEquals((iter1.next()).toString(), "{\"value\":`<!--first-->`}");

    xml b = xml `<one>first</one>`;
    xml<'xml:Element> seq2 = <xml<'xml:Element>> b.concat(xml `<two>second</two>`);

    object {
            public isolated function next() returns record {| 'xml:Element value; |}?;
    } iter2 = seq2.iterator();

    assertEquals((iter2.next()).toString(), "{\"value\":`<one>first</one>`}");

    xml c = xml `bit of text1`;
    xml<'xml:Text> seq3 = <xml<'xml:Text>> c.concat(xml ` bit of text2`);

    object {
        public isolated function next() returns record {| 'xml:Text value; |}?;
    } iter3 = seq3.iterator();

    assertEquals((iter3.next()).toString(), "{\"value\":`bit of text1`}");

    xml d = xml `<?xml-stylesheet href="mystyle.css" type="text/css"?>`;
    xml<'xml:ProcessingInstruction> seq4 = <xml<'xml:ProcessingInstruction>> d.concat(xml `<?pi-node type="cont"?>`);

    object {
        public isolated function next() returns record {| 'xml:ProcessingInstruction value; |}?;
    } iter4 = seq4.iterator();

    assertEquals((iter4.next()).toString(), "{\"value\":`<?xml-stylesheet href=\"mystyle.css\" type=\"text/css\"?>`}");

    xml e = xml `<one>first</one>`;
    xml<'xml:Element|'xml:Text> seq5 = <xml<'xml:Element|'xml:Text>> e.concat(xml `<two>second</two>`);

    object {
        public isolated function next() returns record {| 'xml:Element|'xml:Text value; |}?;
    } iter5 = seq5.iterator();

    assertEquals((iter5.next()).toString(), "{\"value\":`<one>first</one>`}");
}

function testSelectingTextFromXml() {
    xml:Element authors = xml `<authors><author><name>Enid<middleName/>Blyton</name></author></authors>`;
    xml:Text authorsList = authors.text();
    assertEquals(authorsList.toString(), "");

    xml:Text helloText = xml `hello text`;
    xml:Text textValues = helloText.text();
    assertEquals(textValues.toString(), "hello text");

    xml:Comment comment = xml `<!-- This is a comment -->`;
    xml:Text commentText = comment.text();
    assertEquals(commentText.toString(), "");

    xml:ProcessingInstruction instruction = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    xml:Text instructionText = instruction.text();
    assertEquals(instructionText.toString(), "");

    xml<xml:Text> authorName = (authors/<author>/<name>/*).text();
    assertEquals(authorName.toString(),"EnidBlyton");

    var name = xml `<name>Dan<lname>Gerhard</lname><!-- This is a comment -->Brown</name>`;
    xml nameText = (name/*).text();
    assertEquals(nameText.toString(), "DanBrown");

    xml<xml:Text> textValues2 = xml:text(helloText);
    assertEquals(textValues2.toString(), textValues.toString());
}

function testXmlFilter() {
    xml x1 = xml `<authors><name>Enid Blyton</name></authors>`;
    xml x5 = x1.filter(function (xml x2) returns boolean {
        xml elements = (x2/*).elements();
        return (elements/*).toString() == "Enid Blyton";
        });
    assertEquals(x5.toString(), "<authors><name>Enid Blyton</name></authors>");
}

function testGetDescendants() {
    getDescendantsSimpleElement();
    getDescendantsWithNS();
    getDescendantsFilterNonElements();
    getDescendantsFromSeq();
}

function getDescendantsSimpleElement() {
    xml:Element bookCatalog = xml `<CATALOG><CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD>
                           <CD><TITLE>Hide your heart</TITLE><ARTIST>Bonnie Tyler</ARTIST></CD></CATALOG>`;

    xml descendantSeq = bookCatalog.getDescendants();

    xml:Element e1 = xml `<CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD>`;
    xml:Element e2 = xml `<TITLE>Empire Burlesque</TITLE>`;
    xml:Text e3 = <xml:Text>xml `Empire Burlesque`;
    xml:Element e4 = xml `<ARTIST>Bob Dylan</ARTIST>`;
    xml:Text e5 = <xml:Text>xml `Bob Dylan`;
    xml:Element e6 = xml `<CD><TITLE>Hide your heart</TITLE><ARTIST>Bonnie Tyler</ARTIST></CD>`;
    xml:Element e7 = xml `<TITLE>Hide your heart</TITLE>`;
    xml:Text e8 = <xml:Text>xml `Hide your heart`;
    xml:Element e9 = xml `<ARTIST>Bonnie Tyler</ARTIST>`;
    xml:Text e10 = <xml:Text>xml `Bonnie Tyler`;

    assertEquals(descendantSeq.length(), 11);
    assertEquals(descendantSeq[0], e1);
    assertEquals(descendantSeq[1], e2);
    assertEquals(descendantSeq[2], e3);
    assertEquals(descendantSeq[3], e4);
    assertEquals(descendantSeq[4], e5);
    assertEquals(descendantSeq[6], e6);
    assertEquals(descendantSeq[7], e7);
    assertEquals(descendantSeq[8], e8);
    assertEquals(descendantSeq[9], e9);
    assertEquals(descendantSeq[10], e10);
}

function getDescendantsWithNS() {
    xmlns "foo" as ns;
    xml:Element presidents = xml `<Leaders><!-- Comment --><ns:US><fn>Obama</fn></ns:US><US><fn>Trump</fn></US></Leaders>`;
    xml descendants = presidents.getDescendants();

    xml usNs = descendants.elements("{foo}US");
    assertEquals(usNs.length(), 1);
    assertEquals(usNs.toString(), "<ns:US xmlns:ns=\"foo\"><fn>Obama</fn></ns:US>");

    xml:Comment e1 = xml `<!-- Comment -->`;
    xml:Element e2 = xml `<ns:US><fn>Obama</fn></ns:US>`;
    xml:Element e3 = xml `<fn>Obama</fn>`;
    xml:Text e4 = xml `Obama`;
    xml:Element e5 = xml `<US><fn>Trump</fn></US>`;
    xml:Element e6 = xml `<fn>Trump</fn>`;
    xml:Text e7 = xml `Trump`;

    assertEquals(descendants.length(), 7);
    assertEquals(descendants[0], e1);
    assertEquals(descendants[1], e2);
    assertEquals(descendants[2], e3);
    assertEquals(descendants[3], e4);
    assertEquals(descendants[4], e5);
    assertEquals(descendants[5], e6);
    assertEquals(descendants[6], e7);
}

function getDescendantsFilterNonElements() {
    xml:Element books = xml `<bs><?xml-stylesheet type="text/xsl"?><bk><t><en><!-- english --><txt>Everyday Italian</txt></en></t></bk></bs>`;

    xml descendants = books.getDescendants();

    xml:ProcessingInstruction e1 = xml `<?xml-stylesheet type="text/xsl"?>`;
    xml:Element e2 = xml `<bk><t><en><!-- english --><txt>Everyday Italian</txt></en></t></bk>`;
    xml:Element e3 = xml `<t><en><!-- english --><txt>Everyday Italian</txt></en></t>`;
    xml:Element e4 = xml `<en><!-- english --><txt>Everyday Italian</txt></en>`;
    xml:Comment e5 = xml `<!-- english -->`;
    xml:Element e6 = xml `<txt>Everyday Italian</txt>`;
    xml:Text e7 = xml `Everyday Italian`;

    assertEquals(descendants.length(), 7);
    assertEquals(descendants[0], e1);
    assertEquals(descendants[1], e2);
    assertEquals(descendants[2], e3);
    assertEquals(descendants[3], e4);
    assertEquals(descendants[4], e5);
    assertEquals(descendants[5], e6);
    assertEquals(descendants[6], e7);
}

function getDescendantsFromSeq() {
    xml desecndants = xml ``;
    xml x = xml `<a><b><c>helo</c><d>bye</d></b></a>`;
    xml b = x/*;
    if (b is xml:Element) {
        desecndants = b.getDescendants();
    }

    xml:Element e1 = xml `<c>helo</c>`;
    xml:Text e2 = xml `helo`;
    xml:Element e3 = xml `<d>bye</d>`;
    xml:Text e4 = xml `bye`;

    assertEquals(desecndants.length(), 4);
    assertEquals(desecndants[0], e1);
    assertEquals(desecndants[1], e2);
    assertEquals(desecndants[2], e3);
    assertEquals(desecndants[3], e4);
}

function assertEquals(anydata actual, anydata expected) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}

function testData() {
    xml:Element authors = xml `<authors><author><name>Enid<middleName/>Blyton</name></author></authors>`;
    assertEquals(authors.data(), "EnidBlyton");

    xml:Text text = xml `hello&gt;abc`;
    assertEquals(text.data(), "hello>abc");

    xml:Comment cmnt = xml `<!-- this is a comment -->`;
    assertEquals(cmnt.data(), "");

    xml:ProcessingInstruction pi = xml `<?ins key=val other=val?>`;
    assertEquals(pi.data(), "");

    xml x = xml `<elem>&lt;</elem>`;
    assertEquals((x/*).data(), "<");

    xml concat = authors + text + cmnt + pi + x;
    assertEquals(concat.data(), "EnidBlytonhello>abc<");
}

function testXmlSubtypeFillerValue() {
    xml:Text x1 = xml:createText("text 1");
    xml:Text x2 = xml:createText("text 2");
    xml:Text x3 = xml:createText("text 3");

    xml:Text[] x = [x1, x2];
    insertListValue(x, 3, x3);
    assertEquals(x.toString(), "[`text 1`,`text 2`,``,`text 3`]");
}

function insertListValue('xml:Text[] list, int pos, 'xml:Text value) {
    list[pos] = value;
}

function testXmlGetContentOverACommentSequence() {
    xml a = xml `<elem><!--Hello--></elem>`;
    xml c = a/*;
    xml k = c.get(0);
    if (k is 'xml:Comment) {
        string s = k.getContent();
        assertEquals(s, "Hello");
    } else {
        panic error("Assert failure: single item comment should pass `is xml:Comment` type test");
    }

    if (c is 'xml:Comment) {
        string s = c.getContent();
        assertEquals(s, "Hello");
    } else {
        panic error("Assert failure: single item comment sequence should pass `is xml:Comment` type test");
    }

    xml concat = c + xml ``;
    if (concat is 'xml:Comment) {
        string s = concat.getContent();
        assertEquals(s, "Hello");
    } else {
        panic error("Assert failure: single item comment sequence should pass `is xml:Comment` type test");
    }
}

function testXmlGetContentOverAProcInstructionSequence() {
    xml a = xml `<elem><?PI val="PI Val" val2="PI VAl 2"?></elem>`;
    xml c = a/*;
    xml k = c.get(0);
    string expected = "val=\"PI Val\" val2=\"PI VAl 2\"";
    if (k is 'xml:ProcessingInstruction) {
        string s = k.getContent();
        assertEquals(s, expected);
    } else {
        panic error("Assert failure: single item processing instruction should pass " +
            "`is xml:ProcessingInstruction` type test");
    }

    if (c is 'xml:ProcessingInstruction) {
        string s = c.getContent();
        assertEquals(s, expected);
    } else {
        panic error("Assert failure: single item processing instruction sequence should pass " +
            "`is xml:ProcessingInstruction` type test");
    }

    xml concat = c + xml ``;
    if (concat is 'xml:ProcessingInstruction) {
        string s = concat.getContent();
        assertEquals(s, expected);
    } else {
        panic error("Assert failure: single item processing instruction sequence should pass " +
            "`is xml:ProcessingInstruction` type test");
    }
}

function fromStringTest() {
    var a = xml:fromString("hello");
    if !(a is xml:Text) {
        panic error("Assertion error: not a text");
    }

    var b = xml:fromString("<!-- hello -->");
    if !(b is xml:Comment) {
        panic error("Assertion error: not a comment");
    }

    var c = xml:fromString("<?pi data ?>");
    if !(c is xml:ProcessingInstruction) {
        panic error("Assertion error: not a PI");
    }

    var d = checkpanic xml:fromString("<hello/>world<?pi data ?><!-- comment -->");
    if !(d[0] is xml:Element) {
        panic error("Assertion error: not an element");
    }
    if !(d[1] is xml:Text) {
        panic error("Assertion error: not a text");
    }
    if !(d[2] is xml:ProcessingInstruction) {
        panic error("Assertion error: not a pi");
    }
    if !(d[3] is xml:Comment) {
        panic error("Assertion error: not a comment");
    }

    string xmlString = string `<Description><![CDATA[OK]]></Description>`;
    xml xmlWithCData = checkpanic xml:fromString(xmlString);
    assertEquals(xmlWithCData, xml `<Description>OK</Description>`);
}

function testXmlIteratorNextInvocations() {

    'xml:Text x1 = xml `foo`;

    xml<'xml:Element|'xml:Text> x2 = <xml<'xml:Element|'xml:Text>> x1.concat(xml `<bar/>`);

    var iterator = x2.iterator();
    record {| 'xml:Element|'xml:Text value; |}? next = iterator.next();

    test:assertValueEqual(next.toString(), "{\"value\":`foo`}");
    test:assertTrue(next is record {| 'xml:Element|'xml:Text value; |});
    test:assertFalse(next is record {| 'xml:Element value; |});
    test:assertFalse(next is record {| 'xml:Text value; |});

    record {| 'xml:Element|'xml:Text value; |}? nextNext = iterator.next();

    test:assertValueEqual(nextNext.toString(), "{\"value\":`<bar/>`}");
    test:assertTrue(nextNext is record {| 'xml:Element|'xml:Text value; |});
    test:assertFalse(nextNext is record {| 'xml:Element value; |});
    test:assertFalse(nextNext is record {| 'xml:Text value; |});

    xml seq = xml `<?PI ?><!--Comment1-->foo<bar/>`;
    var itr = seq.iterator();

    record {| xml value; |}? itrNext = itr.next();
    test:assertValueEqual(itrNext.toString(), "{\"value\":`<?PI ?>`}");
    test:assertTrue(itrNext is record {| xml value; |});
    test:assertTrue(itrNext is record {| 'xml:Element|'xml:Text|'xml:Comment|'xml:ProcessingInstruction value; |});
    test:assertFalse(itrNext is record {| 'xml:Element|'xml:Comment|'xml:ProcessingInstruction value; |});
    test:assertFalse(itrNext is record {| 'xml:Element|'xml:ProcessingInstruction value; |});
    test:assertFalse(itrNext is record {| 'xml:ProcessingInstruction value; |});

    record {| xml value; |}? itrNextNext = itr.next();
    test:assertValueEqual(itrNextNext.toString(), "{\"value\":`<!--Comment1-->`}");
    test:assertTrue(itrNextNext is record {| 'xml:Element|'xml:Text|'xml:Comment|'xml:ProcessingInstruction value; |});
    test:assertFalse(itrNextNext is record {| 'xml:Element|'xml:Comment|'xml:ProcessingInstruction value; |});
    test:assertFalse(itrNextNext is record {| 'xml:Element|'xml:Comment value; |});
    test:assertFalse(itrNextNext is record {| 'xml:Comment value; |});

    record {| xml value; |}? itrNextNextNext = itr.next();
    test:assertValueEqual(itrNextNextNext.toString(), "{\"value\":`foo`}");
    test:assertTrue(itrNextNextNext is record {| xml value; |});
    test:assertFalse(itrNextNextNext is record {| 'xml:Element|'xml:Text|'xml:ProcessingInstruction value; |});
    test:assertFalse(itrNextNextNext is record {| 'xml:Element|'xml:Text value; |});
    test:assertFalse(itrNextNextNext is record {| 'xml:Text value; |});
}

function testNamespaces() {
    map<string> attributes1 = {"{http://www.w3.org/2000/xmlns/}ns0":"http://sample.com/test","status":"online"};
    xml:Element element1 = xml:createElement("{http://sample.com/test}bookStore", attributes1, xml ``);
    assertEquals(element1.toString(), "<ns0:bookStore xmlns:ns0=\"http://sample.com/test\" status=\"online\"/>");

    map<string> attributes2 = {"{http://www.w3.org/2000/xmlns/}xml":"http://sample.com/test","status":"online"};
    xml:Element element2 = xml:createElement("{http://sample.com/test}bookStore", attributes2, xml ``);
    assertEquals(element2.toString(), "<bookStore xmlns=\"http://sample.com/test\" status=\"online\"/>");

    map<string> attributes3 = {"{http://www.w3.org/XML/1998/namespace}ns0":"http://sample.com/test","status":"online"};
    xml:Element element3 = xml:createElement("{http://sample.com/test}bookStore", attributes3, xml ``);
    assertEquals(element3.toString(), "<bookStore xmlns=\"http://sample.com/test\" xml:ns0=\"http://sample.com/test\" status=\"online\"/>");

    map<string> attributes4 = {"status":"online"};
    xml:Element element4 = xml:createElement("{http://sample.com/test}bookStore", attributes4, xml ``);
    assertEquals(element4.toString(), "<bookStore xmlns=\"http://sample.com/test\" status=\"online\"/>");
}

function testIterableOperationsOnUnionType() {
    xml x;
    int index = 0;
    xml result;

    xml:Element|xml:Comment x1 = xml `<item><name>book</name><price>10</price></item>`;
    x = <xml>x1;
    assertEquals(xml:map(xml:elements(x),
                    v => xml:getChildren(v)), xml `<name>book</name><price>10</price>`);
    assertEquals(xml:filter(xml:elements(x),
                    v => xml:length(xml:getChildren(v)) > 0), xml `<item><name>book</name><price>10</price></item>`);
    xml:forEach(x, function(xml v) {
                index += 1;
            });
    assertEquals(index, 1);

    xml:Element|xml:ProcessingInstruction x2 = xml `<?data?>`;
    x = <xml>x2;
    string s = "";
    assertEquals(xml:map(x, v => v), xml `<?data?>`);
    assertEquals(xml:filter(x, v => xml:getTarget(<xml:ProcessingInstruction>v) == "data").get(0), xml `<?data?>`);
    xml:forEach(x, function(xml v) {
                s += xml:getTarget(<xml:ProcessingInstruction>v);
            });
    assertEquals(s, "data");

    xml:Element|xml:Text x3 = xml `this is a text in xml`;
    x = <xml>x3;
    index = 0;
    assertEquals(xml:map(x, v => xml:concat(v, "new")).get(0), xml `this is a text in xmlnew`);
    assertEquals(xml:filter(x, v => xml:length(<xml>v) > 0).get(0), xml `this is a text in xml`);
    xml:forEach(x, function(xml v) {
                index += 1;
            });
    assertEquals(index, 1);

    xml:Text|xml:Comment x4 = xml `<!--comment-->`;
    x = <xml>x4;
    index = 0;
    assertEquals(xml:map(x, v => xml:concat(v, xml `<a/>`)), xml `<!--comment--><a/>`);
    assertEquals(xml:filter(x, v => xml:data(<xml>v).length() == 0).get(0), xml `<!--comment-->`);
    xml:forEach(x, function(xml v) {
                index += 1;
            });
    assertEquals(index, 1);

    xml<xml:Element>|xml<xml:ProcessingInstruction> x5 = xml `<item><name>book</name><price>10</price></item>`;
    x = <xml>x5;
    index = 0;
    result = xml:map(xml:elements(x), v => xml:getDescendants(v));
    assertEquals(result.get(0), xml `<name>book</name>`);
    assertEquals(result.get(1), xml `book`);
    assertEquals(result.get(2), xml `<price>10</price>`);
    assertEquals(result.get(3), xml `10`);
    assertEquals(xml:filter(xml:elements(x), v => xml:length(xml:getDescendants(v)) > 0),
            xml `<item><name>book</name><price>10</price></item>`);
    xml:forEach(x, function(xml v) {
                index += 1;
            });
    assertEquals(index, 1);

    xml<xml:ProcessingInstruction>|xml<xml:Comment> x6 = xml `<!--comment-->`;
    x = <xml>x6;
    index = 0;
    assertEquals(xml:map(x, v => xml:children(<xml>v)), xml ``);
    assertEquals(xml:filter(x, v => xml:getContent(<xml:Comment>v).length() > 0).get(0), xml `<!--comment-->`);
    xml:forEach(x, function(xml v) {
                index += xml:length(v);
            });
    assertEquals(index, 1);

    xml<xml:ProcessingInstruction>|xml<xml:Comment>|xml:Text x7 = xml `An xml text`;
    x = <xml>x7;
    index = 0;
    assertEquals(xml:map(x, v => xml:text(<xml>v)), xml `An xml text`);
    assertEquals(xml:filter(x, v => xml:length(xml:text(<xml>v)) > 0).get(0), xml `An xml text`);
    xml:forEach(x, function(xml v) {
                index += xml:length(xml:text(v));
            });
    assertEquals(index, 1);

    xml<xml:ProcessingInstruction|xml:Element>|xml<xml:Comment|xml:Text> x8 = xml `<?data2 descending?>`;
    x = <xml>x8;
    index = 0;
    assertEquals(xml:map(x, v => xml:createText(xml:getContent(<xml:ProcessingInstruction>v))), xml `descending`);
    assertEquals(xml:filter(x, v => xml:getTarget(<xml:ProcessingInstruction>v).length() > 0).get(0), xml `<?data2 descending?>`);
    xml:forEach(x, function(xml v) {
                index += 1;
            });
    assertEquals(index, 1);

    xml<xml:Comment|xml:Element>|xml<xml:ProcessingInstruction|xml:Text> x9 = xml `<box><!--comment--><width>10</width><height>5</height></box>`;
    x = <xml>x9;
    index = 0;
    result = xml:map(xml:elements(x), v => xml:elementChildren(v));
    assertEquals(result.get(0), xml `<width>10</width>`);
    assertEquals(result.get(1), xml `<height>5</height>`);
    assertEquals(xml:filter(xml:elements(x), v => xml:getName(v) == "box"), xml `<box><!--comment--><width>10</width><height>5</height></box>`);
    xml:forEach(x, function(xml v) {
                index += 1;
            });
    assertEquals(index, 1);

    xml<xml:Text|xml:ProcessingInstruction>|xml<xml:Comment|xml:Text> x10 = xml `A second xml text`;
    x = <xml>x10;
    index = 0;
    assertEquals(xml:map(x, v => xml:createText(xml:data(<xml>v))), xml `A second xml text`);
    assertEquals(xml:filter(x, v => xml:length(xml:text(<xml>v)) > 0).get(0), xml `A second xml text`);
    xml:forEach(x, function(xml v) {
                index += 1;
            });
    assertEquals(index, 1);

    xml<xml:Element|xml:ProcessingInstruction>|xml<xml:Comment|xml:Text> x11 = xml `<a><b/><c/></a>`;
    index = 0;
    assertEquals(xml:map(xml:elements(<xml>x11), v => xml:getChildren(v)), xml `<b/><c/>`);
    assertEquals(xml:filter(<xml>x11, v => xml:getAttributes(<xml:Element>v).length() > 0), xml ``);
    xml:forEach(<xml>x11, function(xml v) {
                index += 1;
            });
    assertEquals(index, 1);
}

function testXmlMapOnXmlElementSequence() {
    xml result;
    xml<xml:Element> x1 = xml `<item><name>book</name><price>10</price></item>`;
    result = xml:map(xml:elements(x1), v => xml:getChildren(v));
    assertEquals(result.length(), 2);
    assertEquals(result, xml `<name>book</name><price>10</price>`);
    assertXmlSequenceItems(result, [xml `<name>book</name>`, xml `<price>10</price>`]);

    xml<xml:Element> x2 = xml `<item><name>book</name><price>10</price></item><item><name>pen</name><price>12</price></item>`;
    result = xml:map(xml:elements(x2), v => xml:getDescendants(v));
    assertEquals(result.length(), 8);
    assertEquals(result, xml `<name>book</name>book<price>10</price>10<name>pen</name>pen<price>12</price>12`);
    assertXmlSequenceItems(result, [
                xml `<name>book</name>`,
                xml `book`,
                xml `<price>10</price>`,
                xml `10`,
                xml `<name>pen</name>`,
                xml `pen`,
                xml `<price>12</price>`,
                xml `12`
            ]);

    xml x3 = xml `<?data?><a><!--comment--><b/></a>text<c><d/></c>`;
    result = xml:map(x3, v => xml:children(<xml>v));
    assertEquals(result.length(), 3);
    assertEquals(result, xml `<!--comment--><b/><d/>`);
    assertXmlSequenceItems(result, [xml `<!--comment-->`, xml `<b/>`, xml `<d/>`]);

    xml<xml:Element> x4 = xml `<a><!--comment--></a><c><d/></c>`;
    result = xml:map(x4, v => xml:elementChildren(v));
    assertEquals(result.length(), 1);
    assertXmlSequenceItems(result, [xml `<d/>`]);

    xml<xml:Element> x5 = xml `<a><!--comment--><?data?></a><c><d/>text one two</c>`;
    result = xml:map(x5, v => xml:children(v));
    assertEquals(result.length(), 4);
    assertXmlSequenceItems(result, [xml `<!--comment-->`, xml `<?data?>`, xml `<d/>`, xml `text one two`]);

    xml x6 = xml `<a><!--comment--><?data?></a><c>text one two</c>`;
    result = xml:map(x6, v => xml:elementChildren(<xml:Element>v));
    assertEquals(result.length(), 0);
    assertEquals(result, xml ``);
}

function assertXmlSequenceItems(xml actual, xml[] expected) {
    int expectedLen = expected.length();
    int actualLen = actual.length();
    if actualLen != expectedLen {
        string reason = "expected xml sequence with items " + expected.toString() + " of size " + expectedLen.toString()
                            + ", but found xml sequence " + actual.toString() + " of size " + actualLen.toString();
        error e = error(reason);
        panic e;
    }
    foreach int i in 0 ... expectedLen - 1 {
        assertEquals(actual.get(i), expected[i]);
    }
}

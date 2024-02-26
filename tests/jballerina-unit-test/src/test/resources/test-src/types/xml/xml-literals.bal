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

import ballerina/test;

function testXMLSequence() {
    string v1 = "interpolation1";
    string v2 = "interpolation2";
    xml x1 = xml `<foo>foo</foo><?foo?>text1<!--comment-->`;
    test:assertEquals(x1.toString(), "<foo>foo</foo><?foo ?>text1<!--comment-->");
    xml x2 = xml `text1 text2 <foo>foo</foo>text2<!--comment-->text3 text4`;
    test:assertEquals(x2.toString(), "text1 text2 <foo>foo</foo>text2<!--comment-->text3 text4");
    xml x3 = xml `text1${v1}<foo>foo</foo>text2${v2}<!--comment ${v2}-->text3`;
    test:assertEquals(x3.toString(), "text1interpolation1<foo>foo</foo>text2interpolation2<!--comment interpolation2-->text3");
    xml x4 = xml `<!--comment--><?foo ${v1}?>text1${v1}<root>text2 ${v2}${v1} text3!<foo>12</foo><bar></bar></root>text2${v2}`;
    test:assertEquals(x4.toString(), "<!--comment--><?foo interpolation1?>text1interpolation1<root>text2 "+
    "interpolation2interpolation1 text3!<foo>12</foo><bar/></root>text2interpolation2");
    xml x5 = xml `<!--comment-->text1`;
    test:assertEquals(x5.toString(), "<!--comment-->text1");
    xml x6 = xml `<!--comment-->`;
    test:assertEquals(x6.toString(), "<!--comment-->");
    xml x27 = xml `<_>element</_>`;
    test:assertEquals(x27.toString(), "<_>element</_>");
    xml x28 = xml `<_/>`;
    test:assertEquals(x28.toString(), "<_/>");

    xml<'xml:Element> x23 = xml `<foo>Anne</foo><fuu>Peter</fuu>`;
    test:assertEquals(x23.toString(), "<foo>Anne</foo><fuu>Peter</fuu>");
    xml<xml<'xml:Element>> x24 = xml `<foo>Anne</foo><fuu>Peter</fuu>`;
    test:assertEquals(x24.toString(), "<foo>Anne</foo><fuu>Peter</fuu>");
    xml<'xml:Element> x29 = xml `<_>element</_>`;
    test:assertEquals(x29.toString(), "<_>element</_>");

    xml<'xml:ProcessingInstruction> x17 = xml `<?foo?><?faa?>`;
    test:assertEquals(x17.toString(), "<?foo ?><?faa ?>");
    xml<xml<'xml:ProcessingInstruction>> x18 = xml `<?foo?><?faa?>`;
    test:assertEquals(x18.toString(), "<?foo ?><?faa ?>");

    xml<'xml:Text> x7 = xml `text1 text2`;
    test:assertEquals(x7.toString(), "text1 text2");
    'xml:Text x26 = x7;
    test:assertEquals(x26.toString(), "text1 text2");
    xml<xml<'xml:Text>> x19 = xml `text1 text2`;
    test:assertEquals(x19.toString(), "text1 text2");
    'xml:Text x20 = xml `text1 text2`;
    test:assertEquals(x20.toString(), "text1 text2");
    'xml:Text x25 = xml `text1 ${v1}`;
    test:assertEquals(x25.toString(), "text1 interpolation1");
    'xml:Text x8 = xml `text1`;
    test:assertEquals(x8.toString(), "text1");

    xml<'xml:Comment> x21 = xml `<!--comment1--><!--comment2-->`;
    test:assertEquals(x21.toString(), "<!--comment1--><!--comment2-->");
    xml<xml<'xml:Comment>> x22 = xml `<!--comment1--><!--comment2-->`;
    test:assertEquals(x22.toString(), "<!--comment1--><!--comment2-->");

    xml<'xml:Text|'xml:Comment> x9 = xml `<!--comment-->`;
    test:assertEquals(x9.toString(), "<!--comment-->");
    xml<'xml:Text>|xml<'xml:Comment> x12 = xml `<!--comment-->`;
    test:assertEquals(x12.toString(), "<!--comment-->");
    xml<'xml:Text|'xml:Comment> x10 = xml `<!--comment-->text1`;
    test:assertEquals(x10.toString(), "<!--comment-->text1");
    xml<'xml:Element|'xml:ProcessingInstruction> x11 = xml `<root> text1<foo>100</foo><foo>200</foo></root><?foo?>`;
    test:assertEquals(x11.toString(), "<root> text1<foo>100</foo><foo>200</foo></root><?foo ?>");
    'xml:Text x16 = xml `text ${v1}`;
    test:assertEquals(x16.toString(), "text interpolation1");
    
    any x30 = xml `foo<e></e>`;
    test:assertEquals(x30.toString(), "foo<e/>");
    anydata x31 = xml `bar<e></e>`; 
    test:assertEquals(x31.toString(), "bar<e/>");
    any|Template x32 = xml `foo<elem></elem>`;
    test:assertEquals(x32.toString(), "foo<elem/>");
    Template|any x33 = xml `bar<elem></elem>`; 
    test:assertEquals(x33.toString(), "bar<elem/>");
    xml|error x34 = xml `world<elem></elem>`;
    if (x34 is xml) {
        test:assertEquals(x34.toString(), "world<elem/>");
    }
    xml|xml<xml:Text>|xml:Text x35 = xml `hello<e></e>`;
    test:assertEquals(x35.toString(), "hello<e/>");
    xml|xml<xml:Text|xml:Comment> x36 = xml `world<e></e>`;
    test:assertEquals(x36.toString(), "world<e/>");
}

public type Template object {
    public string[] & readonly strings;
    public anydata[] insertions;
};

function testXMLTextLiteral() returns [xml, xml, xml, xml, xml, xml] {
    string v1 = "11";
    string v2 = "22";
    string v3 = "33";
    xml x1 = xml `aaa`;
    xml x2 = xml `${v1}`;
    xml x3 = xml `aaa${v1}bbb${v2}ccc`;
    xml x4 = xml `aaa${v1}bbb${v2}ccc{d{}e}{f{`;
    xml x5 = xml `aaa${v1}b${"${"}bb${v2}c\}cc{d{}e}{f{`;
    xml x6 = xml ` `;
    return [x1, x2, x3, x4, x5, x6];
}

function testXMLCommentLiteral() returns [xml, xml, xml, xml, xml, xml] {
    int v1 = 11;
    string v2 = "22";
    string v3 = "33";
    xml x1 = xml `<!--aaa-->`;
    xml x2 = xml `<!--${v1}-->`;
    xml x3 = xml `<!--aaa${v1}bbb${v2}ccc-->`;
    xml x4 = xml `<!--<aaa${v1}bbb${v2}cccd->e->-f<<{>>>-->`;
    xml x5 = xml `<!---a-aa${v1}b${"${"}bb${v2}c\}cc{d{}e}{f{-->`;
    xml x6 = xml `<!---->`;
    xml x7 = xml `<!-- $ -->`;
    return [x1, x2, x3, x4, x5, x6];
}


function testXMLPILiteral() returns [xml, xml, xml, xml, xml] {
    int v1 = 11;
    string v2 = "22";
    string v3 = "33";
    xml x1 = xml `<?foo ?>`;
    xml x2 = xml `<?foo ${v1}?>`;
    xml x3 = xml `<?foo aaa${v1}bbb${v2}ccc?>`;
    xml x4 = xml `<?foo <aaa${v1}bbb${v2}ccc??d?e>?f<<{>>>?>`;
    xml x5 = xml `<?foo ?a?aa${v1}b${"${"}bb${v2}c\}cc{d{}e}{f{?>`;

    return [x1, x2, x3, x4, x5];
}

function testExpressionAsAttributeValue() returns [xml, xml, xml, xml, xml] {
    string v0 = "\"zzz\"";
    string v1 = "zzz";
    string v2 = "33>22";
    xml x1 = xml `<foo bar="${v0}"/>`;
    xml x2 = xml `<foo bar="aaa${v1}bb'b${v2}ccc?"/>`;
    xml x3 = xml `<foo bar="}aaa${v1}bbb${v2}ccc{d{}e}{f{"/>`;
    xml x4 = xml `<foo bar1='aaa{${v1}}b${"${"}b"b${v2}c\}cc{d{}e}{f{' bar2='aaa{${v1}}b${"${"}b"b${v2}c\}cc{d{}e}{f{'/>`;
    xml x5 = xml `<foo bar=""/>`;
    return [x1, x2, x3, x4, x5];
}

function testElementLiteralWithTemplateChildren() returns [xml, xml] {
    string v2 = "aaa<bbb";
    xml x1 = xml `<fname>John</fname>`;
    xml x2 = xml `<lname>Doe</lname>`;

    xml x3 = xml `<root>hello ${v2} good morning ${x1} ${x2}. Have a nice day!<foo>123</foo><bar></bar></root>`;
    xml x4 = x3/*;
    return [x3, x4];
}

function testDefineInlineNamespace() returns (xml) {
    xml x1 = xml `<foo foo="http://wso2.com" >hello</foo>`;
    return x1;
}

function testTextWithValidMultiTypeExpressions() returns (xml) {
    int v1 = 11;
    string v2 = "world";
    float v3 = 1.35;
    boolean v4 = true;

    xml x = xml `hello ${v1} ${v2}. How ${v3} are you ${v4}?`;
    return x;
}


function testArithmaticExpreesionInXMLTemplate() returns (xml) {
    xml x1 = xml `<foo id="hello ${ 3 + 6 / 3}" >hello</foo>`;

    return x1;
}

function f1() returns (string) {
  return "returned from a function";
}

function testFunctionCallInXMLTemplate() returns (xml) {
    xml x1 = xml `<foo>${ "<-->" + f1()}</foo>`;

    return x1;
}

function testBracketSequenceInXMLLiteral() returns (xml) {
    xml x1 = xml `{}{{ {{{ { } }} }}} - extra }`;
    xml x2 = xml `<elem>{}{{</elem>`;
    return x1 + x2;
}

function testInterpolatingVariousTypes() returns (xml) {
    int i = 42;
    float f = 3.14;
    decimal d = 31.4444;
    string s = "this-is-a-string";
    xml elem = xml `<abc/>`;

    xml ip = xml `<elem>${i}|${f}|${d}|${s}|${elem}</elem>`;
    return ip;
}

function testXMLStartTag() returns [xml, xml, xml, xml] {
    xml x1 = xml `<fname>John</fname>`;
    xml x2 = xml `<Country>US</Country>`;
    xml x3 = xml `<_foo id="hello ${ 3 + 6 / 3}" >hello</_foo>`;
    xml x4 = xml `<_-foo id="hello ${ 3 + 6 / 3}" >hello</_-foo>`;
    return [x1, x2, x3, x4];
}

function testXMLLiteralWithEscapeSequence() returns [xml, int, any[]] {
    xml x1 = xml `hello &lt; &gt; &amp;`;

    any[] elements = [];
    int i = 0;
    // There are no 'xml elements' in x1
    foreach var e in x1.elements() {
        elements[i] = e;
        i += 1;
    }
    return [x1, x1.elements().length(), elements];
}

function testDollarSignOnXMLLiteralTemplate() returns [xml, xml, xml] {
    string a = "hello";
    xml x1 = xml `<foo id="hello $${ 3 + 6 / 3}" >${a}</foo>`;
    xml x2 = xml `<foo id="hello $$${ 3 + 6 / 3}" >$${a}</foo>`;
    xml x3 = xml `<foo id="hello $$ ${ 3 + 6 / 3}" >$$ ${a}</foo>`;

    return [x1, x2, x3];
}

function testXMLWithLeadingWhiteSpace() {
    string title = "title";
    string author = "author";

    xml temp1 = xml `
    <books>
        <book>
            <title>${title}</title>
            <author>${author}</author>
        </book>
    </books>`;

   test:assertTrue(temp1[0] is 'xml:Text);

   xml temp2 = xml `


       <books>
           <book>
               <title>${title}</title>
               <author>${author}</author>
           </book>
       </books>`;

   test:assertFalse(temp2[1] is 'xml:Text);
}

function testXMLSequenceValueAssignment(){
    xml<xml<xml<'xml:Element>>> x1 = xml `<foo>Anne</foo><fuu>Peter</fuu>`;
    xml<'xml:Element> x2 = x1;
    test:assertEquals(x2.toString(), "<foo>Anne</foo><fuu>Peter</fuu>");
    xml<xml<'xml:Element>> x3 = x1;
    test:assertEquals(x3.toString(), "<foo>Anne</foo><fuu>Peter</fuu>");

    xml<xml<xml<'xml:Comment>>> x4 = xml `<!--some comment--><!--some comment-->`;
    xml<'xml:Comment> x5 = x4;
    test:assertEquals(x5.toString(), "<!--some comment--><!--some comment-->");
    xml<xml<'xml:Comment>> x6 = x4;
    test:assertEquals(x6.toString(), "<!--some comment--><!--some comment-->");

    xml<xml<xml<'xml:ProcessingInstruction>>> x7 = xml `<?foo?><?faa?>`;
    xml<'xml:ProcessingInstruction> x8 = x7;
    test:assertEquals(x8.toString(), "<?foo ?><?faa ?>");
    xml<xml<'xml:ProcessingInstruction>> x9 = x7;
    test:assertEquals(x9.toString(), "<?foo ?><?faa ?>");
}

function testXMLTextValueAssignment(){
    xml<xml<xml<'xml:Text>>> x1 = xml `abcd`;
    xml:Text x2 = x1;
    test:assertEquals(x2.toString(), "abcd");
    xml<'xml:Text> x3 = x1;
    test:assertEquals(x3.toString(), "abcd");
}

function testXMLCDATASection() {
    xml x1 = xml `<![CDATA[some text]]>`;
    test:assertEquals(x1.toString(), "some text");
    test:assertTrue(x1 is xml:Text);

    int intVar = 5;
    xml:Text x2 = xml `<![CDATA[${intVar}]]>`;
    test:assertEquals(x2.toString(), "5");

    xml:Text x3 = xml `XML stands for <![CDATA[eXtensible]]> Markup Language`;
    test:assertEquals(x3.toString(), "XML stands for eXtensible Markup Language");

    float floatVar = 3.2;
    xml:Element x4 = xml `<element>some text <![CDATA[example of a flaot ${floatVar} some other text]]></element>`;
    test:assertEquals(x4.toString(), "<element>some text example of a flaot 3.2 some other text</element>");

    xml x5 = x4/*;
    test:assertEquals(x5.toString(), "some text example of a flaot 3.2 some other text");

    xml x6 = xml `<![CDATA[]]>`;
    test:assertEquals(x6.toString(), "");
    test:assertTrue(x6 is xml:Text);

   xml x7 = xml `<![CDATA[ abc --> <!-- --> some more text ]]>`;
   test:assertEquals(x7.toString(), " abc --&gt; &lt;!-- --&gt; some more text ");
   test:assertTrue(x7 is xml:Text);
}

service class Service {
    function getXmlSequence() returns xml|error {
        return xml `<grand_total>${1d}</grand_total>`;
    }

    function getNestedXmlSequence() returns xml|error {
        return getResult();
    }
}

function getResult() returns xml {
    return xml `<grand_total>${2d}</grand_total>`;
}

function getXmlSequence() returns xml|error {
    return xml `<grand_total>${3d}</grand_total>`;
}

function getNestedXmlSequence() returns xml|error {
    return getResult();
}

class XmlError {
    function getXmlSequence() returns xml|error {
        return xml `<grand_total>${4d}</grand_total>`;
    }

    function getNestedXmlSequence() returns xml|error {
        return getResult();
    }
}

function testXMLReturnUnion() returns error? {
    Service testService = new();
    xml|error xmlSequence = testService.getXmlSequence();
    test:assertTrue(xmlSequence is xml);
    test:assertEquals((check xmlSequence).toString(), "<grand_total>1</grand_total>");

    xmlSequence = testService.getNestedXmlSequence();
    test:assertTrue(xmlSequence is xml);
    test:assertEquals((check xmlSequence).toString(), "<grand_total>2</grand_total>");

    xmlSequence = getXmlSequence();
    test:assertTrue(xmlSequence is xml);
    test:assertEquals((check xmlSequence).toString(), "<grand_total>3</grand_total>");

    xmlSequence = getNestedXmlSequence();
    test:assertTrue(xmlSequence is xml);
    test:assertEquals((check xmlSequence).toString(), "<grand_total>2</grand_total>");

    XmlError obj = new();
    xmlSequence = obj.getXmlSequence();
    test:assertTrue(xmlSequence is xml);
    test:assertEquals((check xmlSequence).toString(), "<grand_total>4</grand_total>");

    xmlSequence = obj.getNestedXmlSequence();
    test:assertTrue(xmlSequence is xml);
    test:assertEquals((check xmlSequence).toString(), "<grand_total>2</grand_total>");
}

type T xml;

function testXMLInterpolationExprWithUserDefinedType() {
    T x1 = xml `<foo></foo>`;
    xml x2 = xml `<doc>${x1}</doc>`;
    test:assertEquals(x2.toString(), "<doc><foo/></doc>");
}

function testQueryInXMLTemplateExpr() {
    int[] a = [1, 2, 3];

    var x1 = xml `<doc>${from int i in a select xml `<num>${i}</num>`}</doc>`;
    xml x2 = xml `<doc>${from int i in a select xml `<num>${i}</num>`}</doc>`;
    xml:Element x3 = xml `<doc>${from int i in a select xml `<num>${i}</num>`}</doc>`;

    string str1 = "<doc><num>1</num><num>2</num><num>3</num></doc>";
    test:assertEquals(x1.toString(), str1);
    test:assertEquals(x2.toString(), str1);
    test:assertEquals(x3.toString(), str1);

    int[] b = [1, 2];
    var x4 = xml `<doc>${from int i in b select xml `<row>${from int j in a select xml `<num>${j}</num>`}</row>`}</doc>`;

    string str2 = "<doc><row><num>1</num><num>2</num><num>3</num></row><row><num>1</num><num>2</num><num>3</num></row></doc>";
    test:assertEquals(x4.toString(), str2);

    xml<xml:Element> x5 = xml `<doc><num>1</num><num>2</num><num>3</num></doc>`;
    xml x6 = xml `<doc>${from xml:Element i in x5 select i}</doc>`;

    string str3 = "<doc><doc><num>1</num><num>2</num><num>3</num></doc></doc>";
    test:assertEquals(x6.toString(), str3);

    var x7 = xml `<doc>${(from string s in ["a", "b", "c"] select xml `${s}z`)}</doc>`;
    test:assertEquals(x7.toString(), "<doc>azbzcz</doc>");

    // var x8 = xml `<doc>${xml `foo` + (from string s in ["a", "b"] select xml `${s}z`)}</doc>`; // issue #36541
    // test:assertEquals(x8.toString(), "<doc>fooazbz</doc>")
}

type XMLType xml:Comment|xml:ProcessingInstruction;

function testXMLLiteralWithConditionExpr() {
    xml? s = xml `foo`;
    string target = "foo";

    xml x1 = (s ?: xml `<baz/>`);
    test:assertEquals(x1.toString(), target);

    xml x2 = (s is xml ? s : xml `<baz/>`) + xml ``;
    test:assertEquals(x2.toString(), target);

    xml x3 = (s ?: xml `<baz/>`) + xml ``;
    test:assertEquals(x3.toString(), target);

    s = ();

    xml:Element e1 = xml `<baz/>`;
    xml? v1 = (s ?: e1) + xml `<element>A</element>`;
    test:assertEquals(v1.toString(), "<baz/><element>A</element>");

    xml<xml:Element> e2 = xml `<baz/>`;
    xml v2 = (s ?: e2) + xml `<element>A</element>`;
    test:assertEquals(v2.toString(), "<baz/><element>A</element>");

    XMLType e3 = xml `<!--This is a comment-->`;
    xml v3 = (s ?: e3) + xml `<element>A</element>`;
    test:assertEquals(v3.toString(), "<!--This is a comment--><element>A</element>");

    XMLType e4 = xml `<?target data?>`;
    xml v4 = (s ?: e4) + xml `<element>A</element>`;
    test:assertEquals(v4.toString(), "<?target data?><element>A</element>");

    xml w1 = (s ?: (v1 is xml<xml:Element> ? e2 : xml `<user>Foo</user>`)) + xml `<element>B</element>`;
    test:assertEquals(w1.toString(), "<baz/><element>B</element>");

    xml w2 = (s ?: (v1 ?: xml `<user>Foo</user>`)) + xml `<element>B</element>`;
    test:assertEquals(w2.toString(), "<baz/><element>A</element><element>B</element>");

    xml w3 = (s ?: (s ?: (v1 is xml:Element ? e2 : xml `<user>Foo</user>`))) + xml `<element>B</element>`;
    test:assertEquals(w3.toString(), "<user>Foo</user><element>B</element>");
}

type X xml;
type XE xml<xml:Element>;
type XP xml<xml:ProcessingInstruction>;
type XC xml<xml:Comment>;
type UX XE|XP|XC|xml:Text;

function testXMLSubtype() {
    X x1 = xml `<a></a><b></b>`;
    UX _ = <UX>x1;

    X x2 = xml `<?target instructions?><?a data?>`;
    UX _ = <UX>x2;

    X x3 = xml `<!--comment one--><!--comment two-->`;
    UX _ = <UX>x3;

    X x4 = xml `random sequence of text`;
    UX _ = <UX>x4;

    UX|error ux;
    X x5 = xml `<a></a><b></b><?target instructions?><?c data?>`;
    ux = trap <UX>x5;
    assertError(ux, "{ballerina}TypeCastError",
        "incompatible types: 'xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>' cannot be cast to 'UX'");

    X x6 = xml `<a></a><b></b><!--comment one--><!--comment two-->`;
    ux = trap <UX>x6;
    assertError(ux, "{ballerina}TypeCastError",
        "incompatible types: 'xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>' cannot be cast to 'UX'");

    X x7 = xml `<a></a><b></b>random text`;
    ux = trap <UX>x7;
    assertError(ux, "{ballerina}TypeCastError",
        "incompatible types: 'xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>' cannot be cast to 'UX'");

    X x8 = xml `<?target instructions?><?a data?>text`;
    ux = trap <UX>x8;
    assertError(ux, "{ballerina}TypeCastError",
        "incompatible types: 'xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>' cannot be cast to 'UX'");

    X x9 = xml `<?target instructions?><?a data?><!--comment one--><!--comment two-->`;
    ux = trap <UX>x9;
    assertError(ux, "{ballerina}TypeCastError",
        "incompatible types: 'xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>' cannot be cast to 'UX'");

    X x10 = xml `<!--comment one--><!--comment two-->text`;
    ux = trap <UX>x10;
    assertError(ux, "{ballerina}TypeCastError",
        "incompatible types: 'xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>' cannot be cast to 'UX'");

    X x11 = xml `<a></a><b></b><?d data?><?c data?>?><!--comment--><!--comment-->text`;
    ux = trap <UX>x11;
    assertError(ux, "{ballerina}TypeCastError",
        "incompatible types: 'xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>' cannot be cast to 'UX'");
}

type Error error<record {string message;}>;

function assertError(any|error value, string errorMessage, string expDetailMessage) {
    if value is Error {
        if value.message() != errorMessage {
            panic error("Expected error message: " + errorMessage + " found: " + value.message());
        }

        if value.detail().message == expDetailMessage {
            return;
        }
        panic error("Expected error detail message: " + expDetailMessage + " found: " + value.detail().message);
    }
    panic error("Expected: Error, found: " + (typeof value).toString());
}

function testXMLSequence() {
    string v1 = "interpolation1";
    string v2 = "interpolation2";
    xml x1 = xml `<foo>foo</foo><?foo?>text1<!--comment-->`;
    assert(x1.toString(), "<foo>foo</foo><?foo ?>text1<!--comment-->");
    xml x2 = xml `text1 text2 <foo>foo</foo>text2<!--comment-->text3 text4`;
    assert(x2.toString(), "text1 text2 <foo>foo</foo>text2<!--comment-->text3 text4");
    xml x3 = xml `text1${v1}<foo>foo</foo>text2${v2}<!--comment ${v2}-->text3`;
    assert(x3.toString(), "text1interpolation1<foo>foo</foo>text2interpolation2<!--comment interpolation2-->text3");
    xml x4 = xml `<!--comment--><?foo ${v1}?>text1${v1}<root>text2 ${v2}${v1} text3!<foo>12</foo><bar></bar></root>text2${v2}`;
    assert(x4.toString(), "<!--comment--><?foo interpolation1?>text1interpolation1<root>text2 "+
    "interpolation2interpolation1 text3!<foo>12</foo><bar></bar></root>text2interpolation2");
    xml x5 = xml `<!--comment-->text1`;
    assert(x5.toString(), "<!--comment-->text1");
    xml x6 = xml `<!--comment-->`;
    assert(x6.toString(), "<!--comment-->");

    xml<'xml:Element> x23 = xml `<foo>Anne</foo><fuu>Peter</fuu>`;
    assert(x23.toString(), "<foo>Anne</foo><fuu>Peter</fuu>");
    xml<xml<'xml:Element>> x24 = xml `<foo>Anne</foo><fuu>Peter</fuu>`;
    assert(x24.toString(), "<foo>Anne</foo><fuu>Peter</fuu>");

    xml<'xml:ProcessingInstruction> x17 = xml `<?foo?><?faa?>`;
    assert(x17.toString(), "<?foo ?><?faa ?>");
    xml<xml<'xml:ProcessingInstruction>> x18 = xml `<?foo?><?faa?>`;
    assert(x18.toString(), "<?foo ?><?faa ?>");

    xml<'xml:Text> x7 = xml `text1 text2`;
    assert(x7.toString(), "text1 text2");
    'xml:Text x26 = x7;
    assert(x26.toString(), "text1 text2");
    xml<xml<'xml:Text>> x19 = xml `text1 text2`;
    assert(x19.toString(), "text1 text2");
    'xml:Text x20 = xml `text1 text2`;
    assert(x20.toString(), "text1 text2");
    'xml:Text x25 = xml `text1 ${v1}`;
    assert(x25.toString(), "text1 interpolation1");
    'xml:Text x8 = xml `text1`;
    assert(x8.toString(), "text1");

    xml<'xml:Comment> x21 = xml `<!--comment1--><!--comment2-->`;
    assert(x21.toString(), "<!--comment1--><!--comment2-->");
    xml<xml<'xml:Comment>> x22 = xml `<!--comment1--><!--comment2-->`;
    assert(x22.toString(), "<!--comment1--><!--comment2-->");

    xml<'xml:Text|'xml:Comment> x9 = xml `<!--comment-->`;
    assert(x9.toString(), "<!--comment-->");
    xml<'xml:Text>|xml<'xml:Comment> x12 = xml `<!--comment-->`;
    assert(x12.toString(), "<!--comment-->");
    xml<'xml:Text|'xml:Comment> x10 = xml `<!--comment-->text1`;
    assert(x10.toString(), "<!--comment-->text1");
    xml<'xml:Element|'xml:ProcessingInstruction> x11 = xml `<root> text1<foo>100</foo><foo>200</foo></root><?foo?>`;
    assert(x11.toString(), "<root> text1<foo>100</foo><foo>200</foo></root><?foo ?>");
    xml<'xml:Text>|xml<'xml:Comment> x13 = xml `<!--comment-->text1`;
    assert(x13.toString(), "<!--comment-->text1");
    xml<xml<'xml:Text>>|xml<xml<'xml:Comment>> x14 = xml `<!--comment-->text1`;
    assert(x14.toString(), "<!--comment-->text1");
    xml<'xml:Element>|'xml:Text x15 = xml `<root> text1<foo>100</foo><foo>200</foo></root> text1`;
    assert(x15.toString(), "<root> text1<foo>100</foo><foo>200</foo></root> text1");
    'xml:Text x16 = xml `text ${v1}`;
    assert(x16.toString(), "text interpolation1");
}

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

   assert(temp1[0] is 'xml:Text, true);

   xml temp2 = xml `


       <books>
           <book>
               <title>${title}</title>
               <author>${author}</author>
           </book>
       </books>`;

   assert(temp2[1] is 'xml:Text, false);
}

function testXMLSequenceValueAssignment(){
    xml<xml<xml<'xml:Element>>> x1 = xml `<foo>Anne</foo><fuu>Peter</fuu>`;
    xml<'xml:Element> x2 = x1;
    assert(x2.toString(), "<foo>Anne</foo><fuu>Peter</fuu>");
    xml<xml<'xml:Element>> x3 = x1;
    assert(x3.toString(), "<foo>Anne</foo><fuu>Peter</fuu>");

    xml<xml<xml<'xml:Comment>>> x4 = xml `<!--some comment--><!--some comment-->`;
    xml<'xml:Comment> x5 = x4;
    assert(x5.toString(), "<!--some comment--><!--some comment-->");
    xml<xml<'xml:Comment>> x6 = x4;
    assert(x6.toString(), "<!--some comment--><!--some comment-->");

    xml<xml<xml<'xml:ProcessingInstruction>>> x7 = xml `<?foo?><?faa?>`;
    xml<'xml:ProcessingInstruction> x8 = x7;
    assert(x8.toString(), "<?foo ?><?faa ?>");
    xml<xml<'xml:ProcessingInstruction>> x9 = x7;
    assert(x9.toString(), "<?foo ?><?faa ?>");
}

function testXMLTextValueAssignment(){
    xml<xml<xml<'xml:Text>>> x1 = xml `abcd`;
    xml:Text x2 = x1;
    assert(x2.toString(), "abcd");
    xml<'xml:Text> x3 = x1;
    assert(x3.toString(), "abcd");
}

function testXMLCDATASection() {
    xml x1 = xml `<![CDATA[some text]]>`;
    assert(x1.toString(), "some text");
    assert(x1 is xml:Text, true);

    int intVar = 5;
    xml:Text x2 = xml `<![CDATA[${intVar}]]>`;
    assert(x2.toString(), "5");

    xml:Text x3 = xml `XML stands for <![CDATA[eXtensible]]> Markup Language`;
    assert(x3.toString(), "XML stands for eXtensible Markup Language");

    float floatVar = 3.2;
    xml:Element x4 = xml `<element>some text <![CDATA[example of a flaot ${floatVar} some other text]]></element>`;
    assert(x4.toString(), "<element>some text example of a flaot 3.2 some other text</element>");

    xml x5 = x4/*;
    assert(x5.toString(), "some text example of a flaot 3.2 some other text");

    xml x6 = xml `<![CDATA[]]>`;
    assert(x6.toString(), "");
    assert(x6 is xml:Text, true);

   xml x7 = xml `<![CDATA[ abc --> <!-- --> some more text ]]>`;
   assert(x7.toString(), " abc --&gt; &lt;!-- --&gt; some more text ");
   assert(x7 is xml:Text, true);
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

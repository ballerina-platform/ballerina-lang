function testXMLTextLiteral() returns [xml, xml, xml, xml, xml, xml] {
    string v1 = "11";
    string v2 = "22";
    string v3 = "33";
    xml x1 = xml `aaa`;
    xml x2 = xml `${v1}`;
    xml x3 = xml `aaa${v1}bbb${v2}ccc`;
    xml x4 = xml `aaa${v1}bbb${v2}ccc{d{}e}{f{`;
    xml x5 = xml `aaa${v1}b\${bb${v2}c\}cc{d{}e}{f{`;
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
    xml x5 = xml `<!---a-aa${v1}b\${bb${v2}c\}cc{d{}e}{f{-->`;
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
    xml x3 = xml `<?foo  aaa${v1}bbb${v2}ccc?>`;
    xml x4 = xml `<?foo  <aaa${v1}bbb${v2}ccc??d?e>?f<<{>>>?>`;
    xml x5 = xml `<?foo  ?a?aa${v1}b\${bb${v2}c\}cc{d{}e}{f{?>`;
    
    return [x1, x2, x3, x4, x5];
}

function testExpressionAsAttributeValue() returns [xml, xml, xml, xml, xml] {
    string v0 = "\"zzz\"";
    string v1 = "zzz";
    string v2 = "33>22";
    xml x1 = xml `<foo bar="${v0}"/>`;
    xml x2 = xml `<foo bar="aaa${v1}bb'b${v2}ccc?"/>`;
    xml x3 = xml `<foo bar="}aaa${v1}bbb${v2}ccc{d{}e}{f{"/>`;
    xml x4 = xml `<foo bar1='aaa{${v1}}b\${b"b${v2}c\}cc{d{}e}{f{' bar2='aaa{${v1}}b\${b"b${v2}c\}cc{d{}e}{f{'/>`;
    xml x5 = xml `<foo bar=""/>`;
    return [x1, x2, x3, x4, x5];
}

function testElementLiteralWithTemplateChildren() returns [xml, xml] {
    string v2 = "aaa<bbb";
    xml x1 = xml `<fname>John</fname>`;
    xml x2 = xml `<lname>Doe</lname>`;
    
    xml x3 = xml `<root>hello ${v2} good morning ${x1} ${x2}. Have a nice day!<foo>123</foo><bar></bar></root>`;
    xml x4 = x3.*;
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

function testXMLLiteralWithEscapeSequence() returns [xml, string[], int, any[]] {
    xml x1 = xml `hello &lt; &gt; &amp;`;
    int i = 0;
    string[] strs = [];
    foreach string|xml e in x1 {
        if e is string {
            strs[i] = e;
            i += 1;
        }
    }

    any[] elements = [];
    i = 0;
    // There are not 'xml elements' in x1
    foreach var e in x1.elements() {
        elements[i] = e;
        i += 1;
    }
    return [x1, strs, x1.elements().length(), elements];
}

function testDollarSignOnXMLLiteralTemplate() returns [xml, xml, xml] {
    string a = "hello";
    xml x1 = xml `<foo id="hello $${ 3 + 6 / 3}" >${a}</foo>`;
    xml x2 = xml `<foo id="hello $$${ 3 + 6 / 3}" >$${a}</foo>`;
    xml x3 = xml `<foo id="hello $$ ${ 3 + 6 / 3}" >$$ ${a}</foo>`;

    return [x1, x2, x3];
}

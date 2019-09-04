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

function testExpressionAsAttributeValue1() returns [xml, xml, xml, xml, xml] {
    string v0 = "\"zzz\"";
    string v1 = "zzz";
    string v2 = "33>22";
    xml x1 =
        xml `<
        foo
        bar
        =
        "${
        v0
        }"/>`
    ;
    xml x2 =
        xml `<
        foo
        bar
        =
        "aaa${
        v1
        }bb'b${
        v2
        }ccc?"/>`
    ;
    xml x3 =
        xml `<
        foo
        bar
        =
        "}aaa${
        v1
        }}bbb${
        v2
        }ccc{d{}e}{f{"/>`
    ;
    xml x4 =
        xml `<foo
        bar1
        ='aaa{${v1
        }}b\${b"b${
        v2
        }c\}cc{d{}e}{f{' bar2='aaa{${
        v1
        }}b\${b"b${
        v2
        }c\}cc{d{}e}{f{'/>`
    ;
    xml x5 = xml `<foo
        bar=
        ""/>`
    ;
    return [x1, x2, x3, x4, x5];
}

function testDefineInlineNamespace() returns (xml) {
    xml x1 = xml `<foo foo="http://wso2.com">hello</foo>`;
    return x1;
}

function testDefineInlineNamespace1() returns (xml) {
    xml x1 =
        xml `<
        foo
        foo
        =
        "http://wso2.com"
        >hello</
        foo>`
    ;
    return x1;
}

function testGetAttributesAsMap() returns [map<string>?, map<string>?, string, string] {
    var x1 = xml `<root xmlns:ns0="http://sample.com/wso2/a1" ns0:foo1="bar1" foo2="bar2"/>`;
    var x2 = xml `<root xmlns="http://sample.com/default/namepsace" xmlns:ns0="http://sample.com/wso2/a1" ns0:foo1="bar1" foo2="bar2"/>`;

    map<string>? m1 = x1@;
    map<string>? m2 =
        x2
        @
    ;

    var a = m1["{http://sample.com/wso2/a1}foo1"];
    var s1 = a is string ? a : "";
    a = m1[ns0:foo1];
    var s2 = a is string ? a : "";
    return [m1, m2, s1, s2];
}

function testAttributeAccess() returns string? {
    // Creates an XML element, which has attributes that are bound to a namespace as well as ones that are not.
    xml x1 = xml `<ns0:book ns0:status="available" count="5"/>`;

    // An attribute can also be accessed using the string representation of the qualified name.
    var s = <string?>x1@["{http://sample.com/wso2/a1}status"];
    string y = "";
    if (s is string) {
        justFunc(s);
        y = s;
    }
    return y;
}

function nonSingletonXmlAttributeAccess() returns boolean {
    xml x = xml `<someEle>cont</someEle>`;
    xml y = xml `<elem>More-Stuff</elem>`;
    xml nonSingleton = x + y;
    if (nonSingleton@ is ()) {
        return true;
    }
    return false;
}

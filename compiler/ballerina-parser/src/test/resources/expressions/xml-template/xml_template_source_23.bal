function testXMLTextLiteral() {
    xml x1 = xml `aaa${v1}bbb${v2}ccc`;
    xml x2 = xml `aaa${v1}bbb${v2}ccc{d{}e}{f{`;
    xml x3 = xml `aaa${v1}b\${bb}cc{d{}e}{f{`;
    xml x4 = xml ` `;
    xml x5 = xml `<foo bar="aaa${v1}bb'b${v2}ccc?"/>`;
    xml x6 = xml `<foo bar="}aaa${v1}bbb${v2}ccc{d{}e}{f{"/>`;
    xml x7 = xml `<foo bar1='aaa{${v1}}b"b${v2}c\}cc{d{}e}{f{' bar2='aaa{${v1}}b{b"b${v2}c\}cc{d{}e}{f{'/>`;
    xml x8 = xml `<foo bar=""/>`;
    xml x9 = xml `<_foo id="hello ${3 + 6 / 3}">hello</_foo>`;
    xml x10 = xml `<_-foo id="hello ${3 + 6 / 3}">hello</_-foo>`;
}

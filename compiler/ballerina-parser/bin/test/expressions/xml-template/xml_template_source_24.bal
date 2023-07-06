function testXMLTextLiteral() {
    xml x0 = xml `${a${b}c}`;
    xml x1 = xml `aaa${v1}b\${bb${v2}c\}cc{d{}e}{f{`;
    xml x2 = xml `<foo bar1='aaa{${v1}}b\${b"b${v2}c\}cc{d{}e}{f{' bar2='aaa{${v1}}b\${b"b${v2}c\}cc{d{}e}{f{'/>`;
}

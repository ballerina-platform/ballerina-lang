function testXMLPILiteral() returns [xml, xml, xml, xml, xml] {
    int v1 = 11;
    string v2 = "22";
    string v3 = "33";
    xml x1 = xml `<?foo ?>`;
    xml x2 =
        xml `<?foo ${
        v1
        }?>`;
    xml x3 = xml `<?foo aaa${v1}bbb${v2}ccc?>`;
    xml x4 = xml `<?foo <aaa${v1}bbb${v2}ccc??d?e>?f<<{>>>?>`;
    xml x5 = xml `<?foo ?a?aa${
        v1
        }b\${bb${
        v2
        }c\}cc{d{}e}{f{?>`
    ;

    return [x1, x2, x3, x4, x5];
}

function testXMLPILiteral1() returns [xml, xml, xml, xml, xml] {
    int v1 = 11;
    string v2 = "22";
    string v3 = "33";
    xml x1 =
        xml `<?foo ?>`;
    xml x2 =
        xml `<?foo ${
        v1
        }?>`;
    xml x3 =
        xml `<?foo aaa${
        v1
        }bbb${
        v2
        }ccc?>`
    ;
    xml x4 =
        xml `<?foo <aaa${
        v1
        }bbb${
        v2
        }ccc??d?e>?f<<{>>>?>`
    ;
    xml x5 = xml `<?foo ?a?aa${
        v1
        }b\${bb${
        v2}c\}cc{d{}e}{f{?>`
    ;

    return [x1, x2, x3, x4, x5];
}

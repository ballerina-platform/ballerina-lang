
function testConcat() returns xml {
    xml x = xml `<hello>xml content</hello>`;
    return 'xml:concat(x, "Hello", "hello from String");
}

function testCreateProcessingInstruction() returns xml {
    return 'xml:createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");
}

function testCreateComment() returns xml {
    return 'xml:createComment("This text should be wraped in xml comment");
}

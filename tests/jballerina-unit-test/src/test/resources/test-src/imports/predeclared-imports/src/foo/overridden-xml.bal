import ballerina/lang.'xml as str;

function testConcat() returns xml {
    xml x = xml `<hello>xml content</hello>`;
    return str:concat(x, "Hello", "hello from String");
}

function testConcatWithAutoImports() returns xml {
    xml x = xml `<hello>xml content</hello>`;
    return 'xml:concat(x, "Hello", "hello from String");
}


function testCreateComment() returns xml {
    return str:createComment("This text should be wraped in xml comment");
}

function testCreateCommentWithAutoImports() returns xml {
    return str:createComment("This text should be wraped in xml comment");
}

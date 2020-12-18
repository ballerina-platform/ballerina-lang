import ballerina/io;

xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
                    <p:name>bob</p:name>
                    <p:address>
                        <p:city>NY</p:city>
                        <q:country>US</q:country>
                    </p:address>
                    <q:ID>1131313</q:ID>
                  </p:person>`;

function testExcessVars() {
    foreach var [i, x, y] in xdata {
    }
}

function testExcessVarsIterableOp() {
    xdata.forEach(function ([int, xml, string] entry) {});
}

function xmlTypeParamElementIter() {
    'xml:Element el2 = xml `<foo>foo</foo><bar/>`;

    string result = "";
    foreach 'xml:Element elem in el2 {
        string str = io:sprintf("%s\n", elem);
        result += str;
    }

    record {| 'xml:Element value; |}? nextElement2 = el2.iterator().next();
}

function xmlTypeParamCommentIter() {
    'xml:Comment comment2 = xml `<!--I am a comment-->`;

    string result = "";
    foreach 'xml:Comment elem in comment2 {
        string str = io:sprintf("%s\n", elem);
        result += str;
    }

    record {| 'xml:Comment value; |}? nextComment2 = comment2.iterator().next();
}

function xmlTypeParamPIIter() {
    'xml:ProcessingInstruction pi2 = xml `<?target data?>`;

    string result = "";
    foreach 'xml:ProcessingInstruction elem in pi2 {
        string str = io:sprintf("%s\n", elem);
        result += str;
    }

    record {| 'xml:ProcessingInstruction value; |}? nextPI2 = pi2.iterator().next();
}

function xmlTypeParamUnionIter() {
    'xml:Element|'xml:Text el2 = xml `<foo>foo</foo><bar/>`;
    xml<'xml:Element>|xml<'xml:Text> el3 = xml `<foo>foo</foo><bar/>`;

    string result = "";
    foreach 'xml:Element|'xml:Text elem in el2 {
        string str = io:sprintf("%s\n", elem);
        result += str;
    }

    result = "";
    foreach 'xml:Element|'xml:Text elem in el3 {
        string str = io:sprintf("%s\n", elem);
        result += str;
    }

    record {| 'xml:Element|'xml:Text value; |}? nextUnionXMLVal2 = el2.iterator().next();
    record {| 'xml:Element|'xml:Text value; |}? nextUnionXMLVal3 = el3.iterator().next();
}

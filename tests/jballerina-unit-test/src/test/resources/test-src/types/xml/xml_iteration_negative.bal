xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
                    <p:name>bob</p:name>
                    <p:address>
                        <p:city>NY</p:city>
                        <q:country>US</q:country>
                    </p:address>
                    <q:ID>1131313</q:ID>
                  </p:person>`;

string result = "";

function testExcessVars() {
    foreach var [i, x, y] in xdata {
    }
}

function testExcessVarsIterableOp() {
    xdata.forEach(function ([int, xml, string] entry) {});
}

function concatString (string v) {
    result += v + " ";
}

function xmlTypeParamElementIter() {
    'xml:Element el2 = xml `<foo>foo</foo>`;

    result = "";
    foreach 'xml:Element elem in el2 {
        concatString(elem.toString());
    }
}

function xmlTypeParamCommentIter() {
    'xml:Comment comment2 = xml `<!--I am a comment-->`;

    result = "";
    foreach 'xml:Comment elem in comment2 {
        concatString(elem.toString());
    }
}

function xmlTypeParamPIIter() {
    'xml:ProcessingInstruction pi2 = xml `<?target data?>`;

    result = "";
    foreach 'xml:ProcessingInstruction elem in pi2 {
        concatString(elem.toString());
    }
}

function xmlTypeParamUnionIter() {
    'xml:Element|'xml:Text el2 = xml `<foo>foo</foo><bar/>`;
    xml<'xml:Element>|xml<'xml:Text> el3 = xml `<foo>foo</foo><bar/>`;

    result = "";
    foreach 'xml:Element|'xml:Text elem in el2 {
        concatString(elem.toString());
    }

    result = "";
    foreach 'xml:Element|'xml:Text elem in el3 {
        concatString(elem.toString());
    }

    record {| 'xml:Element|'xml:Text value; |}? nextUnionXMLVal2 = el2.iterator().next();
    record {| 'xml:Element|'xml:Text value; |}? nextUnionXMLVal3 = el3.iterator().next();
}
